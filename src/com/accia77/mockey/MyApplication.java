package com.accia77.mockey;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.Toast;

import com.accia77.mockey.data.EntriesDataSource;
import com.accia77.mockey.data.MySQLiteHelper;
import com.accia77.mockey.model.Entry;
import com.accia77.mockey.services.TextToSpeechService;
import com.accia77.mockey.utils.FileUtils;

public class MyApplication extends Application implements
		OnSharedPreferenceChangeListener {
	
	private static final String TAG = "MyApplication";

	// Application object implemented as a singleton
	private static MyApplication singleton;
	public static MyApplication getInstance() {
		return singleton;
	}

	private static Context context;
	
	private String mAppFilesPath = null;
	
	// NOT USED
	private String mFileNamesStartingSequence = "mockey_";
	private String mFullExportFileNamesStartingSequence = mFileNamesStartingSequence
			+ "FULLEXPORT_";
	private String mPartialExportFileNamesStartingSequence = mFileNamesStartingSequence
			+ "PARTEXPORT_";

	// Shared Preferences
	SharedPreferences sharedPrefs;

	// Background image path for the selected quote screen
	String strBackgroundImagePath;

	// Path of the 3 images which compose the animation
	String strAnimPhase0_Path, strAnimPhase1_Path, strAnimPhase2_Path;

	// Max number of characters for pure text quotes
	private static final int PURE_TEXT_ENTRY_MAX_LENGTH = 50;

	// Max audio clip length
	private static final int MAX_LENGTH_AUDIO_CLIP_SECS = 15;
	private static final int MAX_LENGTH_AUDIO_CLIP_MILLIS = MAX_LENGTH_AUDIO_CLIP_SECS * 1000;

	private AudioRecordPlay audioRecordPlayManager;

	

	public String getFileNamesStartingSequence() {
		return mFileNamesStartingSequence;
	}

	public String getFullExportFileNamesStartingSequence() {
		return mFullExportFileNamesStartingSequence;
	}

	public String getPartialExportFileNamesStartingSequence() {
		return mPartialExportFileNamesStartingSequence;
	}

	public int getMaxLengthAudioClipSeconds() {
		return MAX_LENGTH_AUDIO_CLIP_SECS;
	}

	public int getMaxLengthAudioClipMillis() {
		return MAX_LENGTH_AUDIO_CLIP_MILLIS;
	}

	public int getMaxLengthTextEntry() {
		return PURE_TEXT_ENTRY_MAX_LENGTH;
	}

	// The DAO
	public static EntriesDataSource datasource;

	@Override
	public void onCreate() {
		super.onCreate();

		// DB
		datasource = new EntriesDataSource(this);
		datasource.open();

		context = getApplicationContext();

		// NOTE: It is very important to write on the ExternalFilesDir, so that when the app
		// will be uninstalled, the files it created will be automatically removed.
		//
		// At first, though, i used the path returned by getExternalCacheDir which is dedicated
		// to cache files.
		// mAppFilesPath = context.getExternalCacheDir().getAbsolutePath();
		if(isSdCardMounted(true)) {
		mAppFilesPath = context
				.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
				.getAbsolutePath();
		}
		else mAppFilesPath = "";

		// Singleton initialization
		singleton = this;

		// Shared Preferences
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String mainActivityTitle = sharedPrefs.getString("main_activity_title",
				"NONE");
		if (mainActivityTitle.equals("NONE")) {
			Editor editor = sharedPrefs.edit();
			editor.putString("main_activity_title",
					getResources().getString(R.string.app_name));
			editor.commit();
		}
		
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);

		strBackgroundImagePath = sharedPrefs.getString("backgroundImagePath",
				"");

		strAnimPhase0_Path = sharedPrefs.getString("animationPhase0", "");
		strAnimPhase1_Path = sharedPrefs.getString("animationPhase1", "");
		strAnimPhase2_Path = sharedPrefs.getString("animationPhase2", "");

		m_bitmapPapiro = resolveRightBitmap(POSITION_PAPIRO_BACKGROUND);
		m_bitmapPhase = new Bitmap[3];
		m_bitmapPhase[0] = resolveRightBitmap(POSITION_ANIMATION_PHASE_0);
		m_bitmapPhase[1] = resolveRightBitmap(POSITION_ANIMATION_PHASE_1);
		m_bitmapPhase[2] = resolveRightBitmap(POSITION_ANIMATION_PHASE_2);

		

		audioRecordPlayManager = new AudioRecordPlay(context);

		/*
		 * Moved to a service
		 * Intent startTTSServiceIntent = new Intent(this,
		 * TextToSpeechService.class);
		 * 
		 * // Put Logging message in intent
		 * startTTSServiceIntent.putExtra(TextToSpeechService.QUOTE_TO_PLAY,
		 * "");
		 * 
		 * // Start the Service startService(startTTSServiceIntent);
		 */
	}

	public static Context getAppContext() {
		return MyApplication.context;
	}

	public String getAppFilesPath() {
		return mAppFilesPath;
	}

	// //////// SDCARD MANAGEMENT //////////////////

	// Returns true if the sd card is mounted.
	// If not, notifies the user.
	public boolean isSdCardMounted(boolean isApplicationStart) {
		boolean result = (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));

		if (!result) {
			String stringToShow;
			
			if(isApplicationStart) stringToShow = context.getResources().getString(
					R.string.sdcard_not_mounted_on_application_start);
			else stringToShow = context.getResources().getString(
					R.string.sdcard_not_mounted);
			
			Toast.makeText(
					context,
					stringToShow, Toast.LENGTH_LONG)
					.show();
		}

		return result;
	}
	
	/////////// MAIN ACTIVITY'S TITLE ///////
	// Use the custom title, if any. Otherwise, default to "Mockey"
	public String getMainActivityTitle() {
		String fallbackValue = getResources().getString(R.string.app_name);
		String result;
		
		String titleInPreferences = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString("main_activity_title",
				"NONE");
		titleInPreferences = titleInPreferences.trim();
		
		if(("NONE".equals(titleInPreferences)) ||
				("".equals(titleInPreferences))) {
			result = fallbackValue;
		}
		else
			result = titleInPreferences;
		
		return result;
		
	}

	// //////// BITMAP MANAGEMENT ////////////

	// Animation frames
	Bitmap[] m_bitmapPhase;

	//PapiroActivity background Bitmap
	Bitmap m_bitmapPapiro;

	private static final int POSITION_ANIMATION_PHASE_0 = 0;
	private static final int POSITION_ANIMATION_PHASE_1 = 1;
	private static final int POSITION_ANIMATION_PHASE_2 = 2;
	private static final int POSITION_PAPIRO_BACKGROUND = 3;

	public int getPositionPapiroBackground() {
		return POSITION_PAPIRO_BACKGROUND;
	}

	public int getPositionAnimationPhase0() {
		return POSITION_ANIMATION_PHASE_0;
	}

	public int getPositionAnimationPhase1() {
		return POSITION_ANIMATION_PHASE_1;
	}

	public int getPositionAnimationPhase2() {
		return POSITION_ANIMATION_PHASE_2;
	}

	private String getAnimationPhase0_Path() {
		return strAnimPhase0_Path;
	}

	private String getAnimationPhase1_Path() {
		return strAnimPhase1_Path;
	}

	private String getAnimationPhase2_Path() {
		return strAnimPhase2_Path;
	}

	private String getBackgroundImagePath() {
		return strBackgroundImagePath;
	}

	public boolean isDefaultPapiroLoaded() {
		return bDefaultPapiroLoaded;
	}

	// Thumbnail size
	private final int TARGET_DISPLAY_WIDTH = 600;
	private final int TARGET_DISPLAY_HEIGHT = 400;

	private boolean bDefaultPapiroLoaded = false;

	private Bitmap getBitmapPhase(int phase) {
		return m_bitmapPhase[phase];
	}

	public Bitmap getBitmapPapiro() {
		return m_bitmapPapiro;
	}

	public Bitmap getBackgroundBitmap(int position) {
		if (position == POSITION_PAPIRO_BACKGROUND)
			return m_bitmapPapiro;
		else
			return getBitmapPhase(position);
	}

	// Given the position (0, 1 o 2) and the appropriate fallback resource, returns the
	// Bitmap to load in the Gallery in PickBackgroundImagesActivity 
	private Bitmap resolveRightBitmap(int position) {
		// decode the placeholder image
		String pathAnim = "";
		Bitmap resultBitmap;
		int fallbackResourceId = 0;

		switch (position) {
		case POSITION_PAPIRO_BACKGROUND:
			// Sfondo del PapiroFragment
			pathAnim = getBackgroundImagePath();
			fallbackResourceId = R.drawable.papiro;

			// In PapiroActivity i need to know if the user has chosen a custom background or not,
			// in order to decide whether to show the quote in a TextView or not.
			bDefaultPapiroLoaded = ("".equals(pathAnim));

			break;
		case POSITION_ANIMATION_PHASE_0:
			// First animation frame
			pathAnim = getAnimationPhase0_Path();
			fallbackResourceId = R.drawable.phase0;
			break;
		case POSITION_ANIMATION_PHASE_1:
			// Second animation frame
			pathAnim = getAnimationPhase1_Path();
			fallbackResourceId = R.drawable.phase1;
			break;
		case POSITION_ANIMATION_PHASE_2:
			// Third animation frame
			pathAnim = getAnimationPhase2_Path();
			fallbackResourceId = R.drawable.phase2;
			break;
		}

		if ("".equals(pathAnim)) {
			resultBitmap = decodeSampledBitmapFromResource(getResources(),
					fallbackResourceId, TARGET_DISPLAY_WIDTH,
					TARGET_DISPLAY_HEIGHT);
		} else {
			Uri uri = Uri.parse(pathAnim);

			String path = getRealPathFromURI(uri);

			if (path == null) {
				resultBitmap = decodeSampledBitmapFromResource(getResources(),
						fallbackResourceId, TARGET_DISPLAY_WIDTH,
						TARGET_DISPLAY_HEIGHT);
			} else {
				resultBitmap = decodeSampledBitmapFromFile(path,
						TARGET_DISPLAY_WIDTH, TARGET_DISPLAY_HEIGHT);
				if (resultBitmap == null) {
					resultBitmap = decodeSampledBitmapFromResource(
							getResources(), fallbackResourceId,
							TARGET_DISPLAY_WIDTH, TARGET_DISPLAY_HEIGHT);
				}
			}
		}

		return resultBitmap;
	}

	public int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public String getRealPathFromURI(Uri contentUri) {
		return FileUtils.getPath(this, contentUri);
		/*
		String[] proj = { MediaStore.Images.Media.DATA };

		// This method was deprecated in API level 11
		// Cursor cursor = managedQuery(contentUri, proj, null, null, null);

		CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj,
				null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		if (cursor.getCount() > 0)
			return cursor.getString(column_index);
		else
			return null;
			*/
	}

	// Solution taken from
	// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	// to handle big bitmaps loading
	public Bitmap decodeSampledBitmapFromFile(String path, int reqWidth,
			int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	// /////// DB MANAGEMENT ///////////////////
	public void AddNewEntry(long entryType, String actualEntry,
			String userEditedEntry) {
		//datasource.open();
		datasource.createEntry(entryType, actualEntry, userEditedEntry);
		//datasource.close();
	}

	public void AddNewEntries(List<String> entries) {
		long entryType;
		String actualEntry;
		String userEditedEntry;

		//datasource.open();
		for (String str : entries) {
			List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
			entryType = Long.parseLong(items.get(0));
			actualEntry = items.get(1);
			userEditedEntry = items.get(2);
			datasource.createEntry(entryType, actualEntry, userEditedEntry);
		}
		//datasource.close();
	}

	public Cursor getAllEntries() {
		//datasource.openForReading();
		Cursor cursor = datasource.getAllEntries();
		// datasource.close();
		return cursor;
	}

	public Cursor getEntriesByName(String inputText) {
		//datasource.openForReading();
		Cursor cursor = datasource.getEntriesByName(inputText);
		// datasource.close();
		return cursor;
	}

	public Entry getRandomEntry() {
		//datasource.open();
		Entry entry = datasource.getRandomEntry();
		//datasource.close();
		return entry;
	}

	public int deleteAllEntries() {
		int rowsDeleted = 0;
		
		//datasource.open();
		rowsDeleted = datasource.deleteAllEntries();
		//datasource.close();
		
		return rowsDeleted;
	}

	public int deleteEntry(long entryId) {
		int rowsDeleted = 0;
		
		//datasource.open();
		rowsDeleted = datasource.deleteEntry(entryId);
		//datasource.close();
		
		return rowsDeleted;
	}

	public int updateDescription(long entryId, long entryType,
			String newDescription) {
		int rowsAffected = 0;

		//datasource.open();
		rowsAffected = datasource.updateDescription(entryId, entryType,
				newDescription);
		//datasource.close();

		return rowsAffected;
	}

	// ////////////// Playback ///////////////////////////////
	public void playSelection(Entry selectedEntry) {
		String selectedString = selectedEntry.getActualEntry();

		// Log.d(TAG,"playSelection:" + selectedString);

		if (selectedEntry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_AUDIO)
			audioRecordPlayManager.startPlaying(getAppFilesPath()
					+ File.separator + selectedString);
		else {
			// Create an Intent for starting the LoggingService
			Intent startTTSServiceIntent = new Intent(this,
					TextToSpeechService.class);

			// Put Logging message in intent
			startTTSServiceIntent.putExtra(TextToSpeechService.QUOTE_TO_PLAY,
					selectedString);

			// Start the Service
			// NOTE: This is a non-blocking call
			startService(startTTSServiceIntent);

		}
	}

	public AudioRecordPlay getAudioRecordPlayManager() {
		return audioRecordPlayManager;
	}

	// ///////////// Shared Preferences management //////////////////
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("backgroundImagePath")) {
			String temp;
			temp = sharedPrefs.getString("backgroundImagePath", "");

			if (!temp.equals(strBackgroundImagePath)) {
				strBackgroundImagePath = temp;
				m_bitmapPapiro = resolveRightBitmap(POSITION_PAPIRO_BACKGROUND);
			}
		}

		if (key.equals("animationPhase0")) {
			String temp;
			temp = sharedPrefs.getString("animationPhase0", "");

			if (!temp.equals(strAnimPhase0_Path)) {
				strAnimPhase0_Path = temp;
				m_bitmapPhase[POSITION_ANIMATION_PHASE_0] = resolveRightBitmap(POSITION_ANIMATION_PHASE_0);
			}
		}

		if (key.equals("animationPhase1")) {
			String temp;
			temp = sharedPrefs.getString("animationPhase1", "");

			if (!temp.equals(strAnimPhase1_Path)) {
				strAnimPhase1_Path = temp;
				m_bitmapPhase[POSITION_ANIMATION_PHASE_1] = resolveRightBitmap(POSITION_ANIMATION_PHASE_1);
			}
		}

		if (key.equals("animationPhase2")) {
			String temp;
			temp = sharedPrefs.getString("animationPhase2", "");

			if (!temp.equals(strAnimPhase2_Path)) {
				strAnimPhase2_Path = temp;
				m_bitmapPhase[POSITION_ANIMATION_PHASE_2] = resolveRightBitmap(POSITION_ANIMATION_PHASE_2);
			}
		}
		
		if (key.equals("main_activity_title")) {
			Toast.makeText(this, getResources().getString(R.string.main_activity_title_changed), Toast.LENGTH_SHORT).show();
		}

	}

	// //////////// InputFilter for pure text entries (NOT USED) //////////////
	// I originally wanted to filter user text input, to accept only alphanumeric input + spaces

	// The solution i adopted (but that i have dropped) is from:
	// http://stackoverflow.com/questions/3349121/how-do-i-use-inputfilter-to-limit-characters-in-an-edittext-in-android
	//
	// Łukasz Sromek (the user who contributed it) said:
	// "InputFilters are a little complicated in Android versions that display
	// dictionary suggestions.
	// You sometimes get a SpannableStringBuilder, sometimes a plain String in
	// the source parameter."

	private InputFilter filter = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			if (source instanceof SpannableStringBuilder) {
				SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
				for (int i = end - 1; i >= start; i--) {
					char currentChar = source.charAt(i);
					// pretty ugly here...
					if (!Character.isLetterOrDigit(currentChar)
							&& !Character.isSpaceChar(currentChar)
							&& currentChar != '\''
							&& currentChar != '!'
							&& currentChar != '?'
							&& currentChar != '.'
							&& currentChar != ',') {
						sourceAsSpannableBuilder.delete(i, i + 1);
					}
				}
				return source;
			} else {
				StringBuilder filteredStringBuilder = new StringBuilder();
				for (int i = start; i < end; i++) {
					char currentChar = source.charAt(i);
					// ewww
					if (Character.isLetterOrDigit(currentChar)
							|| Character.isSpaceChar(currentChar)
							|| currentChar == '\''
							|| currentChar == '!'
							|| currentChar == '?'
							|| currentChar == '.'
							|| currentChar == ',') {
						filteredStringBuilder.append(currentChar);
					}
				}
				return filteredStringBuilder.toString();
			}
		}
	};

	// The complete filter is a result of filtering out unwanted characters and limiting
	// the text's max length.
	private InputFilter[] pureTextEntryInputFilter = new InputFilter[] {
			filter, new InputFilter.LengthFilter(PURE_TEXT_ENTRY_MAX_LENGTH) };

	public InputFilter[] getPureTextEntryInputFilter() {
		return pureTextEntryInputFilter;
	}

}
