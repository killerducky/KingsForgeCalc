package com.olsen.andy.kingsforgecalc;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CraftDieAdapter extends ArrayAdapter<Object> {

	private Integer selected_pos;
	
	public CraftDieAdapter(Context context, List<Object> objects, Integer selected_pos) {
		super(context, android.R.layout.simple_list_item_1, objects);
		this.selected_pos = selected_pos;
	}
	
	public void setSelectedPos(Integer selected_pos) {
		this.selected_pos = selected_pos;
		this.notifyDataSetChanged();
	}
	public Integer getSelectedPos() {
		return this.selected_pos;
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView tv = (TextView) super.getView(pos, convertView, parent);
		Object o = super.getItem(pos);
		if (o instanceof GameObject) {
			GameObject go = (GameObject) o;
			switch (go.getColor()) {
			case BLACK: tv.setBackgroundColor(Color.BLACK); break;
			case GREEN: tv.setBackgroundColor(Color.GREEN); break;
			case RED  : tv.setBackgroundColor(Color.RED  ); break;
			case BLUE : tv.setBackgroundColor(Color.BLUE ); break;
			//case WHITE: tv.setBackgroundColor(Color.WHITE); break;
			}
			if (selected_pos != null && pos == selected_pos) {
				tv.setText(">" + go.getOrigValue() + "<");
			}
			switch (go.getColor()) {
			//case WHITE: tv.setTextColor(Color.BLACK); break;
			default: tv.setTextColor(Color.WHITE);    break;
			}
			tv.setGravity(Gravity.CENTER);
		} else {
			if (selected_pos != null && pos == selected_pos) {
				tv.setText(">" + o.toString() + "<");
			}
			if ("Delete".equals(o.toString())) {
			    tv.setText("");
			    tv.setBackgroundColor(android.R.attr.colorBackground);
			} else {
			    tv.setTextColor(Color.WHITE);
			    tv.setBackgroundColor(Color.DKGRAY);
			}
		}
		return tv;
	}

}
