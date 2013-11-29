package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class Rollout {
    private Random random = new Random();
    private StringBuilder debugLog;
    private StringBuilder normalLog;
    private boolean debugLogEnable = false;
    private boolean normalLogEnable = false;

    DiceHashList neededHashList;
    DiceHashList rolledHashList;
    DiceHashList rerollHashList;
    private HashMap<GameObject.GOColor, Integer> supplyHashInt;
    boolean debug_roll_all_1s;
    boolean pref_debug_log_enable;
    GameBonusHashList bonusHashList;
    Integer successes = 0;
    StringBuilder result;
    long startTime;
    Integer totalRolls;    
    boolean haveEnoughDice;
    
//    class RolloutResults {
//        String normalLog;
//        String debugLog;
//        String result;
//        Integer successes;
//        Integer totalRolls;
//    }
    
    public Rollout(boolean debug_roll_all_1s, boolean pref_debug_log_enable) {
        this.debug_roll_all_1s = debug_roll_all_1s;
        this.pref_debug_log_enable = pref_debug_log_enable;
    }

    public HashMap<String, String> doRollout(
            DiceHashList neededHashList,
            HashMap<GameObject.GOColor, Integer> supplyHashInt, 
            List<GameBonus> bonusList, 
            Integer totalRolls
            ) {
        debugLog = new StringBuilder();
        normalLog = new StringBuilder();
        Integer diceDeficit = 0;   

        startTime = System.currentTimeMillis();
        rolledHashList = new DiceHashList();
        rerollHashList  = new DiceHashList();
        bonusHashList   = new GameBonusHashList();
        this.neededHashList = neededHashList;
        this.supplyHashInt  = supplyHashInt;
        this.totalRolls = totalRolls;
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            diceDeficit += Math.max(0, neededHashList.get(color).size() - supplyHashInt.get(color));
        }

        for (GameBonus.Bonus bonusType : GameBonus.Bonus.values()) {
            bonusHashList.put(bonusType, new ArrayList<GameBonus>());
        }
        for (GameBonus bonus : bonusList) {
            bonusHashList.get(bonus.getBonusType()).add(bonus);
        }
        
        normalLog.append("\nCraft Card:\n" + neededHashList.normalString());

        haveEnoughDice = diceDeficit <= bonusHashList.get(GameBonus.Bonus.WD).size();
        if (haveEnoughDice) {
            for (int x = 0; x < totalRolls; x++) {
                normalLogEnable = (x==0);
                debugLogEnable = (x==0 && pref_debug_log_enable); // only log the first run
                if (debugLogEnable) { debugLog.append("\ndebug_roll_all_1s=" + debug_roll_all_1s); }
                rollAllNormalDice();
                rollWhiteDie();
                applyA1TO6();
                moveExtraToRerollHashList();
                setupRecursion();
                recursion();
                if (debugLogEnable) { debugLog.append("\ndbgCount=" + dbgCount); }
                if (!recursionSuccess && bonusHashList.get(GameBonus.Bonus.RR).size() > 0) {
                    pickRecommendedReroll();
                    if (totalRolls==1) {
                        HashMap<String, String> resultHash = new HashMap<String, String>();
                        resultHash.put("RerollDialog", "Yes");
                        return resultHash;
                    }
                    rerollDice();
                    applyA1TO6();
                    moveExtraToRerollHashList();
                    setupRecursion();
                    recursion();
                    if (debugLogEnable) { debugLog.append("\ndbgCount=" + dbgCount); }
                }
                if (recursionSuccess) { 
                    successes++;
                } else {
                    if (normalLogEnable) {
                        normalLog.append("\nFailed to Craft" +
                                "\nBonuses\n" + bonusHashList);
                    }
                }
            }
        }
        return formatResults();
    }

    HashMap<String, String> continueReroll() {
        if (normalLogEnable) {
            normalLog.append("\nAfter picking reroll:\n" +
                    "Keep:\n" + rolledHashList.normalString() +
                    "Reroll:\n" + rerollHashList.normalString()
            		);
        }
        rerollDice();
        applyA1TO6();
        moveExtraToRerollHashList();
        setupRecursion();
        recursion();
        if (debugLogEnable) { debugLog.append("\ndbgCount=" + dbgCount); }
        if (recursionSuccess) { 
            successes++;
        } else {
            if (normalLogEnable) {
                normalLog.append("\nFailed to Craft" +
                        "\nBonuses\n" + bonusHashList);
            }
        }
        return formatResults();
    }


    HashMap<String, String> formatResults() {
        if (!haveEnoughDice) {
            result = new StringBuilder("Insufficient dice");
        } else {
            result = new StringBuilder(String.format("Wins: %2.2f%% (%d/%d)", 
                    (1.0 * successes / totalRolls) * 100,
                    successes,
                    totalRolls
                    ));
        }
        double timeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
        normalLog.insert(0, String.format("Time to calculate: %.2fs\n", timeUsed));

        HashMap<String, String> resultHash = new HashMap<String, String>();
        resultHash.put("result", result.toString());
        resultHash.put("debugLog", debugLog.toString());
        resultHash.put("normalLog", normalLog.toString());
        return resultHash;
    }

    void rollWhiteDie() {
        for (GameBonus bonus : bonusHashList.get(GameBonus.Bonus.WD)) {
            if (debug_roll_all_1s) { 
                bonus.setWhiteDie(1); 
            } else {
                bonus.rollWhiteDie();
            }
        }
        if (debugLogEnable) { debugLog.append("\nBonuses=" + bonusHashList); }
        if (normalLogEnable) { normalLog.append("\nBonuses=" + bonusHashList); }
    }


    void pickRecommendedReroll() {
        if (debugLogEnable) { debugLog.append("\nBegin pick reroll"); }
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = rolledHashList.get(color);
            List<GameObject> needed = neededHashList.get(color);
            for (GameObject go : rolls) {
                // for now don't really consider the bonuses
                go.removeAllBonus();
                // but put back the A1TO6 bonus
                if (bonusHashList.get(GameBonus.Bonus.A1TO6).size() > 0) {
                    go.applyBonus(bonusHashList.get(GameBonus.Bonus.A1TO6).get(0));
                }
            }
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
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            Collections.sort(rerollHashList.get(color), Collections.reverseOrder());
        }
        for (GameBonus gb : bonusHashList.get(GameBonus.Bonus.WD)) {
            gb.setKeepWhiteDie(gb.getWhiteDieValue() >= 4);
        }
        if (normalLogEnable) { 
            normalLog.append("\nKeep:\n" + rolledHashList.normalString() +
                    "\nReroll old:\n" + rerollHashList.normalString());
        }
    }
    
    class SaveState {
        DiceHashList rolledHashList;
        DiceHashList rerollHashList;
        StringBuilder debugLog;
        StringBuilder normalLog;
        boolean debugLogEnable;
        boolean normalLogEnable;
        Integer totalRolls;
    }
    SaveState saveState;
    void saveStateBeforeReroll() {
        saveState = new SaveState();
        saveState.debugLog = new StringBuilder(debugLog);
        saveState.normalLog = new StringBuilder(normalLog);
        saveState.debugLogEnable = debugLogEnable;
        saveState.normalLogEnable = normalLogEnable;
        saveState.totalRolls = totalRolls;
        saveState.rolledHashList = new DiceHashList();
        saveState.rerollHashList = new DiceHashList();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            saveState.rolledHashList.put(color, new ArrayList<GameObject>());
            saveState.rerollHashList.put(color, new ArrayList<GameObject>());
            for (GameObject go : rolledHashList.get(color)) {
                saveState.rolledHashList.get(color).add(new GameObject(go));
            }
            for (GameObject go : rerollHashList.get(color)) {
                saveState.rerollHashList.get(color).add(new GameObject(go));
            }
        }
    }
    void restoreStateBeforeReroll(boolean resetSuccessCounter) {
        if (resetSuccessCounter) {
            successes = 0;
            totalRolls = saveState.totalRolls;
        }
        debugLog = saveState.debugLog;
        normalLog = saveState.normalLog;
        debugLogEnable = saveState.debugLogEnable;
        normalLogEnable = saveState.normalLogEnable;
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            rolledHashList.put(color, new ArrayList<GameObject>());
            rerollHashList.put(color, new ArrayList<GameObject>());
            for (GameObject go : saveState.rolledHashList.get(color)) {
                rolledHashList.get(color).add(new GameObject(go));
            }
            for (GameObject go : saveState.rerollHashList.get(color)) {
                rerollHashList.get(color).add(new GameObject(go));
            }
        }
    }
    
    // Expected State:
    // rolledHashList - dice to keep, gameBonuses removed (including fake white die)
    // rerollHashList - dice to roll
    // bonusHashList  - keepWhiteDie has been set
    void rerollDice() {
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            // reroll all dice in the rerollHashList.
            for (GameObject go : rerollHashList.get(color)) {
                go.setOrigValue(Math.abs(random.nextInt() % 6) + 1);
                rolledHashList.get(color).add(go);
            }
        }
        // log results before sorting again so you can see the original and new separately
        if (normalLogEnable) { normalLog.append("\nReroll new:\n" + rerollHashList.normalString()); }
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            Collections.sort(rolledHashList.get(color), Collections.reverseOrder());
        }
        for (GameBonus gb : bonusHashList.get(GameBonus.Bonus.WD)) {
            // TODO: Improve this, for now just reroll if below average
            if (gb.getKeepWhiteDie()) {
                if (debugLogEnable) { debugLog.append("\nKeep white: " + gb); }
            } else {
                if (normalLogEnable) { normalLog.append("\nReroll Old=" + gb); }
                gb.rollWhiteDie();
                if (debugLogEnable) { debugLog.append("\nReroll white: " + gb); }
                if (normalLogEnable) { normalLog.append(" New=" + gb); }
            }
        }
        if (debugLogEnable) { 
            debugLog.append("\nRolled:" + rolledHashList + "\n");
        }

    }

    void rollAllNormalDice() {
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolls = roll(color, supplyHashInt.get(color));
            rolledHashList.put(color, rolls);
            Collections.sort(rolls, Collections.reverseOrder());
        }
        if (debugLogEnable) { debugLog.append("\nRolled:" + rolledHashList + "\n"); }
        if (normalLogEnable) {
            normalLog.append("\nRolled:\n" + rolledHashList.normalString());
        }
        rerollHashList = new DiceHashList();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            rerollHashList.put(color, new ArrayList<GameObject>());
        }
        if (debugLogEnable) { 
            debugLog.append("\nExtra:\n" + rerollHashList + "\n");
            debugLog.append("\nRolled:\n" + rolledHashList + "\n");
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
        if (bonusHashList.get(GameBonus.Bonus.A1TO6).size() > 0) {
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> rolls = rolledHashList.get(color);
                for (GameObject go : rolls) {
                    if (go.getOrigValue() == 1) {
                        go.applyBonus(bonusHashList.get(GameBonus.Bonus.A1TO6).get(0));  // use the first one.  more than one is redundant 
                    }
                }               
                Collections.sort(rolls, Collections.reverseOrder());
            }
            for (GameBonus gb : bonusHashList.get(GameBonus.Bonus.WD)) {
                if (gb.getWhiteDieValue() == 1) {
                    gb.setWhiteDie(6);
                    if (debugLogEnable) { debugLog.append("\nUse: 1->6 on WD"); }  // TODO: Log nicely in normalLog
                }
            }
        }
        if (debugLogEnable) { debugLog.append("\nAfter 1->6:" + rolledHashList + "\n"); }
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
        iterator = bonusHashList.iterator();
    }
    
    private void recursion() {
        if (recursionSuccess) {
            return;
        }
        dbgCount++;
        if (debugLogEnable) { debugLog.append("\nrecursion start"); }
        if (iterator.hasNext()) {
            GameBonus gb = iterator.next();
            if ( gb.getBonusType() != GameBonus.Bonus.RR &&
                    gb.getBonusType() != GameBonus.Bonus.A1TO6 && 
                    gb.getBonusType() != GameBonus.Bonus.WD ) {
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    // If the white die got placed too early this could happen
                    if (rolledHashList.get(color).size() < neededHashList.get(color).size()) {
                        if (debugLogEnable) { debugLog.append("\nInsufficient dice"); }
                        gb = iterator.previous();
                        return;
                    }
                }
            }
            if (debugLogEnable) { debugLog.append("\nnext=" + gb); }
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
                        List<GameObject> saveRolls = new ArrayList<GameObject>(rolls);
                        Collections.sort(rolls, Collections.reverseOrder());
                        if (debugLogEnable) { debugLog.append("\nAfter applying a bonus: " + rolledHashList); }
                        recursion();
                        rolls = saveRolls;
                        rolls.get(i).removeBonus(gb);
                        if (debugLogEnable) { debugLog.append("\nAfter removing a bonus: " + rolledHashList); }
                    }
                }
                // after looping through all colors and rolls, undo
                iterator.previous();
                if (debugLogEnable) { debugLog.append("\nprevious= " + gb); }
            } else if (gb.getBonusType() == GameBonus.Bonus.P1X3
                    || gb.getBonusType() == GameBonus.Bonus.P1) {
                applyBestBonus(gb);
                if (debugLogEnable) { debugLog.append("\nAfter applying P1X3 " + rolledHashList); }
                recursion();
                // dumb algorithm to just remove it from wherever it got put
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    for (GameObject roll : rolledHashList.get(color)) {
                        roll.removeBonusIfMatch(gb);
                    }
                }
                if (debugLogEnable) { debugLog.append("\nAfter removing all P1 types " + rolledHashList); }
                iterator.previous();

            } else {
                if (debugLogEnable) { debugLog.append("\nERROR! Not handling yet: " + gb); }
                if (normalLogEnable) { normalLog.append("\nERROR! Not handling yet: " + gb); }
                recursion();
                iterator.previous();
            }
        } else {  // All bonuses assigned, check this combination
            if (debugLogEnable) { debugLog.append("\nAfter bonuses: " + rolledHashList); }
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> tmpRolled = new ArrayList<GameObject>(rolledHashList.get(color));
                List<GameObject> needed = neededHashList.get(color);
                Collections.sort(tmpRolled, Collections.reverseOrder());
                boolean tmp = checkSuccess2(tmpRolled, needed);
                if (debugLogEnable) { debugLog.append("\ncolor=" + color + "\ntmp=" + tmp); }
                if (!checkSuccess2(tmpRolled, needed)) {
                    return;
                }
            }
            if (normalLogEnable) {
                normalLog.append("\nUsed:\n" + rolledHashList.verboseString());
                normalLog.append("\nUnused:\n" + rerollHashList.verboseString());
            }
            recursionSuccess = true;
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
                if (debugLogEnable) { debugLog.append("\nUse: " + gb.toString() + " on:" + color); }
                unused = false;
                List<GameObject> saveRolls = new ArrayList<GameObject>(rolls);
                GameObject go = new GameObject(0, color, 0, 6);
                go.applyBonus(gb); 
                rolls.add(go);
                Collections.sort(rolls, Collections.reverseOrder());
                recursion();
                rolledHashList.put(color, saveRolls);  // remove white die and restore original order
                break; // It's required to use the white die here, so just quit now
            } else {
                if (rolls.get(needed.size()-1).getCurrValue() < gb.applyBonus(null)) {
                    if (debugLogEnable) { debugLog.append("\nUse: " + gb.toString() + " on:" + color); }
                    unused = false;
                    List<GameObject> saveRolls = new ArrayList<GameObject>(rolls);
                    rolls.get(needed.size()-1).applyBonus(gb);
                    Collections.sort(rolls, Collections.reverseOrder());
                    recursion();
                    saveRolls.get(needed.size()-1).removeBonus(gb);
                    rolledHashList.put(color, saveRolls);  // remove white die and restore original order
                }
            }
        }
        if (unused) {
            if (debugLogEnable) { debugLog.append("\nUnused: " + gb.toString()); }
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
        // TODO: Changing code so everyone is responsible for resorting dice after making changes
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
                            if (debugLogEnable) { debugLog.append("\nnew P1X3saveIndex:" + P1X3saveIndex); }
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
            if (debugLogEnable) { debugLog.append("\nfinal P1X3saveIndex:" + P1X3saveIndex); }
            for (int j=0; j < 3; j++) {
                if (P1X3largestDeficit.get(j) != Integer.MIN_VALUE) {
                    List<GameObject> rolls = rolledHashList.get(P1X3saveColor.get(j));
                    rolls.get(P1X3saveIndex.get(j)).applyBonus(gb);
                    if (debugLogEnable) { debugLog.append("\nUse P1X3:" + rolledHashList + "\n"); }
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

}
