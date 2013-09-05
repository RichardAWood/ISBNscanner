package com.example.isbnreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class ViewLibrary extends Activity {

	private ScrollView sv;
	private LinearLayout screen;
	private ArrayList<Book> books;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_view_library);
   	
		//Set up screen and header
		screen = new LinearLayout(this);
    	screen.setOrientation(LinearLayout.VERTICAL);
		this.setContentView(screen);		
		
    	LinearLayout header = setupHeader();
    	screen.addView(header); 	
    	
		//Grab books from the server
    	books = getBooks();

    	//Set up a scroll-view of all the books returned
		sv = new ScrollView(this);
		sv = createBookLayout(books);
		
    	screen.addView(sv);

	}

	private LinearLayout setupHeader() {

		LinearLayout header = new LinearLayout(this);	
		header.setOrientation(LinearLayout.HORIZONTAL);
    	
    	//Create a Sort By label and dropdown menu (spinner)
    	TextView sortByLabel = new TextView(this);
    	sortByLabel.setText("Sort by: ");
    	
    	Spinner sortByChooser = new Spinner(this);
    	// Create an ArrayAdapter using the string array and a default spinner layout
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
    	        R.array.sort_options, android.R.layout.simple_spinner_item);
    	// Specify the layout to use when the list of choices appears
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	// Apply the adapter to the spinner
    	sortByChooser.setAdapter(adapter);
    	sortByChooser.setOnItemSelectedListener(new SortByOnItemSelectedListener());
    	
    	header.addView(sortByLabel);
    	header.addView(sortByChooser);
    	
    	return header;
	}

	private ArrayList<Book> getBooks() {
		
    	ArrayList<Book> somebooks  = new ArrayList<Book>();
   	
    	//Get books from server
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpGet get;
    	
    	get = new HttpGet("http://stark-mesa-1616.herokuapp.com/books?format=json");
    	
    	HttpResponse response = null;
    	String bookstuffs = "";
    	
		try {
			response = client.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("ClientProtocol",""+e);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("IO",""+e);
		}
    	if (response != null){
    		try{
    			bookstuffs = MainActivity.convertStreamToString(response.getEntity().getContent());
    			somebooks = createBooks(bookstuffs);
    		} catch (IOException e){
    			e.printStackTrace();
    			Log.e("IO",""+e);    			
    		}
        }
    	
    	return somebooks;
	}

	private ScrollView createBookLayout(ArrayList<Book> somebooks) {
	
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		sv.addView(ll);
		
		for (Book book : somebooks)
		{			
			LinearLayout ll2 = new LinearLayout(this);
			ll2.setOrientation(LinearLayout.HORIZONTAL);
			ll2.setGravity(Gravity.CENTER_VERTICAL);
			
			Object content = null;
			try{
			  URL url = new URL(book.getImage_url());
			  content = url.getContent();
			}
			  catch(Exception ex)
			{
			    ex.printStackTrace();
			}
			
			InputStream is = (InputStream)content;
			Drawable image = Drawable.createFromStream(is, "src");
			ImageView ImageView01 = new ImageView(this);
			ImageView01.setImageDrawable(image);
			
			LinearLayout.LayoutParams layoutParams  = new LinearLayout.LayoutParams(200,200);
			layoutParams.setMargins(24, 12, 24, 12);
			ImageView01.setLayoutParams(layoutParams);
			
			TextView tv = new TextView(this);
			tv.setText(book.getTitle() + "\n" + book.getAuthor());
			
			ll2.addView(ImageView01);
			ll2.addView(tv);
			ll.addView(ll2);
		}
		
		return sv;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_library, menu);
		return true;
	}
	

	private ArrayList<Book> createBooks(String bookstuffs) {
		ArrayList<Book> thesebooks = new ArrayList<Book>();

		try{
			JSONArray railsBooks = new JSONArray(bookstuffs);//json.getJSONArray();	
			
			for(int i = 0; i < railsBooks.length(); i++){
				JSONObject jsonbook = railsBooks.getJSONObject(i);
				String title = jsonbook.getString("title");
				String author = jsonbook.getString("author");
				String isbn = jsonbook.getString("isbn_13");
				String imagethumbnail = jsonbook.getString("Thumbnail_URL");
				Book abook = new Book(title, author, isbn, imagethumbnail);
				thesebooks.add(abook);
			}
			
		}
		catch (JSONException ex){
			ex.printStackTrace();
			return thesebooks;
		}
		
		return thesebooks;
	}	
	
	public class SortByOnItemSelectedListener implements OnItemSelectedListener{

		public class BookComparator implements Comparator<Book> {
			
			private String sortby;
			
			public BookComparator(String sortby){
				this.sortby = sortby;
			}
			@Override
		    public int compare(Book o1, Book o2) {
		    	if (sortby.equals("Title"))
		    		return o1.getTitle().compareTo(o2.getTitle());
		    	else if (sortby.equals("Author"))
		    		return o1.getAuthorLastName().compareTo(o2.getAuthorLastName());
		    	else
		    		return 1;
		    }
		}
		
		  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {  
			  if(parent.getItemAtPosition(pos).toString().equals("By Title")){
				  screen.removeView(sv);
				  sv = new ScrollView(parent.getContext());
				  ArrayList<Book> sortedBooks = sortBooks(books, new BookComparator("Title"));
				  sv = createBookLayout(sortedBooks);
				  screen.addView(sv);
			  }
			  if(parent.getItemAtPosition(pos).toString().equals("By Author")){
				  screen.removeView(sv);
				  sv = new ScrollView(parent.getContext());
				  ArrayList<Book> sortedBooks = sortBooks(books, new BookComparator("Author"));
				  sv = createBookLayout(sortedBooks);
				  screen.addView(sv);
			  }			  
			  
//			  Toast.makeText(parent.getContext(), 
//						"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//						Toast.LENGTH_SHORT).show();
			  }
			 
		    private ArrayList<Book> sortBooks(ArrayList<Book> somebooks, BookComparator comparator) {
		    	Collections.sort(somebooks, comparator);
		    	return somebooks;
		    } 

			@Override
			  public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			  }	
		
	}

}
