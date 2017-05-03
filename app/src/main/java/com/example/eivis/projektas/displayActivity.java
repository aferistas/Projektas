package com.example.eivis.projektas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

public class displayActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Button matavimaiButton =  (Button)findViewById(R.id.matavimaiButton);
        matavimaiButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(displayActivity.this,matavimaiActivity.class));
            }
        });
        Button rezultataiButtton=  (Button)findViewById(R.id.rezultataiButton);
        rezultataiButtton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(displayActivity.this,rezultataiActivity.class));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
