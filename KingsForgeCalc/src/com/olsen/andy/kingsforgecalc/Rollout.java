package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Rollout {
	Random random = new Random(new Date().getTime());
    String log;
    boolean log_enable;
    
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
	
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		if (neededHashList.get(color).size() > supplyHashInt.get(color)) {
    			haveEnoughDice = false;
    			break;
    		}
    	}
		if (haveEnoughDice) {
			for (int x = 0; x < totalRolls; x++) {
				log_enable = x==0;
				if (log_enable) { log += "Bonuses:"; } 
				for (GameBonus bonus : bonusList) { 
					if (log_enable) { log += " " + bonus.toString(); }
					bonus.resetAssignmentsAndReroll();
				}
				boolean success = true;
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = roll(supplyHashInt.get(color));
					if (log_enable) {
						Collections.sort(rolls);
						Collections.reverse(rolls);
						log += "\n" + color + ":"; 
						for (Integer roll : rolls) {
							log += " " + roll;
						}
					}
					for (GameBonus bonus : bonusList) { bonus.apply1to6(rolls); }
					Collections.sort(rolls);
					Collections.reverse(rolls);
					if (log_enable) {
						log += "\nAfter 1->6:";
						Collections.sort(rolls);
						Collections.reverse(rolls);
						for (Integer roll : rolls) {
							log += " " + roll;
						}
						log += "\n";
					}
					if (!checkSuccess(rolls, neededHashList.get(color), bonusList)) {
						success = false;
						continue;
					}
				}
				if (success) { successes++; }
			}
		}
    	if (result == "") { 
    		if (!haveEnoughDice) {
				result = "Insufficient dice";
			} else {
				result = "Total successes: " + successes;
				result += "\nTotal rolls: " + totalRolls;
				// TODO: Why couldn't I put the trailing directly in the format, with/without escaping it.
				result += String.format("\nChance to win: %2.2f", (1.0 * successes / totalRolls) * 100) + "%";
			}
		}
    	HashMap<String, String> resultHash = new HashMap<String, String>();
    	resultHash.put("result", result);
    	resultHash.put("log",  log);
    	return resultHash;
	}


	private boolean checkSuccess(List<Integer> rolled, List<Integer> needed, List<GameBonus> bonusList) {
        int x = 0;
        Integer thisRolled;
        for (Integer need : needed) {
        	thisRolled = rolled.get(x++);
            if (thisRolled < need) {
            	if (!applyCheapestBonus(thisRolled, need, bonusList)) {
            		return false;
            	}
            }
        }
        return true;
    }
    
    // TODO: handle applying multiple bonuses to the same die
    private boolean applyCheapestBonus(Integer rolled, Integer needed, List<GameBonus> bonusList) {
    	Integer lowestCost = Integer.MAX_VALUE;
    	GameBonus currentUsedGb = null;
    	for (GameBonus gb : bonusList) {
        	if (!gb.allUsed()) {
        		if (gb.applyBonus(rolled) >= needed) {
        			if (gb.cost() < lowestCost ) {
        				lowestCost = gb.cost();
        				if (currentUsedGb != null) {
        					// we found a cheaper bonus, so reset the old one to be unused
        					currentUsedGb.resetAssignmentsDoNotReroll();
        				}
        				currentUsedGb = gb;
                		gb.addTarget(new GameObject(rolled, GameObject.GOColor.BLACK)); // FIXME I should point to the original GameObject
        			}
        		}
        	}
        }
    	if (currentUsedGb != null) {
    		if (log_enable) { log += "use " + currentUsedGb.toString(); }
    	}
    	return (currentUsedGb != null);  // if we succeeded this will have a value
    }
  
    private List<Integer> roll(int amountToRoll) {
        List<Integer> rolls = new ArrayList<Integer>();
        for (int x = 0; x < amountToRoll; x++) {
            rolls.add(Math.abs(random.nextInt() % 6) + 1);
 
        }
        return rolls;
    }

}
