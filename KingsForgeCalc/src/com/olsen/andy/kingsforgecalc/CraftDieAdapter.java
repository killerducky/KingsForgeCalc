package com.olsen.andy.kingsforgecalc;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CraftDieAdapter extends ArrayAdapter<Integer> {

	public CraftDieAdapter(Context context, List<Integer> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView tv = (TextView) super.getView(pos, convertView, parent);
		tv.setBackgroundColor(Color.GREEN);
		tv.setTextColor(Color.WHITE);
		tv.setGravity(Gravity.CENTER);
		return tv;
	}

}
