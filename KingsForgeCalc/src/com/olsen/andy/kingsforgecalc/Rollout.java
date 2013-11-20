package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Arrays;
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
				
				// Roll all normal dice
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = roll(supplyHashInt.get(color));
					Collections.sort(rolls);
					Collections.reverse(rolls);
					rolledHashList.put(color, rolls);
				}
				if (log_enable) { log += "\nRolled:" + diceHashListToString(rolledHashList) + "\n"; }

				// Apply bonuses
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = rolledHashList.get(color);
					for (GameBonus bonus : bonusList) { bonus.apply1to6(rolls); }
					Collections.sort(rolls);
					Collections.reverse(rolls);
				}
				if (log_enable) { log += "\nAfter 1->6:" + diceHashListToString(rolledHashList) + "\n"; }
				doBonusWD();
				applyBestBonus(GameBonus.Bonus.A6);
				applyBestBonus(GameBonus.Bonus.P2);
				applyBestBonus(GameBonus.Bonus.P1);
				if (log_enable) { log += "\nAfter A6,P2,P1:" + diceHashListToString(rolledHashList) + "\n"; }
				applyBestBonus(GameBonus.Bonus.P1X3);
				if (log_enable) { log += "\nAfter P1X3:" + diceHashListToString(rolledHashList) + "\n"; }
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
							// need code similar to Bonus.A6 code but have to do a sort...
							continue;
						}
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
				List<Integer> needed = neededHashList.get(color);
				if (needed.size() == 0) {
					continue;
				}
				List<Integer> rolls = rolledHashList.get(color);
				// For the A6 type, only consider the smallest die.
				if (bonusType == GameBonus.Bonus.A6) {
					int newDeficit = 0;
					for (int i=1; i < needed.size(); i++) {
						//      i   0   1   2   3
						// Needed   0   1   2   3
						//  Rolls   A6  0   1   2  <--- new auto 6 goes to top, other rolls shift down one
						//              ^------------- compare needed[i] to rolls[i-1]
						newDeficit += Math.max(needed.get(i) - rolls.get(i-1), 0);
					}
					if (newDeficit > largestDeficit) {
						largestDeficit = newDeficit;
						saveColor = color;
						saveIndex = needed.size()-1;
						saveValue = 6;   // optimize out gb.applyBonus(null)
					}
				} else {
					for (int i=0; i < needed.size(); i++) {
						int currValue = rolls.get(i);
						int deficit = Math.max(needed.get(i) - currValue, 0);
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
						List<Integer> rolls = rolledHashList.get(P1X3saveColor.get(j));
						rolls.set(P1X3saveIndex.get(j), P1X3saveValue.get(j));
						if (log_enable) { log += "\nUse P1X3:" + diceHashListToString(rolledHashList) + "\n"; }
					}
				}
				// can only resort after applying, otherwise the indexes are invalid.
				for (GameObject.GOColor color : GameObject.GOColor.values()) { 
					List<Integer> rolls = rolledHashList.get(color);
					Collections.sort(rolls);
					Collections.reverse(rolls);
				}
			} else {
				if (largestDeficit != Integer.MIN_VALUE) {
					List<Integer> rolls = rolledHashList.get(saveColor);
					rolls.set(saveIndex, saveValue);
					Collections.sort(rolls);
					Collections.reverse(rolls);
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

	private String diceHashListToString(HashMap<GameObject.GOColor, List<Integer>> diceHashList) {
		String log = "\n";
		for (GameObject.GOColor color : GameObject.GOColor.values()) {
			log += rollsToString(color, diceHashList.get(color)) + " ";
		}
		return log;
	}

	private String rollsToString(GameObject.GOColor color, List<Integer> rolls) {
		String log;
		log = color + ":"; 
		for (Integer roll : rolls) {
			log += roll;
		}
		return log;
	}
	
}
