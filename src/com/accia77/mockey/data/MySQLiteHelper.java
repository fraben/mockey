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

package com.accia77.mockey.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
	Android uses the SQLiteOpenHelper class to help with the management of the database schema.
	SQLiteOpenHelper wraps up the logic to create and upgrade a database, per your
	specifications, as needed by your application. You will need to create a custom
	subclass of SQLiteOpenHelper, implementing three methods at minimum:

	1. 	The constructor, chaining upward to the SQLiteOpenHelper constructor. This
		takes the Context (e.g., an Activity), the name of the database, an optional
		cursor factory (typically, just pass null), and an integer representing the
		version of the database schema you are using (typically start at 1 and
		increment from there).
	2. 	onCreate(), called when there is no database and your app needs one, which
		passes you a SQLiteDatabase object, pointing at a newly-created database,
		that you use to populate with tables and initial data, as appropriate.
	3. 	onUpgrade(), called when the schema version you are seeking does not
		match the schema version of the database, which passes you a
		SQLiteDatabase object and the old and new version numbers, so you can
		figure out how best to convert the database from the old schema to the new
		one.
  
	There are two other methods you can elect to override in your SQLiteOpenHelper, if
	you feel the need:
	• 	You can override onOpen(), to get control when somebody opens this
		database. Usually, this is not required.
	• 	Android 3.0 introduced onDowngrade(), which will be called if the code
		requests an older schema than what is in the database presently. This is the
		converse of onUpgrade() — if your version numbers differ, one of these two
		methods will be invoked. Since normally you are moving forward with
		updates, you can usually skip onDowngrade().
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
	
	// Table name
	public static final String TABLE_ENTRIES = "Entries";
	
	// Columns names
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ENTRY_TYPE = "entryType";
	public static final String COLUMN_ACTUAL_ENTRY = "actualEntry";
	public static final String COLUMN_USER_EDITED_ENTRY = "userEditedEntry";
	
	// Entry type
	public static final long ENTRY_TYPE_PURE_TEXT = 1;
	public static final long ENTRY_TYPE_AUDIO = 2;
	public static final long ENTRY_TYPE_PHOTO = 3;
	public static final long ENTRY_TYPE_VIDEO = 4;
	public static final long ENTRY_TYPE_OTHER = 99;
	
			
	// Database name
	public static final String DATABASE_NAME = "allEntries.db";
	
	// Database version
	public static final int DATABASE_VERSION = 2;
	
	// DB creation query
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_ENTRIES + "(" 
			+ COLUMN_ID	+ " integer primary key autoincrement, " 
			+ COLUMN_ENTRY_TYPE + " integer not null, "
			+ COLUMN_ACTUAL_ENTRY + " text not null, "
			+ COLUMN_USER_EDITED_ENTRY + " text not null"
			+ ");";
	
	private Context context;
	
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
		onCreate(db);
	}
	
	public static boolean IsAudioFile(long entryType)
	{
		return (entryType == ENTRY_TYPE_AUDIO);
	}
	
	

}
