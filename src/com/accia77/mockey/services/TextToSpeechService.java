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

package com.accia77.mockey.services;

import java.util.Locale;

import com.accia77.mockey.R;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

public class TextToSpeechService extends Service implements OnInitListener {
	
	private static final String TAG = "TextToSpeechService";
	public static String QUOTE_TO_PLAY = "com.accia77.mockey.QUOTE_TO_PLAY";
	String quote;

	private TextToSpeech tts;
	private boolean m_ttsInitialized;// = false;

	//onCreate: called when the service is NOT running yet
	@Override
	public void onCreate() {
		super.onCreate();
		//Log.d(TAG, "onCreate");
		
		m_ttsInitialized = false;
		tts = new TextToSpeech(this, this);
		
	}

	// Similarly to activities, this method could not be called.
	// In particular, it WON'T be called when it's terminated by Android needing resources
	@Override
	public void onDestroy() {
		//Log.d(TAG, "onDestroy");
		shutdownTTS();
	}

	//onStartCommand: called every time startService is called.
	//Vogella: A service is only started once, no matter how often you call the startService() method.
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		quote = intent.getStringExtra(QUOTE_TO_PLAY);
		
		if(tts == null) {
			m_ttsInitialized = false;
			tts = new TextToSpeech(this, this);
			
			if(!("".equals(quote)))
				Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.tts_init_in_progress),
					Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(!("".equals(quote)))
				speakTTS(quote);
		}
		//Log.d(TAG, "onStartCommand quote=" + quote);
		
		return START_NOT_STICKY;
	}

	@Override
	public void onInit(int code) {
		
		if (code == TextToSpeech.SUCCESS) {
			Locale current = getResources().getConfiguration().locale;
			tts.setLanguage(current);
			//Log.d(TAG, "onInit SUCCESS");

			switch (tts.isLanguageAvailable(current)) {
			case TextToSpeech.LANG_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
				//Log.d(TAG, "onInit SUCCESS OK");
				
				// The language is supported. Let's set it as current language.
				tts.setLanguage(current);
				m_ttsInitialized = true;
				
				if(!("".equals(quote)))
					Toast.makeText(this,
						getResources().getString(R.string.tts_init_done),
						Toast.LENGTH_SHORT).show();
				break;
			case TextToSpeech.LANG_MISSING_DATA:
				// If the language is supported but its data files are missing, let's prompt
				// the user about installing them
				//Log.d(TAG, "onInit SUCCESS LANG_MISSING_DATA");
				
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				m_ttsInitialized = true;
				startActivity(installIntent);			
				
				break;
			case TextToSpeech.LANG_NOT_SUPPORTED:
				// The en_US locale is the fallback one, in case the language is not supported
				//Log.d(TAG, "onInit SUCCESS LANG_NOT_SUPPORTED");
				
				tts.setLanguage(Locale.US);
				
				break;
			}

		} else {
			//Log.d(TAG, "onInit FAILURE");
			tts = null;
			Toast.makeText(this,
					getResources().getString(R.string.tts_init_failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void speakTTS(String text) {
		if(!m_ttsInitialized) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.tts_init_in_progress),
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (tts != null) {
			if (text != null) {
				if (!tts.isSpeaking()) {
					tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		}
			
			
	}

	public void shutdownTTS() {
		//Log.d(TAG, "shutdownTTS");
		if (tts != null) {
			//Log.d(TAG, "Stop the TTS");
			tts.stop();
			tts.shutdown();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
