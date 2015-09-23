package com.builder.ibalance;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 PreferenceManager prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("USER_DATA");
         prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE);
		addPreferencesFromResource(R.xml.preference);
	
	}

}
