package com.example.janda_000.booklistingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String BOOKS_QUERY_INIT = "https://www.googleapis.com/books/v1/volumes?q=";

    private static final String BOOKS_MAX_RESULTS_STRING = "&maxResults=";

    private static final int BOOKS_MAX_RESULTS_VALUE = 20;

    private int totalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void bookSearch(View view) {

        EditText inputText = (EditText) findViewById(R.id.edit_text);
        String text = String.valueOf(inputText.getText());

        BooksAsyncTask task = new BooksAsyncTask(text);
        task.execute();
    }

    private URL createUrl(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
            Log.i(LOG_TAG, stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private ArrayList<Book> getBooks(String bookJSON) {
        try {

            ArrayList<Book> books = new ArrayList<>();

            JSONObject baseJsonResponse = new JSONObject(bookJSON);
            totalItems = baseJsonResponse.getInt("totalItems");
            if (totalItems != 0) {
                JSONArray itemArray = baseJsonResponse.getJSONArray("items");

                for (int i = 0; i < itemArray.length(); i++) {

                    JSONObject item = itemArray.getJSONObject(i);
                    JSONObject volume = item.getJSONObject("volumeInfo");

                    JSONObject imageLinks = volume.optJSONObject("imageLinks");

                    String imageUrl;
                    Bitmap bitmap = null;

                    if (imageLinks != null) {
                        imageUrl = imageLinks.getString("thumbnail");
                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
                    }

                    JSONArray authors = volume.optJSONArray("authors");

                    String title = volume.getString("title");
                    String description = volume.optString("description");

                    if (Objects.equals(description, "")) {
                        description = getString(R.string.no_description);
                    }

                    String authorsList = getString(R.string.no_authors);

                    if (authors != null) {
                        StringBuilder authorsBuilder = new StringBuilder();
                        for (int j = 0; j < authors.length(); j++) {
                            authorsBuilder.append(authors.getString(j));
                            if (j != authors.length() - 1) {
                                authorsBuilder.append(", ");
                            }
                        }
                        authorsList = authorsBuilder.toString();
                    }

                    books.add(new Book(title, authorsList, description, bitmap));
                }

                return books;
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public class BooksAsyncTask extends AsyncTask<URL, String, ArrayList<Book>> {

        private String mText;

        public BooksAsyncTask(String text) {
            mText = text;
        }

        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {

            this.publishProgress(getString(R.string.in_progress));

            StringBuilder query = new StringBuilder();
            query.append(BOOKS_QUERY_INIT);
            try {
                //Now the user can add blank spaces and other non-lettery stuff.
                query.append(URLEncoder.encode(mText, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            query.append(BOOKS_MAX_RESULTS_STRING);
            query.append(BOOKS_MAX_RESULTS_VALUE);

            // Create URL object
            URL url = createUrl(String.valueOf(query));

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";

            try {

                jsonResponse = makeHttpRequest(url);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return getBooks(jsonResponse);
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {

            TextView infoText = (TextView) findViewById(R.id.info_text);

            if (totalItems != 0) {
                //Getting rid of the info_text view once results show up.
                infoText.setVisibility(View.GONE);

                ListView bookListView = (ListView) findViewById(R.id.list);
                BookAdapter adapter = new BookAdapter(MainActivity.this, books);
                bookListView.setAdapter(adapter);

            } else {
                infoText.setText(R.string.no_results);
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            TextView infoText = (TextView) findViewById(R.id.info_text);
            infoText.setText(values[0]);
        }
    }
}
