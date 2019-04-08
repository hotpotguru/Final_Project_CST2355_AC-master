package ca.algonquinstudents.cst2335_group_project;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Integer.parseInt;

/**
 * @author Galen Reid
 * This activity is loaded when the help button is pushed. Gives info on stored movies, and provides
 * general use information
 */

public class M2Help extends Activity {

    /**
     * @param dbHelper2 - An object of the database helper class
     * @param db2 - An object of the SQLite database class
     * @param movieArray - stores the runtime of every saved movie to be accessed for calculations
     * @param yearArray - stores the year of every saved movie to be accessed for calculations
     * @param count - Increases each time the cursor iterates to give the total number of saved movies
     * @param small - stores the shortest found runtime in the movieArray
     * @param large - stores the longest found runtime in the movieArray
     * @param avg - stores the calculated average of the values in the movieArray
     * @param year - stores the calculated average of the values in the yearArray
     * @param saved - stores a reference the add button
     * @param shortest - stores a reference the shortest TextView
     * @param longest - stores a reference the longest TextView
     * @param average - stores a reference the average TextView
     * @param averageYear - stores a reference the average year TextView
     */
    public static M2DatabaseHelper dbHelper2;
    public static SQLiteDatabase db2;
    public static ArrayList<Integer> movieArray;
    public static ArrayList<Integer> yearArray;
    int count;
    int small;
    int large;
    int avg;
    int year;
    public static int calculate;
    TextView saved;
    TextView shortest;
    TextView longest;
    TextView average;
    TextView averageYear;

    /**
     * Loads the layout, creates a readable database, converts the runtime and year strings taken
     * from the database and converts them to integers to store in arrays. Performs calculations to find the smallest
     * largest and average values in the arrays.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m2_help);

        saved = findViewById(R.id.saved_m2);
        shortest = findViewById(R.id.shortest_m2);
        longest = findViewById(R.id.longest_m2);
        average = findViewById(R.id.average_m2);
        averageYear = findViewById(R.id.averageYear_m2);

        movieArray = new ArrayList<>();
        yearArray = new ArrayList<>();
        dbHelper2 = new M2DatabaseHelper(this);
        db2 = dbHelper2.getReadableDatabase();
        count = 0;
        year = 0;
        small = 0;
        large = 0;
        avg = 0;

        Cursor c = db2.rawQuery("select * from Movies", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            movieArray.add(Integer.parseInt((c.getString(c.getColumnIndex(M2DatabaseHelper.Key_runtime))).replaceAll("[^0-9]", "")));
            yearArray.add(Integer.parseInt((c.getString(c.getColumnIndex(M2DatabaseHelper.Key_year)))));

            count++;
            c.moveToNext();
        }
        if (movieArray.size() > 0) {
            small = movieArray.get(movieArray.indexOf(Collections.min(movieArray)));
            large = movieArray.get(movieArray.indexOf(Collections.max(movieArray)));

            for (int i = 0; i < movieArray.size(); i++) {
                avg += movieArray.get(i);
                year += yearArray.get(i);

            }
            avg = avg / movieArray.size();
            year = year / yearArray.size();
        }
        saved.setText(getString(R.string.m2_saveCount) + count);
        shortest.setText(getString(R.string.m2_saveShort) + small + " min.");
        longest.setText(getString(R.string.m2_saveLong) + large + " min.");
        average.setText(getString(R.string.m2_saveAvg) + avg + " min.");
        averageYear.setText(getString(R.string.m2_saveYear) + year);
    }
}
