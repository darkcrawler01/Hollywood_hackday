package com.example.hackathon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.hackathon.parse.api.ParseConstants;
import com.hackathon.parse.api.ParseService;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;
import com.rdio.android.api.RdioListener;
import com.rdio.android.api.RdioSubscriptionType;
import com.rdio.android.api.services.RdioAuthorisationException;

public class MainActivity extends Activity implements RdioListener{

	private static final String TAG = "RdioAPIExample";
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
	private static final int DIALOG_GETTING_LOCATION = 103;
	
	private static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
	private static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";


	//Geo-Coordinates
	private double latitude;
	private double longitude;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageView i = (ImageView)findViewById(R.id.rdio);
		i.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rdioLogin();
			}
		});
	}

	
	private void rdioLogin() {
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
			rdio = new Rdio(appKey, appSecret, accessToken, accessTokenSecret, this, this);	
			//onRdioReady();
		}
		
		
	}
	
	private void doSomethingWithoutApp() {
		Log.i(TAG, "Getting heavy rotation");

		showDialog(DIALOG_GETTING_HEAVY_ROTATION);

		List<NameValuePair> args = new LinkedList<NameValuePair>();
		args.add(new BasicNameValuePair("type", "albums"));
		rdio.apiCall("getHeavyRotation", args, new RdioApiCallback() {
			@Override
			public void onApiSuccess(JSONObject result) {
				try {
					//Log.i(TAG, "Heavy rotation: " + result.toString(2));
					JSONArray albums = result.getJSONArray("result");
					final ArrayList<String> albumKeys = new ArrayList<String>(albums.length());
					for (int i=0; i<albums.length(); i++) {
						JSONObject album = albums.getJSONObject(i);
						String albumKey = album.getString("key");
						albumKeys.add(albumKey);
					}

					// Build our argument to pass to the get api
					StringBuffer keyBuffer = new StringBuffer();
					Iterator<String> iter = albumKeys.iterator();
					while (iter.hasNext()) {
						keyBuffer.append(iter.next());
						if (iter.hasNext()) {
							keyBuffer.append(",");
						}
					}
					Log.i(TAG, "album keys to fetch: " + keyBuffer.toString());
					List<NameValuePair> getArgs = new LinkedList<NameValuePair>();
					getArgs.add(new BasicNameValuePair("keys", keyBuffer.toString()));
					getArgs.add(new BasicNameValuePair("extras", "tracks"));

					// Get more details (like tracks) for all the albums we parsed out of the heavy rotation
					rdio.apiCall("get", getArgs, new RdioApiCallback() {
						@Override
						public void onApiFailure(String methodName, Exception e) {
							Log.e(TAG, "get() failed!", e);
						}

						@Override
						public void onApiSuccess(JSONObject result) {
							try {
								//Log.i(TAG, "get result: " + result.toString(2));
								result = result.getJSONObject("result");
								List<Track> trackKeys = new LinkedList<Track>();

								// Build our list of tracks to put into the player queue
								for (String albumKey : albumKeys) {
									if (!result.has(albumKey)) {
										Log.w(TAG, "result didn't contain album key: " + albumKey);
										continue;
									}
									JSONObject album = result.getJSONObject(albumKey);
									JSONArray tracks = album.getJSONArray("tracks");
									Log.i(TAG, "album " + albumKey + " has " + tracks.length() + " tracks");
									for (int i=0; i<tracks.length(); i++) {
										JSONObject trackObject = tracks.getJSONObject(i);
										String key = trackObject.getString("key");
										String name = trackObject.getString("name");
										String artist = trackObject.getString("artist");
										String albumName = trackObject.getString("album");
										String albumArt = trackObject.getString("icon");
										Log.d(TAG, "Found track: " + key + " => " + trackObject.getString("name"));
										trackKeys.add(new Track(key, name, artist, albumName, albumArt));
									}
								}
							} catch (Exception e) {
								Log.e(TAG, "Failed to handle JSONObject: ", e);
							}
						}
					});
				} catch (Exception e) {
					Log.e(TAG, "Failed to handle JSONObject: ", e);
				} finally {
					dismissDialog(DIALOG_GETTING_HEAVY_ROTATION);
				}
			}

			@Override
			public void onApiFailure(String methodName, Exception e) {
				dismissDialog(DIALOG_GETTING_HEAVY_ROTATION);
				Log.e(TAG, "getHeavyRotation failed. ", e);
			}
		});
	}
	
	private void doSomething() {
		if (rdio.getSubscriptionState() == RdioSubscriptionType.ANONYMOUS) {
			doSomethingWithoutApp();
			return;
		}

		Log.i(TAG, "Getting current user");
		showDialog(DIALOG_GETTING_USER);

		// Get the current user so we can find out their user ID and get their collection key
		List<NameValuePair> args = new LinkedList<NameValuePair>();
		args.add(new BasicNameValuePair("extras", "followingCount,followerCount,username,displayName,subscriptionType,trialEndDate,actualSubscriptionType"));
		rdio.apiCall("currentUser", args, new RdioApiCallback() {
			@Override
			public void onApiSuccess(JSONObject result) {
				dismissDialog(DIALOG_GETTING_USER);
				try {
					result = result.getJSONObject("result");
					Log.i(TAG, result.toString(2));

					// c<userid> is the 'collection radio source' key
//					collectionKey = result.getString("key").replace('s','c');

					LoadMoreTracks();
				} catch (Exception e) {
					Log.e(TAG, "Failed to handle JSONObject: ", e);
				}
			}
			@Override
			public void onApiFailure(String methodName, Exception e) {
				dismissDialog(DIALOG_GETTING_USER);
				Log.e(TAG, "getCurrentUser failed. ", e);
				if (e instanceof RdioAuthorisationException) {
					doSomethingWithoutApp();
				}
			}
		});
	}
	
	private void LoadMoreTracks() {
		if (rdio.getSubscriptionState() == RdioSubscriptionType.ANONYMOUS) {
			Log.i(TAG, "Anonymous user! No more tracks to play.");

			// Notify the user we're out of tracks
			Toast.makeText(this, getString(R.string.no_more_tracks), Toast.LENGTH_LONG).show();

			// Then helpfully point them to the market to go install Rdio ;)
			Log.e(TAG, "redirecting to market place");
			Intent installRdioIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.rdio.android.ui")); 
			installRdioIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(installRdioIntent);
			
			finish();
		
		}else
		{
			Log.i(TAG, "User authorised our app.");
			//rdio.setTokenAndSecret(data);	
			showDialog(DIALOG_GETTING_LOCATION);
			//Location Manager
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new GeoLocationListener();
			locationManager.requestLocationUpdates(LocationManager  
					.GPS_PROVIDER, 500, 10, locationListener);  
			Log.e(TAG,"got location");
			//Parse Initialize
			Parse.initialize(this, "kAhhIhniDBWzaIvliYFNSSDiCdgpItcuPosPBsB5", "Qd0kjJN6qjbk9QM5Ktp16NUafvEkqLbNRd8EuvOC"); 
			ParseAnalytics.trackAppOpened(getIntent());
			String playlistID = "";
			try {
				playlistID = (String)ParseService.getPlaylist(34.074847, -118.398191).get(0).get(ParseConstants.PLAYLIST_ID);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dismissDialog(DIALOG_GETTING_LOCATION);
			ParseConstants.currentRdio = MainActivity.rdio;
			Intent newintent = new Intent(this, PlayActivity.class);
			newintent.putExtra("playlistKey", playlistID);
			startActivity(newintent);
			
		}
		
		/*showDialog(DIALOG_GETTING_COLLECTION);
		List<NameValuePair> args = new LinkedList<NameValuePair>();
		args.add(new BasicNameValuePair("keys", "s5622384"));
		args.add(new BasicNameValuePair("count", "10"));
		rdio.apiCall("get", args, new RdioApiCallback() {
			@Override
			public void onApiFailure(String methodName, Exception e) {
				dismissDialog(DIALOG_GETTING_COLLECTION);
				Log.e(TAG, methodName + " failed: ", e);
			}

			@Override
			public void onApiSuccess(JSONObject result) {
				try {
					result = result.getJSONObject("result");
					List<Track> trackKeys = new LinkedList<Track>();
					JSONArray tracks = result.getJSONArray("tracks");
					for (int i=0; i<tracks.length(); i++) {
						JSONObject trackObject = tracks.getJSONObject(i);
						String key = trackObject.getString("key");
						String name = trackObject.getString("name");
						String artist = trackObject.getString("artist");
						String album = trackObject.getString("album");
						String albumArt = trackObject.getString("icon");
						Log.d(TAG, "Found track: " + key + " => " + trackObject.getString("name"));
						trackKeys.add(new Track(key, name, artist, album, albumArt));
					}
					if (trackKeys.size() > 1)
						trackQueue.addAll(trackKeys);
					dismissDialog(DIALOG_GETTING_COLLECTION);

					// If we're not playing something, then load something up
					if (player == null || !player.isPlaying())
						next(true);

				} catch (Exception e) {
					dismissDialog(DIALOG_GETTING_COLLECTION);
					Log.e(TAG, "Failed to handle JSONObject: ", e);
				}
			}    		
		});*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * Dispatched by the Rdio object when the Rdio object is done initialising, and a connection
	 * to the Rdio app service has been established.  If authorised is true, then we reused our
	 * existing OAuth credentials, and the API is ready for use.
	 * @see com.rdio.android.api.RdioListener#onRdioReady()
	 */
	@Override
	public void onRdioReady() {
		Log.i(TAG, "User state is " + rdio.getSubscriptionState() + " fullstream " + rdio.canUserPlayFullStreams());
		doSomething();
	}

	/*
	 * Dispatched by the Rdio object once the setTokenAndSecret call has finished, and the credentials are
	 * ready to be used to make API calls.  The token & token secret are passed in so that you can
	 * save/cache them for future re-use.
	 * @see com.rdio.android.api.RdioListener#onRdioAuthorised(java.lang.String, java.lang.String)
	 */
	@Override
	public void onRdioAuthorised(String accessToken, String accessTokenSecret) {
		Log.i(TAG, "Application authorised, saving access token & secret.");
		Log.d(TAG, "Access token: " + accessToken);
		Log.d(TAG, "Access token secret: " + accessTokenSecret);

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString(PREF_ACCESSTOKEN, accessToken);
		editor.putString(PREF_ACCESSTOKENSECRET, accessTokenSecret);
		editor.commit();

		doSomething();
	}

	@Override
	public void onRdioUserPlayingElsewhere() {
		// TODO Auto-generated method stub

	}
	
	
	/*
	 * Dispatched by the Rdio object when app approval is needed.  Take the authorisation intent given
	 * and invoke the activity for it
	 * @see com.rdio.android.api.RdioListener#onRdioUserAppApprovalNeeded(android.content.Intent)
	 */
	@Override
	public void onRdioUserAppApprovalNeeded(Intent authorisationIntent) {
		try {
			startActivityForResult(authorisationIntent, REQUEST_AUTHORISE_APP);
		} catch (ActivityNotFoundException e) {
			// Rdio app not found
			Log.e(TAG, "Rdio app not found, limited to 30s samples.");
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_AUTHORISE_APP:
			if (resultCode == Rdio.RESULT_AUTHORISATION_ACCEPTED) {
				Log.i(TAG, "User authorised our app.");
				rdio.setTokenAndSecret(data);	
				showDialog(DIALOG_GETTING_LOCATION);
				//Location Manager
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				LocationListener locationListener = new GeoLocationListener();
				locationManager.requestLocationUpdates(LocationManager  
						.GPS_PROVIDER, 500, 10, locationListener);  
				Log.e(TAG,"got location");
				//Parse Initialize
				Parse.initialize(this, "kAhhIhniDBWzaIvliYFNSSDiCdgpItcuPosPBsB5", "Qd0kjJN6qjbk9QM5Ktp16NUafvEkqLbNRd8EuvOC"); 
				ParseAnalytics.trackAppOpened(getIntent());
				String playlistID = "";
				try {
					playlistID = (String)ParseService.getPlaylist(34.074847, -118.398191).get(0).get(ParseConstants.PLAYLIST_ID);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dismissDialog(DIALOG_GETTING_LOCATION);
				//Intent playIntent = new Intent(this, PlayActivity.class);
				Log.e(TAG,"parse playlist" + playlistID);
				//ParseConstants.currentRdio = MainActivity.rdio;
				//playIntent.putExtra("playlistKey", "p4579531");
				//startActivity(playIntent);
				
			} else if (resultCode == Rdio.RESULT_AUTHORISATION_REJECTED) {
				Log.i(TAG, "User rejected our app.");
			}
			break;
		default:
			Log.e(TAG, "OnActivityResult = " + resultCode);
			break;
		}
	}

	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_GETTING_USER:
			return ProgressDialog.show(this, "", getResources().getString(R.string.getting_user));
		case DIALOG_GETTING_COLLECTION:
			return ProgressDialog.show(this, "", getResources().getString(R.string.getting_collection));
		case DIALOG_GETTING_HEAVY_ROTATION:
			return ProgressDialog.show(this, "", getResources().getString(R.string.getting_heavy_rotation));
		case DIALOG_GETTING_LOCATION:
			return ProgressDialog.show(this, "", getResources().getString(R.string.getting_current_location));
		}
		return null;
	}
	
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "Cleaning up..");

		// Make sure to call the cleanup method on the API object
		rdio.cleanup();

		super.onDestroy();
	}


	/**
	 * Geo Location listener to get the current coordinates
	 */
	private class GeoLocationListener implements LocationListener {  

		@Override  
		public void onLocationChanged(Location loc) {  
			longitude = loc.getLongitude();    
			latitude = loc.getLatitude();  
		}  

		@Override  
		public void onProviderDisabled(String provider) {  
			// TODO Auto-generated method stub           
		}  

		@Override  
		public void onProviderEnabled(String provider) {  
			// TODO Auto-generated method stub           
		}  

		@Override  
		public void onStatusChanged(String provider,   
				int status, Bundle extras) {  
			// TODO Auto-generated method stub           
		}  
	}

}
