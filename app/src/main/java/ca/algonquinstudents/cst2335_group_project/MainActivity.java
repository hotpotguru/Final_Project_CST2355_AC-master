package ca.algonquinstudents.cst2335_group_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author Xue Nian Jiang
 * Main graphic menu for the whole porject
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button member1Btn = findViewById(R.id.imageButton1);
        Button member2Btn = findViewById(R.id.imageButton2);
        Button member3Btn = findViewById(R.id.imageButton3);
        Button member4Btn = findViewById(R.id.imageButton4);

        member1Btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Member1MainActivity.class);
                startActivity(intent);
            }
        });

        member2Btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Member2MainActivity.class);
                startActivity(intent);
            }
        });

        member3Btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Member3MainActivity.class);
                startActivity(intent);
            }
        });

        member4Btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Member4MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
