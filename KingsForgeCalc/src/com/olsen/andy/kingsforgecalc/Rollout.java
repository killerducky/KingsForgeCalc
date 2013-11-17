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
    
    // some state variables for the recursive routines
    // maybe refactor into a separate class but for now just put here
    Integer         rolled;
    Integer         needed;
    List<GameBonus> bonusList;
	Integer         lowestCost;
	List<GameBonus> currentUsedGbList;
	List<Integer>   picked;
	List<GameBonus> pickedList;
	
	private static final int MAX_BONUSES_PER_DIE = 6; // it takes 6 +1 bonuses to make a 1 into a 7 and break a tie with a 6

    
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
				log_enable = x==0; // only log the first run
				if (log_enable) { log += "Bonuses:"; } 
				for (GameBonus bonus : bonusList) { 
					if (log_enable) { log += " " + bonus.toString(); }
					bonus.resetAssignmentsAndReroll();
				}
				boolean success = true;
				for (GameObject.GOColor color : GameObject.GOColor.values()) {
					List<Integer> rolls = roll(supplyHashInt.get(color));
					if (log_enable) { log += rollsToString(color, rolls); }
					for (GameBonus bonus : bonusList) { bonus.apply1to6(rolls); }
					Collections.sort(rolls);
					Collections.reverse(rolls);
					if (log_enable) { log += "\nAfter 1->6:" + rollsToString(color, rolls) + "\n"; }
					if (!checkSuccess(rolls, neededHashList.get(color), bonusList)) {
						success = false;
						break;
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
				result += String.format("\nChance to win: %2.2f%%", (1.0 * successes / totalRolls) * 100);
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
            	// prepare recursion state
            	this.rolled = thisRolled;
            	this.needed = need;
            	this.bonusList = bonusList;
            	if (!applyCheapestBonus()) {
            		return false;
            	}
            }
        }
        return true;
	}

	private boolean applyCheapestBonus() {
		boolean success;
		this.lowestCost = Integer.MAX_VALUE;
		this.currentUsedGbList = null;
		for (int targetDepth=1; targetDepth <= bonusList.size(); targetDepth++) {
			if (targetDepth>MAX_BONUSES_PER_DIE) {
				break;  // FIXME while debugging only this deep
			}
			this.picked = new ArrayList<Integer>();
			success = recursion(targetDepth);
			if (success) { 
				// don't bother going trying with more bonuses if we found something with this many
				// technically it may be better to use more bonuses, but for now good enough
				break;  
			}
		}
		if (this.currentUsedGbList != null) {
			if (log_enable) { 
				log += "use:";
				for (GameBonus gb : this.currentUsedGbList ) {
					log += " " + gb.toString();
				}
			}
		} else {
			if (log_enable) { log += "failed to find working bonus"; }
		}
		return (this.currentUsedGbList != null);  // if we succeeded this will have a value
	}
	
	private boolean recursion(Integer targetDepth) {
		if (picked.size() != targetDepth) {
			int start = 0; // by default start from beginning
			if (picked.size() > 0) {
				// but if we have already picked some bonuses, start picking the next one
				start = picked.get(picked.size()-1)+1;
			}
			for (int currTry=start; currTry < bonusList.size(); currTry++) {
				picked.add(currTry);
				recursion(targetDepth);
				picked.remove(picked.size()-1);
			}
		} else {
			// now that we have reached the targetDepth number of bonuses, and selected indexes for them
			// build the list of GameBonuses they point to, and test the result
			this.pickedList = new ArrayList<GameBonus>();
			if (log_enable) { log += "td=" + targetDepth; }
			for (int i : picked) {
				this.pickedList.add(bonusList.get(i));
				if (log_enable) { log += " " + i; }
			}
			if (log_enable) { log += "\n"; }
			doInnerLoop();
		}
		// when returning to previous recursion level, undo the pick we made
		// if we were successful in finding at least one solution, currentUsedGbList will be set to it
		return currentUsedGbList != null;
	}
	
	private void doInnerLoop() {
		int afterBonus = rolled;
		int totalCost  = 0;
		// loop over all these bonuses and apply them, keeping running total afterBonus and cost
		for (GameBonus gb : pickedList) {
			if (!gb.allUsed()) {
				afterBonus = gb.applyBonus(afterBonus);
				totalCost += gb.cost();
			}
		}
		if (afterBonus >= needed) {
			if (totalCost < lowestCost) {
				if (currentUsedGbList != null) {
					// we found a cheaper bonus combination, so reset the old one to be unused
					for (GameBonus gb : currentUsedGbList) { gb.resetAssignmentsDoNotReroll(); }
				}
				currentUsedGbList = pickedList;
				for (GameBonus gb : currentUsedGbList) {
					gb.addTarget(new GameObject(rolled, GameObject.GOColor.BLACK)); // FIXME I should point to the original GameObject
				}
			}
		}
	}

	private List<Integer> roll(int amountToRoll) {
        List<Integer> rolls = new ArrayList<Integer>();
        for (int x = 0; x < amountToRoll; x++) {
//            rolls.add(Math.abs(random.nextInt() % 6) + 1);
            rolls.add(1); // FIXME !!! test
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
