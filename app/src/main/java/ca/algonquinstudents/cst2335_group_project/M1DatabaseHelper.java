package ca.algonquinstudents.cst2335_group_project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class M1DatabaseHelper extends SQLiteOpenHelper {
    /**
     * static variables for ease of manipulating database
     */
    protected static String M1_DATABASE_NAME = "Feed.db";
    protected static int M1_VERSION_NUM = 2;
    protected static String M1_TABLE_NAME = "FAVOURITES";

    public final static String M1KEY_ID = "key_id";
    public final static String M1KEY_NAME = "Name";
    public final static String M1CALORIES = "Calories";
    public final static String M1FAT = "Fat";
    public final static String M1TAG = "Tag";

    /**
     * default constructor
     * @param ctx
     */

    public M1DatabaseHelper(Context ctx){
        super(ctx, M1_DATABASE_NAME, null, M1_VERSION_NUM);
    }

    /**
     * creates the favourites table in the nutrition database
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " +M1_TABLE_NAME+"("+M1KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+M1KEY_NAME+" text, "
                +M1CALORIES+" text, "+M1FAT+" text, "+M1TAG+" text);");
        Log.i("OCDatabaseHelper", "Calling onCreate");
    }

    /**
     * recreates DB in case of an upgrade
     * @param db
     * @param oldVer
     * @param newVer
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer){
        db.execSQL("DROP TABLE IF EXISTS "+M1_TABLE_NAME);
        onCreate(db);
        Log.i("M1DatabaseHelper", "Calling onUpgrade, oldVersion=" + oldVer + " newVersion=" + newVer);
    }
}
