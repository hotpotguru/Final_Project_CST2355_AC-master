package ca.algonquinstudents.cst2335_group_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.SparseArray;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.KEY_ID;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.ML_TABLE_NAME;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.ROUTE;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.STOP_CODE;
import static ca.algonquinstudents.cst2335_group_project.M4OCDataBaseHelper.STOP_NAME;
import static ca.algonquinstudents.cst2335_group_project.Member4MainActivity.db;

/**
 * this class defines behaviour for the fragment used to display details of a selected item of My List or search result list
 */

public class M4MessageFragment extends Fragment {

    private Activity parent = null;
    private SparseArray<Group> groups = new SparseArray<Group>();
    private View screen;
    private ProgressBar pBar;
    private String[] msgs = new String[3];
    private long idPassed;
    private boolean isInternetOk = true;

    public boolean iAmTablet;
    public int indexParent = 0;

    public M4MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle infoToPass = getArguments(); //returns the arguments set before

        msgs[0] = infoToPass.getString("StationNumber");
        msgs[1] = infoToPass.getString("BusLine");
        msgs[2] = infoToPass.getString("StationName");
        idPassed = infoToPass.getLong("ID");
        boolean removable = infoToPass.getBoolean("Removable");

        screen = inflater.inflate(R.layout.message_fragment_m4, container, false);
        TextView msgTV = screen.findViewById(R.id.textViewStationDetails01M4);

        String passedMessage = "Stop#: " + msgs[0] + "  Name: " + msgs[2] + "  Bus: " + msgs[1] + "\n(ID = " + idPassed + ")";
        msgTV.setText(passedMessage+"\n"+getString(R.string.m4_internet_wait));
        Log.i("Passed Message:", passedMessage);

        pBar = (ProgressBar) screen.findViewById(R.id.ProgressBarM4);

        //initial add, remove and refresh button and set listener
        Button btnRemove = (Button) screen.findViewById(R.id.member4Btn1);
        Button btnAdd = (Button) screen.findViewById(R.id.member4Btn2);
        Button btnRefresh = (Button) screen.findViewById(R.id.member4Btn3);

        btnRemove.setEnabled(removable);
        btnAdd.setEnabled(!removable);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Query the bus stop details from OC website
                new OCBusQuery("https://api.octranspo1.com/v1.2/GetNextTripsForStop?appID=223eb5c3&&apiKey=ab27db5b435b8c8819ffb8095328e775&stopNo=" + msgs[0] + "&routeNo=" + msgs[1]);
            }
        });

        btnRefresh.callOnClick();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMessage(idPassed, msgs);
                if (iAmTablet && indexParent==1) {
                    getActivity().getFragmentManager().popBackStack();
                    ((Member4MainActivity)parent).refreshMessageCursorAndListView();
                } else {
                    Intent intent = new Intent(getActivity(), Member4MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.m4_dialog_message);
                builder.setTitle(R.string.m4_dialog_title);
                builder.setPositiveButton(R.string.positive_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteMessage(idPassed, msgs);
                        if (iAmTablet && indexParent==1) {
                            getActivity().getFragmentManager().popBackStack();
                            ((Member4MainActivity)parent).refreshMessageCursorAndListView();
                        } else {
                            Intent intent = new Intent(getActivity(), Member4MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                });
                builder.setNegativeButton(R.string.negative_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();
            }
        });
        Snackbar.make(screen, R.string.m4_snackbar_message, Snackbar.LENGTH_LONG).show();

        return screen;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);

        if (iAmTablet) {
            switch(indexParent){
                case 1:
                    parent = (Member4MainActivity) context; //find out which activity has the fragment
                    break;
                case 2:
                    parent = (Member4SearchActivity) context; //find out which activity has the fragment
                    break;
                default:
                    parent = null;
            }
        }
    }

    // delete the current item from My List
    public void deleteMessage(long id, String[] msg){
        db.delete(ML_TABLE_NAME, KEY_ID+"=?", new String[]{Long.toString(id)});
    }

    // add the current item to My List
    public void addMessage(long id, String[] msg){
        ContentValues cVals = new ContentValues(  );
        cVals.put(KEY_ID, Long.toString(id));
        cVals.put(STOP_CODE, msg[0]);
        cVals.put(ROUTE, msg[1]);
        cVals.put(STOP_NAME, msg[2]);
        db.insert(ML_TABLE_NAME,"NullColumnName", cVals);
    }

    // this class access the internet to query the database on OC website
    private class OCBusQuery extends AsyncTask<String, Integer, String[]> {
        public OCBusQuery(String url) {
            publishProgress(0);
            execute(url);
            publishProgress(25);
        }

        @Override
        protected String[] doInBackground(String... urls) {

            String[] aVs = new String[50];

            int iTrip = -6, iDir = -6;
            boolean isRoute = false;
            String tagName;

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xPP = factory.newPullParser();
                xPP.setInput(response, "UTF-8");

                publishProgress(25);
                while (xPP.getEventType() != XmlPullParser.END_DOCUMENT) {
                    switch (xPP.getEventType()) {
                        case XmlPullParser.START_TAG:
                            tagName = xPP.getName();
                            if (tagName.equals("StopNo")) {
                                aVs[0] = xPP.nextText();
                                isRoute = aVs[0].equals(msgs[0]);
                                if (isRoute)
                                    iDir = -1;
                            }
                            if (tagName.equals("StopLabel"))
                                aVs[1] = xPP.nextText();
                            if (tagName.equals("RouteDirection"))
                                iDir++;
                            if (tagName.equals("RouteNo")) {
                                isRoute &= xPP.nextText().equals(msgs[1]);
                                if (isRoute && iDir >= 0)
                                    iTrip = 0;
                                publishProgress((iDir + 2) * 25);
                            }
                            if (isRoute) {
                                if (tagName.equals("RouteLabel"))
                                    aVs[iDir + 2] = xPP.nextText();
                                if (tagName.equals("Direction"))
                                    aVs[iDir + 4] = xPP.nextText();
                                if (tagName.equals("RequestProcessingTime"))
                                    aVs[iDir + 6] = xPP.nextText();
                                if (iTrip < 3) {
                                    if (tagName.equals("TripDestination"))
                                        aVs[iDir * 3 + iTrip + 8] = xPP.nextText();
                                    if (tagName.equals("TripStartTime"))
                                        aVs[iDir * 3 + iTrip + 14] = xPP.nextText();
                                    if (tagName.equals("AdjustedScheduleTime"))
                                        aVs[iDir * 3 + iTrip + 20] = xPP.nextText();
                                    if (tagName.equals("AdjustmentAge"))
                                        aVs[iDir * 3 + iTrip + 26] = xPP.nextText();
                                    if (tagName.equals("Latitude"))
                                        aVs[iDir * 3 + iTrip + 32] = xPP.nextText();
                                    if (tagName.equals("Longitude"))
                                        aVs[iDir * 3 + iTrip + 38] = xPP.nextText();
                                    if (tagName.equals("GPSSpeed")) {
                                        aVs[iDir * 3 + iTrip + 44] = xPP.nextText();
                                        iTrip++;
                                    }
                                }
                            }
                            //Log.i("read XML tag:", tagName);
                            break;
                    }
                    xPP.next();
                }
                isInternetOk = true;
            } catch (Exception e) {
                Log.i("Exception", e.getMessage());
                isInternetOk = false;
            }
            publishProgress(100);
            return aVs;
        }

        @Override
        protected void onProgressUpdate(Integer... args) {
            pBar.setVisibility(View.VISIBLE);
            pBar.setProgress(args[0]);
            Log.i("Progress:", args[0].toString());
        }

        //deal with the realtime information got from OC database on the website
        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);

            TextView QDetails01 = screen.findViewById(R.id.textViewStationDetails01M4);

            if(isInternetOk) {
                for (int i = 0; i < s.length; i++)
                    if (s[i] == null || s[i].isEmpty())
                        s[i] = "N/A";

                QDetails01.setText("StopNo: " + s[0] + "    StopLabel: " + s[1]);
                groups.append(0, new Group("RouteNo: " + msgs[1] + "    RouteLabel: " + s[2]));
                groups.append(1, new Group("Direction: " + s[4]));
                groups.append(2, new Group("RequestProcessingTime: " + s[6]));
                for (int i = 0; i < 3; i++) {
                    Group group = new Group("Trip " + (i + 1) + "  StartTime: " + s[14 + i] + "    Destination: " + s[8 + i]);
                    group.children.add("AdjustedScheduleTime: " + s[20 + i] + "   AdjustmentAge: " + s[26 + i]);
                    group.children.add("Latitude: " + s[32 + i] + "   Longitude: " + s[38 + i] + "    GPSSpeed: " + s[44 + i]);
                    groups.append(i + 3, group);
                }
                groups.append(6, new Group("RouteNo: " + msgs[1] + "    RouteLabel: " + s[3]));
                groups.append(7, new Group("Direction: " + s[5]));
                groups.append(8, new Group("RequestProcessingTime: " + s[7]));
                for (int i = 0; i < 3; i++) {
                    Group group = new Group("Trip " + (i + 1) + "  StartTime: " + s[17 + i] + "    Destination: " + s[11 + i]);
                    group.children.add("AdjustedScheduleTime: " + s[23 + i] + "   AdjustmentAge: " + s[29 + i]);
                    group.children.add("Latitude: " + s[35 + i] + "   Longitude: " + s[41 + i] + "    GPSSpeed: " + s[47 + i]);
                    groups.append(i + 9, group);
                }
                ExpandableListView detailsView = (ExpandableListView) screen.findViewById(R.id.listViewDetailsExpM4);
                MyExpandableListAdapter adapter = new MyExpandableListAdapter(getActivity(), groups);
                detailsView.setAdapter(adapter);
            }
            else{
                QDetails01.setText(getString(R.string.m4_internet_error));
            }

            pBar.setVisibility(View.INVISIBLE);
        }
    }


/********************************************************************************************************/
/*        Following code (Expandable List View) modified from                                           */
/*                      Using lists in Android with ListView - Tutorial                                 */
/*                Lars Vogel, (c) 2010, 2016 vogella GmbH Version 6.2, 09.11.2016                       */
/*                 http://www.vogella.com/tutorials/AndroidListView/article.html                        */
/*                                                                                                      */
/********************************************************************************************************/

    public class Group {

        public String string;
        public final List<String> children = new ArrayList<String>();

        public Group(String string) {
            this.string = string;
        }
    }

    public class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private final SparseArray<Group> groups;
        public LayoutInflater inflater;
        public Activity activity;

        public MyExpandableListAdapter(Activity act, SparseArray<Group> groups) {
            activity = act;
            this.groups = groups;
            inflater = act.getLayoutInflater();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groups.get(groupPosition).children.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String children = (String) getChild(groupPosition, childPosition);
            TextView text = null;
            if (childPosition%2 == 0)
                convertView = inflater.inflate(R.layout.list_row_details1_m4, null);
            else
                convertView = inflater.inflate(R.layout.list_row_details2_m4, null);

            text = (TextView) convertView.findViewById(R.id.textViewRowDetailsM4);
            text.setText(children);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, children,
                            Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groups.get(groupPosition).children.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public void onGroupCollapsed(int groupPosition) {
            super.onGroupCollapsed(groupPosition);
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            super.onGroupExpanded(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if ( getChildrenCount( groupPosition ) == 0 )
                convertView = inflater.inflate(R.layout.list_row_group_header_m4, null);
            else {
                convertView = inflater.inflate(R.layout.list_row_group_m4, null);
                View ind = convertView.findViewById(R.id.explist_indicator);
                if( ind != null ) {
                    ImageView indicator = (ImageView) ind;
                    indicator.setVisibility( View.VISIBLE );
                    indicator.setImageResource( isExpanded ? R.drawable.expander_ic_maximized : R.drawable.expander_ic_minimized );
                }
            }
            Group group = (Group) getGroup(groupPosition);
            CheckedTextView text = (CheckedTextView)convertView.findViewById(R.id.textViewRowGroupM4);
            text.setText(group.string);
            text.setChecked(isExpanded);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}