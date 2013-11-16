package com.olsen.andy.kingsforgecalc;

public class GameObject {
	public enum GOColor { BLACK, GREEN, RED, BLUE, WHITE };
	
    private Integer value;
    private GOColor color;
    
    public GameObject(Integer value, GOColor color) {
    	setValue(value);
    	setColor(color);
    }
    
    public String toString() {
    	return String.format("%d", value);
    }
    
    public void setValue(Integer value) { 
    	if (value < 1) { value = 1; }
    	if (value > 6) { value = 6; }
    	this.value = value;
    }
    public void setColor(GOColor color) { this.color = color; }
    public Integer getValue() { return value; }
    public GOColor getColor() { return color; }
}
