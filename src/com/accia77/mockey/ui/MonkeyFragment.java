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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.model.Entry;
import com.actionbarsherlock.app.SherlockFragment;

@SuppressLint("NewApi")
public class MonkeyFragment extends SherlockFragment implements
		SensorEventListener {
	private static final String TAG = "MonkeyFragment";

	public static final String ENTRY_TO_PLAY = "com.fraben.mockey.ENTRY_TO_PLAY";

	// TODO
	// Is the fragment in foreground?
	// CAUTION: this solution was adopted when everything was Activity-based.
	// Now that i switched to mostly fragments, i will probably need to review this
	private boolean isInForeground = false;

	private float mLastX, mLastY, mLastZ;
	private boolean mAccelerometerInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static final int SHAKE_THRESHOLD = 600;
	private long lastUpdate;

	private boolean mMonkeyAnimationInProgress;

	Entry nextRandomEntry;

	final Handler myHandler = new Handler();
	Timer myTimer;
	Timer fragTransitionTimer;

	// Gesture Detector
	// Useful to detect the most common gestures
	private GestureDetector mGestureDetector;

	// Shared Preferences
	SharedPreferences sharedPrefs;

	AnimationDrawable frameAnimation;
	String m_pathAnim1, m_pathAnim2, m_pathAnim3;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.monkey_activity, container, false);
		
		setupGestureDetector();
		
		
		// How to detect doubletap on a View? 
		// Your view needs to implement the onTouchEvent() method, and that method 
		// needs to pass the event along to the onTouchEvent() method of the GestureDetector object.
		// Fragments don't do onTouchEvent, this is done by the view hierarchy.  
		// So your fragment generates its view hierarchy in onCreateView(), and 
		// you implement that view hierarchy to handle touch events as desired.
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		});

		mMonkeyAnimationInProgress = false;

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		m_pathAnim1 = "";
		m_pathAnim2 = "";
		m_pathAnim3 = "";

		setupAnimationDrawableForMainAnimation();

		// Set the volume control stream
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
	}

	public void startAnimation() {
		frameAnimation.setVisible(false, true);
		frameAnimation.stop();
		frameAnimation.selectDrawable(0);
		frameAnimation.setOneShot(true);

		frameAnimation.start();
	}

	private AnimationDrawable createAnimationDrawable() {

		AnimationDrawable newAnim = new AnimationDrawable();
		Drawable phase1, phase2, phase3;

		phase1 = new BitmapDrawable(getResources(), MyApplication.getInstance()
				.getBackgroundBitmap(0));
		phase2 = new BitmapDrawable(getResources(), MyApplication.getInstance()
				.getBackgroundBitmap(1));
		phase3 = new BitmapDrawable(getResources(), MyApplication.getInstance()
				.getBackgroundBitmap(2));

		// TODO: The animation has fixed timings, just for simplicity.
		// Consider something fancier in the future
		newAnim.addFrame(phase1, 600);
		newAnim.addFrame(phase2, 600);
		newAnim.addFrame(phase3, 600);
		newAnim.addFrame(phase2, 500);
		newAnim.addFrame(phase3, 500);
		newAnim.addFrame(phase2, 400);
		newAnim.addFrame(phase3, 400);
		newAnim.addFrame(phase2, 300);
		newAnim.addFrame(phase3, 300);
		newAnim.addFrame(phase2, 200);
		newAnim.addFrame(phase3, 200);
		newAnim.addFrame(phase2, 100);
		newAnim.addFrame(phase3, 100);
		newAnim.addFrame(phase2, 50);
		newAnim.addFrame(phase3, 50);
		newAnim.addFrame(phase1, 50);
		newAnim.setOneShot(true);
		newAnim.setAlpha(255);

		return newAnim;
	}

	// Set up GestureDetector
	private void setupGestureDetector() {
		mGestureDetector = new GestureDetector(getActivity(),
				new GestureDetector.SimpleOnGestureListener() {
					@Override
		            public boolean onDown(MotionEvent e) {
		                return true;
		            }

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (!mMonkeyAnimationInProgress)
							myStartAnimation();
						return true;
					}
				});
	}

	// Sets up the AnimationDrawable used in MainActivity
	void setupAnimationDrawableForMainAnimation() {
		frameAnimation = createAnimationDrawable();

		ImageView img = (ImageView) getView().findViewById(
				R.id.monkeyShakeImageView);

		if (Build.VERSION.SDK_INT >= 16)
			img.setBackground(frameAnimation);
		else {
			img.setBackgroundDrawable(frameAnimation.getFrame(0));
			img.setImageDrawable(frameAnimation);
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().setTitle(
				MyApplication.getInstance().getMainActivityTitle());

		mAccelerometerInitialized = false;
		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (null != mAccelerometer) {
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		isInForeground = true;

		// "Wake-up" Text-to-speech
		MyApplication.getInstance().playSelection(new Entry());

		setupAnimationDrawableForMainAnimation();

	}

	@Override
	public void onPause() {
		super.onPause();
		isInForeground = false;
		mSensorManager.unregisterListener(this);
		MyApplication.getInstance().getAudioRecordPlayManager().stopPlaying();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	private void myStartAnimation() {
		mSensorManager.unregisterListener(this);
		mMonkeyAnimationInProgress = true;

		nextRandomEntry = MyApplication.getInstance().getRandomEntry();

		// Don't listen to the accelerometer input for some seconds
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				UpdateAccelerometer();
			}
		}, 5000);

		// The transition to PapiroActivity starts a little earlier
		fragTransitionTimer = new Timer();
		fragTransitionTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				activityTransitionGo();
			}
		}, 4500);

		startAnimation();
	}

	private void UpdateAccelerometer() {
		myHandler.post(myAccelRunnable);
	}

	private void activityTransitionGo() {
		myHandler.post(swapFragRunnable);
	}

	final Runnable swapFragRunnable = new Runnable() {
		public void run() {
			if (isInForeground) {
				Intent intent = new Intent(getActivity(), PapiroActivity.class);
				intent.putExtra(ENTRY_TO_PLAY, nextRandomEntry);
				startActivity(intent);
			}
		}
	};

	final Runnable myAccelRunnable = new Runnable() {
		public void run() {
			if (isInForeground) {
				if (null != mAccelerometer) {
					mSensorManager.registerListener(MonkeyFragment.this,
							mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				}
			}

			// Did this to prevent a new animation to start: probably when the application 
			// restarts listening to accelerometer input, the new reading is different
			// from the last acquired one.
			mAccelerometerInitialized = false;

			mMonkeyAnimationInProgress = false;
		}
	};

	@Override
	public void onSensorChanged(SensorEvent event) {

		// Check the source of the event
		Sensor mySensor = event.sensor;

		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			// The system's sensors are incredibly sensitive. When holding a
			// device in your hand, it is constantly in motion, no matter how
			// steady your hand is. The result is that the onSensorChanged
			// method is invoked several times per second. We don't need all
			// this data so we need to make sure we only sample a subset of the
			// data we get from the device's accelerometer. We store the
			// system's current time (in milliseconds) store it in curTime and
			// check whether more than 100 milliseconds have passed since the
			// last time onSensorChanged was invoked.
			long curTime = System.currentTimeMillis();

			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				if (!mAccelerometerInitialized) {
					mLastX = x;
					mLastY = y;
					mLastZ = z;

					mAccelerometerInitialized = true;
				} else {
					float speed = Math
							.abs(x + y + z - mLastX - mLastY - mLastZ)
							/ diffTime * 10000;

					if (speed > SHAKE_THRESHOLD) {
						myStartAnimation();
					}

					mLastX = x;
					mLastY = y;
					mLastZ = z;

				}
			}
		}

	}
}
