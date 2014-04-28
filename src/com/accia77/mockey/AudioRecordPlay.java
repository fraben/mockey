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

package com.accia77.mockey;

import java.io.File;
import java.io.IOException;

import com.accia77.mockey.R;


import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.text.format.Time;
import android.widget.Toast;

public class AudioRecordPlay {
	private static final String LOG_TAG = "AudioRecordPlay";
	private String mFileNameFullPath = null;
	private String mFileNameOnly = null;
	private String mRecordingPath = null;

	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	
	private  Context context;
	
	private  AudioManager mAudioManager;
	
	public AudioRecordPlay(Context context)
	{
		this.context = context;
	}

	//Returns just the file name, NOT the full path
	public  String getFileNameOnly() {
		return mFileNameOnly;
	}
	
	public  String getRecordingPath() {
		return mRecordingPath;
	}

	private  void prepareFileName() {
		mRecordingPath = MyApplication.getInstance().getAppFilesPath();
				
		Time now = new Time();
		now.setToNow();
		mFileNameOnly = MyApplication.getInstance().getFileNamesStartingSequence() + now.format2445() + ".3gp";
		mFileNameFullPath = mRecordingPath + File.separator + mFileNameOnly;
	}

	public  void startPlaying(String fileName) {
		if (mPlayer != null)
			return;

		// Request audio focus
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			
		mPlayer = new MediaPlayer();
		mFileNameFullPath = fileName;
		try {
			mPlayer.setDataSource(mFileNameFullPath);
			mPlayer.prepare();
			mPlayer.start();
			
			// At the end of the playback, release the MediaPlayer
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					releaseMediaPlayer();
				}
			});
		} catch (IOException e) {
			Toast.makeText(context, 
					context.getResources().getString(R.string.playback_failed), 
					Toast.LENGTH_SHORT).show();
		}
	}
	
	// Listen for Audio focus changes
	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				mAudioManager.abandonAudioFocus(afChangeListener);
				if (mPlayer.isPlaying()) 
					stopPlaying();
			}
		}
	};

	private  void releaseMediaPlayer() {
		if (mPlayer != null) {
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	public  void stopPlaying() {
		if (mPlayer != null) mPlayer.stop();
		releaseMediaPlayer();
	}

	public  void startRecording() {
		if (mRecorder != null)
			return;
		
		int massimaDurataAudioClipMillis = MyApplication.getInstance().getMaxLengthAudioClipMillis();
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setMaxDuration(massimaDurataAudioClipMillis + 1000);//Max audio clip length
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		prepareFileName();
		mRecorder.setOutputFile(mFileNameFullPath);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			//Log.e(LOG_TAG, "prepare() failed");
			Toast.makeText(context, context.getResources().getString(R.string.recording_prepare_failed), Toast.LENGTH_SHORT);
			return;
		}

		mRecorder.start();
	}

	public  void stopRecording() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
		}
	}

	public  boolean ImRecording() {
		return (mRecorder != null);
	}
	
	 

}
