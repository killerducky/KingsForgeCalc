package com.olsen.andy.kingsforgecalc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;


public class RolloutTests {

    private class CustomRollout extends Rollout {
        private DiceHashList rolledHashList;
        private Integer white_die_value;
        public CustomRollout(DiceHashList rolledHashList) {
            super(false, false);
            this.rolledHashList = rolledHashList;
        }

        @Override
        protected List<GameObject> roll(GameObject.GOColor color, int amountToRoll) {
            List<GameObject> rolls = new ArrayList<GameObject>(rolledHashList.get(color));
            // pad with extra rolls if necessary
            // (having too many rolls is weird but won't hurt)
            for (int x = rolls.size(); x < amountToRoll; x++) {
                rolls.add(new GameObject(1, color));
            }
            return rolls;
        }
        public void setCustomWhiteDieValue(int value) {
            white_die_value = value;
        }
        @Override
        void rollWhiteDie() {
            if (white_die_value != null) {
                for (GameBonus bonus : bonusHashList.get(GameBonus.Bonus.WD)) {
                    bonus.setWhiteDie(white_die_value);
                }
            } else {
                super.rollWhiteDie();
            }
        }
        //if (debugLogEnable) { debugLog.append("\nBonuses=" + bonusHashList); }
        //if (normalLogEnable) { normalLog.append("\nBonuses=" + bonusHashList); }
    }


    @Test
    public void testP2P1A() {
        // Black 632 , supply 3 black, P2, P1, roll 521.  -- you must P2 on the 1
        DiceHashList customRolledList = new DiceHashList.Builder().color(GameObject.GOColor.BLACK, Arrays.asList(5,2,1)).build();
        Rollout rollout = new CustomRollout(customRolledList);
        DiceHashList neededHashList = new DiceHashList.Builder().color(GameObject.GOColor.BLACK, Arrays.asList(6,3,2)).build();
        HashMap<GameObject.GOColor, Integer> supplyHashInt = new HashMap<GameObject.GOColor, Integer>();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            supplyHashInt.put(color, 3);
        }
        List<GameBonus> bonusList = new ArrayList<GameBonus>();
        bonusList.add(new GameBonus(GameBonus.Bonus.P2));
        bonusList.add(new GameBonus(GameBonus.Bonus.P1));
        HashMap<String, String> resultHash = rollout.doRollout(neededHashList, supplyHashInt, bonusList, 1);
        assertTrue (resultHash.get("result").equals("Wins: 100.00% (1/1)"));   
    }

    @Test
    public void testWD1() {
        // Bk2, Gr2, supply 1Bk 0Gr, WD, P1, roll Bk1, WD=3 
        DiceHashList customRolledList = new DiceHashList.Builder().color(GameObject.GOColor.BLACK, Arrays.asList(2)).build();
        CustomRollout rollout = new CustomRollout(customRolledList);
        rollout.setCustomWhiteDieValue(6);
        DiceHashList neededHashList = new DiceHashList.Builder()
        .color(GameObject.GOColor.BLACK, Arrays.asList(1))
        .color(GameObject.GOColor.GREEN, Arrays.asList(6))
        .build();
        HashMap<GameObject.GOColor, Integer> supplyHashInt = new HashMap<GameObject.GOColor, Integer>();
        supplyHashInt.put(GameObject.GOColor.BLACK, 1);
        supplyHashInt.put(GameObject.GOColor.GREEN, 0);
        supplyHashInt.put(GameObject.GOColor.RED, 0);
        supplyHashInt.put(GameObject.GOColor.BLUE, 0);
        List<GameBonus> bonusList = new ArrayList<GameBonus>();
        bonusList.add(new GameBonus(GameBonus.Bonus.WD));
        bonusList.add(new GameBonus(GameBonus.Bonus.P1));
        HashMap<String, String> resultHash = rollout.doRollout(neededHashList, supplyHashInt, bonusList, 1);
        assertTrue (resultHash.get("result").equals("Wins: 100.00% (1/1)"));   
    }

    // TODO: GUI test changing from Axe to Plate Armor
}
