package com.olsen.andy.kingsforgecalc;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CraftToolsAdapter extends ArrayAdapter<String> {
	public CraftToolsAdapter(Context context, List<String> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);
	}
	
	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView tv = (TextView) super.getView(pos, convertView, parent);
		tv.setGravity(Gravity.CENTER);
		return tv;
	}

}
