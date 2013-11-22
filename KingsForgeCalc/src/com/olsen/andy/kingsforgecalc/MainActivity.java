package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;


public class MainActivity extends Activity {

	Random random = new Random(new Date().getTime());
	
    private List<Object>      craftcard_die;    // list of craft card requirements
    private List<Object>      craftcard_tools;  // list of tools to manipulate craftcard_die
    private CraftDieAdapter   ccd_adapter;
    private CraftToolsAdapter cct_adapter;
    
    private List<Object>      supply_die;      // list of supply die available
    private List<Object>      supply_tools;    // list of tools to manipulate suply_die
    private CraftDieAdapter   supply_adapter;  // for now can just use the same adapter class
    private CraftToolsAdapter supplyT_adapter; //   "
    
    public SharedPreferences sharedPref;

    private static final int NUM_ROLLS = 1000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);  // TODO: Where should I put this?

        setContentView(R.layout.activity_main);

        craftcard_die = new ArrayList<Object>();
    	craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
    	craftcard_die.add(new GameObject(1, GameObject.GOColor.BLACK));
    	craftcard_die.add(new GameObject(1, GameObject.GOColor.GREEN));
    	craftcard_die.add(new GameObject(1, GameObject.GOColor.GREEN));
        GridView ccd_gv = (GridView) findViewById(R.id.craftcard_grid);
        ccd_adapter = new CraftDieAdapter(this, craftcard_die, null);
        ccd_gv.setAdapter(ccd_adapter);
        ccd_gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
            	onCraftcardDieClick(pos);
            }
        });
        
        craftcard_tools = new ArrayList<Object>();
        craftcard_tools.add(new String("+"));
        craftcard_tools.add(new String("-"));
        craftcard_tools.add(new String("X"));
        craftcard_tools.add(new String("Black"));
        craftcard_tools.add(new String("Green"));
        craftcard_tools.add(new String("Red"));
        craftcard_tools.add(new String("Blue"));
        GridView cct_gv = (GridView) findViewById(R.id.craftcard_tools);
        cct_adapter = new CraftToolsAdapter(this, craftcard_tools);
        cct_gv.setAdapter(cct_adapter);
        cct_gv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        		onCraftcardToolsClick(pos);
        	}
        });

        supply_die = new ArrayList<Object>();
    	supply_die.add(new GameObject(2, GameObject.GOColor.BLACK, 0, 50));
    	supply_die.add(new GameObject(3, GameObject.GOColor.GREEN, 0, 50));
    	supply_die.add(new GameObject(3, GameObject.GOColor.RED  , 0, 50));
    	supply_die.add(new GameObject(3, GameObject.GOColor.BLUE , 0, 50));
        GridView supply_gv = (GridView) findViewById(R.id.supply_grid);
        supply_adapter = new CraftDieAdapter(this, supply_die, null); // TODO CraftDie?
        supply_gv.setAdapter(supply_adapter);
        supply_gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
            	onSupplyDieClick(pos);
            }
        });
        
        supply_tools = new ArrayList<Object>();
        supply_tools.add(new String("+"));
        supply_tools.add(new String("-"));
        supply_tools.add(new String("X"));
        for (GameBonus.Bonus bonus : GameBonus.Bonus.values()) {
            supply_tools.add(new GameBonus(bonus));
        }
        supply_tools.add(new String("Pick Test"));
        supply_tools.add(new String("Run Test"));
        GridView supplyT_gv = (GridView) findViewById(R.id.supply_tools);
        supplyT_adapter = new CraftToolsAdapter(this, supply_tools);
        supplyT_gv.setAdapter(supplyT_adapter);
        supplyT_gv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        		onSupplyToolsClick(pos);
        	}
        });
        
    }
    
    private void onCraftcardDieClick(int pos) {
    	if (ccd_adapter.getSelectedPos() != null && ccd_adapter.getSelectedPos() == pos) {
    		ccd_adapter.setSelectedPos(null);  // unselect
    	} else {
		    ccd_adapter.setSelectedPos(pos);
    	}
    }

    private void onCraftcardToolsClick(int pos) {
    	String str = craftcard_tools.get(pos).toString();

    	if (ccd_adapter.getSelectedPos() != null) {
        	GameObject go = (GameObject) craftcard_die.get((int) ccd_adapter.getSelectedPos());
    		if      ("+".equals(str)) { go.setOrigValue(go.getOrigValue() + 1); }
    		else if ("-".equals(str)) { go.setOrigValue(go.getOrigValue() - 1); }
    		else if ("X".equals(str)) { 
    			craftcard_die.remove((int) ccd_adapter.getSelectedPos());  // XXX: omg Integer objects mess things up here, cast to int
    			if (craftcard_die.size()==0) {
    				ccd_adapter.setSelectedPos(null);
    			} else if (ccd_adapter.getSelectedPos() >= craftcard_die.size()) { 
    				ccd_adapter.setSelectedPos(craftcard_die.size()-1);
    			}
    		}
    	}
    	if ("Black".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK)); 
    		ccd_adapter.setSelectedPos(craftcard_die.size()-1);
    	} else if ("Green".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.GREEN)); 
    		ccd_adapter.setSelectedPos(craftcard_die.size()-1);
    	} else if ("Red".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.RED)); 
    		ccd_adapter.setSelectedPos(craftcard_die.size()-1);
    	} else if ("Blue".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.BLUE)); 
    		ccd_adapter.setSelectedPos(craftcard_die.size()-1);
    	}

    	// TODO: currently inconsistent on who is responsible to call this
    	ccd_adapter.notifyDataSetChanged();
    }

    private void onSupplyDieClick(int pos) {
    	if (supply_adapter.getSelectedPos() != null && supply_adapter.getSelectedPos() == pos) {
    		supply_adapter.setSelectedPos(null);  // unselect
    	} else {
    		supply_adapter.setSelectedPos(pos);
    	}
    }

    private void onSupplyToolsClick(int pos) {
    	Object clickedO = supply_tools.get(pos);
    	if (supply_adapter.getSelectedPos() != null) { 
    		Object selectedO = supply_die.get(supply_adapter.getSelectedPos());
    		if (selectedO instanceof GameObject) {
    			GameObject go = (GameObject) selectedO;
    			if      ("+".equals(clickedO.toString())) { go.setOrigValue(go.getOrigValue() + 1); }
    			else if ("-".equals(clickedO.toString())) { go.setOrigValue(go.getOrigValue() - 1); }
    			else if ("X".equals(clickedO.toString())) { go.setOrigValue(0);                 }
    		} else {
                // Handle bonuses, which right now are just strings
                if ("X".equals(clickedO.toString())) {  
        			supply_die.remove((int) supply_adapter.getSelectedPos());  // XXX: omg Integer objects mess things up here, cast to int
        			if (supply_die.size()==0) {
        				supply_adapter.setSelectedPos(null); // XXX: Actually this is impossible, cannot delete the 4 colors
        			} else if (supply_adapter.getSelectedPos() >= supply_die.size()) { 
        				supply_adapter.setSelectedPos(supply_die.size()-1);
        			}
                }
    		}
    	}
    	if (clickedO instanceof GameBonus) {
    		supply_die.add(new GameBonus((GameBonus) clickedO));  // copy and add
    		supply_adapter.setSelectedPos(null);
    	} else {
    		if ("Pick Test".equals(clickedO.toString())) {
    			pickTest();
    		} else if ("Run Test".equals(clickedO.toString())) {
    		    runTest();
    		}
    	
    	}
    	// TODO: currently inconsistent on who is responsible to call this
    	supply_adapter.notifyDataSetChanged();
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
    
    public void doRollout1(View view) {
        if (sharedPref.getBoolean("pref_debug_all_1s", false)) {
            doRollout(1, new RollAll1s(sharedPref));
        } else {
            doRollout(1, new Rollout(sharedPref));
        }
    }

    public void doRollout(View view) {
        if (sharedPref.getBoolean("pref_debug_all_1s", false)) {
            doRollout(NUM_ROLLS, new RollAll1s(sharedPref));
        } else {
            doRollout(NUM_ROLLS, new Rollout(sharedPref));
        }
    }
        
    public void doRollout(Integer totalRolls, Rollout rollout) {
    	HashMap<GameObject.GOColor, Integer> supplyHashInt = new HashMap<GameObject.GOColor, Integer>();
    	DiceHashList neededHashList = new DiceHashList();
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		neededHashList.put(color, new ArrayList<GameObject>());
    	}
    	List<GameBonus> bonusList = new ArrayList<GameBonus>();
    	
    	// get craft requirements out of the widgets
    	for (Object o : craftcard_die) {
    		GameObject go = (GameObject) o;
    		neededHashList.get(go.getColor()).add(go);
    	}

        // TODO: hacky but the GameObjects are the dice
    	// The other string objects are bonuses
    	for (Object o : supply_die) {
    		if (o instanceof GameObject) {
    			GameObject go = (GameObject) o;
    			supplyHashInt.put(go.getColor(), go.getOrigValue());
    		} else {
    			bonusList.add((GameBonus) o);
    		}
    	}
    	
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		Collections.sort(neededHashList.get(color));
    		Collections.reverse(neededHashList.get(color));
    	}
        
    	HashMap<String, String> result = rollout.doRollout(neededHashList, supplyHashInt, bonusList, totalRolls);

    	TextView rolloutResults = (TextView) findViewById(R.id.rollout_results);
    	rolloutResults.setText(result.get("result"));

    	if (sharedPref.getBoolean("pref_debug_log_enable",  false)) {
    		AlertDialog.Builder resultbox = new AlertDialog.Builder(this);
    		resultbox.setMessage("Final Results:\n" + result.get("result") + "\n\nDebug Info:\n" + result.get("log"));
    		resultbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface arg0, int arg1) {}
    		});
        	resultbox.show();
    	}

    }

    // for a time test, require 3 black 6s, and have 4 "+1 (3)" bonuses
    // 1000 rolls
    // 16bd584526a9337112c93092ac681d3260d97640 - Time=0.54s,  Odds=59.04%
    //                                            Time=0.30s
    //                                            Time=0.88s,  Odds=57.34%
    //
    // TODO: more tests:
    // Black 4333, supply 4 black, P1X3:3, roll 1s.  Should pass 100%
    // Black 632 , supply 3 black, P2, P1, P1, roll 521.  -- you must P2 on the 1
    // Black 652 , supply 3 black, P2, P1, P1, roll 541.  -- you must P2 on the 4

    class RollAll1s extends Rollout {
        public RollAll1s(SharedPreferences sharedPref) {
            super(sharedPref);
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
    
    DiceHashList neededHashList = new DiceHashList();

    class CustomRollout extends Rollout {
        private DiceHashList rolledHashList;
        public CustomRollout(SharedPreferences sharedPref, DiceHashList rolledHashList) {
            super(sharedPref);
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
    }

    interface SetupTest {
        public void setupTest();
    }
    
    class TestPerformance implements SetupTest {
        public void setupTest() {
            ccd_adapter.setSelectedPos(null);
            craftcard_die.clear();
            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
            ccd_adapter.notifyDataSetChanged();
            supply_adapter.setSelectedPos(null);
            supply_die.clear();
            supply_die.add(new GameObject(3, GameObject.GOColor.BLACK, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1X3));
            testRollout = new Rollout(sharedPref);
        }
    }
    
    class TestP2Pass implements SetupTest {
        public void setupTest() {
            ccd_adapter.setSelectedPos(null);
            craftcard_die.clear();
            craftcard_die.add(new GameObject(2, GameObject.GOColor.BLACK));
            ccd_adapter.notifyDataSetChanged();
            supply_adapter.setSelectedPos(null);
            supply_die.clear();
            supply_die.add(new GameObject(1, GameObject.GOColor.BLACK, 0, 50));   
            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1));
            testRollout = new RollAll1s(sharedPref);
        }
    }
    
    class TestP2Fail implements SetupTest {
        public void setupTest() {
            ccd_adapter.setSelectedPos(null);
            craftcard_die.clear();
            craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(1, GameObject.GOColor.BLACK));
            ccd_adapter.notifyDataSetChanged();
            supply_adapter.setSelectedPos(null);
            supply_die.clear();
            supply_die.add(new GameObject(2, GameObject.GOColor.BLACK, 0, 50));   
            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
            supply_die.add(new GameBonus(GameBonus.Bonus.P2));
            testRollout = new RollAll1s(sharedPref);
        }
    }
    
    class TestP2P1A implements SetupTest {
        public void setupTest() {
            // Black 632 , supply 3 black, P2, P1, roll 521.  -- you must P2 on the 1
            supply_adapter.notifyDataSetChanged();
            ccd_adapter.setSelectedPos(null);
            craftcard_die.clear();
            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(3, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(2, GameObject.GOColor.BLACK));
            ccd_adapter.notifyDataSetChanged();
            supply_adapter.setSelectedPos(null);
            supply_die.clear();
            supply_die.add(new GameObject(3, GameObject.GOColor.BLACK, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
            supply_die.add(new GameBonus(GameBonus.Bonus.P2));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1));
            testRollout = new CustomRollout(
                    sharedPref, 
                    diceHashListBuilder(Arrays.asList(5,2,1), new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<Integer>())
                    );
        }
    }
    
    class TestP2P1B implements SetupTest {
        public void setupTest() {
            // Black 652 , supply 3 black, P2, P1, roll 541.  -- you must P2 on the 4
            supply_adapter.notifyDataSetChanged();
            ccd_adapter.setSelectedPos(null);
            craftcard_die.clear();
            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(5, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(2, GameObject.GOColor.BLACK));
            ccd_adapter.notifyDataSetChanged();
            supply_adapter.setSelectedPos(null);
            supply_die.clear();
            supply_die.add(new GameObject(3, GameObject.GOColor.BLACK, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.GREEN, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
            supply_die.add(new GameBonus(GameBonus.Bonus.P2));
            supply_die.add(new GameBonus(GameBonus.Bonus.P1));
            testRollout = new CustomRollout(
                    sharedPref, 
                    diceHashListBuilder(Arrays.asList(5,4,1), new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<Integer>())
                    );
        }
    }
    
    class TestWD implements SetupTest {
        public void setupTest() {
            supply_adapter.notifyDataSetChanged();
            ccd_adapter.setSelectedPos(null);
            craftcard_die.clear();
            craftcard_die.add(new GameObject(6, GameObject.GOColor.BLACK));
            craftcard_die.add(new GameObject(6, GameObject.GOColor.GREEN));
            ccd_adapter.notifyDataSetChanged();
            supply_adapter.setSelectedPos(null);
            supply_die.clear();
            supply_die.add(new GameObject(0, GameObject.GOColor.BLACK, 0, 50));
            supply_die.add(new GameObject(1, GameObject.GOColor.GREEN, 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.RED  , 0, 50));
            supply_die.add(new GameObject(0, GameObject.GOColor.BLUE , 0, 50));
            supply_die.add(new GameBonus(GameBonus.Bonus.WD));
            supply_die.add(new GameBonus(GameBonus.Bonus.A6));
            supply_die.add(new GameBonus(GameBonus.Bonus.A6));
            testRollout = new RollAll1s(sharedPref);
        }
    }
    
    List<SetupTest> setupTests = new ArrayList<SetupTest>();
    Iterator<SetupTest> testIter;
    Rollout testRollout;
    private void pickTest() {
        if (testRollout == null) {
            setupTests.add(new TestWD());
            setupTests.add(new TestP2P1A());
            setupTests.add(new TestP2P1B());
            setupTests.add(new TestPerformance());
            setupTests.add(new TestP2Pass());
            setupTests.add(new TestP2Fail());
            testIter = setupTests.iterator();
        }
        // if at end, restart
        if (!testIter.hasNext()) { testIter = setupTests.iterator(); }
        testIter.next().setupTest();
    }
    
    private void runTest() {
        if (testRollout == null) {
            pickTest();
        }
        doRollout(1, testRollout);
    }
    
    private DiceHashList diceHashListBuilder(
            List<Integer> black, List<Integer> green, List<Integer> red, List<Integer> blue) {
        DiceHashList diceHashList = new DiceHashList();
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            diceHashList.put(color, new ArrayList<GameObject>());
        }
        for (int i : black) {
            diceHashList.get(GameObject.GOColor.BLACK).add(new GameObject(i, GameObject.GOColor.BLACK));
        }
        for (int i : green) {
            diceHashList.get(GameObject.GOColor.GREEN).add(new GameObject(i, GameObject.GOColor.GREEN));
        }
        for (int i : red) {
            diceHashList.get(GameObject.GOColor.RED).add(new GameObject(i, GameObject.GOColor.RED));
        }
        for (int i : blue) {
            diceHashList.get(GameObject.GOColor.BLUE).add(new GameObject(i, GameObject.GOColor.BLUE));
        }
        return diceHashList;
    }
            
}
