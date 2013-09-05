package com.example.isbnreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayBook extends Activity {

	
	public static class Globals{
		public static long isbn_l = 0;	
		public static String isbn_s = "";
		public static boolean dev = false;
		public static String thumbnailURL = "";
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_book);
		
		//Get the message from the intent
		Intent intent = getIntent();
		String isbn = intent.getStringExtra("isbn");
		String bookstuffs = intent.getStringExtra("json");
		Globals.dev = intent.getBooleanExtra("dev", false);
		
		//Set the global ISBN_NUMBER with the isbn string passed from the intent
		Globals.isbn_l = Long.parseLong(isbn);
		Globals.isbn_s = isbn;
		
		//Attempt to create a JSON object out of the intent string, and return string array:
		//[title, thumbnail url].
		String[] bookinfo;
		bookinfo = checkJSON(bookstuffs);

		//Display the book image and title.
		TextView textView = (TextView)findViewById(R.id.BookTitle);
		textView.setText(bookinfo[0]);	
		TextView textView2 = (TextView)findViewById(R.id.BookAuthor);
		textView2.setText(bookinfo[2]);		
		TextView textView3 = (TextView)findViewById(R.id.BookDesc);
		textView3.setText(isbn);	
		
		Object content = null;
		try{
		  URL url = new URL(bookinfo[1]);
		  Globals.thumbnailURL = url.toString();
		  content = url.getContent();
		}
		  catch(Exception ex)
		{
		    ex.printStackTrace();
		}
		InputStream is = (InputStream)content;
		Drawable image = Drawable.createFromStream(is, "src");
		ImageView ImageView01 = (ImageView)findViewById(R.id.BookImage);
		ImageView01.setImageDrawable(image);
			
		// Show the Up button in the action bar.
		setupActionBar();
	}

	public void addBook(View v){
		TextView textView = (TextView)findViewById(R.id.BookTitle);	
		TextView textView2 = (TextView)findViewById(R.id.BookAuthor);
		
		postEvents(Globals.isbn_s, textView.getText().toString(), textView2.getText().toString(), Globals.thumbnailURL);
	}
	
	
	private void postEvents(String isbn, String title, String author, String ThumbnailURL)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		
		//FOR LOCAL DEV 
		HttpPost post;
		if (Globals.dev){
			post = new HttpPost("http://192.168.0.21:3000/books");
		}
		else{
			post = new HttpPost("http://stark-mesa-1616.herokuapp.com/books");
		}
		post.setHeader("Content-Type","application/json");
		
		JSONObject params = new JSONObject();
		
		try {	
			
			params.put("isbn_13", isbn);
			params.put("title", title);
			params.put("author", author);
			params.put("Thumbnail_URL", ThumbnailURL);

			StringEntity entity = new StringEntity(params.toString());
			post.setEntity(entity);
						
			
			} catch (UnsupportedEncodingException e) {
				Log.e("Error",""+e);
				e.printStackTrace();
			} catch (JSONException js) {
				js.printStackTrace();
		}
		
		HttpResponse response = null;
		
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("ClientProtocol",""+e);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("IO",""+e);
		}
		
		HttpEntity entity = response.getEntity();
		try {
			InputStream inputString = entity.getContent();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if (entity != null) {
//			try {
//				entity.consumeContent();
//			} catch (IOException e) {
//					Log.e("IO E",""+e);
//					e.printStackTrace();
//			}
//		}
		
		Toast.makeText(this, "Your post was successfully uploaded", Toast.LENGTH_LONG).show();
	
	}
	
	
	private String[] checkJSON(String bookstuffs) {
		// TODO Auto-generated method stub
		String[] bookinfo;
		bookinfo = new String[4];
		
		//Pre-populate array with error messages in case of bad JSON
		bookinfo[0] = "No book was found with your ISBN";
		bookinfo[1] = "";
		bookinfo[2] = "";
		bookinfo[3] = "";
		
		String title = "";
		String imagethumbnail = "";
		String author = "";
		String description = "";
		try{
			JSONObject json = new JSONObject(bookstuffs);
			JSONArray items = json.getJSONArray("items");
			JSONObject item = items.getJSONObject(0);
			JSONObject volumeinfo = item.getJSONObject("volumeInfo");
			JSONObject imagelinks = volumeinfo.getJSONObject("imageLinks");
			JSONArray authors = volumeinfo.getJSONArray("authors");
			title = volumeinfo.getString("title");
			description = volumeinfo.getString("description");
			imagethumbnail = imagelinks.getString("thumbnail");	
			author = authors.getString(0);
		}
		catch (JSONException ex){
			ex.printStackTrace();
			return bookinfo;
		}
		
		bookinfo[0] = title;
		bookinfo[1] = imagethumbnail;
		bookinfo[2] = author;
		bookinfo[3] = description;
		
		return bookinfo;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_book, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
