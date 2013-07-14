package com.example.hackathon;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.hackathon.parse.api.ParseConstants;
import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;

public class PlayActivity extends Activity {
	private Rdio rdio;
	private Queue<Track> trackQueue;
	private MediaPlayer player;
	
	private ImageView albumArt;
	private ImageView playPause;
	
	// Dialog codes used for createDialog
	private static final int DIALOG_GETTING_COLLECTION = 101;
	
	private static final String TAG = "RdioAPIExample";	
	private static String playlistKey = null; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		Intent parentIntent = getIntent();
		rdio = ParseConstants.currentRdio;
		playlistKey = parentIntent.getStringExtra("playlistKey");
		
		trackQueue = new LinkedList<Track>();
		

		ImageView i = (ImageView)findViewById(R.id.next);
		i.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				next(true);
			}
		});

		playPause = (ImageView)findViewById(R.id.playPause);
		playPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playPause();
			}
		});

		albumArt = (ImageView)findViewById(R.id.albumArt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_play, menu);
		return true;
	}

	
	private void playPause() {
		if (player != null) {
			if (player.isPlaying()) {
				player.pause();
				updatePlayPause(false);
			} else {
				player.start();
				updatePlayPause(true);
			}
		} else {
			next(true);
		}
	}
	
	private void LoadMoreTracks() {

		showDialog(DIALOG_GETTING_COLLECTION);
		List<NameValuePair> args = new LinkedList<NameValuePair>();
		args.add(new BasicNameValuePair("keys", playlistKey));
		args.add(new BasicNameValuePair("count", "50"));
		rdio.apiCall("get", args, new RdioApiCallback() {
			
			@Override
			public void onApiSuccess(JSONObject result) {
				try {
					result = result.getJSONObject("result");
					result = result.getJSONObject(playlistKey);
					List<Track> trackKeys = new LinkedList<Track>();
					JSONArray tracks = result.getJSONArray("tracks");
					for (int i=0; i<tracks.length(); i++) {
						JSONObject trackObject = tracks.getJSONObject(i);
						String key = trackObject.getString("key");
						String name = trackObject.getString("name");
						String artist = trackObject.getString("artist");
						String album = trackObject.getString("album");
						String albumArt = trackObject.getString("icon");
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
				}
			} 
			
			@Override
			public void onApiFailure(String methodName, Exception e) {
				dismissDialog(DIALOG_GETTING_COLLECTION);
			}
		}); 
	}
	
	private void next(final boolean manualPlay) {
		if (player != null) {
			player.stop();
			player.release();
			player = null;
		}

		final Track track = trackQueue.poll();
		if (trackQueue.size() < 3) {
			//Log.i(TAG, "Track queue depleted, loading more tracks");
			LoadMoreTracks();
		}

		if (track == null) {
			return;
		}

		// Load the next track in the background and prep the player (to start buffering)
		// Do this in a bkg thread so it doesn't block the main thread in .prepare()
		AsyncTask<Track, Void, Track> task = new AsyncTask<Track, Void, Track>() {
			@Override
			protected Track doInBackground(Track... params) {
				Track track = params[0];
				try {
					player = rdio.getPlayerForTrack(track.key, null, manualPlay);
					player.prepare();
					player.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							next(false);
						}
					});
					player.start();
				} catch (Exception e) {
					Log.e("Test", "Exception " + e);
				}
				return track;
			}

			@Override
			protected void onPostExecute(Track track) {
				updatePlayPause(true);
			}
		};
		task.execute(track);

		// Fetch album art in the background and then update the UI on the main thread
		AsyncTask<Track, Void, Bitmap> artworkTask = new AsyncTask<Track, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Track... params) {
				Track track = params[0];
				try {
					String artworkUrl = track.albumArt.replace("square-200", "square-600");
					Bitmap bm = null; 
					try { 
						URL aURL = new URL(artworkUrl); 
						URLConnection conn = aURL.openConnection(); 
						conn.connect(); 
						InputStream is = conn.getInputStream(); 
						BufferedInputStream bis = new BufferedInputStream(is); 
						bm = BitmapFactory.decodeStream(bis); 
						bis.close(); 
						is.close(); 
					} catch (IOException e) { 
						Log.e(TAG, "Error getting bitmap", e); 
					} 
					return bm; 				
				} catch (Exception e) {
					Log.e(TAG, "Error downloading artwork", e);
					return null;
				}
			}

			@Override
			protected void onPostExecute(Bitmap artwork) {
				if (artwork != null) {
					albumArt.setImageBitmap(artwork);
				} else
					albumArt.setImageResource(R.drawable.blank_album_art);
			}
		};
		artworkTask.execute(track);

		Toast.makeText(this, String.format(getResources().getString(R.string.now_playing), track.trackName, track.albumName, track.artistName), Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_GETTING_COLLECTION:
			return ProgressDialog.show(this, "", getResources().getString(R.string.getting_collection));
		}
		return null;
	}
	
	
	private void updatePlayPause(boolean playing) {
		if (playing) {
			playPause.setImageResource(R.drawable.pause);
		} else {
			playPause.setImageResource(R.drawable.play);
		}
	}

}
