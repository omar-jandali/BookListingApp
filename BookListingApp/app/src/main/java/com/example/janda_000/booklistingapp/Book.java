package com.example.janda_000.booklistingapp;

import android.graphics.Bitmap;

/**
 * Created by janda_000 on 2/23/2017.
 */

public class Book {

    //The following are the variables that are going to be collected and stored in to display
    // the book information in the app
    private String mTitle;
    private String mAuthors;
    private String mDescription;
    private Bitmap mImage;

    //This is the book instance that is created with all the correct variables
    public Book(String title, String authors, String description, Bitmap image) {
        mTitle = title;
        mAuthors = authors;
        mDescription = description;
        mImage = image;
    }

    //These are all of the get function that will be used to grab the content that is saved and
    //stored
    public String getTitle() {
        return mTitle;
    }

    public String getAuthors() {
        return mAuthors;
    }

    public String getDescription() {
        return mDescription;
    }

    public Bitmap getImage() {
        return mImage;
    }

}
