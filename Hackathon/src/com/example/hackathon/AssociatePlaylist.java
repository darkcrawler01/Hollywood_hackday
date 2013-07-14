package com.example.hackathon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AssociatePlaylist extends Activity {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 1337;
	private Uri fileUri = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.activity_associate_playlist, null);

		// Find the ScrollView 
		ScrollView sv = (ScrollView) v.findViewById(R.id.scrollView1);

		// Create a LinearLayout element
		LinearLayout ll = (LinearLayout)v.findViewById(R.id.scroll_linearView);

		for(int i=0; i<5; i++)
		{
			// Add text
			TextView tv = new TextView(this);
			tv.setText("my playlist "+i);
			ll.addView(tv);
		}

		// Add the LinearLayout element to the ScrollView
		//sv.addView(ll);

		// Display the view
		setContentView(v);

		//setContentView(R.layout.activity_associate_playlist);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.associate_playlist, menu);
		return true;
	}

	public void doCameraActivity(View v)
	{
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//		File file = getOutputPhotoFile();
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQ );
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Image saved successfully", 
						Toast.LENGTH_LONG).show();
				Bitmap bitmap;
				try {
					bitmap = MediaStore.Images.Media.getBitmap( getApplicationContext().getContentResolver(),  fileUri);
					//imageView.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					//Log.d(DEBUG_TAG, "Bitmap image view display failed.", e);
				} catch (IOException e) {
					//Log.d(DEBUG_TAG, "Bitmap image view display failed.", e);
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Callout for image capture failed!", 
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Create file uri to store image or video
	 * @param type
	 * @return
	 */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "GeoPrint");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	public void uploadToDatabase(View v)
	{
		/* TODO */
	}

}
