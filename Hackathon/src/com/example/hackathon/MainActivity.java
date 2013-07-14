package com.example.hackathon;

import java.util.LinkedList;
import java.util.Queue;

import com.rdio.android.api.Rdio;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	private Queue<Track> trackQueue;
	private static final String TAG = "RdioAPIExample";
	private MediaPlayer player;
	private static Rdio rdio;
	private static final String appKey = "prgspfgm6u734kza26ed8s3z";
	private static final String appSecret = "gMvhtPgtw7";
	private static String accessToken = null;
	private static String accessTokenSecret = null;
	// Request code used for startActivityForResult/onActivityResult
	private static final int REQUEST_AUTHORISE_APP = 100;
	// Dialog codes used for createDialog
	private static final int DIALOG_GETTING_USER = 100;
	private static final int DIALOG_GETTING_COLLECTION = 101;
	private static final int DIALOG_GETTING_HEAVY_ROTATION = 102;

	private static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
	private static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";

	private static String collectionKey = null;

	// Our model for the metadata for a track that we care about
	private class Track {
		public String key;
		public String trackName;
		public String artistName;
		public String albumName;
		public String albumArt;

		public Track(String k, String name, String artist, String album, String uri) {
			key = k;
			trackName = name;
			artistName = artist;
			albumName = album;
			albumArt = uri;
		}
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		trackQueue = new LinkedList<Track>();

		// Initialise our Rdio object.  If we have cached access credentials, then use them - otherwise
		// initialise w/ null values and the user will be prompted (if the Rdio app is installed), or
		// we'll fallback to 30s samples.
		if (rdio == null) {
			SharedPreferences settings = getPreferences(MODE_PRIVATE);
			accessToken = settings.getString(PREF_ACCESSTOKEN, null);
			accessTokenSecret = settings.getString(PREF_ACCESSTOKENSECRET, null);

			if (accessToken == null || accessTokenSecret == null) {
				// If either one is null, reset both of them
				accessToken = accessTokenSecret = null;
			} else {
				Log.d(TAG, "Found cached credentials:");
				Log.d(TAG, "Access token: " + accessToken);
				Log.d(TAG, "Access token secret: " + accessTokenSecret);
			}

			// Initialise our API object
			//rdio = new Rdio(appKey, appSecret, accessToken, accessTokenSecret, this, this);	
		}


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
