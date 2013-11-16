package com.olsen.andy.kingsforgecalc;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CraftDieAdapter extends ArrayAdapter<GameObject> {

	public CraftDieAdapter(Context context, List<GameObject> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView tv = (TextView) super.getView(pos, convertView, parent);
		GameObject go = super.getItem(pos);
		switch (go.getColor()) {
		case BLACK: tv.setBackgroundColor(Color.BLACK); break;
		case GREEN: tv.setBackgroundColor(Color.GREEN); break;
		case RED  : tv.setBackgroundColor(Color.RED  ); break;
		case BLUE : tv.setBackgroundColor(Color.BLUE ); break;
		case WHITE: tv.setBackgroundColor(Color.WHITE); break;
		}
		switch (go.getColor()) {
		case WHITE: tv.setTextColor(Color.BLACK); break;
		default: tv.setTextColor(Color.WHITE);    break;
		}
		tv.setGravity(Gravity.CENTER);
		return tv;
	}

}
