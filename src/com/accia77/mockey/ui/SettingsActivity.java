package com.accia77.mockey.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import com.accia77.mockey.R;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity {
	private static final int PICK_IMAGE = 1;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Listener for the item that lets the user select the background image(s)
		Preference selectAnimationFramesPref = findPreference("select_animation_frames");
		selectAnimationFramesPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(getApplicationContext(), PickBackgroundImagesActivity.class);
						startActivity(intent);
						
						return true;
					}
				});
		
		// Listener for the item that lets the user reset the background image(s)
		Preference resetBackgroundImgPref = findPreference("reset_animation_frames");
		resetBackgroundImgPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences spref = PreferenceManager
								.getDefaultSharedPreferences(SettingsActivity.this);
						Editor editor = spref.edit();
						editor.putString("backgroundImagePath", "");
						editor.commit();
						editor.putString("animationPhase0", "");
						editor.commit();
						editor.putString("animationPhase1", "");
						editor.commit();
						editor.putString("animationPhase2", "");
						editor.commit();

						String strAvviso = getResources().getString(
								R.string.reset_background_success);
						Toast.makeText(getApplicationContext(), strAvviso,
								Toast.LENGTH_LONG).show();
						return true;
					}
				});
		

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}


}
