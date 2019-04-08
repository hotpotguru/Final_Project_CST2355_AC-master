package ca.algonquinstudents.cst2335_group_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1CALORIES;
import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1FAT;
import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1KEY_NAME;
import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1TAG;
import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1_TABLE_NAME;
import static ca.algonquinstudents.cst2335_group_project.Member1MainActivity.db1;

public class M1FavActivity extends Activity {
    /**
     * static convenience variables for database access and logging purpsoes
     */
//    protected static String TABLE_NAME = "FAVOURITES";
//    private final static String KEY_NAME = "Name";
//    private final static String CALORIES = "Calories";
//    private final static String FAT = "Fat";
    public final static String ACTIVITY_NAME = "M1FavActivity";

    /**
     * will evaluate to true if using a tablet/landscape layout, if so fl will be set to that frame layout
     */
    boolean frameExists = false;
    FrameLayout fl;


    /**
     * database management variables
     */
    Cursor c;

    /**
     * variables for dealing with the favourites
     */
    ListView favs;
    private ArrayList<String[]> favData = new ArrayList<>();
    M1FavFragment f;
    FavAdapter favAdapter;

    Button backButton, searchByTagButton;
    EditText searchBar;

    int currentPosition;

    /**
     * sets view, finds important elements from the view, sets listeners for them
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m1_fav);
        favs = findViewById(R.id.m1Favs);

        favAdapter = new FavAdapter(M1FavActivity.this);
        favs.setAdapter(favAdapter);

        backButton=findViewById(R.id.m1BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(M1FavActivity.this, Member1MainActivity.class);
                startActivity(intent);
                M1FavActivity.this.finish();
            }
        });


        searchByTagButton=findViewById(R.id.m1SearchFavsButton);
        searchBar=findViewById(R.id.m1SearchFavsBar);

        searchByTagButton.setOnClickListener((s)->{
            searchByTag();
        });


        if (findViewById(R.id.m1FrameLayout) != null) {
            frameExists=true;
            fl = findViewById(R.id.m1FrameLayout);
        }
        else{
            Log.i(ACTIVITY_NAME, "frameExists staying null");
        }

        c = db1.rawQuery("select * from "+M1_TABLE_NAME, null);
        c.moveToFirst();
        while(!c.isAfterLast() ) {

            String temp = c.getString(c.getColumnIndex(M1DatabaseHelper.M1KEY_NAME));
            favAdapter.add(c.getString(c.getColumnIndex(M1DatabaseHelper.M1KEY_NAME)));
            //Log.i(ACTIVITY_NAME, "SQL MESSAGE:" + c.getString(c.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE)));

            c.moveToNext();
        }
        refreshFavCursorAndListView();
        favAdapter.notifyDataSetChanged();

        favs.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {

                currentPosition=position;
                Bundle infoToPass = new Bundle();

                infoToPass.putString("Name", (favData.get(position))[0]);
                infoToPass.putString("Calories", (favData.get(position))[1]);
                infoToPass.putString("Fat", (favData.get(position))[2]);
                infoToPass.putString("Tag", (favData.get(position))[3]);
                //Log.i(ACTIVITY_NAME, "Id: "+id+" position: "+position);

                //currentPosition=position;
                if(frameExists){
                    Log.i(ACTIVITY_NAME, "tablet onclick");
                    //not on phone
                    f = new M1FavFragment();
                    f.setIsTablet(true);
                    f.setChatWindow(M1FavActivity.this);
                    f.setArguments(infoToPass);
                    FragmentTransaction t = getFragmentManager().beginTransaction();

                    //fl = findViewById(R.id.frameLayout);
                    t.replace(R.id.m1FrameLayout, f);
                    t.commit();

                }
                else{
                    Intent i = new Intent(M1FavActivity.this, M1FavDetails.class);
                    i.putExtras(infoToPass);
                    startActivityForResult(i, 1);
                    //on phone

                }
            }
        });






    }


    /**
     * clears cursor, listview. Repopulates with contents of the database.
     */
    public void refreshFavCursorAndListView() {

        favData.clear();
        favAdapter.clear();

        c = db1.rawQuery("SELECT * from " + M1_TABLE_NAME, null);
            Log.i(ACTIVITY_NAME, "Cursor's column count = " + c.getColumnCount());
            for (int i = 0; i < c.getColumnCount(); i++)
                Log.i(ACTIVITY_NAME, "Cursor's column name: " + c.getColumnName(i));

            String name, calories, fat, tag;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                name = c.getString(c.getColumnIndex(M1KEY_NAME));
                calories = Integer.toString(c.getInt(c.getColumnIndex(M1CALORIES)));
                fat = Integer.toString(c.getInt(c.getColumnIndex(M1FAT)));
                tag = c.getString(c.getColumnIndex(M1TAG));
                String[] viewRow = {name, calories, fat, tag};
                favData.add(viewRow);
                c.moveToNext();
            }
    }

    /**
     * removes item from both list view and database
     * @param name item to remove from DB
     */
    public void deleteItem(String name){
        favData.remove(currentPosition);
        db1.delete(M1_TABLE_NAME, "Name = ?", new String[]{name});
        favAdapter.notifyDataSetChanged();

    }

    /**
     * updates database entry, refreshes list view
     * @param name the food to tag
     * @param tag the tag to update
     */
    public void addATag(String name, String tag){
        ContentValues cv = new ContentValues();
        cv.put(M1TAG, tag);
        db1.update(M1_TABLE_NAME, cv, "Name = ?", new String[]{name});
        refreshFavCursorAndListView();
        //db.up
    }

    /**
     * selects all foods from the DB that are tagged with the user's requested tag
     * loops through these foods to find their min, max, total and avg calories
     */
    public void searchByTag(){
        String tag = searchBar.getText().toString();
        searchBar.setText("");
        if(!tag.isEmpty()) {
            //refreshFavCursorAndListView(true, tag);
            String query = "select * from FAVOURITES where Tag = \"" + tag + "\"";
            Cursor t = db1.rawQuery(query, null);
            if (t.getCount() != 0) {
                int smallest = 0, largest = 0, tally = 0, total = 0, avg = 0;
                t.moveToFirst();
                smallest = t.getInt(t.getColumnIndex(M1CALORIES));
                largest = t.getInt(t.getColumnIndex(M1CALORIES));


                while (!t.isAfterLast()) {
                    tally++;
                    int cals = t.getInt(t.getColumnIndex(M1CALORIES));
                    total += cals;
                    if (cals < smallest) {
                        smallest = cals;
                    }
                    if (cals > largest) {
                        largest = cals;
                    }
                    t.moveToNext();
                }
                avg = (total / tally);
                showResultDialog(tag, smallest, largest, total, avg);

            }
            else{
                Toast toast = Toast.makeText(M1FavActivity.this, R.string.m1NoResults, Toast.LENGTH_SHORT); //this is the ListActivity
                toast.show(); //display your message box

            }
        }
    }

    /**
     * shows the results of a "search by tag", including relevant statistics
     * @param tag
     * @param smallest
     * @param largest
     * @param total
     * @param avg
     */
    public void showResultDialog(String tag, int smallest, int largest, int total, int avg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(M1FavActivity.this);
        builder.setTitle(getText(R.string.m1ResultsForTag)+" "+tag);
        String message = getString(R.string.m1TotalCalories) +" "+ total + "\n"+getString(R.string.m1AvgCalories) + " "+avg + "\nMax: " +largest+"\nMin: "+smallest;
        builder.setMessage(message);
        builder.setPositiveButton(R.string.positive_ok, (dialog, id) -> {
        });

        builder.create().show();
    }

    /**
     * return from fragment - deletes the item from getExtras, if desired
     * @param requestCode
     * @param responseCode
     * @param data info about the item that was just selected
     */


    public void onActivityResult(int requestCode, int responseCode, Intent data){
        refreshFavCursorAndListView();
        if(requestCode==1&&responseCode==1){
            Bundle b = data.getExtras();
            String deleteName = b.getString("name");
            deleteItem(deleteName);
        }

    }

    /**
     * this class extends ArrayAdapter and is the adapter to be set on my list view
     */

    private class FavAdapter extends ArrayAdapter<Object>{

        /**
         * default constructor
         * @param ctx
         */
        public FavAdapter(Context ctx) {
            super(ctx, 0);
            //Log.i("ChatWindow", "constucting chatAdapter "+favs.size());
        }

        /**
         *
         * @return number of elements in the list view
         */
        public int getCount(){
            return favData.size();
        }

        /**
         *
         * @param position
         * @return the name, calories and fat of the item
         */
        public String getItem(int position) {
            String[] items = favData.get(position);
            return items[0]+";"+items[1]+";"+items[2];
        }

        /**
         *
         * @param position
         * @return database ID of the current position
         */

        public long getItemId(int position){
            c = db1.rawQuery("select * from "+M1_TABLE_NAME, null);
            int count = c.getCount();
            if(count>0) {
                c.moveToPosition(position);
                return c.getLong(c.getColumnIndex(M1DatabaseHelper.M1KEY_ID));
            }
            return 0;
        }

        /**
         *
         * @param position
         * @param convertView
         * @param parent
         * @return a row for the ListView
         */

        public View getView(int position, View convertView, ViewGroup parent){
            //temporary:
            LayoutInflater inflater = M1FavActivity.this.getLayoutInflater();

            View result = null ;

            result = inflater.inflate(R.layout.m1_fav_row, null);

            TextView favName = (TextView)result.findViewById(R.id.m1FavRowName);
            Log.i("ChatWindow", "message "+favName.toString());
            String s = getItem(position);
            s = s.substring(0, s.indexOf(';'));
            favName.setText(s); // get the string at position
            return result;

        }


    }

}
