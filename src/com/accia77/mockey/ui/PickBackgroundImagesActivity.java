//Heavily based on:
//http://code.tutsplus.com/tutorials/android-sdk-displaying-images-with-an-enhanced-gallery--mobile-11130

package com.accia77.mockey.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.accia77.mockey.MyApplication;
import com.accia77.mockey.R;
import com.accia77.mockey.utils.FileUtils;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class PickBackgroundImagesActivity extends SherlockActivity {

	private static final String TAG = "PickBackgroundImagesActivity";

	// Variable for selection intent
	private final int PICKER_PHASE0 = MyApplication.getInstance()
			.getPositionAnimationPhase0();
	private final int PICKER_PHASE1 = MyApplication.getInstance()
			.getPositionAnimationPhase1();
	private final int PICKER_PHASE2 = MyApplication.getInstance()
			.getPositionAnimationPhase2();
	private final int PICKER_PAPIRO_BACKGROUND = MyApplication.getInstance()
			.getPositionPapiroBackground();
	
	private Gallery picGallery;
	private ImageView picView;

	// adapter for gallery view
	private PicAdapter imgAdapt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_background_images);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// get the large image view
		picView = (ImageView) findViewById(R.id.picture);

		// get the gallery view
		picGallery = (Gallery) findViewById(R.id.gallery);

		// create a new adapter
		imgAdapt = new PicAdapter(this);

		// set the gallery adapter
		picGallery.setAdapter(imgAdapt);

		// set long click listener for each gallery thumbnail item
		picGallery.setOnItemLongClickListener(new OnItemLongClickListener() {
			// handle long clicks
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				// take user to choose an image
				// update the currently selected position so that we assign the
				// imported bitmap to correct item

				// take the user to their chosen image selection app (gallery or
				// file manager)
				Intent pickIntent = new Intent();
				pickIntent.setType("image/*");
				pickIntent.setAction(Intent.ACTION_GET_CONTENT);

				// position's value if the request code for startActivityForResult
				startActivityForResult(Intent.createChooser(
						pickIntent,
						getResources().getString(
								R.string.select_background_image)), position);

				return true;

			}
		});

		// set the click listener for each item in the thumbnail gallery
		picGallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// set the larger image view to display the chosen bitmap
				// calling method of adapter class
				picView.setImageBitmap(imgAdapt.getPic(position));
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if ((requestCode == PICKER_PHASE0)
					|| (requestCode == PICKER_PHASE1)
					|| (requestCode == PICKER_PHASE2)
					|| (requestCode == PICKER_PAPIRO_BACKGROUND)) {
				// the returned picture URI
				Uri selectedImage = data.getData();
				Bitmap pic = null;

				// Path of the chosen image
				String imgPath = "";
				
				imgPath = FileUtils.getPath(this, selectedImage);
				
				// if we have a new URI attempt to decode the image bitmap
				if (selectedImage != null) {
					// set the width and height we want to use as maximum
					// display
					int targetWidth = 600;
					int targetHeight = 400;

					// create bitmap options to calculate and use sample size
					BitmapFactory.Options bmpOptions = new BitmapFactory.Options();

					// first decode image dimensions only - not the image bitmap
					// itself
					bmpOptions.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(imgPath, bmpOptions);

					// image width and height before sampling
					int currHeight = bmpOptions.outHeight;
					int currWidth = bmpOptions.outWidth;

					// variable to store new sample size
					int sampleSize = 1;

					// calculate the sample size if the existing size is larger
					// than target size
					if (currHeight > targetHeight || currWidth > targetWidth) {
						// use either width or height
						if (currWidth > currHeight)
							sampleSize = Math.round((float) currHeight
									/ (float) targetHeight);
						else
							sampleSize = Math.round((float) currWidth
									/ (float) targetWidth);
					}

					// use the new sample size
					bmpOptions.inSampleSize = sampleSize;

					// now decode the bitmap using sample options
					bmpOptions.inJustDecodeBounds = false;

					// get the file as a bitmap
					pic = BitmapFactory.decodeFile(imgPath, bmpOptions);
					
					// pic is not really a bitmap
					if(pic == null) {
						String strAvviso = getResources().getString(
								R.string.background_modify_failure);
						Toast.makeText(getApplicationContext(), strAvviso,
								Toast.LENGTH_LONG).show();
						return;
					}

					// redraw the gallery thumbnails to reflect the new addition
					picGallery.setAdapter(imgAdapt);

					picView.setImageBitmap(null);
					// scale options
					picView.setScaleType(ImageView.ScaleType.FIT_CENTER);

					// Update Shared Preferences
					SharedPreferences spref = PreferenceManager
							.getDefaultSharedPreferences(this);
					Editor editor = spref.edit();
					
					//Determine which sharedPreference is to be modified
					String whichPreference;
					if(requestCode == PICKER_PAPIRO_BACKGROUND)
						whichPreference = "backgroundImagePath";
					else
						whichPreference = "animationPhase" + requestCode;
					
					editor.putString(whichPreference,
							selectedImage.toString());
					editor.commit();

					String strAvviso = getResources().getString(
							R.string.background_modify_success);
					Toast.makeText(getApplicationContext(), strAvviso,
							Toast.LENGTH_SHORT).show();
				} else {
					String strAvviso = getResources().getString(
							R.string.background_modify_failure);
					Toast.makeText(getApplicationContext(), strAvviso,
							Toast.LENGTH_LONG).show();
				}

			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public class PicAdapter extends BaseAdapter {

		// use the default gallery background image
		int defaultItemBackground;
		// gallery context
		private Context galleryContext;

		public PicAdapter(Context c) {

			// instantiate context
			galleryContext = c;

			// get the styling attributes - use default Android system resources
			TypedArray styleAttrs = galleryContext
					.obtainStyledAttributes(R.styleable.PicGallery);

			// get the background resource
			defaultItemBackground = styleAttrs.getResourceId(
					R.styleable.PicGallery_android_galleryItemBackground, 0);

			// recycle attributes
			styleAttrs.recycle();

		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// create the view
			ImageView imageView = new ImageView(galleryContext);
			// specify the bitmap at this position in the array
			imageView.setImageBitmap(MyApplication.getInstance()
					.getBackgroundBitmap(position));
			// set layout options
			imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
			// scale type within view area
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			// set default gallery item background
			imageView.setBackgroundResource(defaultItemBackground);
			// return the view
			return imageView;
		}

		// return bitmap at specified position for larger display
		public Bitmap getPic(int posn) {
			// return bitmap at posn index
			return MyApplication.getInstance().getBackgroundBitmap(posn);
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}



}
