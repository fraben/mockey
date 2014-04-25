package com.accia77.mockey.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Toast;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.R.id;
import com.accia77.mockey.R.layout;
import com.accia77.mockey.R.menu;
import com.accia77.mockey.R.string;
import com.accia77.mockey.adapters.ManageEntriesCustomCursorAdapter;
import com.accia77.mockey.data.EntriesDataSource;
import com.accia77.mockey.model.Entry;
import com.accia77.mockey.zip.Decompress;
import com.actionbarsherlock.app.SherlockListFragment;

public class ManageEntriesFragment extends SherlockListFragment {
	
	private static final String TAG = "ManageEntriesFragment";

	ProgressDialog progressDialog;

	ListView listView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_manage_entries, container,
				false);

		new LoadCursorTask().execute();

		// Hide the progress spin
		getActivity().setProgressBarIndeterminateVisibility(false);

		// Set the volume control stream
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Add the context menu to the listview
		ListView theList = (ListView) getView().findViewById(android.R.id.list);// getListView();
		registerForContextMenu(theList);

		// Show the context menu also with a simple click on an item of the list
		// Requiring a long click is pretty annoying
		theList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.showContextMenu();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public void onResume() {
		// "Wake-up" Text-to-speech
		MyApplication.getInstance().playSelection(new Entry());

		refreshListView();
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		MyApplication.getInstance().getAudioRecordPlayManager().stopPlaying();

	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// Get the current selected entry
		final Entry selectedEntry = EntriesDataSource
				.cursorToEntry(((ManageEntriesCustomCursorAdapter) getListView()
						.getAdapter()).getCursor());

		switch (item.getItemId()) {
		case R.id.context_menu_edit:
			// Lets the user modify the description of the selected item
			// For text-only items, it effectively modifies the string that will
			// be read by the tts engine.
			// For other kinds of items it will only modify the description
			final EditText txtInputUtente = new EditText(getActivity());
			txtInputUtente.append(selectedEntry.getUserEditedEntry());
			
			// I used to set an input filter here, but i removed since it gave some weird problems
			// txtInputUtente.setFilters(MyApplication.getInstance()
			// .getPureTextEntryInputFilter());

			// Show a confirmation "message box": in Android there is nothing predefined
			// for that purpose.
			// AlertDialog provides the Builder, which, using chaining of calls, makes possible to
			// build the dialog attaching the desired features, one by one.
			// 
			// NOTE: setView sets the View to show inside the AlertDialog
			Resources myRes = getResources();
			new AlertDialog.Builder(getActivity())
					.setTitle(myRes.getString(R.string.title_user_edit_note))
					.setMessage(
							myRes.getString(R.string.message_user_edit_note))
					.setView(txtInputUtente)
					.setPositiveButton(myRes.getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String inputUtente = txtInputUtente
											.getText().toString();

									if (!("").equals(inputUtente)) {
										MyApplication
												.getInstance()
												.updateDescription(
														selectedEntry.getId(),
														selectedEntry
																.getEntryType(),
														inputUtente);

										refreshListView();
									}
								}
							})
					.setNegativeButton(myRes.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
			return true;
		case R.id.context_menu_delete:
			// Exit immediately if the sd card is not mounted
			if (MyApplication.getInstance().isSdCardMounted(false)) {
				int rowsDeleted = MyApplication.getInstance().deleteEntry(
						selectedEntry.getId());

				if (rowsDeleted > 0) {
					refreshListView();
					resetSearchFilter();
				}
			}
			return true;

		case R.id.context_menu_play:
			// No playback if the sd card is not mounted
			if (MyApplication.getInstance().isSdCardMounted(false)) {
				MyApplication.getInstance().playSelection(selectedEntry);
			}

			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private void refreshListView() {
		new LoadCursorTask().execute();
	}

	void resetSearchFilter() {
		EditText filterText = (EditText) getView().findViewById(R.id.searchFilter);
		filterText.setText("");
	}

	// Handles the deletion of every entries from the db
	public void HandleDeleteAllEntries() {
		// Exit immediately if the sd card is not mounted
		if (!MyApplication.getInstance().isSdCardMounted(false))
			return;

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());

		// set title
		alertDialogBuilder.setTitle(getResources().getString(
				R.string.alert_deleteallentries_confirm_request));

		// set dialog message
		alertDialogBuilder.setMessage(getResources().getString(
				R.string.alert_deleteallentries_title));
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						int rowsDeleted = MyApplication.getInstance()
								.deleteAllEntries();

						if (rowsDeleted > 0) {
							refreshListView();
							resetSearchFilter();
						}
					}
				});
		alertDialogBuilder.setNegativeButton(android.R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	// TODO: Not used right now. Will use when export/import of the entries is re-introduced.
	void sendEmailWithAttachment(String attachmentFullPath) {
		try {
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			Uri uri = Uri.fromFile(new File(attachmentFullPath));
			
			// Attach a binary file
			// Solution taken from:
			// http://developer.android.com/training/sharing/send.html#send-binary-content
			intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getResources().getString(R.string.email_default_subject));
			intent.putExtra(android.content.Intent.EXTRA_TEXT, getResources()
					.getString(R.string.email_default_message));
			startActivity(Intent.createChooser(intent, getResources()
					.getString(R.string.email_notice_to_user)));
		} catch (Throwable t) {
			Toast.makeText(
					getActivity(),
					getResources().getString(R.string.email_sending_failed)
							+ t.toString(), Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		((ManageEntriesCustomCursorAdapter) getListAdapter()).getCursor()
				.close();
	}

	private class LoadCursorTask extends AsyncTask<Void, Void, Void> {
		private Cursor entriesCursor = null;

		@Override
		protected Void doInBackground(Void... params) {
			entriesCursor = MyApplication.getInstance().getAllEntries();
			entriesCursor.getCount();

			return (null);
		}

		@Override
		public void onPostExecute(Void arg0) {
			final ManageEntriesCustomCursorAdapter adapter;

			adapter = new ManageEntriesCustomCursorAdapter(getActivity()
					.getApplicationContext(), entriesCursor, 0);

			setListAdapter(adapter);

			// Search EditText
			EditText myFilter = (EditText) getView().findViewById(R.id.searchFilter);
			myFilter.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// Sets the filter for the search
					adapter.getFilter().filter(s.toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});

			adapter.setFilterQueryProvider(new FilterQueryProvider() {

				@Override
				public Cursor runQuery(CharSequence constraint) {
					return MyApplication.getInstance().getEntriesByName(
							constraint.toString());
				}
			});

		}
	}

}
