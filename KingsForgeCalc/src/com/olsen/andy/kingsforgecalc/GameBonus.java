package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GameBonus {
    private Bonus name;
    private List<GameObject> targetList = new ArrayList<GameObject>();
    private Integer white_die_value;
	Random random = new Random(new Date().getTime());  // TODO should this be like some super global thing?
	
	public enum Bonus { 
		A1TO6,
		P1X3,
		P1,
		P2,
		A6,
		WD,
		RR
	};

    public GameBonus(Bonus name) {
    	this.name = name;
    	if (name == Bonus.WD) {
    		white_die_value = Math.abs(random.nextInt() % 6) + 1;
    	}
    }
    
    public GameBonus(GameBonus o) {
    	this.name = o.name;
    	if (name == Bonus.WD) {
    		white_die_value = o.white_die_value;  // TODO should I reroll on object copies?  Probably no...
    	}
    }

    public String toString() {
    	String str = "";
    	switch (name) {
    	case A1TO6: str = "1->6"  ; break;
    	case P1X3 : str = "+1 (3)"; break;
    	case P1   : str = "+1"    ; break; 
    	case P2   : str = "+2"    ; break;
    	case A6   : str = "Auto 6"; break;
    	case WD   : str = "White Die"; break;
    	case RR   : str = "Reroll"   ; break;
    	}
    	if (name == Bonus.WD) {
    		str += "=" + white_die_value;
    	}
    	return str;
    }
    
    public boolean allUsed() {
    	if (name == Bonus.A1TO6) {
    		return false;  // no limit
    	}
      	if (name == Bonus.P1X3) {
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
    	if (name == Bonus.WD) {
    		white_die_value = Math.abs(random.nextInt() % 6) + 1;
    	}
    }
    
    public void resetAssignmentsDoNotReroll() {
    	targetList.clear();
    }
    
    public Integer applyBonus(Integer value) {
    	if (name == Bonus.A1TO6) { return (value==1) ? 6 : value; }
        if (name == Bonus.P1X3 ) { return value + 1; }
        if (name == Bonus.P1   ) { return value + 1; }
    	if (name == Bonus.P2   ) { return value + 2; }
    	if (name == Bonus.A6   ) { return 6; }
    	if (name == Bonus.WD   ) { return white_die_value; }
    	if (name == Bonus.RR   ) { return value; }  // cannot simply reroll one by one
    	return value;
    }
    
    public void apply1to6(List<Integer> rolls) {
    	if (name != Bonus.A1TO6) { return; }
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
    	if (name == Bonus.A1TO6) { return 1; } // this is applied first, so we never use this
        if (name == Bonus.P1X3 ) { return 2; }
        if (name == Bonus.P1   ) { return 3; } 
    	if (name == Bonus.P2   ) { return 4; }
    	if (name == Bonus.A6   ) { return 5; }
    	if (name == Bonus.WD   ) { return 6; }
    	if (name == Bonus.RR   ) { return 7; } // TODO this is applied in a separate step so we never use this
    	//throw new Exception("impossible");
    	return 8;
    }
}

