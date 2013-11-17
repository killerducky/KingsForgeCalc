package com.olsen.andy.kingsforgecalc;

public class GameObject {
	public enum GOColor { BLACK, GREEN, RED, BLUE };
	
    private Integer value;
    private GOColor color;
    private Integer min;    // TODO it's dumb to have a bunch of copies of these values...
    private Integer max;
    
    public GameObject(Integer value, GOColor color) {
    	this(value, color, 1, 6);
    }
    
	public GameObject(Integer value, GOColor color, int min, int max) {
    	setMin(min);
    	setMax(max);
    	setValue(value);
    	setColor(color);
    }
    
    public String toString() {
    	return value.toString();
    }
    
    public void setValue(Integer value) { 
    	if (value < min) { value = min; }
    	if (value > max) { value = max; }
    	this.value = value;
    }
    public void setColor(GOColor color) { this.color = color; }
    public void setMin(Integer min) { this.min = min; }
    public void setMax(Integer max) { this.max = max; }

    public Integer getValue() { return value; }
    public GOColor getColor() { return color; }
}
