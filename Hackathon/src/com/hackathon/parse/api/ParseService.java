package com.hackathon.parse.api;

import static com.hackathon.parse.api.ParseConstants.GEOLOCATION;
import static com.hackathon.parse.api.ParseConstants.IMAGE;
import static com.hackathon.parse.api.ParseConstants.PLAYLIST_ID;
import static com.hackathon.parse.api.ParseConstants.PLAYLIST_NAME;
import static com.hackathon.parse.api.ParseConstants.PLAYLIST_OBJECT_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ParseService {

	public static String createPlaylist(double latitude, double longitude,
			String playlistID, String playlistName, byte[] fileBytes){
		//Save the file
		ParseFile parseFile = null;
		if(fileBytes != null){
			parseFile = new ParseFile(fileBytes);
			parseFile.saveInBackground();
		}

		//Save the object
		ParseObject playlist = new ParseObject(PLAYLIST_OBJECT_NAME);
		ParseGeoPoint geolocation = new ParseGeoPoint(latitude, longitude);
		playlist.put(GEOLOCATION, geolocation);
		playlist.put(PLAYLIST_ID, playlistID);
		playlist.put(PLAYLIST_NAME, playlistName);

		if(parseFile != null)
			playlist.put(IMAGE, parseFile);
		playlist.saveInBackground();
		return playlist.getObjectId();
	}

	public static boolean updatePlaylist(String objectId, final String newPlaylistName, final byte[] fileBytes) throws ParseException{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(PLAYLIST_OBJECT_NAME);
		query.getInBackground(objectId, new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject parseObject, ParseException e) {
				parseObject.put(PLAYLIST_NAME, newPlaylistName);

				if(fileBytes != null){
					ParseFile parseFile = new ParseFile(fileBytes);
					try {
						parseFile.save();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					parseObject.put(IMAGE, parseFile);
				}
				parseObject.saveInBackground();
			}
		});

		return true;
	}

	public static void deletePlaylist(String objectId){
		ParseQuery<ParseObject> query = ParseQuery.getQuery(PLAYLIST_OBJECT_NAME);
		query.getInBackground(objectId, new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (object != null) {
					object.deleteInBackground();
				}
			}
		});
	}

	public List<Map<String, Object>> getPlaylist(double latitude, double longitude) throws ParseException{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(PLAYLIST_OBJECT_NAME);
		final List<Map<String, Object>> playlistsMapList = new ArrayList<Map<String, Object>>();
		query.whereWithinMiles(GEOLOCATION, new ParseGeoPoint(latitude, longitude), 1);
		List<ParseObject> objects = query.find();
		if(objects != null){
			Map<String, Object> playlistsMap = null;
			System.out.println("Inside the done method....");
			for(ParseObject parseObject : objects){
				playlistsMap = new HashMap<String, Object>();
				playlistsMap.put(GEOLOCATION, parseObject.get(GEOLOCATION));
				playlistsMap.put(PLAYLIST_ID, parseObject.get(PLAYLIST_ID));
				playlistsMap.put(PLAYLIST_NAME, parseObject.get(PLAYLIST_NAME));
				try {
					ParseFile parseFile = (ParseFile)parseObject.get(IMAGE);
					if(parseFile != null){
						playlistsMap.put(IMAGE, parseFile.getData());
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				playlistsMapList.add(playlistsMap);
			}

		}

		return playlistsMapList;
	}
}
