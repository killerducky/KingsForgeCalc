package com.olsen.andy.kingsforgecalc;

import java.util.Random;

import android.content.SharedPreferences;

public class GameState {
    public static final int NUM_ROLLS = 1000;
    public Random random = new Random();
    public SharedPreferences sharedPref;
    public Rollout rollout;
    public MainActivity mainActivity;
    
    private static GameState instance;

    
    public static GameState getInstance() {
        if (instance==null) {
            instance = new GameState();
        }
        return instance;
    }
}
