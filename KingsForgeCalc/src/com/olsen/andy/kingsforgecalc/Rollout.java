package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import android.content.SharedPreferences;

public class Rollout {
    private SharedPreferences sharedPref;

    private Random random = new Random();
    private String debugLog;
    private String normalLog;
    private boolean debugLogEnable = false;
    private boolean normalLogEnable = false;

    private DiceHashList neededHashList;
    private DiceHashList rolledHashList;
    private DiceHashList rerollHashList;
    private HashMap<GameObject.GOColor, Integer> supplyHashInt;
    boolean debug_roll_all_1s;
    private GameBonusHashList bonusListHash;

    public Rollout(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
        debug_roll_all_1s = sharedPref.getBoolean("pref_debug_all_1s", false);
    }

    public HashMap<String, String> doRollout(
            DiceHashList neededHashList,
            HashMap<GameObject.GOColor, Integer> supplyHashInt, 
            List<GameBonus> bonusList, 
            Integer totalRolls
            ) {
        Integer diceDeficit = 0;
        Integer successes = 0;
        String result = "";
        debugLog = ""; // initialize log
        normalLog = "";
        
        long startTime = System.currentTimeMillis();
        this.rolledHashList = new DiceHashList();
        this.rerollHashList  = new DiceHashList();
        this.bonusListHash   = new GameBonusHashList();
        this.neededHashList = neededHashList;
        this.supplyHashInt  = supplyHashInt;
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            diceDeficit += Math.max(0, neededHashList.get(color).size() - supplyHashInt.get(color));
        }

        for (GameBonus.Bonus bonusType : GameBonus.Bonus.values()) {
            bonusListHash.put(bonusType, new ArrayList<GameBonus>());
        }
        for (GameBonus bonus : bonusList) {
            bonusListHash.get(bonus.getBonusType()).add(bonus);
        }
        
        normalLog += "\nCraft Card:\n" + neededHashList.normalString();

        boolean haveEnoughDice = diceDeficit <= bonusListHash.get(GameBonus.Bonus.WD).size();
        if (haveEnoughDice) {
            for (int x = 0; x < totalRolls; x++) {
                normalLogEnable = (x==0);
                debugLogEnable = (x==0 && sharedPref.getBoolean("pref_debug_log_enable",  false)); // only log the first run
                if (debugLogEnable) { debugLog += "\ndebug_roll_all_1s=" + debug_roll_all_1s; }
                rollAllNormalDice();
                rollWhiteDie();
                applyA1TO6();
                moveExtraToRerollHashList();
                setupRecursion();
                recursion();
                if (debugLogEnable) { debugLog += "\ndbgCount=" + dbgCount; }
                if (!recursionSuccess && bonusListHash.get(GameBonus.Bonus.RR).size() > 0) {
                    rerollDice();
                    applyA1TO6();
                    moveExtraToRerollHashList();
                    setupRecursion();
                    recursion();
                    if (debugLogEnable) { debugLog += "\ndbgCount=" + dbgCount; }
                }
                if (recursionSuccess) { 
                    successes++;
                } else {
                    if (normalLogEnable) {
                        normalLog += "\nFailed to Craft";
                        normalLog += "\nBonuses\n" + bonusListHash;
                    }
                }
            }
        }
        if (!haveEnoughDice) {
            result = "Insufficient dice";
        } else {
            result = String.format("Wins: %2.2f%% (%d/%d)", 
                    (1.0 * successes / totalRolls) * 100,
                    successes,
                    totalRolls
                    );
        }
        double timeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
        normalLog = String.format("Time to calculate: %.2fs\n", timeUsed) + normalLog;

        HashMap<String, String> resultHash = new HashMap<String, String>();
        resultHash.put("result", result);
        resultHash.put("debugLog",  debugLog);
        resultHash.put("normalLog", normalLog);
        return resultHash;
    }

    void rollWhiteDie() {
        for (GameBonus bonus : bonusListHash.get(GameBonus.Bonus.WD)) {
            if (debug_roll_all_1s) { 
                bonus.setWhiteDie(1); 
            } else {
                bonus.rollWhiteDie();
            }
        }
        if (debugLogEnable) { debugLog += "\nBonuses=" + bonusListHash; }
        if (normalLogEnable) { normalLog += "\nBonuses=" + bonusListHash; }
    }


    void rerollDice() {
        if (debugLogEnable) { debugLog += "\nBegin reroll dice"; }
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = rolledHashList.get(color);
            List<GameObject> needed = neededHashList.get(color);
            Collections.sort(rolls, Collections.reverseOrder());
            // Simple reroll algorithm:
            // find the first die that is short.  Reroll it and all smaller ones.
            for (int i=0; i < rolls.size(); i++) {
                if (rolls.get(i).getCurrValue() < needed.get(i).getCurrValue()) {
                    rerollHashList.get(color).addAll(rolls.subList(i,  rolls.size()));
                    rolls.subList(i, rolls.size()).clear();
                }
            }
        }
        if (normalLogEnable) { 
            normalLog += "\nKeep:\n" + rolledHashList.normalString();
            normalLog += "\nReroll old:\n" + rerollHashList.normalString();
        }
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            // reroll all dice in the rerollHashList.
            for (GameObject go : rerollHashList.get(color)) {
                go.setOrigValue(Math.abs(random.nextInt() % 6) + 1);
                rolledHashList.get(color).add(go);
            }
        }
        if (normalLogEnable) { 
            normalLog += "\nReroll new:\n" + rerollHashList.normalString();
        }
        for (GameBonus gb : bonusListHash.get(GameBonus.Bonus.WD)) {
            // TODO: Improve this, for now just reroll if below average
            if (gb.getWhiteDieValue() < 4) {
                if (normalLogEnable) {
                    normalLog += "\nReroll Old=" + gb;
                }
                gb.rollWhiteDie();
                if (debugLogEnable) { debugLog += "\nReroll white: " + gb; }
                if (normalLogEnable) { normalLog += " New=" + gb; }
            } else {
                if (debugLogEnable) { debugLog += "\nKeep white: " + gb; }
            }
        }
        if (debugLogEnable) { 
            debugLog += "\nRolled:" + rolledHashList + "\n";
        }

    }

    void rollAllNormalDice() {
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = roll(color, supplyHashInt.get(color));
            rolledHashList.put(color, rolls);
            Collections.sort(rolls, Collections.reverseOrder());
        }
        if (debugLogEnable) { debugLog += "\nRolled:" + rolledHashList + "\n"; }
        if (normalLogEnable) {
            normalLog += "\nRolled:\n" + rolledHashList.normalString();
        }
        rerollHashList = new DiceHashList();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            rerollHashList.put(color, new ArrayList<GameObject>());
        }
        if (debugLogEnable) { 
            debugLog += "\nExtra:\n" + rerollHashList + "\n";
            debugLog += "\nRolled:\n" + rolledHashList + "\n";
        }
    }

    void moveExtraToRerollHashList() {
        // Move extra dice into the rerollHashList
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = rolledHashList.get(color);
            rerollHashList.put(color, new ArrayList<GameObject>());
            Integer numNeeded = neededHashList.get(color).size();
            if (rolls.size() > numNeeded) {
                List<GameObject> extraRolls = new ArrayList<GameObject>(rolls.subList(numNeeded, rolls.size()));
                rolls.subList(numNeeded, rolls.size()).clear();
                rerollHashList.put(color, extraRolls);
            }
        }
    }

    void applyA1TO6() {
        if (bonusListHash.get(GameBonus.Bonus.A1TO6).size() > 0) {
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> rolls = rolledHashList.get(color);
                for (GameObject go : rolls) {
                    if (go.getOrigValue() == 1) {
                        go.applyBonus(bonusListHash.get(GameBonus.Bonus.A1TO6).get(0));  // use the first one.  more than one is redundant 
                    }
                }               
                Collections.sort(rolls, Collections.reverseOrder());
            }
            for (GameBonus gb : bonusListHash.get(GameBonus.Bonus.WD)) {
                if (gb.getWhiteDieValue() == 1) {
                    gb.setWhiteDie(6);
                    if (debugLogEnable) { debugLog += "\nUse: 1->6 on WD"; }  // TODO: Log nicely in normalLog
                }
            }
        }
        if (debugLogEnable) { debugLog += "\nAfter 1->6:" + rolledHashList + "\n"; }
    }


    // Permute each bonus to every color/die
    // Order is important:
    // 1) WD
    // 2) A6
    // 3) most others
    // 4) reroll (handled outside of this routine though)

    private ListIterator<GameBonus> iterator;
    private boolean recursionSuccess;
    private Integer dbgCount; 
    private void setupRecursion() {
        recursionSuccess = false;
        dbgCount = 0;
        iterator = bonusListHash.iterator();
    }
    
    private void recursion() {
        dbgCount++;
        if (debugLogEnable) { debugLog += "\nrecursion start"; }
        if (iterator.hasNext()) {
            GameBonus gb = iterator.next();
            if (debugLogEnable) { debugLog += "\nnext=" + gb; }
            if (gb.getBonusType() == GameBonus.Bonus.RR || gb.getBonusType() == GameBonus.Bonus.A1TO6) {
                // These are handled outside, just continue down the recursion
                recursion();
                iterator.previous();
            } else if (gb.getBonusType() == GameBonus.Bonus.WD) {
                doBonusWD(gb);
                iterator.previous();
            } else if (gb.getBonusType() == GameBonus.Bonus.P2 || gb.getBonusType() == GameBonus.Bonus.A6 ) {
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    List<GameObject> needed = neededHashList.get(color);
                    List<GameObject> rolls  = rolledHashList.get(color);
                    if (needed.size() == 0 || rolls.size() == 0) {
                        // rolls.size()==0 can happen if the white die isn't here yet
                        continue;
                    }
                    // usually, apply the bonus to each die in order, up to the number needed
                    // pick the minimum of needed vs rolls, they may not match if we're going to use a white die to fill the gap later
                    int start = 0;
                    int end   = Math.min(needed.size(), rolls.size());
                    // but only apply A6 to the lowest die needed doing more wouldn't break, but is unneeded work
                    if (gb.getBonusType() == GameBonus.Bonus.A6) {
                        start = end-1;
                    }
                    for (int i=start; i < end; i++) {
                        rolls.get(i).applyBonus(gb);
                        if (debugLogEnable) { debugLog += "\nAfter applying a bonus: " + rolledHashList; }
                        recursion();
                        if (recursionSuccess) { 
                            return;
                        }
                        rolls.get(i).removeBonus(gb);
                        if (debugLogEnable) { debugLog += "\nAfter removing a bonus: " + rolledHashList; }
                    }
                }
                // after looping through all colors and rolls, undo
                iterator.previous();
                if (debugLogEnable) { debugLog += "\nprevious= " + gb; }
            } else if (gb.getBonusType() == GameBonus.Bonus.P1X3
                    || gb.getBonusType() == GameBonus.Bonus.P1) {
                applyBestBonus(gb);
                if (debugLogEnable) { debugLog += "\nAfter applying P1X3 " + rolledHashList; }
                recursion();
                // dumb algorithm to just remove it from wherever it got put
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    for (GameObject roll : rolledHashList.get(color)) {
                        roll.removeBonusIfMatch(gb);
                    }
                }
                if (debugLogEnable) { debugLog += "\nAfter removing all P1 types " + rolledHashList; }
                iterator.previous();

            } else {
                if (debugLogEnable) { debugLog += "\nERROR! Not handling yet: " + gb; }
                if (normalLogEnable) { normalLog += "\nERROR! Not handling yet: " + gb; }
                recursion();
                iterator.previous();
            }
        } else {  // All bonuses assigned, check this combination
            if (debugLogEnable) { debugLog += "\nAfter bonuses: " + rolledHashList; }
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> tmpRolled = new ArrayList<GameObject>(rolledHashList.get(color));
                List<GameObject> needed = neededHashList.get(color);
                Collections.sort(tmpRolled, Collections.reverseOrder());
                boolean tmp = checkSuccess2(tmpRolled, needed);
                if (debugLogEnable) { debugLog += "\ncolor=" + color + "\ntmp=" + tmp; }
                if (!checkSuccess2(tmpRolled, needed)) {
                    return;
                }
            }
            if (normalLogEnable) {
                normalLog += "\nUsed:\n" + rolledHashList.verboseString();
                normalLog += "\nUnused:\n" + rerollHashList.verboseString();
            }
            recursionSuccess = true;
            return;
        }
    }

    private void doBonusWD(GameBonus gb) {
        boolean unused = true;
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = rolledHashList.get(color);
            List<GameObject> needed = neededHashList.get(color);
            if (needed.size() == 0) { 
                continue;
            }
            if (needed.size() > rolls.size()) {
                // if we didn't have enough of this color, add the white die here
                if (debugLogEnable) { debugLog += "\nUse: " + gb.toString() + " on:" + color; }
                unused = false;
                List<GameObject> saveRolls = new ArrayList<GameObject>(rolls);
                GameObject go = new GameObject(0, color, 0, 6);
                go.applyBonus(gb); 
                rolls.add(go);
                Collections.sort(rolls, Collections.reverseOrder());
                recursion();
                rolls = saveRolls;  // remove white die and restore original order
                break; // It's required to use the white die here, so just quit now
            } else {
                if (rolls.get(needed.size()-1).getCurrValue() < gb.applyBonus(null)) {
                    if (debugLogEnable) { debugLog += "\nUse: " + gb.toString() + " on:" + color; }
                    unused = false;
                    List<GameObject> saveRolls = new ArrayList<GameObject>(rolls);
                    rolls.get(needed.size()-1).applyBonus(gb);
                    Collections.sort(rolls, Collections.reverseOrder());
                    recursion();
                    rolls = saveRolls;  // restore original order
                    rolls.get(needed.size()-1).removeBonus(gb);
                }
            }
        }
        if (unused) {
            if (debugLogEnable) { debugLog += "\nUnused: " + gb.toString(); }
            recursion();
        }
    }

    // find the die that is farthest from it's goal (largestDeficit) and apply the bonus to it
    private DiceHashList applyBestBonusSortedCopy;
    Integer largestDeficit;
    GameObject.GOColor saveColor;
    Integer saveIndex;
    Integer saveValue;
    List<Integer> P1X3largestDeficit;
    List<GameObject.GOColor> P1X3saveColor;
    List<Integer> P1X3saveIndex;
    List<Integer> P1X3saveValue;
    private void applyBestBonus(GameBonus gb) {
        // This part of the algorithm needs to always have a sorted copy
        // But if we sort in place, the recursion algorithm above this gets confused.
        applyBestBonusSortedCopy = new DiceHashList();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            applyBestBonusSortedCopy.put(color, new ArrayList<GameObject>(rolledHashList.get(color)));
        }
        largestDeficit = Integer.MIN_VALUE;
        P1X3largestDeficit = new ArrayList<Integer>();
        P1X3saveColor = new ArrayList<GameObject.GOColor>();
        P1X3saveIndex = new ArrayList<Integer>();
        P1X3saveValue = new ArrayList<Integer>();
        for (int i=0; i < 3; i++) {
            P1X3largestDeficit.add(Integer.MIN_VALUE);
            P1X3saveColor.add(null);
            P1X3saveIndex.add(null);
            P1X3saveValue.add(null);
        }

        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> needed = neededHashList.get(color);
            if (needed.size() == 0) {
                continue;
            }
            List<GameObject> rolls = applyBestBonusSortedCopy.get(color);
            Collections.sort(rolls, Collections.reverseOrder());
            for (int i=0; i < needed.size(); i++) {
                int currValue = rolls.get(i).getCurrValue();
                int deficit = Math.max(needed.get(i).getCurrValue() - currValue, 0);
                if (gb.getBonusType() == GameBonus.Bonus.P1X3) {
                    for (int j=0; j < 3; j++) {
                        if (deficit > P1X3largestDeficit.get(j)) {
                            // do an insertion sort, and throw away last element
                            P1X3largestDeficit.remove(3-1);
                            P1X3saveColor.remove(3-1);
                            P1X3saveIndex.remove(3-1);
                            P1X3saveValue.remove(3-1);
                            P1X3largestDeficit.add(j, deficit);
                            P1X3saveColor.add(j, color);
                            P1X3saveIndex.add(j, i);
                            P1X3saveValue.add(j, gb.applyBonus(currValue));
                            if (debugLogEnable) { debugLog += "\nnew P1X3saveIndex:" + P1X3saveIndex; }
                            break;
                        }
                    }
                } else {
                    if (deficit > largestDeficit) {
                        largestDeficit = deficit;
                        saveColor = color;
                        saveIndex = i;
                        saveValue = gb.applyBonus(currValue);
                    }
                }
            }
        }
        if (gb.getBonusType() == GameBonus.Bonus.P1X3) {
            if (debugLogEnable) { debugLog += "\nfinal P1X3saveIndex:" + P1X3saveIndex; }
            for (int j=0; j < 3; j++) {
                if (P1X3largestDeficit.get(j) != Integer.MIN_VALUE) {
                    List<GameObject> rolls = rolledHashList.get(P1X3saveColor.get(j));
                    rolls.get(P1X3saveIndex.get(j)).applyBonus(gb);
                    if (debugLogEnable) { debugLog += "\nUse P1X3:" + rolledHashList + "\n"; }
                }
            }
            // can only resort after applying, otherwise the indexes are invalid.
            for (GameObject.GOColor color : GameObject.GOColor.values()) { 
                List<GameObject> rolls = applyBestBonusSortedCopy.get(color);
                Collections.sort(rolls, Collections.reverseOrder());
            }
        } else {
            if (largestDeficit != Integer.MIN_VALUE) {
                List<GameObject> rolls = applyBestBonusSortedCopy.get(saveColor);
                rolls.get(saveIndex).applyBonus(gb);
                Collections.sort(rolls, Collections.reverseOrder());
            }
        }
    }

    private boolean checkSuccess2(List<GameObject> rolled, List<GameObject> needed) {
        if (rolled.size() < needed.size()) {
            return false;
        }
        for (int i=0; i < needed.size(); i++) {
            if (rolled.get(i).getCurrValue() < needed.get(i).getCurrValue()) {
                return false;
            }
        }
        return true;
    }

    protected List<GameObject> roll(GameObject.GOColor color, int amountToRoll) {
        List<GameObject> rolls = new ArrayList<GameObject>();
        if (!debug_roll_all_1s) {
            for (int x = 0; x < amountToRoll; x++) {
                rolls.add(new GameObject(Math.abs(random.nextInt() % 6) + 1, color));
            }
        } else {
            for (int x = 0; x < amountToRoll; x++) {
                rolls.add(new GameObject(1, color));
            }
        }
        return rolls;
    }

//    private String diceHashListToString(HashMap<GameObject.GOColor, List<Integer>> diceHashList) {
//        String log = "\n";
//        for (GameObject.GOColor color : GameObject.GOColor.values()) {
//            log += rollsToString(color, diceHashList.get(color)) + " ";
//        }
//        return log;
//    }

//    private String rollsToString(GameObject.GOColor color, List<Integer> rolls) {
//        String log;
//        log = color + ":" + rolls; 
//        return log;
//    }

}
