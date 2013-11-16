package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;


public class MainActivity extends Activity {

	Random random = new Random(new Date().getTime());
	
    private List<GameObject>  craftcard_die;    // list of craft card requirements
    private List<String>      craftcard_tools;  // list of tools to manipulate craftcard_die
    private Integer           selected_craft_pos = null;
    private CraftDieAdapter   ccd_adapter;
    private CraftToolsAdapter cct_adapter;
    
    private List<GameObject>  supply_die;      // list of supply die available
    private List<String>      supply_tools;    // list of tools to manipulate suply_die
    private Integer           selected_supply_pos = null;
    private CraftDieAdapter   supply_adapter;  // for now can just use the same adapter class
    private CraftToolsAdapter supplyT_adapter; //   " 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        craftcard_die = new ArrayList<GameObject>();
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK));
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK));
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.GREEN));
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.GREEN));
        GridView ccd_gv = (GridView) findViewById(R.id.craftcard_grid);
        ccd_adapter = new CraftDieAdapter(this, craftcard_die, selected_craft_pos);
        ccd_gv.setAdapter(ccd_adapter);
        ccd_gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
            	onCraftcardDieClick(pos);
            }
        });
        
        craftcard_tools = new ArrayList<String>();
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

        supply_die = new ArrayList<GameObject>();
    	supply_die.add(new GameObject(3, GameObject.GOColor.BLACK));
    	supply_die.add(new GameObject(3, GameObject.GOColor.GREEN));
    	supply_die.add(new GameObject(3, GameObject.GOColor.RED  ));
    	supply_die.add(new GameObject(3, GameObject.GOColor.BLUE ));
    	for (GameObject go : supply_die) { go.setMin(0); go.setMax(50); }
        GridView supply_gv = (GridView) findViewById(R.id.supply_grid);
        supply_adapter = new CraftDieAdapter(this, supply_die, selected_supply_pos); // TODO CraftDie?
        supply_gv.setAdapter(supply_adapter);
        supply_gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
            	onSupplyDieClick(pos);
            }
        });
        
        supply_tools = new ArrayList<String>();
        supply_tools.add(new String("+"));
        supply_tools.add(new String("-"));
        supply_tools.add(new String("X"));
        supply_tools.add(new String("+1"));
        supply_tools.add(new String("+1 (3)"));
        supply_tools.add(new String("+2"));
        supply_tools.add(new String("1->6"));
        supply_tools.add(new String("auto6"));
        supply_tools.add(new String("reroll"));
        supply_tools.add(new String("white die"));
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
    	if (selected_craft_pos != null && selected_craft_pos == pos) {
    		selected_craft_pos = null;  // unselect
    	} else {
		    selected_craft_pos = pos;
    	}
    	ccd_adapter.setSelectedPos(selected_craft_pos);
    }

    private void onCraftcardToolsClick(int pos) {
    	String str = craftcard_tools.get(pos).toString();

    	if (selected_craft_pos != null) {
        	GameObject go = craftcard_die.get(selected_craft_pos);
    		if      ("+".equals(str)) { go.setValue(go.getValue() + 1); }
    		else if ("-".equals(str)) { go.setValue(go.getValue() - 1); }
    		else if ("X".equals(str)) { 
    			craftcard_die.remove((int) selected_craft_pos);  // XXX: omg Integer objects mess things up here, cast to int
    			selected_craft_pos = null;
    			ccd_adapter.setSelectedPos(selected_craft_pos);  // FIXME there are two copies of this variable -- bad!
    		}
    	}
    	if ("Black".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK)); 
    		//selected_craft_pos = craftcard_die.size()-1;
    	} else if ("Green".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.GREEN)); 
    		//selected_craft_pos = craftcard_die.size()-1;
    	} else if ("Red".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.RED)); 
    		//selected_craft_pos = craftcard_die.size()-1;
    	} else if ("Blue".equals(str)) { 
    		craftcard_die.add(new GameObject(4, GameObject.GOColor.BLUE)); 
    		//selected_craft_pos = craftcard_die.size()-1;
    	}

    	// TODO: currently inconsistent on who is responsible to call this
    	ccd_adapter.notifyDataSetChanged();
    }

    private void onSupplyDieClick(int pos) {
    	if (selected_supply_pos != null && selected_supply_pos == pos) {
    		selected_supply_pos = null;  // unselect
    	} else {
		    selected_supply_pos = pos;
    	}
    	supply_adapter.setSelectedPos(selected_supply_pos);
    }
   
    private void onSupplyToolsClick(int pos) {
    	if (selected_supply_pos == null) { return; }  // TODO: Eventually there are add tools we need to handle here
    	GameObject go = supply_die.get(selected_supply_pos);
    	String str = supply_tools.get(pos).toString();
    	if      ("+".equals(str)) { go.setValue(go.getValue() + 1); }
    	else if ("-".equals(str)) { go.setValue(go.getValue() - 1); }
    	else if ("X".equals(str)) { go.setValue(0);                 }
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
        
    public void doRollout(View view, int totalRolls) {
        // for now store supply list in array -- should be a hash with the colors as keys
    	List<Integer> supplyList = new ArrayList<Integer>();
    	// should be a hash of arrays -- colors as keys
    	List<Integer> blackNeeded = new ArrayList<Integer>();
    	List<Integer> greenNeeded = new ArrayList<Integer>();

    	String result = "";
    	double successes = 0;
    	boolean haveEnoughDice = true;

    	// get craft requirements out of the widgets
    	for (GameObject go : craftcard_die) {
    		switch (go.getColor()) {
    		case BLACK: blackNeeded.add(go.getValue()); break;
    		case GREEN: greenNeeded.add(go.getValue()); break;
    		default: break;	// TODO: add other colors
    		}
    	}

        // the supply_die list is in the same order as supplyList 
    	for (GameObject go : supply_die) {
    		supplyList.add(go.getValue());
    	}
    	
    	Collections.sort(blackNeeded);
    	Collections.sort(greenNeeded);
    	Collections.reverse(blackNeeded);
    	Collections.reverse(greenNeeded);

    	if (blackNeeded.size() > supplyList.get(0)) {
    		haveEnoughDice = false;
    	}
    	if (greenNeeded.size() > supplyList.get(1)) {
    		haveEnoughDice = false;
    	}
    	if (haveEnoughDice) {
    		for (int x = 0; x < totalRolls; x++) {
    			List<Integer> blackRolls = roll(supplyList.get(0));
    			boolean blackSuccess = checkSuccess(blackRolls, blackNeeded);

    			if (!blackSuccess) {
    				continue;
    			}

    			List<Integer> greenRolls = roll(supplyList.get(1));
    			boolean greenSuccess = checkSuccess(greenRolls, greenNeeded);
    			if (!greenSuccess) {
    				continue;
    			}

    			successes++;
    		}
    	}


    	TextView rolloutResults = (TextView) findViewById(R.id.rollout_results);
    	if (result == "") { 
    		if (!haveEnoughDice) {
				result = "Insufficient dice";
			} else {
				result = "Total successes: " + successes;
				result += "\nTotal rolls: " + totalRolls;
				// TODO: Why couldn't I put the trailing directly in the format, with/without escaping it.
				result += String.format("\nChance to win: %2.2f", (successes / totalRolls) * 100) + "%";
			}
		}
		rolloutResults.setText(result);

		
    }

    private boolean checkSuccess(List<Integer> rolled, List<Integer> needed) {
        int x = 0;
        for (Integer need : needed) {
            // Is this oversimplifying?
            if (rolled.get(x++) < need) {
                return false;
            }
        }
        return true;
    }
  
    private List<Integer> roll(int amountToRoll) {
        List<Integer> rolls = new ArrayList<Integer>();
        for (int x = 0; x < amountToRoll; x++) {
            rolls.add(Math.abs(random.nextInt() % 6) + 1);
 
        }
        Collections.sort(rolls);
        Collections.reverse(rolls);
        return rolls;
    }
}
