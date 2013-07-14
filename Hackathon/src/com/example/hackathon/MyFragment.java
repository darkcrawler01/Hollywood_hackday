package com.example.hackathon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyFragment extends Fragment {
	 public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	 
	 public static final MyFragment newInstance(String message)
	 {
	   MyFragment f = new MyFragment();
	   Bundle bdl = new Bundle(1);
	   bdl.putString(EXTRA_MESSAGE, message);
	   f.setArguments(bdl);
	   return f;
	 }
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	   Bundle savedInstanceState) {
	   View v = null;
	   String message = getArguments().getString(EXTRA_MESSAGE);
	   if(message == "Fragment 2")
	   {
		   System.out.println("Fragment 2");
		   v = inflater.inflate(R.layout.add_track_layout, container, false);
	   }
	   else
	   {
		   //v = inflater.inflate(R.layout.activity_associate_playlist, container, false);
		   v = inflater.inflate(R.layout.activity_play, container, false);
		   //TextView messageTextView = (TextView)v.findViewById(R.id.textView);
		   //messageTextView.setText(message);
	   }
	 
	   return v;
	 }
	}
