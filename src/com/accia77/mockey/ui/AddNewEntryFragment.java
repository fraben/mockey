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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.R.id;
import com.accia77.mockey.R.layout;
import com.accia77.mockey.R.string;
import com.accia77.mockey.data.MySQLiteHelper;
import com.actionbarsherlock.app.SherlockFragment;

public class AddNewEntryFragment extends SherlockFragment {

	private Chronometer chronometer;
	final Handler myHandler = new Handler();
	Timer myTimer;

	@Override
	public void onPause() {
		super.onPause();
		handleExitFromFragment();
	}	

	@Override
	public void onStop() {
		super.onStop();
		handleExitFromFragment();
	}
	
	private void handleExitFromFragment() {
		if (MyApplication.getInstance().getAudioRecordPlayManager().ImRecording())
			MyApplication.getInstance().getAudioRecordPlayManager().stopRecording();
		
		if(myTimer != null) myTimer.cancel();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_add_new_entry,
				container, false);

		chronometer = (Chronometer) view.findViewById(R.id.chronometer);
		setChronometerVisibility();

		return view;
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Button addNewTextEntry = (Button) getView()
				.findViewById(R.id.buttonAddTextEntry);
		Button addNewAudioEntry = (Button) getView()
				.findViewById(R.id.buttonRecording);

		addNewTextEntry.setText(getResources().getString(
				R.string.add_new_text_entry,
				MyApplication.getInstance().getMaxLengthTextEntry()));
		addNewAudioEntry.setText(getResources().getString(
				R.string.start_recording,
				MyApplication.getInstance().getMaxLengthAudioClipSeconds()));

		addNewTextEntry.setOnClickListener(addNewEntryListener);
		addNewAudioEntry.setOnClickListener(addNewEntryListener);
	}

	private void setChronometerVisibility() {
		chronometer.setVisibility(MyApplication.getInstance()
				.getAudioRecordPlayManager().ImRecording() ? View.VISIBLE
				: View.INVISIBLE);
	}


	private OnClickListener addNewEntryListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.buttonAddTextEntry:
				EditText pureTextEntryEditText = (EditText) getView()
						.findViewById(R.id.pureTextEntryEditText);
				String enteredText = pureTextEntryEditText.getText().toString();

				if ("".equals(enteredText)) {
					// Empty strings are not allowed. Notify the user.
					Toast toast = Toast.makeText(getActivity(), getResources()
							.getString(R.string.item_not_added_empty_string),
							Toast.LENGTH_SHORT);

					// Toasts in this fragment are shown at the center of the screen,
					// to avoid them showing on top of the soft keyboard (pretty annoying)
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {
					// ok, the string is not empty
					MyApplication.getInstance().AddNewEntry(
							MySQLiteHelper.ENTRY_TYPE_PURE_TEXT, enteredText,
							enteredText);
					pureTextEntryEditText.setText("");
					Toast toast = Toast.makeText(getActivity(), getResources()
							.getString(R.string.item_successfully_added),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				break;
			case R.id.buttonRecording:
				Button buttonRecording = (Button) getView()
						.findViewById(R.id.buttonRecording);
				if (MyApplication.getInstance().getAudioRecordPlayManager()
						.ImRecording()) {
					myTimer.cancel();
					handleRecordingStop();
				} else {
					// Exit immediately if the sd card is not mounted
					if (!MyApplication.getInstance().isSdCardMounted(false))
						return;

					// Close soft keyboard
					InputMethodManager inputManager = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(getActivity()
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					MyApplication.getInstance().getAudioRecordPlayManager()
							.startRecording();
					buttonRecording.setText(R.string.stop_recording);
					chronometer.setBase(SystemClock.elapsedRealtime());
					chronometer.start();

					// Safety timer for max length of the recording
					myTimer = new Timer();
					myTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							UpdateGUI();
						}
					}, MyApplication.getInstance()
							.getMaxLengthAudioClipMillis());
				}
				setChronometerVisibility();
				break;
			}

		}
	};


	private void UpdateGUI() {
		myHandler.post(myRunnable);
	}

	private void handleRecordingStop() {
		Button buttonRecording = (Button) getView()
				.findViewById(R.id.buttonRecording);
		MyApplication.getInstance().getAudioRecordPlayManager().stopRecording();
		buttonRecording.setText(getResources().getString(
				R.string.start_recording,
				MyApplication.getInstance().getMaxLengthAudioClipSeconds()));
		chronometer.stop();

		MyApplication.getInstance().AddNewEntry(
				MySQLiteHelper.ENTRY_TYPE_AUDIO,
				MyApplication.getInstance().getAudioRecordPlayManager()
						.getFileNameOnly(), null);
	}

	final Runnable myRunnable = new Runnable() {
		public void run() {
			handleRecordingStop();
			setChronometerVisibility();
		}
	};

	

}
