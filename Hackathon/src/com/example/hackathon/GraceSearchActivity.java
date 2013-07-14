package com.example.hackathon;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gracenote.mmid.MobileSDK.GNOperations;
import com.gracenote.mmid.MobileSDK.GNSearchResponse;
import com.gracenote.mmid.MobileSDK.GNSearchResultReady;
import com.gracenote.mmid.MobileSDK.GNSearchResult;
import com.gracenote.mmid.MobileSDK.GNConfig;

public class GraceSearchActivity extends Activity {

	private GNConfig config;
	private TextView song_info;
	private Button gn_button;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grace_search);


		config = GNConfig.init("10353664-3657B1006A5B4E9DC358F1C2F02E5C15", this.getApplicationContext());

		gn_button = (Button) findViewById(R.id.gn_button);

		gn_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				SearchFromMic micInput = new SearchFromMic();
				micInput.searchSong();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grace_search, menu);
		return true;
	}

	class SearchFromMic implements GNSearchResultReady
	{
		void searchSong()
		{
			GNOperations.recognizeMIDStreamFromMic(this, config);
		}

		public void GNResultReady(GNSearchResult result)
		{
			song_info = (TextView) findViewById(R.id.song_info);
			//			System.out.println("song text: " + song_info);

			if(result.isFingerprintSearchNoMatchStatus())
			{
				song_info.setText("Song not found");
			}
			else
			{
				GNSearchResponse response = result.getBestResponse();

				if(response != null)
				{
					song_info.setText(response.getAlbumArtist() + " " + response.getAlbumTitle());


					Intent data = new Intent();
					data.putExtra("AlbumArtist", response.getAlbumArtist());
					data.putExtra("AlbumTitle", response.getAlbumTitle());
					data.putExtra("TrackTitle", response.getTrackTitle());

					setResult(RESULT_OK,data);


				}
				else
				{
					setResult(RESULT_CANCELED);
				}
				finish();
			}

		}
	}
}
