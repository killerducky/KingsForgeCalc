package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CraftCardDialog extends Activity {
    private GameState gameState;
    private List<CraftCard> ccList;

    public class CraftCard {
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
        public CraftCard addBlack(Integer value) {
            goList.add(new GameObject(value, GameObject.GOColor.BLACK));
            return this;
        }
        public CraftCard addGreen(Integer value) {
            goList.add(new GameObject(value, GameObject.GOColor.GREEN));
            return this;
        }
        public CraftCard addRed(Integer value) {
            goList.add(new GameObject(value, GameObject.GOColor.RED));
            return this;
        }
        public CraftCard addBlue(Integer value) {
            goList.add(new GameObject(value, GameObject.GOColor.BLUE));
            return this;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameState = GameState.getInstance();
        initList();

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv;
        for (final CraftCard cc : ccList) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            tv = new TextView(this);
            tv.setText(cc.rank.toString() + " - ");  // TODO nicer layout
            tv.setTextSize(30);
            row.addView(tv);
            tv = new TextView(this);
            tv.setText(cc.name);
            tv.setTextSize(20);
            row.addView(tv);
            row.setGravity(Gravity.CENTER);
            row.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    finish();
                    gameState.mainActivity.setCraftCard(cc);
                }
            });
            layout.addView(row);
        }
        scroll.addView(layout);
        setContentView(scroll);
    }
    
    public void initList() {
        ccList = new ArrayList<CraftCard>();
        ccList.add(new CraftCard(1, "Anvil").addBlack(1).addBlack(1).addBlack(1));
        ccList.add(new CraftCard(2, "Knife").addBlack(6));
        ccList.add(new CraftCard(3, "Shield").addBlack(5).addBlack(5));
        ccList.add(new CraftCard(4, "Mace").addBlack(4).addBlack(4).addBlack(4));
        ccList.add(new CraftCard(5, "Axe").addGreen(3).addGreen(3).addBlack(5));
        ccList.add(new CraftCard(6, "Giant Club").addGreen(3).addGreen(4).addGreen(5));
        ccList.add(new CraftCard(7, "Ruby Collar").addRed(2));
        ccList.add(new CraftCard(8, "Bracelet").addRed(6));
        ccList.add(new CraftCard(9, "Fancy Pipe").addGreen(1).addGreen(1).addRed(6));
        ccList.add(new CraftCard(10, "Magic Wand").addGreen(3).addBlue(2));
        ccList.add(new CraftCard(11, "Necklace").addRed(1).addRed(1).addRed(1));
        ccList.add(new CraftCard(12, "Fancy Toys").addGreen(3).addGreen(5).addRed(2).addRed(2));
        ccList.add(new CraftCard(13, "Plate Armor").addBlack(3).addBlack(3).addBlack(4).addBlack(4).addBlack(5).addBlack(5));
        ccList.add(new CraftCard(14, "Short Staff").addGreen(4).addGreen(4).addRed(1).addBlue(4));
        ccList.add(new CraftCard(15, "Royal Sword").addBlack(3).addBlack(5).addRed(3).addRed(5));
        ccList.add(new CraftCard(16, "Braced Bow").addBlack(2).addBlack(2).addGreen(3).addGreen(3).addBlue(1).addBlue(2));
        ccList.add(new CraftCard(17, "Ceremonial Shield").addGreen(3).addGreen(3).addGreen(4).addGreen(4).addRed(3).addRed(3));
        ccList.add(new CraftCard(18, "Shiny Crossbow").addGreen(4).addGreen(5).addRed(4).addBlue(4));
        ccList.add(new CraftCard(19, "Ice Sword").addBlack(5).addBlack(5).addBlue(2).addBlue(4));
        ccList.add(new CraftCard(20, "Holy Hand Grenade").addBlue(1).addBlue(1).addBlue(2));
        ccList.add(new CraftCard(21, "Blessed Gauntlets").addBlack(6).addBlack(6).addBlack(6).addBlue(2));
        ccList.add(new CraftCard(22, "Battle Axe").addBlack(4).addBlack(4).addBlack(6).addGreen(4).addRed(4).addBlue(4));
        ccList.add(new CraftCard(23, "Crown").addBlack(4).addBlack(5).addGreen(3).addRed(2).addRed(4).addRed(5).addBlue(4));
        ccList.add(new CraftCard(24, "Sword of Destiny").addBlack(6).addGreen(6).addRed(6).addBlue(6));

        ccList.add(new CraftCard(1, "Anvil").addBlack(2).addBlack(2).addBlack(2));
        ccList.add(new CraftCard(5, "Shield").addBlack(5).addBlack(5));
        ccList.add(new CraftCard(6, "Horseshoes").addBlack(4).addBlack(4).addBlack(4));
        ccList.add(new CraftCard(7, "Table").addGreen(3).addGreen(4));
        ccList.add(new CraftCard(10, "Ruby Collar").addRed(4));
        ccList.add(new CraftCard(11, "Axe").addBlack(5).addGreen(3).addGreen(3));
        ccList.add(new CraftCard(12, "Pick").addBlack(5).addGreen(2).addGreen(2).addGreen(2));
        ccList.add(new CraftCard(14, "Bracelet").addBlack(2).addRed(6));
        ccList.add(new CraftCard(15, "Giant Club").addGreen(3).addGreen(4).addGreen(5));
        ccList.add(new CraftCard(19, "Magic Wand").addGreen(5).addBlue(2));
        ccList.add(new CraftCard(21, "Ceremonial Pipe").addGreen(4).addGreen(4).addRed(5));
        ccList.add(new CraftCard(22, "Necklace").addRed(2).addRed(2).addRed(2));
        ccList.add(new CraftCard(23, "Fancy Toys").addGreen(3).addGreen(5).addRed(2).addRed(2));
        ccList.add(new CraftCard(26, "Witch's Brooch").addRed(4).addBlue(4));
        ccList.add(new CraftCard(27, "Saphire Spear").addGreen(2).addGreen(2).addGreen(2).addGreen(3).addGreen(3).addRed(1));
        ccList.add(new CraftCard(29, "Short Staff").addGreen(4).addGreen(4).addRed(3).addBlue(4));
        ccList.add(new CraftCard(32, "Swift Sword").addBlack(5).addBlack(5).addRed(4).addRed(4));
        ccList.add(new CraftCard(33, "Bow of Fire").addBlack(2).addBlack(2).addGreen(3).addGreen(3).addBlue(2).addBlue(2));
        ccList.add(new CraftCard(35, "Platemail of Loyalty").addBlack(3).addBlack(4).addBlack(4).addBlack(5).addBlack(5).addBlack(6));
        ccList.add(new CraftCard(36, "Queen's Crossbow").addBlack(5).addGreen(5).addRed(4).addBlue(4));
        ccList.add(new CraftCard(38, "Ice Sword").addBlack(5).addBlack(5).addBlue(3).addBlue(4));
        ccList.add(new CraftCard(40, "Holy Hand Grenade").addBlue(1).addBlue(2).addBlue(5));
        ccList.add(new CraftCard(43, "Blessed Gauntlets").addBlack(6).addBlack(6).addBlack(6).addBlue(2));
        ccList.add(new CraftCard(44, "Battle Axe").addBlack(4).addBlack(5).addBlack(6));
        ccList.add(new CraftCard(46, "Crown").addBlack(4).addBlack(5).addRed(4).addRed(4).addRed(4).addBlue(4));
        ccList.add(new CraftCard(48, "Sword of Destiny").addBlack(6).addGreen(6).addRed(6).addBlue(6));
    }

}
