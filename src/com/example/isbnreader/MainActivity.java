package com.example.isbnreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

		

    public void scanBook(View v) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
    
    public void lookupISBN(String isbn) throws IllegalStateException, IOException, JSONException{
    	HttpResponse response = null;
    	try {        
    	        HttpClient client = new DefaultHttpClient();
    	        HttpGet request = new HttpGet();
    	        request.setURI(new URI("https://www.googleapis.com/books/v1/volumes?q=isbn:"+isbn));
    	        response = client.execute(request);
    	    } catch (URISyntaxException e) {
    	        e.printStackTrace();
    	    } catch (ClientProtocolException e) {
    	        // TODO Auto-generated catch block
    	        e.printStackTrace();
    	    } catch (IOException e) {
    	        // TODO Auto-generated catch block
    	        e.printStackTrace();
    	    } 
    	if (response != null){
    		String bookstuffs = convertStreamToString(response.getEntity().getContent());
    		openDisplayBook(isbn, bookstuffs);
          	}
	}

    
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            // Handle successful scan
	            EditText editText = (EditText)findViewById(R.id.ISBN_message);
	            editText.setText(contents, TextView.BufferType.EDITABLE);
	            try{
	            	lookupISBN(contents);
	            }
	            catch(IOException ex){
	            	ex.printStackTrace();
	            }
	            catch(JSONException ex)
	            {
	            	ex.printStackTrace();
	            }
	        }
	        else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	            EditText editText = (EditText)findViewById(R.id.ISBN_message);
	            editText.setText("", TextView.BufferType.EDITABLE);
	        }
	    }
	}	
	
    /**Called if the ISBN code returns a valid book */
    public void openDisplayBook(String isbn, String bookJSON) {
    	Intent intent = new Intent(this, DisplayBook.class);
    	intent.putExtra("isbn", isbn);
    	intent.putExtra("json", bookJSON);
    	startActivity(intent);
    }	
	
}
