package com.olsen.andy.kingsforgecalc;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class GameBonus {
    private Bonus name;
    private Integer white_die_value;
	private static Random random = new Random(new Date().getTime());  // TODO should this be like some super global thing?
	
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
    		rollWhiteDie();
    	}
    }
    
    public GameBonus(GameBonus o) {
    	this.name = o.name;
    	if (name == Bonus.WD) {
    		white_die_value = o.white_die_value;  // TODO should I reroll on object copies?  Probably no...
    	}
    }

    public Bonus getBonusType() { return name; }

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
    
    public void resetAssignmentsAndReroll() {
    	if (name == Bonus.WD) {
    	    rollWhiteDie();
    	}
    }
    
    public void rollWhiteDie() {
        white_die_value = Math.abs(random.nextInt() % 6) + 1;
    }

    public void setWhiteDie(Integer value) {
        white_die_value = value;
    }
    
    public Integer getWhiteDieValue() {
        return white_die_value;
    }
    
    public Integer applyBonus(Integer value) {
    	switch(name) {
    	case A1TO6: return (value==1) ? 6 : value;
    	case P1X3 : return value + 1;
    	case P1   : return value + 1;
    	case P2   : return value + 2;
    	case A6   : return 6;
    	case WD   : return white_die_value;
    	case RR   : return value; // cannot simply reroll one by one
    	default: return value;
    	}
    }
    
    public void apply1to6(List<GameObject> rolls) {
    	if (name != Bonus.A1TO6) { return; }
        for (int i = 0; i < rolls.size(); i++) {
        	if (rolls.get(i).getCurrValue() == 1) {
        		rolls.get(i).applyBonus(this);
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

    public Integer getNumDups() { return (name == Bonus.P1X3) ? 3 : 1; }
}

