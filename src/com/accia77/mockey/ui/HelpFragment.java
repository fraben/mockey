package com.accia77.mockey.ui;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.accia77.mockey.R;
import com.actionbarsherlock.app.SherlockFragment;

public class HelpFragment extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_help_screen, container, false);
		
		return view;
	}

}
