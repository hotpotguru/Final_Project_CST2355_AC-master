package ca.algonquinstudents.cst2335_group_project;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

/**
 * this class defines behaviour for the fragment used to display a Favourite
 */

public class M1FavFragment extends Fragment {


    TextView messageHere;

    /**
     * place for user to enter their desired tag to set
     */
    EditText tagToSet;
    Button deleteMessage, add, ok;
    /**
     * strings which we get from the bundle to populate this fragment
     */
    String name, myMsg, tagText;


    /**
     * if running on a tablet, we will call methods directly on this m1FavActivity
     */
    M1FavActivity m1FavActivity = null;
    boolean isTablet;


    /**
     * default oonstructor
     */
    public M1FavFragment(){}



    /**
     *
     * @param fa the parent activity to set so we can call methods on it, if on a tablet
     */

    public void setChatWindow(M1FavActivity fa){
        m1FavActivity=fa;
    }

    /**
     * saves relevant info from the calling activity
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            name = bundle.getString("Name");
            myMsg = bundle.getString("Name")+"\n"+bundle.getString("Calories")+" calories\n"+bundle.getString("Fat")+"g fat";
            tagText = bundle.getString("Tag");

            //dbID = bundle.getLong("dbId");
            //Log.i("MessageFragment", myMsg);
        }



    }


    /**
     * inflates view, finds important items from the view and sets appropriate onCLICKS for them d
     * based on whether it is a phone or tablet
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.m1_fav_list, null);
        messageHere = (TextView) v.findViewById(R.id.m1FoodName);
        messageHere.setText(myMsg);

        TextView tag= v.findViewById(R.id.m1FoodTag);
        if(tagText!=null) {
            tag.setText("Tag: "+tagText);
        }

        tagToSet=v.findViewById(R.id.m1AddATag);
        add = v.findViewById(R.id.m1TagButton);
        add.setOnClickListener((l)->{
            String newTag =tagToSet.getText().toString();
            if(!newTag.isEmpty()) {
                tagToSet.setText("");
                if(isTablet) {
                    m1FavActivity.addATag(name, newTag);
                }
                else{
                    ((M1FavDetails)getActivity()).addATag(name, newTag);
                }
                tag.setText("Tag: "+newTag);
                Log.i("Fragment", "adding tag");
            }


        });




        deleteMessage=(Button)v.findViewById(R.id.m1DeleteButton);

        deleteMessage.setOnClickListener((e)->{
                    if(isTablet){
                        Log.i("MessageFragment", "tablet-specific deleteMessage function");
                        m1FavActivity.deleteItem(name);
                        getActivity().getFragmentManager().popBackStack();
                        getActivity().getFragmentManager().beginTransaction().remove(this);
                        m1FavActivity.getFragmentManager().beginTransaction().remove(this).commit();



                    }
                    else{
                        //on phone
                        Intent i = new Intent();

                        i.putExtra("name", name);
                        getActivity().setResult(1, i);
                        getActivity().finish();
                    }

                }

        );
        ok = v.findViewById(R.id.m1BackFromFavButton);
        ok.setOnClickListener((o)->{
            if(isTablet){

                getActivity().getFragmentManager().popBackStack();
                getActivity().getFragmentManager().beginTransaction().remove(this);
                m1FavActivity.refreshFavCursorAndListView();
                m1FavActivity.getFragmentManager().beginTransaction().remove(this).commit();
            }
            else{
                //on phone
                Intent i = new Intent();
                getActivity().setResult(2, i);
                getActivity().finish();
                }
        });
        return v;
    }

    /**
     *
     * @param context if on a tablet, context is the FavActivity parent
     */

    @Override
    public void onAttach(Activity context){
        super.onAttach(context);
        if(isTablet) {
            setChatWindow((M1FavActivity) context);
        }
    }

    /**
     *
     * @param b true if running on tablet, otherwise false
     */
    public void setIsTablet(boolean b){
        isTablet=b;
    }

}
