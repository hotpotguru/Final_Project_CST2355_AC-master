package ca.algonquinstudents.cst2335_group_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;

/**
 * @author Xue Nian Jiang
 *
 * Toolbar Menu class to handle the toolbar issue for the whole project
 */

public class ToolbarMenu {
    private Context context;
    private String helpTitle, aboutTitle;
    private String helpMessage, aboutMessage;

    ToolbarMenu(Context context) {
        this.context = context;
    }

    // set help title for each activity
    public void setHelpTitle(String title){
        helpTitle = title;
    }

    // set help message for each activity
    public void setHelpMessage(String message){
        helpMessage = message;
    }

    // set about title for each activity
    public void setAboutTitle(String title){
        aboutTitle = title;
    }

    // set about message for each activity
    public void setAboutMessage(String message){
        aboutMessage = message;
    }

    // handle selected toolbar menu item
    public Intent onToolbarItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.food_item_m1:
                intent = new Intent(context, Member1MainActivity.class);
                break;
            case R.id.movie_item_m2:
                intent = new Intent(context, Member2MainActivity.class);
                break;
            case R.id.cbc_item_m3:
                intent = new Intent(context, Member3MainActivity.class);
                break;
            case R.id.octrans_item_m4:
                intent = new Intent(context, Member4MainActivity.class);
                break;
            case R.id.help_menu_item:
                showMessageDialog( helpTitle, helpMessage);
                break;
            case R.id.about_menu_item:
                showMessageDialog( context.getString(R.string.project_title), getAboutMessage());
                break;
            default:
                intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }
        return intent;
    }

    // show help or about dialog
    public void showMessageDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.positive_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create().show();
    }

    public String getAboutMessage() {
        String msg = context.getString(R.string.version_info) + "\n";
        msg += "\n" + context.getString(R.string.course_message) + "\n";
        msg += "\n" + context.getString(R.string.group_message);
        msg += "\n" + context.getString(R.string.button1_name)+" - "+context.getString (R.string.member1_name);
        msg += "\n" + context.getString(R.string.button2_name)+" - "+context.getString(R.string.member2_name);
        msg += "\n" + context.getString(R.string.button3_name)+" - "+context. getString(R.string.member3_name);
        msg += "\n" + context.getString(R.string.button4_name)+" - "+context.getString(R.string.member4_name);
        return msg;
    }
}
