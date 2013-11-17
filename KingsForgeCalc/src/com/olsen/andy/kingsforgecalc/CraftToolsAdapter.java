package com.olsen.andy.kingsforgecalc;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CraftToolsAdapter extends ArrayAdapter<Object> {
	public CraftToolsAdapter(Context context, List<Object> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);
	}
	
	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView tv = (TextView) super.getView(pos, convertView, parent);
		tv.setGravity(Gravity.CENTER);
		if ("Black".equals(tv.getText())) {
			tv.setText("+");
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.BLACK);
		} else if ("Green".equals(tv.getText())) {
			tv.setText("+");
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.GREEN);
		} else if ("Red".equals(tv.getText())) {
			tv.setText("+");
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.RED);
		} else if ("Blue".equals(tv.getText())) {
			tv.setText("+");
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.BLUE);
		} else {
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.DKGRAY);
		}
		return tv;
	}

}
