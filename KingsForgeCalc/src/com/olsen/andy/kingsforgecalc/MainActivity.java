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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	Random random = new Random(new Date().getTime());

	public static final String EXTRA_MESSAGE = "com.olsen.andy.kingsforgecalc.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText editText;
        editText = (EditText) findViewById(R.id.supply_black); editText.setText("3");
        editText = (EditText) findViewById(R.id.supply_green); editText.setText("3");
        editText = (EditText) findViewById(R.id.supply_red);   editText.setText("3");
        editText = (EditText) findViewById(R.id.supply_blue);  editText.setText("3");
        editText = (EditText) findViewById(R.id.craft_black0); editText.setText("3");
        editText = (EditText) findViewById(R.id.craft_black1); editText.setText("3");
        editText = (EditText) findViewById(R.id.craft_green0); editText.setText("3");
        editText = (EditText) findViewById(R.id.craft_green1); editText.setText("3");
        
        List<Integer> number_array = new ArrayList<Integer>();
        for (int i=0; i<10; i++) {
        	number_array.add(i);
        }
        GridView gridview = (GridView) findViewById(R.id.craftcard_grid);
        CraftDieAdapter adapter = new CraftDieAdapter(this, number_array);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void doRollout(View view) {
        // for now store supply list in array -- should be a hash with the colors as keys
    	List<Integer> supplyList = new ArrayList<Integer>();
    	// should be a hash of arrays -- colors as keys
    	List<Integer> blackNeeded = new ArrayList<Integer>();
    	List<Integer> greenNeeded = new ArrayList<Integer>();

    	int tmpInt;
    	EditText editText;
		String result = "";
    	double successes = 0;
    	int totalRolls = 10000;  
    	boolean haveEnoughDice = true;
    	 

    	// get craft requirements out of the widgets
    	try {
    		editText = (EditText) findViewById(R.id.craft_black0);
    		tmpInt = Integer.parseInt(editText.getText().toString());
    		if (tmpInt>0) { blackNeeded.add(tmpInt); }
    		editText = (EditText) findViewById(R.id.craft_black1);
    		tmpInt = Integer.parseInt(editText.getText().toString());
    		if (tmpInt>0) { blackNeeded.add(tmpInt); }

    		editText = (EditText) findViewById(R.id.craft_green0);
    		tmpInt = Integer.parseInt(editText.getText().toString());
    		if (tmpInt>0) { greenNeeded.add(tmpInt); }
    		editText = (EditText) findViewById(R.id.craft_green1);
    		tmpInt = Integer.parseInt(editText.getText().toString());
    		if (tmpInt>0) { greenNeeded.add(tmpInt); }

    		// get dice supply counts out of the widgets
    		editText = (EditText) findViewById(R.id.supply_black);
    		supplyList.add(Integer.parseInt(editText.getText().toString()));
    		editText = (EditText) findViewById(R.id.supply_green);
    		supplyList.add(Integer.parseInt(editText.getText().toString()));
    		editText = (EditText) findViewById(R.id.supply_red);
    		supplyList.add(Integer.parseInt(editText.getText().toString()));
    		editText = (EditText) findViewById(R.id.supply_blue);
    		supplyList.add(Integer.parseInt(editText.getText().toString()));

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
    	} catch (NumberFormatException e) {
     	    result = "bad number";
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
