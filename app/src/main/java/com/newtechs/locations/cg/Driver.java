package com.newtechs.locations.cg;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Driver extends AppCompatActivity {
    FloatingActionButton floating,floating1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        floating=findViewById(R.id.fab);
        floating1=findViewById(R.id.fab1);
        floating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Driver.this,MainActivity.class));
            }
        });
        floating1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Driver.this,MainActivity.class));
            }
        });
    }
}
