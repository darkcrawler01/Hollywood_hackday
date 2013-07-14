package com.example.hackathon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AssociatePlaylist extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View v = inflater.inflate(R.layout.activity_associate_playlist, null);

	    // Find the ScrollView 
	    ScrollView sv = (ScrollView) v.findViewById(R.id.scrollView1);

	    for(int i=0; i<5; i++)
	    {
		    // Create a LinearLayout element
		    LinearLayout ll = new LinearLayout(this);
		    ll.setOrientation(LinearLayout.VERTICAL);

		    // Add text
		    TextView tv = new TextView(this);
		    tv.setText("my playlist "+i);
		    ll.addView(tv);

		    // Add the LinearLayout element to the ScrollView
		    sv.addView(ll);
	    }

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

}
