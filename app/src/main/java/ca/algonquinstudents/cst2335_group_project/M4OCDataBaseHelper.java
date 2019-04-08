package ca.algonquinstudents.cst2335_group_project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Xue Nian Jiang   (Credit for online tutorials)
 *
 * Database Helper class for installing the OC SQLite database
 * The pre-build database: OCRouteStop.db will be distributed with APk
 * If this database is not exist or need upgrade, the database will be copied from assets
 * There are two tables in database: RouteStopTable and UserBusStopList
 * RouteStopTable is generated from google_transit.zip which is download from OC website
 *                  search this table by route number, stop code or stop name
 *                  user can easily get a search result list of route, stop code and stop name
 * UserBusStopList has the same structure as the RouteStopTable and keeps user maintained list: My List
 */

public class M4OCDataBaseHelper extends SQLiteOpenHelper {
    /**
     * @param OC_DATABASE_NAME - Holds the name of the database
     * @param OC_VERSION_NUM - Keeps track of the current version
     * @param OC_TABLE_NAME - Holds the name of the OC route stop table
     * @param ML_TABLE_NAME - Holds the name of the user my list table
     *
     * @param KEY_ID - Holds the name of the ID column in the tables
     * @param ROUTE - Holds the name of the route column in the tables
     * @param STOP_CODE - Holds the name of the stop code column in the tables
     * @param STOP_NAME - Holds the name of the stop name column in the tables
     */

    protected static String OC_DATABASE_NAME = "OCRouteStop.db";
    protected static int OC_VERSION_NUM = 1;
    protected static String OC_TABLE_NAME = "RouteStopTable";
    protected static String ML_TABLE_NAME = "UserBusStopList";

    public final static String KEY_ID = "List_id";
    public final static String ROUTE = "Route";
    public final static String STOP_CODE = "StopCode";
    public final static String STOP_NAME = "StopName";

    private Context ctx;

    private boolean createDb = false, upgradeDb = false;

    public M4OCDataBaseHelper(Context ctx){
        super(ctx, OC_DATABASE_NAME, null, OC_VERSION_NUM);
        this.ctx = ctx;
    }

    /**
     * Copy the Database from assets to current working environment
     *
     * @param db
     */
    private void copyDatabaseFromAssets(SQLiteDatabase db) {
        Log.i("M4OCDataBaseHelper", "copy OC Database");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = ctx.getAssets().open(OC_DATABASE_NAME);
            outputStream = new FileOutputStream(db.getPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            SQLiteDatabase copiedDb = ctx.openOrCreateDatabase(OC_DATABASE_NAME, 0, null);
            copiedDb.execSQL("PRAGMA user_version = " + OC_VERSION_NUM);
            copiedDb.close();
        } catch (IOException e) {
            Log.i("M4OCDataBaseHelper", "OC Database load failure.");
            e.printStackTrace();
            throw new Error("M4OCDataBaseHelper" + " Error copying database");
        } finally {
            // Close the streams
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("M4OCDataBaseHelper" + " Error closing streams");
            }
        }
    }

    /**
     * the Database did not exist, set the copy db flag.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        Log.i("M4OCDataBaseHelper", "onCreate db");
        createDb = true;
    }

    /**
     * the Database needs upgrade, set the upgrade db flag.
     *
     * @param db
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer){
        Log.i("M4OCDataBaseHelper", "onUpgrade db "+oldVer+" to "+newVer);
        upgradeDb = true;
    }

    /**
     * Check the Database copy and upgrade flag to do the copy db.
     *
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i("M4OCDataBaseHelper", "onOpen db");
        if (createDb || upgradeDb) {
            createDb = false;
            upgradeDb = false;
            copyDatabaseFromAssets(db);
        }
    }
}
