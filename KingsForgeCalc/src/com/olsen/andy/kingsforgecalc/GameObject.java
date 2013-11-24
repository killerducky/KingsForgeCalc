package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.List;

// Currently this object is used for 3(!) purposes:
// Craft Card aka needed GUI objects
// Supply objects, a number between 0 and 50 representing how many dice of that color we have
// Rolled Dice objects, representing a dice rolled, and what bonuses have been applied
public class GameObject implements Comparable<GameObject> {
	public enum GOColor { BLACK, GREEN, RED, BLUE };
	
    private Integer origValue;
    private Integer value;
    private GOColor color;
    private Integer min;    // TODO it's dumb to have a bunch of copies of these values...
    private Integer max;
    private List<GameBonus> gbList = new ArrayList<GameBonus>();
    
    public int compareTo(GameObject go) {
        return this.value - go.getCurrValue();
    }
    
    public GameObject(Integer value, GOColor color) {
    	this(value, color, 1, 6);
    }
    
	public GameObject(Integer value, GOColor color, int min, int max) {
    	setMin(min);
    	setMax(max);
    	setOrigValue(value);
    	setColor(color);
    }
	
	public String verboseToString() {
	    String str = "";
	    str += "" + color + ":" + origValue;
	    Integer tmpValue = origValue;
	    for (GameBonus gb : gbList) {
	        tmpValue = gb.applyBonus(tmpValue);
	        str += " " + gb.getBonusType() + "=" + tmpValue;
	    }
	    return str;
	}
	
	public void applyBonus(GameBonus gb) {
	    gbList.add(gb);
	    value = gb.applyBonus(value);
	}

	public void removeBonusIfMatch(GameBonus gb) {
	    if (gbList.size() > 0 && gbList.get(gbList.size()-1) == gb) {
	        removeBonus(gb);
	    }
	}
	public void removeBonus(GameBonus gb) {
	    gbList.remove(gbList.size()-1);
	    value = origValue;
	    for (GameBonus tmpGb : gbList) {
	        value = tmpGb.applyBonus(value);
	    }
	}
	
    public String toString() {
    	return value.toString();
    }
    
    public boolean duplicateCheck(GameBonus gb) {
        return (gbList.contains(gb));
    }
    
    public void setOrigValue(Integer value) { 
    	if (value < min) { value = min; }
    	if (value > max) { value = max; }
        this.origValue = value;
        this.value     = value;
    }
    public void setColor(GOColor color) { this.color = color; }
    public void setMin(Integer min) { this.min = min; }
    public void setMax(Integer max) { this.max = max; }

    public Integer getOrigValue() { return origValue; }
    public Integer getCurrValue() { return value; }
    public GOColor getColor() { return color; }
}
