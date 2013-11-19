package com.olsen.andy.kingsforgecalc;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// FIXME: should be using a fragment instead I think to get rid of this warning
        addPreferencesFromResource(R.xml.preferences);
	}

}
