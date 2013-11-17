package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Rollout {
	static Random random = new Random(new Date().getTime());

	public static String doRollout(
			HashMap<GameObject.GOColor, List<Integer>> neededHashList,
			HashMap<GameObject.GOColor, Integer> supplyHashInt, 
			List<GameBonus> bonusList, 
			Integer totalRolls
			) {
		boolean haveEnoughDice = true;
    	double successes = 0;

		String result = "";
		if (haveEnoughDice) {
			for (int x = 0; x < totalRolls; x++) {
				for (GameBonus bonus : bonusList) { bonus.resetAssignmentsAndReroll(); }
				boolean success = true;
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = roll(supplyHashInt.get(color));
					for (GameBonus bonus : bonusList) { bonus.apply1to6(rolls); }
					Collections.sort(rolls);
					Collections.reverse(rolls);
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
				result += String.format("\nChance to win: %2.2f", (successes / totalRolls) * 100) + "%";
			}
		}
    	return result;
	}


	private static boolean checkSuccess(List<Integer> rolled, List<Integer> needed, List<GameBonus> bonusList) {
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
    private static boolean applyCheapestBonus(Integer rolled, Integer needed, List<GameBonus> bonusList) {
    	Integer tmp = 0;
    	Integer highestPriority = 0;
    	GameBonus currentUsedGb = null;
    	for (GameBonus gb : bonusList) {
        	if (!gb.allUsed()) {
        		if (gb.applyBonus(rolled) >= needed) {
        			if (gb.priority() > highestPriority ) {
        				highestPriority = gb.priority();
        				if (currentUsedGb != null) {
        					// we found a cheaper bonus, so reset the old one to be unused
        					currentUsedGb.resetAssignmentsDoNotReroll();
        				}
        				currentUsedGb = gb;
                		gb.addTarget(new GameObject(rolled, GameObject.GOColor.BLACK)); // FIXME I should point to the original GameObject
        			}
        		    return true;
        		}
        	}
        }
    	tmp++;
        return false; // could not find a bonus that works
    }
  
    private static List<Integer> roll(int amountToRoll) {
        List<Integer> rolls = new ArrayList<Integer>();
        for (int x = 0; x < amountToRoll; x++) {
            rolls.add(Math.abs(random.nextInt() % 6) + 1);
 
        }
        return rolls;
    }

}
