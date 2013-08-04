package com.example.isbnreader;

import java.io.InputStream;
import java.net.URL;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayBook extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_book);
		
		//Get the message from the intent
		Intent intent = getIntent();
		String bookstuffs = intent.getStringExtra("json");
		
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
		textView3.setText(bookinfo[3]);	
		
		Object content = null;
		try{
		  URL url = new URL(bookinfo[1]);
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
