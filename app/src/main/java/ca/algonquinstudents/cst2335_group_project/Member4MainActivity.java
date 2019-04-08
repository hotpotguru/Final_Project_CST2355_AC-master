package ca.algonquinstudents.cst2335_group_project;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.ML_TABLE_NAME;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.KEY_ID;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.ROUTE;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.STOP_CODE;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.STOP_NAME;

/**
 * @author Xue Nian Jiang
 *
 * Start activity of OC bus route (Member4: Xue Nian Jiang)
 */

public class Member4MainActivity extends AppCompatActivity {

    protected static M4OCDataBaseHelper dbOCTranHelper = null;
    protected static SQLiteDatabase db;

    protected static final String ACTIVITY_NAME = "Activity_Member4_main";

    private ArrayList<String> statNameList = new ArrayList<>();
    private StationAdapter stAdapter;

    private Cursor c;

    private boolean frameExists;
    private boolean isFirstClick = true;

    private TextView listTitle;
    private EditText searchText;
    private int searchMethod;

    private ToolbarMenu toolItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member4_main);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm4);
        setSupportActionBar(toolbar);

        toolItem = new ToolbarMenu(Member4MainActivity.this);

        RadioButton rBtn1 = findViewById(R.id.radioSearchStationNumber);
        RadioButton rBtn2 = findViewById(R.id.radioSearchStationName);
        RadioButton rBtn3 = findViewById(R.id.radioSearchBusNumber);

        searchText = (EditText)findViewById(R.id.SearchTextM4);

        //initial radio button and set listener
        if (!(rBtn1.isChecked()||rBtn2.isChecked()||rBtn3.isChecked())) {
            rBtn3.setChecked(true);
            searchMethod = 1;
            searchText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        rBtn1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                searchMethod = 2;
                searchText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        rBtn2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                searchMethod = 3;
                searchText.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        });

        rBtn3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                searchMethod = 1;
                searchText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        //check for fragment
        frameExists = (findViewById(R.id.frameLayoutdetailsmainm4)!=null);

        //setup list view
        listTitle = findViewById(R.id.ListViewNameM4);
        ListView stationView = findViewById(R.id.ListViewM4);
        Button searchBtn = findViewById(R.id.SearchButtonM4);

        listTitle.setText(getString(R.string.m4_lv_mylist));
        stAdapter = new StationAdapter(this);
        stationView.setAdapter(stAdapter);

        // create connection to database once for whole OC activity
        if (dbOCTranHelper == null) {
            dbOCTranHelper = new M4OCDataBaseHelper(this);
            db = dbOCTranHelper.getWritableDatabase();
        }

        // get My List from database and show on list view
        refreshMessageCursorAndListView();

        //set search button listener
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = searchText.getText().toString();

                if(text.trim().isEmpty()||text==null)
                    return;

                Bundle infoToPass = new Bundle();
                infoToPass.putString("SearchContent", text);
                infoToPass.putInt("SearchMethod", searchMethod);
                Intent intent = new Intent(Member4MainActivity.this, Member4SearchActivity.class);
                intent.putExtras(infoToPass); //send info
                startActivity(intent);
            }
        });

        //set list view item selection listener
        stationView.setOnItemClickListener(new AdapterView.OnItemClickListener( ) {
            @Override
            public void onItemClick(AdapterView<?> adpV, View v, int i, long l) {

                String[] msg = statNameList.get( i ).split(";");
                long id = stAdapter.getItemId(i);

                Bundle infoToPass = new Bundle();
                infoToPass.putString("StationNumber", msg[0]);
                infoToPass.putString("BusLine", msg[1]);
                infoToPass.putString("StationName", msg[2]);
                infoToPass.putLong("ID", id);
                infoToPass.putLong("Position", i);
                infoToPass.putBoolean("Removable", true);

                if(frameExists){
                    //for frame using fragment
                    if (isFirstClick)
                        isFirstClick=false;
                    else
                        getFragmentManager().popBackStack();

                    M4MessageFragment newFragment = new M4MessageFragment();
                    newFragment.iAmTablet = true;

                    newFragment.setArguments( infoToPass ); //give information to bundle

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ftrans = fm.beginTransaction();
                    ftrans.replace(R.id.frameLayoutdetailsmainm4, newFragment); //load a fragment into the framelayout
                    ftrans.addToBackStack("name doesn't matter"); //changes the back button behaviour
                    ftrans.commit(); //actually load it
                }
                else{
                    //no frame using individual activity to display details
                    Intent intent = new Intent(Member4MainActivity.this, M4BusStopDetailsActivity.class);
                    intent.putExtras(infoToPass); //send info
                    startActivity(intent);
                }
            }
        });
        Snackbar.make(searchBtn, R.string.m4_snackbar_search, Snackbar.LENGTH_LONG).show();
    }

    // setup toolbar menu
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(4).setVisible(false);
        toolItem.setHelpTitle(getString(R.string.m4_help_title));
        toolItem.setHelpMessage(getString(R.string.m4_help_message));
        return true;
    }

    // action on selected toolbar menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Intent intent = toolItem.onToolbarItemSelected(item);
        if( intent != null) {
            startActivity(intent);
            Member4MainActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    /**
     * query the database for My List and show the result in list view
     */
    public void refreshMessageCursorAndListView() {
        statNameList.clear();
        stAdapter.notifyDataSetChanged();
        c = db.rawQuery("SELECT * from " + ML_TABLE_NAME + " where " + KEY_ID + " > ?", new String[]{"0"});
        Log.i(ACTIVITY_NAME, "Cursor's column count = " + c.getColumnCount());
        for (int i = 0; i < c.getColumnCount(); i++)
            Log.i(ACTIVITY_NAME, "Cursor's column name: " + c.getColumnName(i));
        c.moveToFirst();
        while (!c.isAfterLast()) {
            statNameList.add(c.getString(c.getColumnIndex(STOP_CODE))+";"+c.getString(c.getColumnIndex(ROUTE))+";"+c.getString(c.getColumnIndex(STOP_NAME)));
            stAdapter.notifyDataSetChanged();
            c.moveToNext();
        }
    }

    /**
     * this class extends ArrayAdapter and is the adapter to be set on my list view
     */
    private class StationAdapter extends ArrayAdapter<String> {
        public StationAdapter(Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return statNameList.size();
        }

        public String getItem(int position) {
            return statNameList.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = Member4MainActivity.this.getLayoutInflater();
            View result = null;
            result = inflater.inflate(R.layout.list_row_content_m4, null);
            TextView stationNumber = (TextView) result.findViewById(R.id.StationNumberM4);
            TextView busline = (TextView) result.findViewById(R.id.RouteNumberM4);
            TextView stationName = (TextView) result.findViewById(R.id.StationNameM4);
            String[] items = getItem(position).split(";");
            stationNumber.setText(items[0]);
            busline.setText(items[1]);
            stationName.setText(items[2]);
            return result;
        }

        public long getItemId(int position){
                c.moveToPosition(position);
                return c.getLong(c.getColumnIndex(KEY_ID));
        }
    }
}
