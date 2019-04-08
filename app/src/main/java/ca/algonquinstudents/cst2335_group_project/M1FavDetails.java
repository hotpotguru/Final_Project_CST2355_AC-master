package ca.algonquinstudents.cst2335_group_project;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.os.Bundle;
import android.widget.FrameLayout;

import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1TAG;
import static ca.algonquinstudents.cst2335_group_project.Member1MainActivity.db1;

public class M1FavDetails extends Activity {


    /**
     * the frame layout to fill with fragment
     */
    FrameLayout fl;

    /**
     * sets fragment and initialized database in case we need to access it
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fl = findViewById(R.id.m1PhoneFrameLayout);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1_fav_details);

        Bundle infoToPass =  getIntent().getExtras();
        M1FavFragment newFragment = new M1FavFragment();

        newFragment.setArguments( infoToPass ); //give information to bundle

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ftrans = fm.beginTransaction();
        ftrans.replace(R.id.m1PhoneFrameLayout, newFragment); //load a fragment into the framelayout
        ftrans.addToBackStack("name doesn't matter"); //changes the back button behaviour
        ftrans.commit(); //actually load it

    }

    /**
     * called if a tag is added while on a phone
     * @param name item to tag
     * @param tag to add
     */

    public void addATag(String name, String tag){
        ContentValues cv = new ContentValues();
        cv.put(M1TAG, tag);
        db1.update(M1DatabaseHelper.M1_TABLE_NAME, cv, "Name = ?", new String[]{name});
        //db.up
    }
}
