/*
 * Copyright (C) 2014 fraben 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.accia77.mockey.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.data.MySQLiteHelper;
import com.accia77.mockey.model.Entry;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class PapiroActivity extends SherlockActivity {
	int count = 0;
	private final static String TAG = "PapiroActivity";
	Entry currentEntry;
	String stringToPlay;
	ImageView backgroundImage;
	TextView massimaSceltaTextView;
	private boolean mDoShowAndPlayQuote;

	Bitmap currentlyActiveBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.papiro_activity);

		backgroundImage = (ImageView) findViewById(R.id.papiroImageView);
		backgroundImage.setImageBitmap(MyApplication.getInstance()
				.getBitmapPapiro());
		mDoShowAndPlayQuote = true;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		currentlyActiveBitmap = null;
	}

	@Override
	protected void onStart() {
		super.onStart();

		currentEntry = (Entry) getIntent().getParcelableExtra(
				MonkeyFragment.ENTRY_TO_PLAY);
	}

	@Override
	public void onPause() {
		super.onPause();
		MyApplication.getInstance().getAudioRecordPlayManager().stopPlaying();
	}

	@Override
	public void onResume() {
		super.onResume();

		setTitle(MyApplication.getInstance().getMainActivityTitle());

		massimaSceltaTextView = (TextView) findViewById(R.id.massimaSceltaTextView);
		String stringaTextView = currentEntry.getUserEditedEntry();

		if (mDoShowAndPlayQuote) {
			// Custom toast con il testo della frase
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.papiro_toast,
					(ViewGroup) findViewById(R.id.toast_layout_root));

			ImageView image = (ImageView) layout
					.findViewById(R.id.papiro_toast_image);
			image.setImageResource(R.drawable.ic_small_monkey_head);
			TextView text = (TextView) layout
					.findViewById(R.id.papiro_toast_text);
			text.setText(stringaTextView);

			Toast toast = new Toast(this);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			// Playback of the quote
			if (currentEntry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_PURE_TEXT) {
				MyApplication.getInstance().playSelection(currentEntry);
			} else if (currentEntry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_AUDIO) {
				// No playback if the sd card is not mounted
				if (MyApplication.getInstance().isSdCardMounted(false)) {
					MyApplication.getInstance().playSelection(currentEntry);
				}
			}

			mDoShowAndPlayQuote = false;
		}

		if (MyApplication.getInstance().isDefaultPapiroLoaded()) {
			massimaSceltaTextView.setText(stringaTextView);
			massimaSceltaTextView.setVisibility(View.VISIBLE);
		} else
			massimaSceltaTextView.setVisibility(View.INVISIBLE);

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
