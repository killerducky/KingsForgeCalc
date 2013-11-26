package com.olsen.andy.kingsforgecalc;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RerollDialog extends Activity {
    private GameState gameState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameState = GameState.getInstance();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv;
        tv = new TextView(this);
        tv.setTextSize(40);
        tv.setText("Pick dice to reroll");
        layout.addView(tv);
        tv = new TextView(this);
        tv.setText("Craft Card:\n" + gameState.rollout.neededHashList.normalString() + "\n" +
                "Recommend keep:\n" + gameState.rollout.rolledHashList.normalString() + "\n" +
                "Recommend reroll:\n" + gameState.rollout.rerollHashList.normalString());
        layout.addView(tv);

        //        DiceSpinner spinnerOrig;
        //        spinnerOrig = (DiceSpinner) findViewById(R.id.black_supply);
        ////        spinnerOrig.buildSpinner(true, 0, 25);
        ////        spinnerOrig.setSelection(5); // default black to 5
        //        for (GameObject.GOColor color : GameObject.GOColor.values()) {
        //            DiceSpinner spinnerClone = new DiceSpinner(this);
        //            spinnerClone.setLayoutParams(spinnerOrig.getLayoutParams());
        //            spinnerClone.buildSpinner(true, 1, 6);
        //            spinnerClone.setColor(color);
        //            layout.addView(spinnerClone);
        //        }
        //        tv = new TextView(this);
        //        tv.setText("Go");
        //        tv.setOnClickListener(new OnClickListener() {
        //            public void onClick(View view) {
        //                TextView tv = (TextView) view;
        //                Toast.makeText(getApplicationContext(), "selected" + tv.getText(), Toast.LENGTH_LONG).show();
        //                finish();
        //                HashMap<String, String> result;
        //                result = gameState.rollout.continueReroll(); 
        //                gameState.mainActivity.showRolloutResults(result);
        //            }
        //        });
        //        layout.addView(tv);
        tv = new TextView(this);
        tv.setText("Go");
        tv.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TextView tv = (TextView) view;
                Toast.makeText(getApplicationContext(), "selected" + tv.getText(), Toast.LENGTH_LONG).show();
                finish();
                HashMap<String, String> result;
                result = gameState.rollout.continueReroll(); 
                gameState.mainActivity.showRolloutResults(result);
            }
        });
        layout.addView(tv);

        setContentView(layout);
    }
}
