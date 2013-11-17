package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GameBonus {
    private String name;
    private List<GameObject> targetList = new ArrayList<GameObject>();
    private Integer white_die_value;
	Random random = new Random(new Date().getTime());  // TODO should this be like some super global thing?

    public GameBonus(String name) {
    	this.name = name;
    	if ("white die".equals(name)) {
    		white_die_value = Math.abs(random.nextInt() % 6) + 1;
    	}
    }

    public String toString() {
    	String str = this.name;
    	if ("white die".equals(name)) {
    		str += "=" + white_die_value;
    	}
    	return str;
    }
    
    public boolean allUsed() {
    	if ("1->6".equals(name)) {
    		return false;  // no limit
    	}
      	if ("+1 (3)".equals(name)) {
    		return (targetList.size() == 3);
    	} else {
    		return (targetList.size() == 1);
    	}
    }
    
    public void addTarget(GameObject go) {
    	targetList.add(go);
    }
    
    public void resetAssignmentsAndReroll() {
        resetAssignmentsDoNotReroll();
    	if ("white die".equals(name)) {
    		white_die_value = Math.abs(random.nextInt() % 6) + 1;
    	}
    }
    
    public void resetAssignmentsDoNotReroll() {
    	targetList.clear();
    }
    
    public Integer applyBonus(Integer value) {
    	if (     "1->6".equals(name)) { return (value==1) ? 6 : value; }
        if (   "+1 (3)".equals(name)) { return value + 1; }
        if (       "+1".equals(name)) { return value + 1; }
    	if (       "+2".equals(name)) { return value + 2; }
    	if (    "auto6".equals(name)) { return 6; }
    	if ("white die".equals(name)) { return white_die_value; }
    	if (   "reroll".equals(name)) { return value; }  // cannot simply reroll one by one
    	return value;
    }
    
    public void apply1to6(List<Integer> rolls) {
    	if (!"1->6".equals(name)) { return; }
//    	for (Integer roll : rolls) {
//    		if (roll == 1) { roll = 6; } // FIXME iterating over integers does not change them.  :(
//    	}
        for (int i = 0; i < rolls.size(); i++) {
        	if (rolls.get(i) == 1) {
        		rolls.set(i, 6);
        	}
        }
    }
    
    public Integer cost() {
    	if (     "1->6".equals(name)) { return 1; } // this is applied first, so we never use this
        if (   "+1 (3)".equals(name)) { return 2; }
        if (       "+1".equals(name)) { return 3; }
    	if (       "+2".equals(name)) { return 4; }
    	if (    "auto6".equals(name)) { return 5; }
    	if ("white die".equals(name)) { return 6; }
    	if (   "reroll".equals(name)) { return 7; } // this is applied in a separate step so we never use this
    	//throw new Exception("impossible");
    	return 8;
    }
}

