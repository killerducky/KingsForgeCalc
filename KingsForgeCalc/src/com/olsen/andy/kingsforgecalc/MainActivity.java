package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
	
    private List<Object>      craftcard_die;    // list of craft card requirements
    private List<String>      craftcard_tools;  // list of tools to manipulate craftcard_die
    private CraftDieAdapter   ccd_adapter;
    private CraftToolsAdapter cct_adapter;
    
    private List<Object>      supply_die;      // list of supply die available
    private List<String>      supply_tools;    // list of tools to manipulate suply_die
    private CraftDieAdapter   supply_adapter;  // for now can just use the same adapter class
    private CraftToolsAdapter supplyT_adapter; //   "
    
    public List<String> bonuses = Arrays.asList("+1", "+1 (3)", "+2", "1->6", "auto6", "reroll", "white die");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        craftcard_die = new ArrayList<Object>();
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK));
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.BLACK));
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.GREEN));
    	craftcard_die.add(new GameObject(4, GameObject.GOColor.GREEN));
        GridView ccd_gv = (GridView) findViewById(R.id.craftcard_grid);
        ccd_adapter = new CraftDieAdapter(this, craftcard_die, null);
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

        supply_die = new ArrayList<Object>();
    	supply_die.add(new GameObject(3, GameObject.GOColor.BLACK));
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
        
        supply_tools = new ArrayList<String>();
        supply_tools.add(new String("+"));
        supply_tools.add(new String("-"));
        supply_tools.add(new String("X"));
        for (String bonus : bonuses) {
            supply_tools.add(bonus);
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
    	String str = supply_tools.get(pos).toString();
    	if (supply_adapter.getSelectedPos() != null) { 
    		Object o = supply_die.get(supply_adapter.getSelectedPos());
    		if (o instanceof GameObject) {
    			GameObject go = (GameObject) o;
    			if      ("+".equals(str)) { go.setValue(go.getValue() + 1); }
    			else if ("-".equals(str)) { go.setValue(go.getValue() - 1); }
    			else if ("X".equals(str)) { go.setValue(0);                 }
    		} else {
                // Handle bonuses, which right now are just strings
                if ("X".equals(str)) {  
        			supply_die.remove((int) supply_adapter.getSelectedPos());  // XXX: omg Integer objects mess things up here, cast to int
        			if (supply_die.size()==0) {
        				supply_adapter.setSelectedPos(null); // XXX: Actually this is impossible, cannot delete the 4 colors
        			} else if (supply_adapter.getSelectedPos() >= supply_die.size()) { 
        				supply_adapter.setSelectedPos(supply_die.size()-1);
        			}
                }
    		}
    	}
    	if (bonuses.contains(str)) {
    		supply_die.add(new String(str)); 
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
        
    public void doRollout(View view, int totalRolls) {
    	HashMap<GameObject.GOColor, Integer> supplyHashInt = new HashMap<GameObject.GOColor, Integer>();
    	HashMap<GameObject.GOColor, List<Integer>> neededHashList = new HashMap<GameObject.GOColor, List<Integer>>();
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		neededHashList.put(color, new ArrayList<Integer>());
    	}

    	String result = "";
    	double successes = 0;
    	boolean haveEnoughDice = true;

    	// get craft requirements out of the widgets
    	for (Object o : craftcard_die) {
    		GameObject go = (GameObject) o;
    		neededHashList.get(go.getColor()).add(go.getValue());
    	}

        // TODO: hacky but the GameObjects in order are what we want
    	// The other objects are bonuses
    	for (Object o : supply_die) {
    		if (o instanceof GameObject) {
    			GameObject go = (GameObject) o;
    			supplyHashInt.put(go.getColor(), go.getValue());
    		}
    	}
//    	supplyHashInt.put(GameObject.GOColor.WHITE, 0);  // TODO: Hack to workaround white.  Probably white should be a bonus string not a color.
    	
    	for (GameObject.GOColor color : GameObject.GOColor.values()) {
    		List<Integer> tmpList = new ArrayList<Integer>();
    		if (neededHashList.get(color).size() > supplyHashInt.get(color)) {
    			haveEnoughDice = false;
    			break;
    		}
    		Collections.sort(neededHashList.get(color));
    		Collections.reverse(neededHashList.get(color));
    	}

    	if (haveEnoughDice) {
    		for (int x = 0; x < totalRolls; x++) {
    			boolean success = true;
    			for (GameObject.GOColor color : GameObject.GOColor.values()) {
    				List<Integer> rolls = roll(supplyHashInt.get(color));
    				if (!checkSuccess(rolls, neededHashList.get(color))) {
    					success = false;
    					continue;
    				}
    			}
    			if (success) { successes++; }
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
