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

//Heavily based on:
//http://www.androidhive.info/2013/11/android-sliding-menu-using-navigation-drawer/
package com.accia77.mockey.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.accia77.mockey.R;
import com.accia77.mockey.adapters.NavDrawerListAdapter;
import com.accia77.mockey.model.NavDrawerItem;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	// NavigationDrawer's items indexes
	private final int IDX_NAVDRAWER_HOME = 0;
	private final int IDX_NAVDRAWER_ADD_QUOTE = 1;
	private final int IDX_NAVDRAWER_MANAGE_QUOTES = 2;
	private final int IDX_NAVDRAWER_SETTINGS = 3;
	private final int IDX_NAVDRAWER_HELP = 4;
	
	SharedPreferences sharedPrefs;
	boolean bAutoLoadHelpScreen;
	int iAppVersion;
	
	boolean m_doShowAddNewQuoteAction;
	boolean m_bDoShowDeleteAllEntries;
	
	ManageEntriesFragment manageEntriesFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[IDX_NAVDRAWER_HOME],
				navMenuIcons.getResourceId(IDX_NAVDRAWER_HOME, -1)));
		
		// Add new quote
		navDrawerItems.add(new NavDrawerItem(
				navMenuTitles[IDX_NAVDRAWER_ADD_QUOTE], navMenuIcons
						.getResourceId(IDX_NAVDRAWER_ADD_QUOTE, -1)));
		// Manage quotes
		navDrawerItems.add(new NavDrawerItem(
				navMenuTitles[IDX_NAVDRAWER_MANAGE_QUOTES], navMenuIcons
						.getResourceId(IDX_NAVDRAWER_MANAGE_QUOTES, -1)));
		// Options
		navDrawerItems.add(new NavDrawerItem(
				navMenuTitles[IDX_NAVDRAWER_SETTINGS], navMenuIcons
						.getResourceId(IDX_NAVDRAWER_SETTINGS, -1)));
		// Help
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[IDX_NAVDRAWER_HELP],
				navMenuIcons.getResourceId(IDX_NAVDRAWER_HELP, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				
				// calling onPrepareOptionsMenu() to show action bar icons
				// invalidateOptionsMenu();
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				// Close soft keyboard
				InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this
						.getSystemService(Activity.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(MainActivity.this
						.getCurrentFocus().getWindowToken(), 0);

				getSupportActionBar().setTitle(mDrawerTitle);
				
				// calling onPrepareOptionsMenu() to hide action bar icons
				// invalidateOptionsMenu();
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// If this is the first start of the app or of a new update, show the help screen 
			sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			
			bAutoLoadHelpScreen = sharedPrefs
					.getBoolean("autoLoadHelpScreen", true);
			
			iAppVersion = sharedPrefs
					.getInt("appVersion", 0);
			
			//TODO: a better way to check for the app version is definitely needed here...
			if (bAutoLoadHelpScreen || iAppVersion != 6) {
				displayView(IDX_NAVDRAWER_HELP);
				
				// Set the autoLoadHelpScreen and appVersion to the current values
				// in order to avoid showing the help screen all the times
				Editor editor = sharedPrefs.edit();
				editor.putBoolean("autoLoadHelpScreen", false);
				editor.putInt("appVersion", 6);
				editor.commit();
			}
			else 
				displayView(IDX_NAVDRAWER_HOME);
		}
		
		m_doShowAddNewQuoteAction = true;
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main, menu);	
		return (super.onCreateOptionsMenu(menu));
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		
		// Code not working with ActionbarSherlock
		// if (mDrawerToggle.onOptionsItemSelected(item)) {
		// return true;
		// }
		
		if (item.getItemId() == android.R.id.home) {

			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}

		switch (item.getItemId()) {
		case R.id.action_new:
			displayView(IDX_NAVDRAWER_ADD_QUOTE);
			supportInvalidateOptionsMenu();
			break;
		case R.id.action_delete_all_entries:
			manageEntriesFragment.HandleDeleteAllEntries();
			break;
		}
		return (super.onOptionsItemSelected(item));
	}

	/***
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If nav drawer is opened, hide all the action items
		// otherwise, show them only when it makes sense.
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		
		if(drawerOpen)
		{
			menu.findItem(R.id.action_new).setVisible(false);
			menu.findItem(R.id.action_delete_all_entries).setVisible(false);

		}
		else
		{
			menu.findItem(R.id.action_new).setVisible(m_doShowAddNewQuoteAction);
			menu.findItem(R.id.action_delete_all_entries).setVisible(m_bDoShowDeleteAllEntries);
		}
				
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		m_doShowAddNewQuoteAction = true;
		m_bDoShowDeleteAllEntries = false;
		switch (position) {
		case IDX_NAVDRAWER_HOME:
			fragment = new MonkeyFragment();
			break;
		case IDX_NAVDRAWER_ADD_QUOTE:
			fragment = new AddNewEntryFragment();
			m_doShowAddNewQuoteAction = false;
			break;
		case IDX_NAVDRAWER_MANAGE_QUOTES:
			fragment = manageEntriesFragment = new ManageEntriesFragment();
			m_bDoShowDeleteAllEntries = true;
			break;
		case IDX_NAVDRAWER_SETTINGS:
			startActivity(new Intent(this, SettingsActivity.class));
			return;
		case IDX_NAVDRAWER_HELP:
			fragment = new HelpFragment();
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else { // error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

}
