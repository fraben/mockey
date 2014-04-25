package com.accia77.mockey.data;

import java.io.File;
import java.io.IOException;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.model.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/*
In Android, a Cursor represents the entire result set of the query — all the rows and all 
the columns that the query returned. 
As such, a Cursor can be quite the memory hog. Please close() the Cursor when
you are done with it, to free up the heap space it consumes and make that memory
available to the rest of your application.

With the Cursor, you can:
1. Find out how many rows are in the result set via getCount()
2. Iterate over the rows via moveToFirst(), moveToNext(), and isAfterLast()
3. Find out the names of the columns via getColumnNames(), convert those into
column numbers via getColumnIndex(), and get values for the current row
for a given column via methods like getString(), getInt(), etc.
 */
public class EntriesDataSource {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private Context context;

	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_ENTRY_TYPE,
			MySQLiteHelper.COLUMN_ACTUAL_ENTRY,
			MySQLiteHelper.COLUMN_USER_EDITED_ENTRY };

	public EntriesDataSource(Context context) {
		this.context = context;
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void openForReading() throws SQLException {
		database = dbHelper.getReadableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void createEntry(long entryType, String actualEntry,
			String userEditedEntry) {
		ContentValues values = new ContentValues();

		// String userEditedEntry = actualEntry;
		int intEntryType = (int) entryType;

		// If a user edited entry is not specified, it defaults to the kind of file
		// or to the initial string set by the user
		if (userEditedEntry == null) {
			switch (intEntryType) {
			case (int) MySQLiteHelper.ENTRY_TYPE_AUDIO:
				userEditedEntry = "AUDIO";
				break;

			case (int) MySQLiteHelper.ENTRY_TYPE_PHOTO:
				userEditedEntry = "PHOTO";
				break;

			case (int) MySQLiteHelper.ENTRY_TYPE_VIDEO:
				userEditedEntry = "VIDEO";
				break;

			default:
				userEditedEntry = actualEntry;
				break;
			}
		}

		values.put(MySQLiteHelper.COLUMN_ENTRY_TYPE, entryType);
		values.put(MySQLiteHelper.COLUMN_ACTUAL_ENTRY, actualEntry);
		values.put(MySQLiteHelper.COLUMN_USER_EDITED_ENTRY, userEditedEntry);

		database.insert(MySQLiteHelper.TABLE_ENTRIES, null,
				values);
	}

	// Returns true if it's an entry with an associated file (i.e. it's not a pure text entry)
	private boolean isEntryWithPhysicalFile(long entryType) {
		return (entryType == MySQLiteHelper.ENTRY_TYPE_AUDIO)
				|| (entryType == MySQLiteHelper.ENTRY_TYPE_PHOTO)
				|| (entryType == MySQLiteHelper.ENTRY_TYPE_VIDEO);
	}

	public int deleteEntry(long id) {
		Cursor cursor = null;
		boolean deleted = false;
		int rowsDeleted = 0;

		try {

			cursor = database.rawQuery("SELECT * FROM "
					+ MySQLiteHelper.TABLE_ENTRIES + " WHERE "
					+ MySQLiteHelper.COLUMN_ID + "=?",
					new String[] { String.valueOf(id) });

			// If the entry has an associated file, let's delete that file first
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				long entryType = cursor.getLong(cursor
						.getColumnIndex(MySQLiteHelper.COLUMN_ENTRY_TYPE));
				String fileName = null;

				if (isEntryWithPhysicalFile(entryType)) {
					fileName = cursor
							.getString(cursor
									.getColumnIndex(MySQLiteHelper.COLUMN_ACTUAL_ENTRY));
					File file = new File(MyApplication.getInstance()
							.getAppFilesPath() + File.separator + fileName);
					deleted = file.delete();
				} else
					deleted = true;
			}
		} finally {
			cursor.close();
		}

		// Now let's delete the entry from the database
		if (deleted) {
			rowsDeleted = database.delete(MySQLiteHelper.TABLE_ENTRIES,
					MySQLiteHelper.COLUMN_ID + " = " + id, null);
		}
		
		return rowsDeleted;
	}

	public int updateDescription(long id, long entryType, String newDescription) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_USER_EDITED_ENTRY, newDescription);
		
		// If it's a text-only entry, also update the actual entry
		if(entryType == MySQLiteHelper.ENTRY_TYPE_PURE_TEXT)
			values.put(MySQLiteHelper.COLUMN_ACTUAL_ENTRY, newDescription);
		
		return database.update(MySQLiteHelper.TABLE_ENTRIES, values,
				MySQLiteHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
	}

	public int deleteAllEntries() {	
		int rowsDeleted = 0;
		
		// Delete all text-only entries 
		rowsDeleted = database.delete(MySQLiteHelper.TABLE_ENTRIES,
				MySQLiteHelper.COLUMN_ENTRY_TYPE + " = '"
						+ MySQLiteHelper.ENTRY_TYPE_PURE_TEXT + "'", null);

		// Now remove all audio files
		Cursor cursor = null;
		boolean deleted = false;
		try {

			cursor = database.rawQuery("SELECT * FROM "
					+ MySQLiteHelper.TABLE_ENTRIES, null);

			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {

				long entryType = cursor.getLong(cursor
						.getColumnIndex(MySQLiteHelper.COLUMN_ENTRY_TYPE));
				String fileName = null;

				if (isEntryWithPhysicalFile(entryType)) {
					fileName = cursor
							.getString(cursor
									.getColumnIndex(MySQLiteHelper.COLUMN_ACTUAL_ENTRY));
					File file = new File(MyApplication.getInstance()
							.getAppFilesPath() + File.separator + fileName);
					deleted = file.delete();
					cursor.moveToNext();
				} else
					deleted = true;
			}
		} finally {
			cursor.close();
		}

		// Removes all the remaining rows
		rowsDeleted = rowsDeleted + database.delete(MySQLiteHelper.TABLE_ENTRIES, null, null);

		return rowsDeleted;
	}

	public Cursor getAllEntries() {
		return database.query(MySQLiteHelper.TABLE_ENTRIES,
				allColumns, null, null, null, null, MySQLiteHelper.COLUMN_USER_EDITED_ENTRY + " Collate NOCASE");
	}
	
	public Cursor getEntriesByName(String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			mCursor = database.query(MySQLiteHelper.TABLE_ENTRIES,
					allColumns, null, null, null, null, MySQLiteHelper.COLUMN_USER_EDITED_ENTRY + " Collate NOCASE");

		} else {
			mCursor = database.query(true, MySQLiteHelper.TABLE_ENTRIES, allColumns, 
					MySQLiteHelper.COLUMN_USER_EDITED_ENTRY + " like '" + inputText + "%'", null, null, null, null,
					null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public Entry getRandomEntry() {
		Entry entry = new Entry();

		// Initializes the entry with the message shown when no entries are present
		Resources myRes = context.getResources();
		String warning = myRes.getString(R.string.zero_entries_warning);
		entry.setActualEntry(warning);
		entry.setUserEditedEntry(warning);
		entry.setEntryType(MySQLiteHelper.ENTRY_TYPE_PURE_TEXT);
		entry.setId(-1);

		// TODO: I honestly forgot i did this ugly thing here... 
		// Needs to be fixed, i'm querying on the main thread
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ENTRIES,
				allColumns, null, null, null, null, null);

		// Random choice
		if (cursor.getCount() > 0) {
			cursor.moveToPosition((int) (Math.random() * cursor.getCount()));
			entry = cursorToEntry(cursor);
		}

		cursor.close();

		return entry;
	}

	// Gets the record from the cursor
	public static Entry cursorToEntry(Cursor cursor) {
		Entry entry = new Entry();
		entry.setId(cursor.getLong(0));
		entry.setEntryType(cursor.getLong(1));
		entry.setActualEntry(cursor.getString(2));
		entry.setUserEditedEntry(cursor.getString(3));
		return entry;
	}

	

}
