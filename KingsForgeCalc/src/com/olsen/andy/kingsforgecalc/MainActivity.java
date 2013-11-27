package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity {
    private GameState gameState;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameState = GameState.getInstance();
        gameState.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);  // TODO: Where should I put this?
        gameState.mainActivity = this;
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        LinearLayout new_grid;
        DiceSpinner spinnerOrig;
        new_grid = (LinearLayout) findViewById(R.id.new_craftcard_grid);
        spinnerOrig = (DiceSpinner) findViewById(R.id.spinner_test);
        spinnerOrig.buildSpinner(false, 1, 6);
        spinnerOrig.setSelection(6-1); // default since black 6 is needed
        for (int i=0; i<6; i++) {
            DiceSpinner spinnerClone = new DiceSpinner(this);
            spinnerClone.setLayoutParams(spinnerOrig.getLayoutParams());
            spinnerClone.buildSpinner(false, 1, 6);
            setSpinnerDeleted(spinnerClone);
            new_grid.addView(spinnerClone);
        }

        new_grid = (LinearLayout) findViewById(R.id.new_supply_grid);
        spinnerOrig = (DiceSpinner) findViewById(R.id.black_supply);
        spinnerOrig.buildSpinner(true, 0, 25);
        spinnerOrig.setSelection(5); // default black to 5
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            if (color == GameObject.GOColor.BLACK) { continue; }
            DiceSpinner spinnerClone = new DiceSpinner(this);
            spinnerClone.setLayoutParams(spinnerOrig.getLayoutParams());
            spinnerClone.buildSpinner(true, 0, 25);
            spinnerClone.setColor(color);
            new_grid.addView(spinnerClone);
        }
        
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_WD);
        spinnerOrig.buildSpinner(true, 0, 2);
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_A6);
        spinnerOrig.buildSpinner(true, 0, 1);
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_RR);
        spinnerOrig.buildSpinner(true, 0, 1);
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_A1TO6);
        spinnerOrig.buildSpinner(true, 0, 1);
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_P1);
        spinnerOrig.buildSpinner(true, 0, 5);
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_P1X3);
        spinnerOrig.buildSpinner(true, 0, 5);
        spinnerOrig = (DiceSpinner) findViewById(R.id.bonus_P2);
        spinnerOrig.buildSpinner(true, 0, 5);
    }

    private void setSpinnerDeleted(Spinner spinner) {
        spinner.setSelection(6+4+1-1);   // FIXME super hack to set default to deleted
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the menu; this adds items to the action bar if it is present.
    	getMenuInflater().inflate(R.menu.main, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_settings:
    		startActivity(new Intent (this, Settings.class));
    		return true;
    	}
    	return true;
    }
    
    public void doPickCraftCard(View view) {
        //Toast.makeText(getApplicationContext(), "Pick craft card not implemented", Toast.LENGTH_LONG).show();
    }

    public void doRollout1(View view) {
        playRolloutSound(R.raw.dice_roll2);
        if (gameState.sharedPref.getBoolean("pref_debug_all_1s", false)) {
            gameState.rollout = new RollAll1s();
            doRollout(1);
        } else {
            gameState.rollout = new Rollout(
                    gameState.sharedPref.getBoolean("pref_debug_all_1s", false),
                    gameState.sharedPref.getBoolean("pref_debug_log_enable", false));
            doRollout(1);
        }
    }

    public void doRollout(View view) {
        if (gameState.sharedPref.getBoolean("pref_debug_all_1s", false)) {
            gameState.rollout = new RollAll1s();
            doRollout(gameState.NUM_ROLLS);
        } else {
            gameState.rollout = new Rollout(
                    gameState.sharedPref.getBoolean("pref_debug_all_1s", false),
                    gameState.sharedPref.getBoolean("pref_debug_log_enable", false));
            doRollout(gameState.NUM_ROLLS);
        }
    }

    OnAudioFocusChangeListener afChangeListner = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            // TODO Auto-generated method stub
        }
    };
    
    public void playRolloutSound(int resid) {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = am.requestAudioFocus(afChangeListner,               
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        
        if (result == AudioManager.AUDIOFOCUS_GAIN) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, resid);
            mediaPlayer.start();
        }
    }

    public void doRollout(Integer totalRolls) {
        HashMap<GameObject.GOColor, Integer> supplyHashInt = new HashMap<GameObject.GOColor, Integer>();
        DiceHashList neededHashList = new DiceHashList();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            neededHashList.put(color, new ArrayList<GameObject>());
        }
        List<GameBonus> bonusList = new ArrayList<GameBonus>();

        // get craft requirements out of the widgets
    	LinearLayout new_grid;
    	new_grid = (LinearLayout) findViewById(R.id.new_craftcard_grid);
    	for (int i=0; i<new_grid.getChildCount(); i++) {
    	    Spinner spinner = (Spinner) new_grid.getChildAt(i);
    	    if (spinner.getSelectedItem() instanceof GameObject) {
    	        GameObject go = (GameObject) spinner.getSelectedItem();
    	        neededHashList.get(go.getColor()).add(go);
    	    }
    	}

        new_grid = (LinearLayout) findViewById(R.id.new_supply_grid);
        for (int i=0; i < new_grid.getChildCount(); i++) {
            Spinner spinner = (Spinner) new_grid.getChildAt(i);
            GameObject go = (GameObject) spinner.getSelectedItem();
            supplyHashInt.put(go.getColor(), go.getOrigValue());
        }

        for (GameBonus.Bonus bonusType : GameBonus.Bonus.values()) {
            Spinner spinner = null;
            switch (bonusType) {
            case WD : spinner = (Spinner) findViewById(R.id.bonus_WD); break;
            case A1TO6 : spinner = (Spinner) findViewById(R.id.bonus_A1TO6); break;
            case P1X3 : spinner = (Spinner) findViewById(R.id.bonus_P1X3); break;
            case P1 : spinner = (Spinner) findViewById(R.id.bonus_P1); break;
            case P2 : spinner = (Spinner) findViewById(R.id.bonus_P2); break;
            case A6 : spinner = (Spinner) findViewById(R.id.bonus_A6); break;
            case RR : spinner = (Spinner) findViewById(R.id.bonus_RR); break;
            }
            GameObject go = (GameObject) spinner.getSelectedItem();
            for (int i=0; i < go.getCurrValue(); i++) {
                bonusList.add(new GameBonus(bonusType));
            }
        }

    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		Collections.sort(neededHashList.get(color));
    		Collections.reverse(neededHashList.get(color));
    	}
        
    	HashMap<String, String> result = gameState.rollout.doRollout(neededHashList, supplyHashInt, bonusList, totalRolls);
    	if (result.containsKey("RerollDialog")) {
            Intent intent = new Intent(gameState.mainActivity, RerollDialog.class);
            gameState.mainActivity.startActivity(intent);
    	} else {
    	    showRolloutResults(result);
    	}
    }
    
    public void showRolloutResults(HashMap<String, String> result) {
        TextView rolloutResults = (TextView) findViewById(R.id.rollout_results);
        rolloutResults.setText(result.get("result"));

        if (gameState.sharedPref.getBoolean("pref_debug_log_enable",  false)) {
            AlertDialog.Builder resultbox = new AlertDialog.Builder(this);
            resultbox.setMessage(
                    "Final Results:\n" + result.get("result") + 
                    "\n\nNormal Log:\n" + result.get("normalLog") + 
                    "\n\nDebug Info:\n" + result.get("debugLog")
                    );
            resultbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {}
            });
            resultbox.show();
        } else {
            AlertDialog.Builder resultbox = new AlertDialog.Builder(this);
            resultbox.setMessage(
                    "Final Results:\n" + result.get("result") + 
                    "\n\n" + result.get("normalLog"));
            resultbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {}
            });
            resultbox.show();
        }
    }

    // for a time test, require 3 black 6s, and have 4 "+1 (3)" bonuses = (5/6)^3 = 57.87
    // 1000 rolls
    //             Time=0.54s,  Odds=59.04%  16bd584526a9337112c93092ac681d3260d97640
    //             Time=0.30s
    //             Time=0.88s,  Odds=57.34%
    // 2013/11/24  Time=0.92s,  Odds=56.88%  2d4055049fd91cec25a0f9185e6aa61c70bdd44f  
    //
    // TODO: more tests:
    // Black 4333, supply 4 black, P1X3:3, roll 1s.  Should pass 100%
    // Black 632 , supply 3 black, P2, P1, P1, roll 521.  -- you must P2 on the 1
    // Black 652 , supply 3 black, P2, P1, P1, roll 541.  -- you must P2 on the 4

    class RollAll1s extends Rollout {
        public RollAll1s() {
            super(
                    gameState.sharedPref.getBoolean("pref_debug_all_1s", false),
                    gameState.sharedPref.getBoolean("pref_debug_log_enable", false));
        }

        @Override
        protected List<GameObject> roll(GameObject.GOColor color, int amountToRoll) {
            List<GameObject> rolls = new ArrayList<GameObject>();
            for (int x = 0; x < amountToRoll; x++) {
                rolls.add(new GameObject(1, color));
            }
            return rolls;
        }
    }
    
//    DiceHashList neededHashList = new DiceHashList();

//    class TestPerformance implements SetupTest {
//        public void setupTest() {
//            LinearLayout new_grid;
//            new_grid = (LinearLayout) findViewById(R.id.new_craftcard_grid);
//            for (int i=0; i<new_grid.getChildCount(); i++) {
//                Spinner spinner = (Spinner) new_grid.getChildAt(i);
//                setSpinnerDeleted(spinner);
//            }
//            for (int i=0; i<3; i++) {
//                Spinner spinner = (Spinner) new_grid.getChildAt(i);
//                spinner.setSelection(6-1);
//                // FIXME still need to set it to black...
//            }
//
//            new_grid = (LinearLayout) findViewById(R.id.new_supply_grid);
//            Spinner spinner;
//            spinner = (Spinner) new_grid.getChildAt(0);
//            spinner.setSelection(3);
//            spinner = (Spinner) new_grid.getChildAt(1);
//            spinner.setSelection(0);
//            spinner = (Spinner) new_grid.getChildAt(2);
//            spinner.setSelection(0);
//            spinner = (Spinner) new_grid.getChildAt(3);
//            spinner.setSelection(0);
//            // FIXME: Add back in new bonus method
////            supply_adapter.setSelectedPos(null);
////            supply_die.clear();
////            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
////            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
////            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
////            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
//            testRollout = new Rollout(this.parent);
//        }
//    }
    
//    class TestP2Pass implements SetupTest {
//        public void setupTest() {
//            ccd_adapter.setSelectedPos(null);
//            craftcard_die.clear();
//            craftcard_die.add(new GameObject(2, GameObject.GOColor.BLACK));
//            ccd_adapter.notifyDataSetChanged();
//            supply_adapter.setSelectedPos(null);
//            supply_die.clear();
//            supply_die.add(new GameObject(1, GameObject.GOColor.BLACK, 0, 50));   
//            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
//            supply_die.add(new GameBonus(GameBonus.Bonus.P1));
//            testRollout = new RollAll1s(sharedPref);
//        }
//    }
//    
//    class TestP2Fail implements SetupTest {
//        public void setupTest() {
//            ccd_adapter.setSelectedPos(null);
//            craftcard_die.clear();
//            craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK));
//            craftcard_die.add(new GameObject(1, GameObject.GOColor.BLACK));
//            ccd_adapter.notifyDataSetChanged();
//            supply_adapter.setSelectedPos(null);
//            supply_die.clear();
//            supply_die.add(new GameObject(2, GameObject.GOColor.BLACK, 0, 50));   
//            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
//            supply_die.add(new GameBonus(GameBonus.Bonus.P2));
//            testRollout = new RollAll1s(sharedPref);
//        }
//    }
//    
//    class TestP2P1A implements SetupTest {
//        public void setupTest() {
//            // Black 632 , supply 3 black, P2, P1, roll 521.  -- you must P2 on the 1
//            supply_adapter.notifyDataSetChanged();
//            ccd_adapter.setSelectedPos(null);
//            craftcard_die.clear();
//            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
//            craftcard_die.add(new GameObject(3, GameObject.GOColor.BLACK));
//            craftcard_die.add(new GameObject(2, GameObject.GOColor.BLACK));
//            ccd_adapter.notifyDataSetChanged();
//            supply_adapter.setSelectedPos(null);
//            supply_die.clear();
//            supply_die.add(new GameObject(3, GameObject.GOColor.BLACK, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
//            supply_die.add(new GameBonus(GameBonus.Bonus.P2));
//            supply_die.add(new GameBonus(GameBonus.Bonus.P1));
//            testRollout = new CustomRollout(
//                    sharedPref, 
//                    diceHashListBuilder(Arrays.asList(5,2,1), new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<Integer>())
//                    );
//        }
//    }
//    
//    class TestP2P1B implements SetupTest {
//        public void setupTest() {
//            // Black 652 , supply 3 black, P2, P1, roll 541.  -- you must P2 on the 4
//            supply_adapter.notifyDataSetChanged();
//            ccd_adapter.setSelectedPos(null);
//            craftcard_die.clear();
//            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
//            craftcard_die.add(new GameObject(5, GameObject.GOColor.BLACK));
//            craftcard_die.add(new GameObject(2, GameObject.GOColor.BLACK));
//            ccd_adapter.notifyDataSetChanged();
//            supply_adapter.setSelectedPos(null);
//            supply_die.clear();
//            supply_die.add(new GameObject(3, GameObject.GOColor.BLACK, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
//            supply_die.add(new GameBonus(GameBonus.Bonus.P2));
//            supply_die.add(new GameBonus(GameBonus.Bonus.P1));
//            testRollout = new CustomRollout(
//                    sharedPref, 
//                    diceHashListBuilder(Arrays.asList(5,4,1), new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<Integer>())
//                    );
//        }
//    }
//    
//    class TestWD implements SetupTest {
//        public void setupTest() {
//            supply_adapter.notifyDataSetChanged();
//            ccd_adapter.setSelectedPos(null);
//            craftcard_die.clear();
//            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
//            craftcard_die.add(new GameObject(6, GameObject.GOColor.GREEN));
//            ccd_adapter.notifyDataSetChanged();
//            supply_adapter.setSelectedPos(null);
//            supply_die.clear();
//            supply_die.add(new GameObject(0, GameObject.GOColor.BLACK, 0, 50));
//            supply_die.add(new GameObject(1, GameObject.GOColor.GREEN, 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
//            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
//            supply_die.add(new GameBonus(GameBonus.Bonus.WD));
//            supply_die.add(new GameBonus(GameBonus.Bonus.A6));
//            supply_die.add(new GameBonus(GameBonus.Bonus.A6));
//            testRollout = new RollAll1s(sharedPref);
//        }
//    }
    
//    List<SetupTest> setupTests = new ArrayList<SetupTest>();
//    Iterator<SetupTest> testIter;
//    Rollout testRollout;
//    private void pickTest() {
//        if (testRollout == null) {
////            setupTests.add(new TestWD());
////            setupTests.add(new TestP2P1A());
////            setupTests.add(new TestP2P1B());
////            setupTests.add(new TestPerformance());
////            setupTests.add(new TestP2Pass());
////            setupTests.add(new TestP2Fail());
//            testIter = setupTests.iterator();
//        }
//        // if at end, restart
//        if (!testIter.hasNext()) { testIter = setupTests.iterator(); }
//        testIter.next().setupTest();
//    }
//    
//    // FIXME add back way to select this
//    private void runTest() {
//        if (testRollout == null) {
//            pickTest();
//        }
//        gameState.rollout = testRollout;
//        doRollout(1);
//    }
    
//    private DiceHashList diceHashListBuilder(
//            List<Integer> black, List<Integer> green, List<Integer> red, List<Integer> blue) {
//        DiceHashList diceHashList = new DiceHashList();
//        for (GameObject.GOColor color : GameObject.GOColor.values()) {
//            diceHashList.put(color, new ArrayList<GameObject>());
//        }
//        for (int i : black) {
//            diceHashList.get(GameObject.GOColor.BLACK).add(new GameObject(i, GameObject.GOColor.BLACK));
//        }
//        for (int i : green) {
//            diceHashList.get(GameObject.GOColor.GREEN).add(new GameObject(i, GameObject.GOColor.GREEN));
//        }
//        for (int i : red) {
//            diceHashList.get(GameObject.GOColor.RED).add(new GameObject(i, GameObject.GOColor.RED));
//        }
//        for (int i : blue) {
//            diceHashList.get(GameObject.GOColor.BLUE).add(new GameObject(i, GameObject.GOColor.BLUE));
//        }
//        return diceHashList;
//    }
            
}
