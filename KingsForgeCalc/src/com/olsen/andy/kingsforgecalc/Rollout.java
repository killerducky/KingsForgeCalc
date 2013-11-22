package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;

public class Rollout {
    public SharedPreferences sharedPref;

    private Random random = new Random(new Date().getTime());
    private String log;
    private boolean log_enable;
    
    private HashMap<GameObject.GOColor, List<GameObject>> neededHashList;
    private HashMap<GameObject.GOColor, List<GameObject>> rolledHashList2;
    private HashMap<GameObject.GOColor, List<GameObject>> rerollHashList;

    private List<GameBonus> bonusList;
    private HashMap<GameBonus.Bonus, List<GameBonus>> bonusListHash;
    
    
    public Rollout(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }
    
    public HashMap<String, String> doRollout(
            HashMap<GameObject.GOColor, List<GameObject>> neededHashList,
            HashMap<GameObject.GOColor, Integer> supplyHashInt, 
            List<GameBonus> bonusList, 
            Integer totalRolls
            ) {
        boolean haveEnoughDice = true;
        Integer successes = 0;
        String result = "";
        log = ""; // initialize log
        long startTime = System.currentTimeMillis();
        this.rolledHashList2 = new HashMap<GameObject.GOColor, List<GameObject>>();
        this.rerollHashList  = new HashMap<GameObject.GOColor, List<GameObject>>();
        this.bonusListHash   = new HashMap<GameBonus.Bonus, List<GameBonus>>();
        this.neededHashList = neededHashList;
        this.bonusList = bonusList;

        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            if (neededHashList.get(color).size() > supplyHashInt.get(color)) {
                haveEnoughDice = false;
                break;
            }
        }
        
        for (GameBonus.Bonus bonusType : GameBonus.Bonus.values()) {
            bonusListHash.put(bonusType, new ArrayList<GameBonus>());
        }
        for (GameBonus bonus : bonusList) {
            bonusListHash.get(bonus.getBonusType()).add(bonus);
        }
        
        if (haveEnoughDice) {
            for (int x = 0; x < totalRolls; x++) {
                log_enable = (x==0 && sharedPref.getBoolean("pref_debug_log_enable",  false)); // only log the first run
                if (log_enable) { log += "Bonuses:"; } 

                // reset bonuses and reroll the white die
                for (GameBonus bonus : bonusList) { 
                    if (log_enable) { log += " " + bonus.toString(); }
                    bonus.resetAssignmentsAndReroll();  // after refactoring only really need the reroll part
                }
                boolean success = true;
                
                // Roll all normal dice
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
                    rolledHashList2.put(color, rolls);
                }
                if (log_enable) { log += "\nRolled:" + rolledHashList2 + "\n"; }

                // Apply bonuses
                if (bonusListHash.get(GameBonus.Bonus.A1TO6).size() > 0) {
                    for (GameObject.GOColor color : GameObject.GOColor.values()) {
                        List<GameObject> rolls = rolledHashList2.get(color);
                        for (GameObject go : rolls) {
                            if (go.getOrigValue() == 1) {
                                go.applyBonus(bonusListHash.get(GameBonus.Bonus.A1TO6).get(0));  // use the first one.  more than one is redundant 
                            }
                        }               
                        Collections.sort(rolls);
                        Collections.reverse(rolls);
                    }
                }
                if (log_enable) { log += "\nAfter 1->6:" + rolledHashList2 + "\n"; }
                doBonusWD();
                setupRecursion();
                recursion();
                if (log_enable) { log += "\ndbgCount=" + dbgCount; }
                // TODO: reroll
                success = recursionSuccess;
                
                if (success) { successes++; }
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

    // Permute each bonus to every color/die
    // Order is important:
    // 1) WD
    // 2) A6
    // 3) most others
    // 4) reroll (handled outside of this routine though)
    
    private Integer currBonusIndex;
    private boolean recursionSuccess;
    private Integer dbgCount; 
    private void setupRecursion() {
        currBonusIndex = 0;
        recursionSuccess = false;
        dbgCount = 0;
    }
    
    private void recursion() {
        if (currBonusIndex < bonusList.size()) { // Until we have assigned all the bonuses, keep going
            GameBonus gb = bonusList.get(currBonusIndex);
            if (gb.getBonusType() == GameBonus.Bonus.RR) {
                // RR is handled outside this
                currBonusIndex++;
                recursion();
                currBonusIndex--;
            } else {
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    List<GameObject> needed = neededHashList.get(color);
                    if (needed.size() == 0) {
                        continue;
                    }
                    List<GameObject> rolls  = rolledHashList2.get(color);
                    // usually, apply the bonus to each die in order, up to the number needed
                    int start = 0;
                    int end   = needed.size();
                    // but only apply A6 to the lowest die needed doing more wouldn't break, but is unneeded work
                    if (gb.getBonusType() == GameBonus.Bonus.A6) {
                        start = end-1;
                    }
                    for (int dupNum=0; dupNum<gb.getNumDups(); dupNum++) {
                        for (int i=start; i < end; i++) {
                            if (gb.getBonusType() == GameBonus.Bonus.P1X3) {
                                // if *this* P1X3 has been applied to *this* die
                                if (rolls.get(i).duplicateCheck(gb)) { 
                                    continue;
                                }
                            }
                            rolls.get(i).applyBonus(gb);
                            currBonusIndex++;
                            recursion();
                            if (recursionSuccess) { 
                                return;
                            }
                            rolls.get(i).removeBonus(gb);
                            currBonusIndex--;
                        }
                    }
                }
            }
        } else {  // All bonuses assigned, check this combination
            //if (log_enable) { log += "\nAfter bonuses: " + rolledHashList2; }
            dbgCount++;
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> tmpRolled = new ArrayList<GameObject>(rolledHashList2.get(color));
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

    private void doBonusWD() {
        for (GameBonus gb : bonusListHash.get(GameBonus.Bonus.WD)) {
            for (GameObject.GOColor color : GameObject.GOColor.values()) {
                List<GameObject> rolls = rolledHashList2.get(color);
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
                    continue;
                } else {
                    if (rolls.get(needed.size()-1).getCurrValue() < gb.applyBonus(null)) {
                        rolls.get(needed.size()-1).applyBonus(gb);
                        Collections.sort(rolls);
                        Collections.reverse(rolls);
                        // FIXME: Improve by trying other colors too
                        continue;
                    }
                }
            }
        }
    }

    // find the die that is farthest from it's goal (largestDeficit) and apply the bonus to it
    // FIXME: P1X3 not correct
    Integer largestDeficit;
    GameObject.GOColor saveColor;
    Integer saveIndex;
    Integer saveValue;
    List<Integer> P1X3largestDeficit;
    List<GameObject.GOColor> P1X3saveColor;
    List<Integer> P1X3saveIndex;
    List<Integer> P1X3saveValue;
    private void applyBestBonus(GameBonus.Bonus bonusType) {
        for (GameBonus gb : bonusList) {
            if (gb.getBonusType() != bonusType) {
                continue;
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
                List<GameObject> rolls = rolledHashList2.get(color);
                // For the A6 type, only consider the smallest die.
                if (bonusType == GameBonus.Bonus.A6) {
                    int newDeficit = 0;
                    for (int i=1; i < needed.size(); i++) {
                        //      i   0   1   2   3
                        // Needed   0   1   2   3
                        //  Rolls   A6  0   1   2  <--- new auto 6 goes to top, other rolls shift down one
                        //              ^------------- compare needed[i] to rolls[i-1]
                        newDeficit += Math.max(needed.get(i).getCurrValue() - rolls.get(i-1).getCurrValue(), 0);
                    }
                    if (newDeficit > largestDeficit) {
                        largestDeficit = newDeficit;
                        saveColor = color;
                        saveIndex = needed.size()-1;
                        saveValue = 6;   // optimize out gb.applyBonus(null)
                    }
                } else {
                    for (int i=0; i < needed.size(); i++) {
                        int currValue = rolls.get(i).getCurrValue();
                        int deficit = Math.max(needed.get(i).getCurrValue() - currValue, 0);
                        if (bonusType == GameBonus.Bonus.P1X3) {
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
            }
            if (bonusType == GameBonus.Bonus.P1X3) {
                if (log_enable) { log += "\nfinal P1X3saveIndex:" + P1X3saveIndex; }
                for (int j=0; j < 3; j++) {
                    if (P1X3largestDeficit.get(j) != Integer.MIN_VALUE) {
                        List<GameObject> rolls = rolledHashList2.get(P1X3saveColor.get(j));
                        rolls.get(P1X3saveIndex.get(j)).applyBonus(gb);
                        if (log_enable) { log += "\nUse P1X3:" + rolledHashList2 + "\n"; }
                    }
                }
                // can only resort after applying, otherwise the indexes are invalid.
                for (GameObject.GOColor color : GameObject.GOColor.values()) { 
                    List<GameObject> rolls = rolledHashList2.get(color);
                    Collections.sort(rolls);
                    Collections.reverse(rolls);
                }
            } else {
                if (largestDeficit != Integer.MIN_VALUE) {
                    List<GameObject> rolls = rolledHashList2.get(saveColor);
                    rolls.get(saveIndex).applyBonus(gb);
                    Collections.sort(rolls);
                    Collections.reverse(rolls);
                }
            }
        }
    }


    private boolean checkSuccess() {
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            List<GameObject> rolled = rolledHashList2.get(color);
            List<GameObject> needed = neededHashList.get(color);
            for (int i=0; i < needed.size(); i++) {
                if (rolled.get(i).getCurrValue() < needed.get(i).getCurrValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkSuccess2(List<GameObject> rolled, List<GameObject> needed) {
        for (int i=0; i < needed.size(); i++) {
            if (rolled.get(i).getCurrValue() < needed.get(i).getCurrValue()) {
                return false;
            }
        }
        return true;
    }

    private List<GameObject> roll(GameObject.GOColor color, int amountToRoll) {
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

    private String diceHashListToString(HashMap<GameObject.GOColor, List<Integer>> diceHashList) {
        String log = "\n";
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            log += rollsToString(color, diceHashList.get(color)) + " ";
        }
        return log;
    }

    private String rollsToString(GameObject.GOColor color, List<Integer> rolls) {
        String log;
        log = color + ":" + rolls; 
        return log;
    }
    
}
