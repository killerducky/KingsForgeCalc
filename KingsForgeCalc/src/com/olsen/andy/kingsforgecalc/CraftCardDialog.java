package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CraftCardDialog extends Activity {
    private GameState gameState;
    private List<CraftCard> ccList;

    private class CraftCard {
        public Integer rank;
        public String  name;
        public List<GameObject> goList;
        CraftCard (Integer rank, String name) {
            this.rank = rank;
            this.name = name;
            goList = new ArrayList<GameObject>();
        }
        public CraftCard addGO(GameObject.GOColor color, Integer value) {
            goList.add(new GameObject(value, color));
            return this;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameState = GameState.getInstance();
        ScrollView scroll = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv;
        tv = new TextView(this);
        tv.setText("test");
        layout.addView(tv);
        initList();
        for (CraftCard cc : ccList) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            tv = new TextView(this);
//            tv.setText(cc.rank);  // TODO: Why is this not a compile error??
            tv.setText(cc.rank.toString());
            row.addView(tv);
            tv = new TextView(this);
            tv.setText(cc.name);
            row.addView(tv);
            row.setGravity(Gravity.CENTER);
            layout.addView(row);
        }
        scroll.addView(layout);
        setContentView(scroll);
    }
    
    public void initList() {
        ccList = new ArrayList<CraftCard>();
        ccList.add(new CraftCard(1, "Knife").addGO(GameObject.GOColor.BLACK, 6));
        ccList.add(new CraftCard(2, "Knfdife").addGO(GameObject.GOColor.BLACK, 5));
        ccList.add(new CraftCard(3, "Knifdfe").addGO(GameObject.GOColor.BLACK, 4));
    }

}
