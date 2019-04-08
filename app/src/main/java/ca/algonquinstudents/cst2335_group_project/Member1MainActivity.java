package ca.algonquinstudents.cst2335_group_project;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1CALORIES;
import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1FAT;
import static ca.algonquinstudents.cst2335_group_project.M1DatabaseHelper.M1KEY_NAME;

/**
 *  Start activity of food nutrition (Member1: Claire Hendrickson-Jones)
 */

public class Member1MainActivity extends AppCompatActivity {
    /**
     * static strings for accessing DB and logging purposes
     */
    private static final String ACTIVITY_NAME = "Member1MainActivity";
    private Button favButton, searchButton;
    private double x;

    /**
     * used for querying and updating the DB
     */
    protected static M1DatabaseHelper dbHelper1 = null;
    protected static SQLiteDatabase db1;

    /**
     * used for AsyncTask
     */
    private NutritionQuery n;

    private EditText searchBar;
    private ProgressBar progressBar;

    private String foodToSearch;
    private ToolbarMenu toolItem;
    private List<Food> searchResults = new ArrayList<>();
    private ListView showResults;

    /**
     * ArrayAdapter for the list view
     */
    private FoodAdapter resultsAdapter;

    /**
     * for DB insertion
     */
    ContentValues cValues = new ContentValues();

    /**
     * sets view, finds important items in that view, and sets onClick listeners for them
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member1_main);
        favButton = findViewById(R.id.m1FavButton);
        searchButton = findViewById(R.id.m1SearchButton);
        searchBar = findViewById(R.id.m1SearchBar);

        showResults = findViewById(R.id.m1SearchResults);
        resultsAdapter = new FoodAdapter(this);
        showResults.setAdapter(resultsAdapter);

        if (dbHelper1 == null) {
            dbHelper1 = new M1DatabaseHelper(Member1MainActivity.this);
            db1 = dbHelper1.getWritableDatabase();
        }

        progressBar = findViewById(R.id.m1ProgressBar);
        progressBar.setVisibility(View.INVISIBLE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm1);
        setSupportActionBar(toolbar);

        toolItem = new ToolbarMenu(Member1MainActivity.this);


        showResults.setOnItemClickListener((arg0, view, position, id) -> {
            showResultDialog(searchResults.get(position));
            String s = (searchResults.get(position)).name;
            Log.i(ACTIVITY_NAME, "you clicked on "+s);

            });




        favButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Member1MainActivity.this, M1FavActivity.class);
                startActivity(intent);
                Member1MainActivity.this.finish();
            }
        });




        searchButton.setOnClickListener((e)->{
            foodToSearch=searchBar.getText().toString();
            if(!foodToSearch.isEmpty()){
                searchResults.clear();
                n = new NutritionQuery();
                n.execute();
                //actual internet search is done here
            }

        });


    }


    /**
     * setting up toolbar menu
     *
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(1).setVisible(false);
        toolItem.setHelpTitle(getString(R.string.m1_help_title));
        toolItem.setHelpMessage(getString(R.string.m1_help_message));
        return true;
    }

    /**
     * switch activity
     *
     * @param item
     * @return
     */
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
            Member1MainActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * inserts param to database, and displays a notification that it was added
     * @param food
     */
    public void onFavAdded(Food food) {
        String foodAddedMessage=  food.name+" "+getText(R.string.m1FoodAddedMessage);
        Snackbar.make(searchBar, foodAddedMessage, Snackbar.LENGTH_LONG).show();


        cValues.put(M1KEY_NAME, food.name );
        cValues.put(M1CALORIES, food.calories);
        cValues.put(M1FAT, food.fat);
        db1.insert(M1DatabaseHelper.M1_TABLE_NAME,"NullColumnName", cValues);

    }


    /**
     * private class to store details about each search result
     */
    private class Food{
        String name;
        String calories, fat;

        Food(){}
        Food(String n, String c, String f){
            name=n;
            calories=c;
            fat = f;
        }

    }

    /**
     * inner class for executing AsyncTask and getting results from the internet
     */


    private class NutritionQuery extends AsyncTask<String, Integer, String> {



        @Override
        protected String doInBackground(String... strings) {
            publishProgress(25);

            String name = "Unknown";
            int calories, fat;
            searchResults = new ArrayList<>();

//            String urlString = "https://api.edamam.com/api/food-database/parser?app_id=0f6ce556&app_key=744e0095e589a8eefb25582fbfbefec9&ingr=";
            String urlString = "https://webhose.io/filterWebContent?token=74058bea-17b6-4898-bfb2-6244e2d4a482&sort=crawled&q=";
            urlString += foodToSearch;

            URL url = null;
            InputStream is;

            try {

                url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(100000);     // milliseconds
                conn.setConnectTimeout(150000);  // milliseconds
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query

                is = conn.getInputStream();

                JsonReader jsonReader = new JsonReader(new InputStreamReader(is, "UTF-8"));
                try {
                    searchResults = readFoodsArray2(jsonReader);

                } finally {
                    jsonReader.close();
                    conn.disconnect();
                }

            } catch (IOException e) {
                Log.i(e.toString(), "EXCEPTION :In post execute");
            }

            return "";

        }


        /**
         * hides progress bar and updates results
         * @param s not used
         */
        @Override
        protected void onPostExecute(String s){
            Log.i(ACTIVITY_NAME, "In post execute");

            progressBar.setVisibility(View.INVISIBLE);
            for(int i =1;i<searchResults.size();i++){
                Log.i(ACTIVITY_NAME, i+searchResults.get(i).name);
            }

            resultsAdapter.notifyDataSetChanged();
        }

        /**
         * updates the progress bar
         * @param values passed to progress bar
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            //super.onProgressUpdate(values);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(values[0]);
        }



    }



    public List<Food> readFoodsArray(JsonReader reader) throws IOException {
        List<Food> foods = new ArrayList<Food>();


        reader.beginObject();
        //int forceCounter=0;
        while (reader.hasNext()) {
            //reader.beginObject();
            String s = reader.nextName();
            if (s.equals("posts")){//posts hints
                reader.beginArray();
                while(reader.hasNext()) {
                    JsonToken jt = reader.peek();

                    if(reader.peek().equals(JsonToken.BEGIN_ARRAY)){//||reader.peek().equals(JsonToken.NAME)
                        reader.skipValue();
                        //reader.beginArray();
                    }
                    else if(reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                        reader.beginObject();


                        if (reader.nextName().equals("thread")) {//thread food
                            //we are finally in a food object
                            String label = null;
                            String cals = "";
                            String fat = "";
                            reader.beginObject();
                            while (reader.hasNext()) { //referring only to the current obj, "food"
                                String name = reader.nextName();
                                if (name.equals("url")) {//url label
                                    label = reader.nextString();
                                }
//                                    else if (name.equals("nutrients")) {//time to enter the nutrients obj and pull cals and fat//social
//                                    reader.beginObject();
//                                    while (reader.hasNext()) {
//                                        String name2 = reader.nextName();
//                                        if (name2.equals("ENERC_KCAL")) {
//                                            cals = reader.nextDouble();
//                                        } else if (name2.equals("FAT")) {
//                                            fat = reader.nextDouble();
//                                        } else {
//                                            reader.skipValue();
//                                        }
//                                    }
//                                    reader.endObject();
//                                }
                                else {
                                    reader.skipValue();
                                }
                            }
                            cals ="";
                            fat = "";
                            Food f = new Food(label,  cals,  fat);
                            foods.add(f);
                            reader.endObject();

                        }
                    }
                    else {
                        //this is probably the "values" section which we don't care about
                        reader.skipValue();
                    }
                }//the end of the hints array
                //reader.endArray();
                //foods.add(readFood(reader));
            }
            else{//not the hints array
                reader.skipValue();

            }
        }
        reader.endObject();
        return foods;
    }


    /**
     *
     * @param reader JsonReader to parse the data
     * @return array of foods resulting from the search
     * @throws IOException if there is an error while reading the input
     */

    public List<Food> readFoodsArray2(JsonReader reader) throws IOException {
        List<Food> foods = new ArrayList<Food>();

        reader.beginObject();

        while(reader.hasNext()){
            String s = reader.nextName();

            //if "posts" then
            if (s.equals("posts")){

                reader.beginArray();
                while (reader.hasNext()){
                    String ss = "";
                    String url= "";
                    String title = "";
                    String text = "";
//                    reader.skipValue();
                    reader.beginObject();
                    while (reader.hasNext()){
                        ss = reader.nextName();
                        if (ss.equals("url")){
                            url = reader.nextString();
                        }else if (ss.equals("title")){
                            title = reader.nextString();
                        }else if (ss.equals("text")){
                            text = reader.nextString();
                        }else {
                            reader.skipValue();
                        }


                    }
                    reader.endObject();



                    foods.add(new Food(title,url,text));
                }
                reader.endArray();


            }else{
                reader.skipValue();
            }



        }

        reader.endObject();

        return foods;


    }

    /**
     * called by the readFoodsArray function
     * @param reader Json reader to parse data
     * @return Food to add to the ArrayList of search results
     * @throws IOException if error encountered
     */

    public Food readOneFood(JsonReader reader) throws IOException{
        String label = null;
        String cals = "";
        String fat = "";

        reader.beginObject();
        while (reader.hasNext()) { //referring only to the current obj, "food"
            String name = reader.nextName();
            if (name.equals("url")) {//url  label
                label = reader.nextString();
            }
//            else if (name.equals("nutrients")) {//time to enter the nutrients obj and pull cals and fat
////                Double[] nutrients = readNutrients(reader);
////                cals = nutrients[0];fat=nutrients[1];
//
//            }
            else {
                reader.skipValue();
            }
        }
        cals = "";fat="";
        Food f = new Food(label, cals, fat);
        reader.endObject();
        return f;
    }

    /**
     * called by the readOneFood function to collect that food's cals and fat
     * @param reader Jsonreader to parse data
     * @return array of doubles containing the food's fat and calories
     * @throws IOException
     */
    public Double[] readNutrients(JsonReader reader) throws IOException{
        double cals=0, fat=0;
//        reader.beginObject();
//        while (reader.hasNext()) {
//            String name2 = reader.nextName();
//            if (name2.equals("ENERC_KCAL")) {
//                cals = reader.nextDouble();
//            } else if (name2.equals("FAT")) {
//                fat = reader.nextDouble();
//            } else {
//                reader.skipValue();
//            }
//        }
//        reader.endObject();
        cals = 100;fat=200;
        return new Double[]{cals, fat};
    }


    /**
     * shows details of the parameter food
     * @param feed
     */
    public void showResultDialog(Food feed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Member1MainActivity.this);
        builder.setTitle(R.string.m1Result);
        String message = "Title: " + feed.name + "\nURL: " + feed.calories + "\nText: " + feed.fat+"g";
        builder.setMessage(message);
        builder.setPositiveButton(R.string.positive_ok, (dialog, id) -> {
        });
        builder.setNeutralButton(R.string.m1Save, (dialog, id)->{
            onFavAdded(feed);
        });
        builder.create().show();
    }


    /**
     * private class to use on our ListView
     */
   private class FoodAdapter extends ArrayAdapter<Food> {

        public FoodAdapter(Context ctx) {
            super(ctx, 0);
        }
        public int getCount(){
            return searchResults.size();
        }

        public Food getItem(int position){
            return searchResults.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            //temporary:
            LayoutInflater inflater = Member1MainActivity.this.getLayoutInflater();
            View result = null ;

            result = inflater.inflate(R.layout.m1_fav_row, null);

            TextView favName = (TextView)result.findViewById(R.id.m1FavRowName);
            //Log.i("ChatWindow", "message "+favName.toString());
            Food f = getItem(position);
            String foodName = f.name;
            //String s = getItem(position);

            favName.setText(foodName); // get the string at position
            return result;

        }



    }

}
