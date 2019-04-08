package ca.algonquinstudents.cst2335_group_project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Galen Reid
 * Databse Helper class for creating the SQLite datbase
*/
public class M2DatabaseHelper extends SQLiteOpenHelper {
    /**
     * @param Database_name - Holds the name of the database
     * @param VERSION_NUM - Keeps track of the current version
     * @param Key_table - Holds the name of the the Movies table
     * @param Key_ID - Holds the name of the Name column in the movies table
     * @param Key_year - Holds the name of the year column in the movies table
     * @param Key_rated - Holds the name of the rated column in the movies table
     * @param Key_released - Holds the name of the released column in the movies table
     * @param Key_runtime - Holds the name of the runtime column in the movies table
     * @param Key_genre - Holds the name of the genre column in the movies table
     * @param Key_director - Holds the name of director Name column in the movies table
     * @param Key_actors - Holds the name of the actors column in the movies table
     * @param Key_plot - Holds the name of the plot column in the movies table
     * @param Key_url - Holds the name of the URL column in the movies table
     */

    public static String Database_name = "Movie.db";
    public static int VERSION_NUM = 6;
    public final static String Key_table = "Movies";
    public final static String Key_ID = "Name";
    public final static String Key_year = "year";
    public final static String Key_rated = "rated";
    public final static String Key_released = "released";
    public final static String Key_runtime = "runtime";
    public final static String Key_genre = "genre";
    public final static String Key_director = "dircector";
    public final static String Key_actors = "actors";
    public final static String Key_plot = "plot";
    public final static String Key_url = "url";

    /**
     * Creates the Database
     *
     * @param ctx
     */
    public M2DatabaseHelper(Context ctx) {
        super(ctx, Database_name, null, VERSION_NUM);
    }

    /**
     * Creates the Movies table
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(" CREATE TABLE " + Key_table + " (" +
                Key_ID + " TEXT PRIMARY KEY, " +
                Key_year + " TEXT NOT NULL, " +
                Key_rated + " TEXT NOT NULL, " +
                Key_released + " TEXT NOT NULL, " +
                Key_runtime + " TEXT NOT NULL, " +
                Key_genre + " TEXT NOT NULL, " +
                Key_director + " TEXT NOT NULL, " +
                Key_actors + " TEXT NOT NULL, " +
                Key_plot + " TEXT NOT NULL, " +
                Key_url + " TEXT NOT NULL);"
        );


    }

    /**
     * If the database it version is upgraded it drops the existing tables and recreates them
     *
     * @param db
     * @param oldVersion gets the previous version number of the database
     * @param newVersion gets the new version number of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("ChatDatabaseHelper", "Calling onUpgrade, oldVersion=" + oldVersion + " newVerison=" + newVersion);

        db.execSQL("DROP TABLE IF EXISTS " + "Movies");
        onCreate(db);
    }
}
