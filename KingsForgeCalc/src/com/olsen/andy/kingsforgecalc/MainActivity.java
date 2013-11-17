package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    	supply_die.add(new GameObject(2, GameObject.GOColor.BLACK));
    	supply_die.add(new GameObject(3, GameObject.GOColor.GREEN));
    	supply_die.add(new GameObject(3, GameObject.GOColor.RED  ));
    	supply_die.add(new GameObject(3, GameObject.GOColor.BLUE ));
    	for (Object o : supply_die) { GameObject go = (GameObject) o; go.setMin(0); go.setMax(50); }
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
    		if      ("+".equals(str)) { go.setValue(go.getValue() + 1); }
    		else if ("-".equals(str)) { go.setValue(go.getValue() - 1); }
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
    			if      ("+".equals(clickedO.toString())) { go.setValue(go.getValue() + 1); }
    			else if ("-".equals(clickedO.toString())) { go.setValue(go.getValue() - 1); }
    			else if ("X".equals(clickedO.toString())) { go.setValue(0);                 }
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
    
    // TODO: Eventually this will be a separate thing that nicely displays your one roll result
    public void doRollout1(View view) {
    	doRollout(view, 1);
    }

    public void doRollout(View view) {
    	doRollout(view, 10000);
    }
        
    public void doRollout(View view, Integer totalRolls) {
    	HashMap<GameObject.GOColor, Integer> supplyHashInt = new HashMap<GameObject.GOColor, Integer>();
    	HashMap<GameObject.GOColor, List<Integer>> neededHashList = new HashMap<GameObject.GOColor, List<Integer>>();
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		neededHashList.put(color, new ArrayList<Integer>());
    	}
    	List<GameBonus> bonusList = new ArrayList<GameBonus>();
    	
    	// get craft requirements out of the widgets
    	for (Object o : craftcard_die) {
    		GameObject go = (GameObject) o;
    		neededHashList.get(go.getColor()).add(go.getValue());
    	}

        // TODO: hacky but the GameObjects are the dice
    	// The other string objects are bonuses
    	for (Object o : supply_die) {
    		if (o instanceof GameObject) {
    			GameObject go = (GameObject) o;
    			supplyHashInt.put(go.getColor(), go.getValue());
    		} else {
    			bonusList.add((GameBonus) o);
    		}
    	}
    	
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		Collections.sort(neededHashList.get(color));
    		Collections.reverse(neededHashList.get(color));
    	}
        
    	Rollout rollout = new Rollout();
    	HashMap<String, String> result = rollout.doRollout(neededHashList, supplyHashInt, bonusList, totalRolls);

    	TextView rolloutResults = (TextView) findViewById(R.id.rollout_results);
    	rolloutResults.setText(result.get("result"));

    	AlertDialog.Builder resultbox = new AlertDialog.Builder(this);
    	resultbox.setMessage(result.get("log") + "\n" + result.get("result"));
    	resultbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface arg0, int arg1) {}
    	});
    	resultbox.show();

    }

}
