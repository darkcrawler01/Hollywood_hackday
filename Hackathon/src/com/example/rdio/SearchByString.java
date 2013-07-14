package com.example.rdio;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.hackathon.Track;
import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;

public class SearchByString {

	public List<Track> searchByTextString(Rdio rdio, String searchtext){
		final List<Track> trackobjs= new LinkedList<Track>();
		List<NameValuePair> search=null;
		final String txt=searchtext;
		NameValuePair query=new NameValuePair() {

			@Override
			public String getValue() {
				// TODO Auto-generated method stub
				return txt;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "query";
			}
		}; 
		search.add(query);
		NameValuePair types=new NameValuePair() {
			@Override
			public String getValue() {
				// TODO Auto-generated method stub
				return "Track";
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "types";
			}
		};
		search.add(types);
		rdio.apiCall("search", search, new RdioApiCallback() {

			@Override
			public void onApiSuccess(JSONObject keys) {
				// TODO Auto-generated method stub
				try {
					//Log.i(TAG, "Heavy rotation: " + result.toString(2));

					JSONArray tracks = keys.getJSONArray("resuts");
					final ArrayList<String> trackKeys = new ArrayList<String>(tracks.length());
					for (int i=0; i<tracks.length()&&i<10; i++) {
						JSONObject trackstring = tracks.getJSONObject(i);
						String trackKey = trackstring.getString("key");
						Track temp=new Track(trackstring.getString("key"),trackstring.getString("name"),trackstring.getString("albumArtist"),trackstring.getString("album"),trackstring.getString("baseIcon"));
						trackobjs.add(temp);
						trackKeys.add(trackKey);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					
				}

			}

			@Override
			public void onApiFailure(String arg0, Exception arg1) {
				// TODO Auto-generated method stub
				//Sorry Couldnt find music using given query
			}
		});
		return trackobjs;
	}
}
