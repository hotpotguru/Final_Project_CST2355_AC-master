package ca.algonquinstudents.cst2335_group_project;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.ML_TABLE_NAME;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.OC_TABLE_NAME;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.KEY_ID;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.ROUTE;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.STOP_CODE;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.STOP_NAME;
import static ca.algonquinstudents.cst2335_group_project.Member4MainActivity.db;

/**
 * @author Xue Nian Jiang
 *
 * Activity for search and display the search result
 * Get the search parameters passed from the Member4MainActivity
 */

public class Member4SearchActivity extends AppCompatActivity {

    protected static final String ACTIVITY_NAME = "Activity_Member4_search";

    private ListView stopView;
    private ProgressBar progressBar;
    private ArrayList<String[]> searchResults = new ArrayList<>();

    private Cursor c;

    private boolean frameExists;
    private boolean isFirstClick = true;

    private StopBusAdapter sbAdapter;
    private ToolbarMenu toolitem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member4_search);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm4);
        setSupportActionBar(toolbar);

        toolitem = new ToolbarMenu(Member4SearchActivity.this);

        // get search parameters passed from previous activity
        Bundle infoPassed =  getIntent().getExtras();
        String searchContent = infoPassed.getString("SearchContent");
        int searchMethod = infoPassed.getInt("SearchMethod");

        //set list view
        stopView = findViewById(R.id.ListViewSearchM4);
        progressBar = findViewById(R.id.ProgressBarM4);

        sbAdapter = new Member4SearchActivity.StopBusAdapter(this);
        stopView.setAdapter(sbAdapter);

        //check for fragment
        frameExists = (findViewById(R.id.frameLayoutdetailsearchm4)!=null);

        // search database and display the search result in list view
        refreshMessageCursorAndListView(searchContent, searchMethod);

        //set list view selected item listener
        stopView.setOnItemClickListener(new AdapterView.OnItemClickListener( ) {
            @Override
            public void onItemClick(AdapterView<?> adpV, View v, int i, long l) {

                String[] msg = searchResults.get( i );
                long id = sbAdapter.getItemId(i);

                /* check if the selected item is in My List. then pass to next activity or fragment to enable / disable the remove / add button */
                boolean removeEnabled;
                Cursor cursor = db.rawQuery("SELECT * from " + ML_TABLE_NAME + " where " + KEY_ID + " = ?", new String[]{Long.toString(id)});
                removeEnabled = (cursor.getCount()>0);

                // pass the parameters of the selected item
                Bundle infoToPass = new Bundle();
                infoToPass.putString("StationNumber", msg[0]);
                infoToPass.putString("BusLine", msg[1]);
                infoToPass.putString("StationName", msg[2]);
                infoToPass.putLong("ID", id);
                infoToPass.putLong("Position", i);
                infoToPass.putBoolean("Removable", removeEnabled);

                // check for fragment back action
                if(frameExists){
                    if (isFirstClick)
                        isFirstClick=false;
                    else
                        getFragmentManager().popBackStack();

                    M4MessageFragment newFragment = new M4MessageFragment();
                    newFragment.iAmTablet = true;
                    newFragment.indexParent = 2;

                    newFragment.setArguments( infoToPass ); //give information to bundle

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ftrans = fm.beginTransaction();
                    ftrans.replace(R.id.frameLayoutdetailsearchm4, newFragment); //load a fragment into the framelayout
                    ftrans.addToBackStack("name doesn't matter"); //changes the back button behaviour
                    ftrans.commit(); //actually load it

                }
                else {
                    Intent intent = new Intent(Member4SearchActivity.this, M4BusStopDetailsActivity.class);
                    intent.putExtras(infoToPass); //send info
                    startActivity(intent);
                }
            }
        });
        Snackbar.make(progressBar, R.string.m4_snackbar_search, Snackbar.LENGTH_LONG).show();
    }

    // setup toolbar menu
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(4).setVisible(false);
        toolitem.setHelpTitle(getString(R.string.m4_help_title));
        toolitem.setHelpMessage(getString(R.string.m4_help_message));
        return true;
    }

    // action on selected toolbar menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = toolitem.onToolbarItemSelected(item);
        if( intent != null) {
            startActivity(intent);
            Member4SearchActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    /**
     * Search the database (RouteStopTable) table with selected method and show the search result in list view
     */
    private void refreshMessageCursorAndListView(String search, int method) {
        searchResults.clear();
        sbAdapter.notifyDataSetChanged();
        TextView listTitle = findViewById(R.id.ListViewSearchResultM4);
        String[] searchContent = {search};
        String[] methodName = new String[3];
        methodName[0] = getString(R.string.m4_bus_line);
        methodName[1] = getString(R.string.m4_station_number);
        methodName[2] = getString(R.string.m4_station_name);
        String searchMsg;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        searchMsg = getString(R.string.m4_searchbtn)+" "+methodName[method-1]+": \""+searchContent[0]+"\"";
        listTitle.setText(getString(R.string.m4_db_searching_wait)+"\n"+searchMsg);
        switch (method) {
            case 2:
                c = db.rawQuery("SELECT * from " + OC_TABLE_NAME + " where " + STOP_CODE + " = ?", searchContent);
                break;
            case 3:
                searchContent[0] = "%"+searchContent[0].toUpperCase()+"%";
                c = db.rawQuery("SELECT * from " + OC_TABLE_NAME + " where " + STOP_NAME + " LIKE  ?", searchContent);
                break;
            default:
                c = db.rawQuery("SELECT * from " + OC_TABLE_NAME + " where " + ROUTE + " = ?", searchContent);
        }
        Log.i(ACTIVITY_NAME, "Cursor's column count = " + c.getColumnCount());
        progressBar.setProgress(25);
        for (int i = 0; i < c.getColumnCount(); i++)
            Log.i(ACTIVITY_NAME, "Cursor's column name: " + c.getColumnName(i));
        c.moveToFirst();
        if (c.getCount()>0){
            listTitle.setText(getString(R.string.m4_db_search_result));
            int dP = 75/c.getCount();
            while (!c.isAfterLast()) {
                String[] viewRow = {c.getString(c.getColumnIndex(STOP_CODE)), c.getString(c.getColumnIndex(ROUTE)), c.getString(c.getColumnIndex(STOP_NAME))};
                searchResults.add(viewRow);
                sbAdapter.notifyDataSetChanged();
                c.moveToNext();
                progressBar.setProgress(progressBar.getProgress()+dP);
            }
        }
        else
            listTitle.setText(getString(R.string.m4_db_search_result)+getString(R.string.m4_db_search_result_no_match)+"\n"+searchMsg);
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * this class extends ArrayAdapter and is the adapter to be set on search result list view
     */
    private class StopBusAdapter extends ArrayAdapter<String> {
        public StopBusAdapter(Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return searchResults.size();
        }

        public String getItem(int position) {
            String[] items = searchResults.get(position);
            return items[0]+";"+items[1]+";"+items[2];
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = Member4SearchActivity.this.getLayoutInflater();
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
