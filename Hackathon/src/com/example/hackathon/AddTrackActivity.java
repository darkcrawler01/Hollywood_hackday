package com.example.hackathon;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class AddTrackActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_track);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_track, menu);
		return true;
	}
	  public void loadGraceNoteSearch(View v)
	  {
		  System.out.println("gracenote");
	  }
	  
	  public void loadTumblrSearch(View v)
	  {
		  System.out.println("tumblr");
	  }

}
