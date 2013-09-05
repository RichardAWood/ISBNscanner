package com.example.isbnreader;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable{

	private String title;
	private String author;
	private String isbn;
	private String image_url;	
	
	public Book(String title, String author, String isbn, String image_url) {
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.image_url = image_url;
	}
	
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(author);
        out.writeString(isbn);
        out.writeString(image_url);
    }	
	
    public static final Parcelable.Creator<Book> CREATOR
    	= new Parcelable.Creator<Book>() {
    		public Book createFromParcel(Parcel in) {
    			return new Book(in);
    	}
	
		public Book[] newArray(int size) {
			return new Book[size];
		}
    };
    
    private Book(Parcel in) {
        title = in.readString();
        author = in.readString();
        isbn = in.readString();
        image_url = in.readString();
    }

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getAuthorLastName(){
		String[] names = author.split("\\s+");
		int len = names.length;
		return names[len-1];
	}

	public String getIsbn() {
		return isbn;
	}

	public String getImage_url() {
		return image_url;
	}  	
        
}
