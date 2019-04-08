package ca.algonquinstudents.cst2335_group_project;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Xue Nian Jiang
 *
 * Display the details of the selected item from My List or search result
 * using the fragment in individul activity which have no frame.
 */

public class M4BusStopDetailsActivity extends AppCompatActivity {

    private ToolbarMenu toolitem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_details_m4);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm4);
        setSupportActionBar(toolbar);

        toolitem = new ToolbarMenu(M4BusStopDetailsActivity.this);
        Bundle infoToPass =  getIntent().getExtras(); //get passed information

        M4MessageFragment newFragment = new M4MessageFragment();
        newFragment.iAmTablet = false;
        newFragment.setArguments( infoToPass ); //give information to bundle

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ftrans = fm.beginTransaction();
        ftrans.replace(R.id.frameLayoutdetailsm4, newFragment); //load a fragment into the framelayout
        ftrans.commit(); //actually load it
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(4).setVisible(false);
        toolitem.setHelpTitle(getString(R.string.m4_help_title));
        toolitem.setHelpMessage(getString(R.string.m4_help_message));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = toolitem.onToolbarItemSelected(item);
        if( intent != null) {
            startActivity(intent);
            M4BusStopDetailsActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
