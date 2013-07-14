package com.example.hackathon;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;

public class SwipeActivity extends FragmentActivity {
	
	CustomPageAdapter pageAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swipe);
		List<Fragment> fragments = getFragments();
		pageAdapter = new CustomPageAdapter(getSupportFragmentManager(), fragments);
		ViewPager pager =
				(ViewPager)findViewById(R.id.viewpager);
		pager.setAdapter(pageAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu., menu);
		return true;
	}
	
	private List<Fragment> getFragments(){
		  List<Fragment> fList = new ArrayList<Fragment>();
		 
		  fList.add(MyFragment.newInstance("Fragment 1"));
		  fList.add(MyFragment.newInstance("Fragment 2")); 
		  	 
		  return fList;
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

