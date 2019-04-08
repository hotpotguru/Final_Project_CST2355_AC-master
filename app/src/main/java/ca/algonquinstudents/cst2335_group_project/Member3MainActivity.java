package ca.algonquinstudents.cst2335_group_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 *  Start activity of CBC news reader (Member3: Vithura Sribalachandran)
 */


public class Member3MainActivity extends AppCompatActivity {

    private ToolbarMenu toolitem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member3_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm3);
        setSupportActionBar(toolbar);

        TextView comingSoon = findViewById(R.id.textComingSoonM3);
        comingSoon.setText("Coming Soon ......\n\nBy "+getString(R.string.member3_name));

        toolitem = new ToolbarMenu(Member3MainActivity.this);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(3).setVisible(false);
        toolitem.setHelpTitle(getString(R.string.m3_help_title));
        toolitem.setHelpMessage(getString(R.string.m3_help_message));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Intent intent = toolitem.onToolbarItemSelected(item);
        if( intent != null) {
            startActivity(intent);
            Member3MainActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
