package com.olsen.andy.kingsforgecalc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class DiceSpinner extends Spinner {
    private List<Object> list = new ArrayList<Object>();
    private CraftDieAdapter adapter = new CraftDieAdapter(super.getContext(), list, null);
    private int prevPos = 0;

    public DiceSpinner(Context context) {
        super(context);
    }

    public DiceSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setColor(GameObject.GOColor color) {
        for (Object o : list) {
            if (o instanceof GameObject) {
                GameObject go = (GameObject) o;
                go.setColor(color);
            }
        }
        adapter.notifyDataSetChanged();
    }
    
    public void setDeleted() {
        this.setSelection(6+4+1-1); // FIXME horrible hack
    }
    
    public GameObject getSelectedGameObject() {
        return (GameObject) getSelectedItem();
    }
    
    public void buildSpinner(boolean isSupplyType, int min, int max) {
        if (isSupplyType) {
            for (int i=min; i<=max; i++) {
                list.add(new GameObject(i, GameObject.GOColor.BLACK, min, max));
            }
        } else {
            for (int i=min; i<=max; i++) {
                list.add(new GameObject(i, GameObject.GOColor.BLACK));
            }
            if (!isSupplyType) {
                for (GameObject.GOColor color : GameObject.GOColor.values()) {
                    list.add(color);
                }
                list.add(new String("Delete"));
            }
        }
        super.setAdapter(adapter);
        super.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object selected = parent.getItemAtPosition(pos);
                if (selected instanceof GameObject.GOColor) {
                    GameObject.GOColor color = (GameObject.GOColor) selected;
                    parent.setSelection(prevPos < 6 ? prevPos : 0);
                    setColor(color);
                }
                prevPos = pos;
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }


}
