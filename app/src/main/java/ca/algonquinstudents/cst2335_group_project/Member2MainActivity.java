package ca.algonquinstudents.cst2335_group_project;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * @author Galen Reid
 * This is the Movie Information app, It allows usres to search for a movie name and then gives
 * information on that mnovie. It alows the user to save a delete movies and thier info to their
 * device.
 */
public class Member2MainActivity extends AppCompatActivity {
    /**
     * @param toolitem - stores a reference the toolbar
     * @param movieArray - stores the list of saved movies to populate the ListView
     * @param addButton - stores a reference of the add button
     * @param helpButton - stores a reference of the help button
     * @param aboutButton - stores a reference of the about button
     * @param movieList - stores a reference of the Listview of saved movies
     * @param movieSearch - stores a reference of the about edit text for entering movie names
     * @param movieBar - stores a reference of the progress bar
     * @param help - stores a reference of the help TextView for use with the fragment
     * @param movieName - Used to store the users input into the edit text
     * @param dbHelper2 - An object of the database helper class
     * @param db2 - An object of the SQLite database class
     * @param movieQuery - An object of the movieQuery inner class
     * @param option - An integer that keeps track of if the user is searching a new or saved movie
     * @param arrayAdapter - An array adapter to covert array objects into View items for list view
     * @param frame - Is used to check if the fragment was loaded onto the screen
     */
    private ToolbarMenu toolitem;
    public static ArrayList<String> movieArray;
    Button addButton;
    Button helpButton;
    Button aboutButton;
    ListView movieList;
    EditText movieSearch;
    ProgressBar movieBar;
    TextView help;
    String movieName;
    public static M2DatabaseHelper dbHelper2;
    public static SQLiteDatabase db2;
    MovieQuery movieQuery;
    int option;
    ArrayAdapter<String> arrayAdapter;
    public static boolean frame;



    /**
     * Loads the main layout, creates and reads the database, populates Listview with data read
     * from database, creates the button and listview listeners
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member2_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm2);
        movieArray = new ArrayList<>();
        addButton = findViewById(R.id.addButton_m2);
        helpButton = findViewById(R.id.helpButton_m2);
        aboutButton = findViewById(R.id.aboutButton_m2);
        movieList = findViewById(R.id.movieList_m2);
        movieSearch = findViewById(R.id.movieTextEdit_m2);
        movieBar = findViewById(R.id.progressBar_m2);
        help = findViewById(R.id.help_m2);
        movieQuery = new MovieQuery();
        option = 0;
        setSupportActionBar(toolbar);


        toolitem = new ToolbarMenu(Member2MainActivity.this);

        if (findViewById(R.id.frame_m2) == null) {
            frame = false;
        } else {
            frame = true;

            help.setText("");

        }

        //Reads the database
        dbHelper2 = new M2DatabaseHelper(this);
        db2 = dbHelper2.getReadableDatabase();

        Cursor c = db2.rawQuery("select * from Movies", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            movieArray.add(c.getString(c.getColumnIndex(M2DatabaseHelper.Key_ID)));

            c.moveToNext();
        }


        //Creates array adapter
        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                movieArray);
        movieList.setAdapter(arrayAdapter);

        //add button listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                movieName = movieSearch.getText().toString();
                try {
                    movieName = URLEncoder.encode(movieName, "UTF-8");
                } catch (Exception e) {
                    Log.i(TAG, "URLEncoder failed");

                }

                option = 1;
                new MovieQuery().execute();

                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }


            }

        });

        //help button listener
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (frame == false) {
                    Intent intent = new Intent(Member2MainActivity.this, M2Help.class);
                    startActivity(intent);
                }

                else if (frame == true){
                    help.setText(getString(R.string.m2_helpInfo));

                }
            }

        });

        //about button listener
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(findViewById(R.id.aboutButton_m2), getString(R.string.m2_aboutText), Snackbar.LENGTH_SHORT).show();

            }

        });


        //listview listener
        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                movieName = movieArray.get(i);
                option = 2;
                new MovieQuery().execute();


            }


        });


    }

    /**
     * Connects to the OMDB website, creates and XML parser to retrieve desired data from website.
     */
    public class MovieQuery extends AsyncTask<String, Integer, String> {
        /**
         * @param title - Stores the title of the searched movie
         * @param year - Stores the year of the searched movie
         * @param rated - Stores the rating of the searched movie
         * @param released - Stores the release date of the searched movie
         * @param runtime - Stores the runtime of the searched movie
         * @param genre - Stores the genre of the searched movie
         * @param director - Stores the director of the searched movie
         * @param actors - Stores the actors of the searched movie
         * @param plot - Stores the plot of the searched movie
         * @param poster - Stores the poster URL as a string for database storage
         * @param posterURL - Stores the poster URL of the searched movie
         * @param posterPic - Stores the image downloaded from the poster URL or from the device
         */
        String title;
        String year;
        String rated;
        String released;
        String runtime;
        String genre;
        String director;
        String actors;
        String plot;
        String poster;
        URL posterURL;
        Bitmap posterPic;


        /**
         * If a new movie is being searched it Connects to Site, searches device to see if poster
         * has already been downloaded and executes the xml parser. If a save movie is selected
         * it accesses the database and retrieves the information.
         *
         * @param strings
         * @return
         */
        @Override
        protected String doInBackground(String... strings) {

            if (option == 1) {
                try {
                    URL url = new URL("http://www.omdbapi.com/?apikey=6c9862c2&r=xml&t=" + movieName);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    parse(conn.getInputStream());


                    if (fileExistance(title + ".jpg") == false) {

                        posterURL = new URL(poster);
                        posterPic = getImage(posterURL);


                        FileOutputStream outputStream = openFileOutput(title + ".jpg", Context.MODE_PRIVATE);
                        posterPic.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        Log.i(TAG, "poster did not exist and was downloaded");
                    } else {
                        FileInputStream fis = null;
                        try {
                            fis = openFileInput(title + ".jpg");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        posterPic = BitmapFactory.decodeStream(fis);

                        Log.i(TAG, "poster was already saved and was accessed from device");

                    }


                } catch (Exception e) {
                    Log.i(TAG, "error");
                }

            } else if (option == 2) {

                db2 = dbHelper2.getReadableDatabase();

                Cursor c = db2.rawQuery("select * from Movies", null);
                c.moveToFirst();
                while (!c.isAfterLast()) {


                    if (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_ID)).equals(movieName)) {

                        title = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_ID)));
                        year = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_year)));
                        rated = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_rated)));
                        released = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_released)));
                        runtime = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_runtime)));
                        genre = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_genre)));
                        director = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_director)));
                        actors = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_actors)));
                        plot = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_plot)));
                        poster = (c.getString(c.getColumnIndex(M2DatabaseHelper.Key_url)));
                    }
                    c.moveToNext();
                }

            }

            return "true";
        }

        /**
         * Searches local device for a file
         *
         * @param fname Retrieves the file name being looked for
         * @return returns true if the file exists, false if not
         */
        public boolean fileExistance(String fname) {
            File file = getBaseContext().getFileStreamPath(fname);
            return file.exists();
        }

        /**
         * Connects to the post URL and downloads the image
         *
         * @param url Retrieves the poster URL
         * @return returns the downloaded image
         */
        public Bitmap getImage(URL url) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                return BitmapFactory.decodeStream(connection.getInputStream());

            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        /**
         * Updates the progress bar
         *
         * @param values Receives the amount the bar should increment
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            movieBar.setVisibility(View.VISIBLE);
            movieBar.setProgress(values[0]);

        }

        /**
         * Generates a dialog box containing the info of the movie searched.
         * <p>
         * If a search for a new movie was performed the option to save is given, if saved the info
         * is written to the database.
         * <p>
         * If a saved movie is searched the info is displayed from the database and an option to
         * delete is given, if deleted the information is removed from the database and ListView.
         *
         * @param s Executes after everything else has finished
         */
        @Override
        protected void onPostExecute(String s) {

            movieBar.setVisibility(View.INVISIBLE);
            if (option == 1 && title == null) {
                Toast toast = Toast.makeText(Member2MainActivity.this, getString(R.string.m2_noMovie), Toast.LENGTH_SHORT);
                toast.show();
                movieSearch.setText("");

            } else if (option == 1 && title != null) {

                AlertDialog.Builder builder2 = new AlertDialog.Builder(Member2MainActivity.this);
                View view2 = getLayoutInflater().inflate(R.layout.m2snackbar, null);

                builder2.setView(view2);
                final TextView title_view = view2.findViewById(R.id.title_m2);
                final TextView year_view = view2.findViewById(R.id.year_m2);
                final TextView rated_view = view2.findViewById(R.id.rated_m2);
                final TextView released_view = view2.findViewById(R.id.released_m2);
                final TextView runtime_view = view2.findViewById(R.id.runtime_m2);
                final TextView genre_view = view2.findViewById(R.id.genre_m2);
                final TextView director_view = view2.findViewById(R.id.director_m2);
                final TextView actors_view = view2.findViewById(R.id.actors_m2);
                final TextView plot_view = view2.findViewById(R.id.plot_m2);
                final ImageView picture = view2.findViewById(R.id.poster_m2);

                picture.setImageBitmap(posterPic);
                title_view.setText(getString(R.string.m2_Title) + title);
                year_view.setText(getString(R.string.m2_Year) + year);
                rated_view.setText(getString(R.string.m2_Rated) + rated);
                released_view.setText(getString(R.string.m2_Released) + released);
                runtime_view.setText(getString(R.string.m2_Runtime) + runtime);
                genre_view.setText(getString(R.string.m2_Genre) + genre);
                director_view.setText(getString(R.string.m2_Director) + director);
                actors_view.setText(getString(R.string.m2_Actors) + actors);
                plot_view.setText(getString(R.string.m2_Plot) + plot);


                builder2.setNegativeButton(getString(R.string.m2_back), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        movieSearch.setText("");
                    }
                });

                builder2.setPositiveButton(getString(R.string.m2_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        db2 = dbHelper2.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(M2DatabaseHelper.Key_ID, title);
                        contentValues.put(M2DatabaseHelper.Key_year, year);
                        contentValues.put(M2DatabaseHelper.Key_rated, rated);
                        contentValues.put(M2DatabaseHelper.Key_released, released);
                        contentValues.put(M2DatabaseHelper.Key_runtime, runtime);
                        contentValues.put(M2DatabaseHelper.Key_genre, genre);
                        contentValues.put(M2DatabaseHelper.Key_director, director);
                        contentValues.put(M2DatabaseHelper.Key_actors, actors);
                        contentValues.put(M2DatabaseHelper.Key_plot, plot);
                        contentValues.put(M2DatabaseHelper.Key_url, poster);

                        db2.insert(M2DatabaseHelper.Key_table, null, contentValues);
                        movieArray.add(title);
                        arrayAdapter.notifyDataSetChanged();
                        movieSearch.setText("");
                        Toast toast = Toast.makeText(Member2MainActivity.this, getString(R.string.m2_saveMovie), Toast.LENGTH_SHORT);
                        toast.show();

                    }
                });

                AlertDialog dialog2 = builder2.create();
                dialog2.show();

            } else if (option == 2) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(Member2MainActivity.this);
                View view2 = getLayoutInflater().inflate(R.layout.m2snackbar, null);
                builder2.setView(view2);
                final TextView title_view = view2.findViewById(R.id.title_m2);
                final TextView year_view = view2.findViewById(R.id.year_m2);
                final TextView rated_view = view2.findViewById(R.id.rated_m2);
                final TextView released_view = view2.findViewById(R.id.released_m2);
                final TextView runtime_view = view2.findViewById(R.id.runtime_m2);
                final TextView genre_view = view2.findViewById(R.id.genre_m2);
                final TextView director_view = view2.findViewById(R.id.director_m2);
                final TextView actors_view = view2.findViewById(R.id.actors_m2);
                final TextView plot_view = view2.findViewById(R.id.plot_m2);
                final ImageView picture = view2.findViewById(R.id.poster_m2);


                picture.setImageBitmap(posterPic);
                title_view.setText(getString(R.string.m2_Title) + title);
                year_view.setText(getString(R.string.m2_Year) + year);
                rated_view.setText(getString(R.string.m2_Rated) + rated);
                released_view.setText(getString(R.string.m2_Released) + released);
                runtime_view.setText(getString(R.string.m2_Runtime) + runtime);
                genre_view.setText(getString(R.string.m2_Genre) + genre);
                director_view.setText(getString(R.string.m2_Director) + director);
                actors_view.setText(getString(R.string.m2_Actors) + actors);
                plot_view.setText(getString(R.string.m2_Plot) + plot);

                FileInputStream fis = null;
                try {
                    fis = openFileInput(title + ".jpg");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                posterPic = BitmapFactory.decodeStream(fis);
                picture.setImageBitmap(posterPic);

                builder2.setNegativeButton(getString(R.string.m2_back), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });

                builder2.setPositiveButton(getString(R.string.m2_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db2 = dbHelper2.getWritableDatabase();
                        movieArray.remove(movieName);
                        arrayAdapter.notifyDataSetChanged();
                        db2.delete("Movies", "Name = ?", new String[]{movieName});
                        Toast toast = Toast.makeText(Member2MainActivity.this, getString(R.string.m2_deleteMovie), Toast.LENGTH_SHORT);
                        toast.show();

                    }
                });


                AlertDialog dialog2 = builder2.create();
                dialog2.show();

            }


        }

        /**
         * Creates the XML pull parser
         *
         * @param in Recieves the input stream
         * @throws XmlPullParserException
         * @throws IOException
         * @throws InterruptedException
         */
        public void parse(InputStream in) throws XmlPullParserException, IOException, InterruptedException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                readFeed(parser);


            } finally {
                in.close();
            }
        }

        /**
         * Reads the information gathered by the pull parser. Iterates through it looking for
         * specified fields and retrieving their attributes.
         *
         * @param parser Specifies the pull parser to be read
         * @throws XmlPullParserException
         * @throws IOException
         * @throws InterruptedException
         */
        private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException, InterruptedException {
            parser.require(XmlPullParser.START_TAG, null, "root");
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = parser.getName();

                    if (name.equals("movie")) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {

                            if (parser.getAttributeName(i).equals("title")) {
                                title = parser.getAttributeValue(i);
                                publishProgress(10);
                                Thread.sleep(50);


                            } else if (parser.getAttributeName(i).equals("year")) {
                                year = parser.getAttributeValue(null, "year");
                                publishProgress(20);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("rated")) {
                                rated = parser.getAttributeValue(null, "rated");
                                publishProgress(30);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("released")) {
                                released = parser.getAttributeValue(null, "released");
                                publishProgress(40);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("runtime")) {
                                runtime = parser.getAttributeValue(null, "runtime");
                                publishProgress(50);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("genre")) {
                                genre = parser.getAttributeValue(null, "genre");
                                publishProgress(60);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("director")) {
                                director = parser.getAttributeValue(null, "director");
                                publishProgress(70);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("actors")) {
                                actors = parser.getAttributeValue(null, "actors");
                                publishProgress(80);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("plot")) {
                                plot = parser.getAttributeValue(null, "plot");
                                publishProgress(90);
                                Thread.sleep(50);

                            } else if (parser.getAttributeName(i).equals("poster")) {
                                poster = parser.getAttributeValue(null, "poster");
                                publishProgress(100);
                            }

                        }
                    }


                }
                eventType = parser.next();
            }
        }


    }

    /**
     * Creates the toolbar
     *
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(2).setVisible(false);
        toolitem.setHelpTitle(getString(R.string.m2_help_title));
        toolitem.setHelpMessage(getString(R.string.m2_help_message));
        return true;
    }

    /**
     * Loads the activity that was selected on the toolbar
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        intent = toolitem.onToolbarItemSelected(item);
        if (intent != null) {
            startActivity(intent);
            Member2MainActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
