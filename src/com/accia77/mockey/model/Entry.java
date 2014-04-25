package com.accia77.mockey.model;

import com.accia77.mockey.data.MySQLiteHelper;

import android.os.Parcel;
import android.os.Parcelable;

// Model class for the Entry db table
public class Entry implements Parcelable {
	private long id;
	private String actualEntry;
	private long entryType;
	private String userEditedEntry;

	public Entry() {
		id = 0;
		actualEntry = "";
		entryType = MySQLiteHelper.ENTRY_TYPE_PURE_TEXT;
		userEditedEntry = "";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getActualEntry() {
		return actualEntry;
	}

	public void setActualEntry(String actualEntry) {
		this.actualEntry = actualEntry;
	}

	public String getUserEditedEntry() {
		return userEditedEntry;
	}

	public void setUserEditedEntry(String userEditedEntry) {
		this.userEditedEntry = userEditedEntry;
	}

	public long getEntryType() {
		return entryType;
	}

	public void setEntryType(long entryType) {
		this.entryType = entryType;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return userEditedEntry;
	}

	public String toCSVString() {
		return entryType + "," + actualEntry + "," + userEditedEntry;
	}

	// ---------------------- Methods for Parcelable interface
	// taken from:
	// http://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents

	// 99.9% of the time you can just ignore this
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(id);
		out.writeString(actualEntry);
		out.writeLong(entryType);
		out.writeString(userEditedEntry);
	}

	// this is used to regenerate your object. All Parcelables must have a
	// CREATOR that implements these two methods
	public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
		public Entry createFromParcel(Parcel in) {
			return new Entry(in);
		}

		public Entry[] newArray(int size) {
			return new Entry[size];
		}
	};

	// example constructor that takes a Parcel and gives you an object populated
	// with it's values
	private Entry(Parcel in) {
		id = in.readLong();
		actualEntry = in.readString();
		entryType = in.readLong();
		userEditedEntry = in.readString();
	}

}
