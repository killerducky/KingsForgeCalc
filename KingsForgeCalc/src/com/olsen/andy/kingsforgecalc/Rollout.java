package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import android.content.SharedPreferences;

public class Rollout {
    private SharedPreferences sharedPref;

    private Random random = new Random(new Date().getTime());
    private String log;
    private boolean log_enable;

    private DiceHashList neededHashList;
    private DiceHashList rolledHashList;
    private DiceHashList rerollHashList;
    private HashMap<GameObject.GOColor, Integer> supplyHashInt;

    private GameBonusHashList bonusListHash;

    public Rollout(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
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
        log = ""; // initialize log
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

        boolean haveEnoughDice = diceDeficit <= bonusListHash.get(GameBonus.Bonus.WD).size();
        if (haveEnoughDice) {
            for (int x = 0; x < totalRolls; x++) {
                log_enable = (x==0 && sharedPref.getBoolean("pref_debug_log_enable",  false)); // only log the first run
                // reset bonuses and reroll the white die
                for (GameBonus bonus : bonusList) { 
                    bonus.resetAssignmentsAndReroll();  // after refactoring only really need the reroll part
                }
                if (log_enable) { log += "\nBonuses=" + bonusListHash; }

                rollAllNormalDice();
                applyA1TO6();
                setupRecursion();
                recursion();
                if (log_enable) { log += "\ndbgCount=" + dbgCount; }
                if (!recursionSuccess) {
                    rerollDice();
                }
                // TODO: reroll
                if (recursionSuccess) { successes++; }
            }
        }
        if (result == "") { 
            if (!haveEnoughDice) {
                result = "Insufficient dice";
            } else {
                result = "Total successes: " + successes;
                result += "\nTotal rolls: " + totalRolls;
                result += String.format("\nChance to win: %2.2f%%", (1.0 * successes / totalRolls) * 100);
            }
        }
        double timeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
        log = String.format("Time: %.2fs\n", timeUsed) + log;

        HashMap<String, String> resultHash = new HashMap<String, String>();
        resultHash.put("result", result);
        resultHash.put("log",  log);
        return resultHash;
    }
    
    void rerollDice() {
        
    }

    void rollAllNormalDice() {
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = roll(color, supplyHashInt.get(color));
            Collections.sort(rolls);
            Collections.reverse(rolls);
            // Move extra dice into the rerollHashList
            Integer numNeeded = neededHashList.get(color).size();
            if (rolls.size() > numNeeded) {
                List<GameObject> extraRolls = rolls.subList(numNeeded, rolls.size());
                rolls.subList(numNeeded, rolls.size()).clear();
                rerollHashList.put(color, extraRolls);
            }
            rolledHashList.put(color, rolls);
        }
        if (log_enable) { log += "\nRolled:" + rolledHashList + "\n"; }
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
                Collections.sort(rolls);
                Collections.reverse(rolls);
            }
        }
        if (log_enable) { log += "\nAfter 1->6:" + rolledHashList + "\n"; }
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
        if (log_enable) { log += "\nrecursion start"; }
        if (iterator.hasNext()) {
            GameBonus gb = iterator.next();
            if (log_enable) { log += "\nnext=" + gb; }
            if (gb.getBonusType() == GameBonus.Bonus.RR) {
                // RR is handled outside this, just continue down the recursion
                recursion();
                iterator.previous();
            }
            if (gb.getBonusType() == GameBonus.Bonus.WD) {
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
                        if (log_enable) { log += "\nAfter applying a bonus: " + rolledHashList; }
                        recursion();
                        if (recursionSuccess) { 
                            return;
                        }
                        rolls.get(i).removeBonus(gb);
                        if (log_enable) { log += "\nAfter removing a bonus: " + rolledHashList; }
                    }
                }
                // after looping through all colors and rolls, undo
                iterator.previous();
                if (log_enable) { log += "\nprevious= " + gb; }
            } else if (gb.getBonusType() == GameBonus.Bonus.P1X3
                    || gb.getBonusType() == GameBonus.Bonus.P1) {
                applyBestBonus(gb);
                if (log_enable) { log += "\nAfter applying P1X3 " + rolledHashList; }
                recursion();
                // dumb algorithm to just remove it from wherever it got put
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    for (GameObject roll : rolledHashList.get(color)) {
                        roll.removeBonusIfMatch(gb);
                    }
                }
                if (log_enable) { log += "\nAfter removing all P1 types " + rolledHashList; }
                iterator.previous();

            } else {
                if (log_enable) { log += "\nNot handling yet"; }
                recursion();
                iterator.previous();
            }
        } else {  // All bonuses assigned, check this combination
            if (log_enable) { log += "\nAfter bonuses: " + rolledHashList; }
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> tmpRolled = new ArrayList<GameObject>(rolledHashList.get(color));
                List<GameObject> needed = neededHashList.get(color);
                Collections.sort(tmpRolled);
                Collections.reverse(tmpRolled);
                boolean tmp = checkSuccess2(tmpRolled, needed);
                if (log_enable) { log += "\ncolor=" + color + "\ntmp=" + tmp; }
                if (!checkSuccess2(tmpRolled, needed)) {
                    return;
                }
            }
            recursionSuccess = true;
            return;
        }
    }

    private void doBonusWD(GameBonus gb) {
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = rolledHashList.get(color);
            List<GameObject> needed = neededHashList.get(color);
            if (needed.size() == 0) { 
                continue;
            }
            if (needed.size() > rolls.size()) {
                // if we didn't have enough of this color, add the white die here
                log += "\nUse: " + gb.toString() + "on:" + color;
                GameObject go = new GameObject(1, color);
                go.applyBonus(gb); 
                rolls.add(go);
                recursion();
                go.removeBonus(gb);
                break; // It's required to use the white die here, so just quit now
            } else {
                if (rolls.get(needed.size()-1).getCurrValue() < gb.applyBonus(null)) {
                    log += "\nUse: " + gb.toString() + "on:" + color;
                    rolls.get(needed.size()-1).applyBonus(gb);
                    Collections.sort(rolls);
                    Collections.reverse(rolls);
                    recursion();
                    rolls.get(needed.size()-1).removeBonus(gb);
                }
            }
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
            Collections.sort(rolls);
            Collections.reverse(rolls);
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
                            if (log_enable) { log += "\nnew P1X3saveIndex:" + P1X3saveIndex; }
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
            if (log_enable) { log += "\nfinal P1X3saveIndex:" + P1X3saveIndex; }
            for (int j=0; j < 3; j++) {
                if (P1X3largestDeficit.get(j) != Integer.MIN_VALUE) {
                    List<GameObject> rolls = rolledHashList.get(P1X3saveColor.get(j));
                    rolls.get(P1X3saveIndex.get(j)).applyBonus(gb);
                    if (log_enable) { log += "\nUse P1X3:" + rolledHashList + "\n"; }
                }
            }
            // can only resort after applying, otherwise the indexes are invalid.
            for (GameObject.GOColor color : GameObject.GOColor.values()) { 
                List<GameObject> rolls = applyBestBonusSortedCopy.get(color);
                Collections.sort(rolls);
                Collections.reverse(rolls);
            }
        } else {
            if (largestDeficit != Integer.MIN_VALUE) {
                List<GameObject> rolls = applyBestBonusSortedCopy.get(saveColor);
                rolls.get(saveIndex).applyBonus(gb);
                Collections.sort(rolls);
                Collections.reverse(rolls);
            }
        }
    }

//    private boolean checkSuccess() {
//        for (GameObject.GOColor color : GameObject.GOColor.values()) {
//            List<GameObject> rolled = rolledHashList.get(color);
//            List<GameObject> needed = neededHashList.get(color);
//            for (int i=0; i < needed.size(); i++) {
//                if (rolled.get(i).getCurrValue() < needed.get(i).getCurrValue()) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

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
        boolean debug_roll_all_1s = sharedPref.getBoolean("pref_debug_all_1s", false);
        if (log_enable) { log += "\ndebug_roll_all_1s=" + debug_roll_all_1s; }
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
