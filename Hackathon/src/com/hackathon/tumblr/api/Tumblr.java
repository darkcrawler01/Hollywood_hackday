package com.hackathon.tumblr.api;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.AudioPost;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;


public class Tumblr {

	private static List<Blog> getBlogs(){
		// Authenticate via OAuth
		JumblrClient client = new JumblrClient(
				"GFMIVyFf9cGSeBoFrZrXf6jhNsMFk1AED5WJ8KDWkoMkYBeFlB",
				"PcPZGoeu0c4UKXg2dcJj3AwraQPvsWnB4TK6b9XATu0xdEkvRQ"
				);
		client.setToken(
				"iYPFv3Z6kIaDdeCfU7b3HlKZIqtsndoEgjBcUoFYKYcygAr9go",
				"urxs1hMU6UHSiHRE1CrZJamv7eIIdmUmAeuhSx4L6z3yJhLE0K"
				);

		// Make the request
		List<Blog> blogs = client.userFollowing();
		return blogs;
	}

	private static List<Post> getAudioPosts(String blogname){

		// Authenticate via API Key
		JumblrClient client = new JumblrClient(
				"GFMIVyFf9cGSeBoFrZrXf6jhNsMFk1AED5WJ8KDWkoMkYBeFlB",
				"PcPZGoeu0c4UKXg2dcJj3AwraQPvsWnB4TK6b9XATu0xdEkvRQ"
				);

		// Make the request
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", "audio");
		List<Post> posts = client.blogPosts(blogname, params);

		return posts;

	}

	public static List<String> getTrackNames(){
		List<Blog> blogs = getBlogs();
		List<String> trackNameList = new ArrayList<String>();
		List<Post> audioPosts = null;
		for(Blog blog : blogs){
			audioPosts = getAudioPosts(blog.getName());
			for(Post post : audioPosts){
				if(post.getType().equalsIgnoreCase("audio")){
					trackNameList.add(((AudioPost)post).getTrackName());
				}
			}
		}
		
		return trackNameList;
	}

}
