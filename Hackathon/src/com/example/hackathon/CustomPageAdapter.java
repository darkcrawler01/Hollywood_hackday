package com.example.hackathon;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CustomPageAdapter extends FragmentPagerAdapter {
		  String[] pageTitles = {"Fragment 1","Your Playlist","Add Track"};
		  private List<Fragment> fragments;

		  public CustomPageAdapter(FragmentManager fm, List<Fragment> fragments) {
		    super(fm);
		    this.fragments = fragments;
		  }
		  @Override 
		  public Fragment getItem(int position) {
		    return this.fragments.get(position);
		  }

		  @Override
		  public int getCount() {
		    return this.fragments.size();
		  }
		  
		  @Override
	       public CharSequence getPageTitle (int position) {
	           return pageTitles[position];
	       }
		  

}

