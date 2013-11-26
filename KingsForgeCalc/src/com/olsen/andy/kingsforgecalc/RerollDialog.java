package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class RerollDialog extends Activity {
    private GameState gameState;
    private List<CheckBox> cbList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameState = GameState.getInstance();
        cbList = new ArrayList<CheckBox>();
        ScrollView scroll = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv;
        tv = new TextView(this);
        tv.setTextSize(20);
        tv.setText("Mark the dice you want to keep.\nOthers will be rerolled.\n");
        layout.addView(tv);
        tv = new TextView(this);
        tv.setText("Craft Card:\n" + gameState.rollout.neededHashList.normalString() + "\n" +
                "Recommend keep:\n" + gameState.rollout.rolledHashList.normalString() + "\n" +
                "Recommend reroll:\n" + gameState.rollout.rerollHashList.normalString());
        layout.addView(tv);

        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            for (GameObject go : gameState.rollout.rolledHashList.get(color)) {
                addRow(layout, go, true);
            }
            for (GameObject go : gameState.rollout.rerollHashList.get(color)) {
                addRow(layout, go, false);
            }
        }
        tv = new TextView(this);
        tv.setTextSize(40);
        tv.setText("Reroll now");
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                finish();
                HashMap<String, String> result;
                updateRerollHashList();
                result = gameState.rollout.continueReroll(); 
                gameState.mainActivity.showRolloutResults(result);
            }
        });
        layout.addView(tv);
        scroll.addView(layout);
        setContentView(scroll);
    }

    public void updateRerollHashList() {
        DiceHashList newRolledHashList = new DiceHashList();
        DiceHashList newRerollHashList = new DiceHashList();
        Iterator<CheckBox> cbiter = cbList.iterator();
        CheckBox cb;
        // NOTE: This relies on the order not changing
        for (GameObject.GOColor color : GameObject.GOColor.values()) {
            newRolledHashList.put(color, new ArrayList<GameObject>());
            newRerollHashList.put(color, new ArrayList<GameObject>());
            for (GameObject go : gameState.rollout.rolledHashList.get(color)) {
                cb = cbiter.next();
                if (cb.isChecked()) {
                    newRolledHashList.get(color).add(go);
                } else {
                    newRerollHashList.get(color).add(go);
                }
            }
            for (GameObject go : gameState.rollout.rerollHashList.get(color)) {
                cb = cbiter.next();
                if (cb.isChecked()) {
                    newRolledHashList.get(color).add(go);
                } else {
                    newRerollHashList.get(color).add(go);
                }
            }
        }
        gameState.rollout.rolledHashList = newRolledHashList;
        gameState.rollout.rerollHashList = newRerollHashList;
    }
    
    public void addRow(ViewGroup layout, GameObject go, boolean keep) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        DiceSpinner spinnerClone = new DiceSpinner(this);
        spinnerClone.buildSpinner(true, 1, 6);
        spinnerClone.setSelection(go.getOrigValue()-1);  // FIXME super hack
        spinnerClone.setColor(go.getColor());
        row.addView(spinnerClone);
        CheckBox cb;
        cb = new CheckBox(this.getApplicationContext());
        cb.setChecked(keep);
        cbList.add(cb);
        row.addView(cb);
        row.setGravity(Gravity.CENTER);
        layout.addView(row);
    }
    
}
