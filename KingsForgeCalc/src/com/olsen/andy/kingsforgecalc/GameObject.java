package com.olsen.andy.kingsforgecalc;

public class GameObject {
	public enum GOColor { BLACK, GREEN, RED, BLUE, WHITE };
	
    private Integer value;
    private GOColor color;
    
    public GameObject(Integer value, GOColor color) {
    	this.value = value;
    	this.color = color;
    }
    
    public String toString() {
    	return String.format("%d", value);
    }
    
    public Integer getValue() { return value; }
    public GOColor getColor() { return color; }
}
