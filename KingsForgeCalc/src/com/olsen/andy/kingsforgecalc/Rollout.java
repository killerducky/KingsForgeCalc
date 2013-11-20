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
    
	private HashMap<GameObject.GOColor, List<Integer>> neededHashList;
    private List<GameBonus> bonusList;
	private HashMap<GameObject.GOColor, List<Integer>> rolledHashList;
	
	
	public Rollout(SharedPreferences sharedPref) {
		this.sharedPref = sharedPref;
	}
	
	public HashMap<String, String> doRollout(
			HashMap<GameObject.GOColor, List<Integer>> neededHashList,
			HashMap<GameObject.GOColor, Integer> supplyHashInt, 
			List<GameBonus> bonusList, 
			Integer totalRolls
			) {
		boolean haveEnoughDice = true;
    	Integer successes = 0;
		String result = "";
		log = ""; // initialize log
		long startTime = System.currentTimeMillis();
		this.rolledHashList = new HashMap<GameObject.GOColor, List<Integer>>();
		this.neededHashList = neededHashList;
		this.bonusList = bonusList;

    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		if (neededHashList.get(color).size() > supplyHashInt.get(color)) {
    			haveEnoughDice = false;
    			break;
    		}
    	}
    	
		if (haveEnoughDice) {
			for (int x = 0; x < totalRolls; x++) {
				log_enable = (x==0 && sharedPref.getBoolean("pref_debug_log_enable",  false)); // only log the first run
				if (log_enable) { log += "Bonuses:"; } 

				// reset bonuses and reroll the white die
				for (GameBonus bonus : bonusList) { 
					if (log_enable) { log += " " + bonus.toString(); }
					bonus.resetAssignmentsAndReroll();
				}
				boolean success = true;
				
				// roll all other dice, and apply A1TO6 bonus
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = roll(supplyHashInt.get(color));
					rolledHashList.put(color, rolls);
					if (log_enable) { log += rollsToString(color, rolledHashList.get(color)); }
					for (GameBonus bonus : bonusList) { bonus.apply1to6(rolledHashList.get(color)); }
					Collections.sort(rolls);
					Collections.reverse(rolls);
					if (log_enable) { log += "\nAfter 1->6:" + rollsToString(color, rolls) + "\n"; }
				}
				// apply more bonuses
				doBonusWD();
				findLargestGainAndApplyBonus(GameBonus.Bonus.A6);
				findLargestGainAndApplyBonus(GameBonus.Bonus.P2);
				findLargestGainAndApplyBonus(GameBonus.Bonus.P1);
				findLargestGainAndApplyBonus(GameBonus.Bonus.P1X3);
				// TODO: reroll
				success = checkSuccess();
				
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

	private void doBonusWD() {
		for (GameBonus gb : bonusList) {
			if (gb.getBonusType() == GameBonus.Bonus.WD) {
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = rolledHashList.get(color);
					List<Integer> needed = neededHashList.get(color);
					if (needed.size() == 0) { 
						continue;
					}
					if (needed.size() > rolls.size()) {
						// if we didn't have enough of this color, add the white die here
						log += "\nUse: " + gb.toString() + "on:" + color;
						rolls.add(gb.applyBonus(null));
						continue;
					} else {
						if (rolls.get(needed.size()-1) < gb.applyBonus(null)) {
							rolls.set(needed.size()-1, gb.applyBonus(null));
							Collections.sort(rolls);
							Collections.reverse(rolls);
							// FIXME: Improve by trying other colors too
							continue;
						}
					}
				}
			}
		}
	}

	// find the smallest die that we actually need, and apply the bonus to it
	// FIXME: P1X3 not correct
	private void findLargestGainAndApplyBonus(GameBonus.Bonus bonusType) {
		for (GameBonus gb : bonusList) {
			if (gb.getBonusType() == bonusType) {
				int largestGain = 0;
				GameObject.GOColor saveColor = null;
				Integer saveIndex = null;
				Integer saveValue = null;
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> needed = neededHashList.get(color);
					if (needed.size() == 0) {
						continue;
					}
					List<Integer> rolls = rolledHashList.get(color);
					for (int i=0; i < needed.size(); i++) {
						int currValue = rolls.get(i);
						int gain = gb.applyBonus(currValue) - currValue;
						gain = Math.min(gain, needed.get(i) - currValue);  // don't give extra credit to overshooting the value
						if (gain > largestGain) {
							largestGain = gain;
							saveColor = color;
							saveIndex = i;
							saveValue = gb.applyBonus(currValue);
						}
					}
				}
				if (largestGain != 0) {
					rolledHashList.get(saveColor).set(saveIndex, saveValue);
				}
			}
		}
	}
	
	
	private boolean checkSuccess() {
		for (GameObject.GOColor color : GameObject.GOColor.values()) {
			List<Integer> rolled = rolledHashList.get(color);
			List<Integer> needed = neededHashList.get(color);
			for (int i=0; i < needed.size(); i++) {
				if (rolled.get(i) < needed.get(i)) {
					return false;
				}
			}
		}
        return true;
	}

	private List<Integer> roll(int amountToRoll) {
        List<Integer> rolls = new ArrayList<Integer>();
        boolean debug_roll_all_1s = sharedPref.getBoolean("pref_debug_all_1s", false);
        if (log_enable) { log += "\ndebug_roll_all_1s=" + debug_roll_all_1s; }
        if (!debug_roll_all_1s) {
        	for (int x = 0; x < amountToRoll; x++) {
        		rolls.add(Math.abs(random.nextInt() % 6) + 1);
        	}
        } else {
        	for (int x = 0; x < amountToRoll; x++) {
        		rolls.add(1);
        	}
        }
        return rolls;
    }

	private String rollsToString(GameObject.GOColor color, List<Integer> rolls) {
		String log;
		Collections.sort(rolls);
		Collections.reverse(rolls);
		log = "\n" + color + ":"; 
		for (Integer roll : rolls) {
			log += " " + roll;
		}
		return log;
	}
	
}
