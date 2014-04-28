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

//Based on:
//http://www.gustekdev.com/2013/05/custom-cursoradapter-and-why-not-use.html
package com.accia77.mockey.adapters;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.data.EntriesDataSource;
import com.accia77.mockey.data.MySQLiteHelper;
import com.accia77.mockey.model.Entry;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ManageEntriesCustomCursorAdapter extends CursorAdapter {

	private LayoutInflater mInflater;

	public ManageEntriesCustomCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 * CursorAdapter won't call the newView each time it needs a new row; if it
	 * already has a View, it will call the bindView, so the created view is
	 * actually reused. Thus, there's no need to use a ViewHolder in this case.
	 * 
	 * private class ViewHolder {
	 * 
	 * ImageView imageView; // TextView txtTitle; TextView txtDesc;
	 * 
	 * }
	 */

	/*
	 * newView method is called to create a View object representing on item in
	 * the list, here You just create an object don't set any values.
	 * 
	 * The View returned from newView is passed as first parameter to bindView,
	 * it is there where You will set values to display.
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// LayoutInflater creates a view based on xml structure in resource file
		// passed to inflate method as first parameter.
		return mInflater.inflate(R.layout.manage_entries_list_row, parent,
				false);
	}

	/*
	 * The View returned from newView is passed as first parameter to bindView,
	 * it is here where You will set values to display.
	 * 
	 * Worth mentioning is that bindView is called for each element when it has
	 * to be displayed but newView is not. Android is reusing view objects of
	 * items that are not visible any more and populate pass them to bindView to
	 * populate with data from cursor to be displayed. Thanks to that ListView
	 * doesn't consume so much memory and works nicely without crating lots of
	 * unnecessary objects.
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final Entry entry = EntriesDataSource.cursorToEntry(cursor);
		
		TextView txtDesc = (TextView) view.findViewById(R.id.desc);
		// holder.txtTitle = (TextView)
		// convertView.findViewById(R.id.title);
		ImageView imageView = (ImageView) view.findViewById(R.id.icon);

		txtDesc.setText(entry.getUserEditedEntry());

		//iconId defaults to ic_action_edit
		int iconId = R.drawable.ic_action_edit;
		// if(entry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_PURE_TEXT)
		// iconId = R.drawable.ic_action_edit;
		if (entry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_AUDIO)
			iconId = R.drawable.ic_action_microphone;
		
		//NOTE: PHOTO and VIDEO not supported yet
		if (entry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_PHOTO)
			iconId = R.drawable.ic_action_photo;
		if (entry.getEntryType() == MySQLiteHelper.ENTRY_TYPE_VIDEO)
			iconId = R.drawable.ic_action_video;
		
		imageView.setImageResource(iconId);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//No playback if the sdcard is not mounted
				if(MyApplication.getInstance().isSdCardMounted(false)) {
					MyApplication.getInstance().playSelection(entry);
				}
			}
		});
	}

}
