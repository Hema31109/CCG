package com.newtechs.locations.cg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

public class splashscreen extends AppCompatActivity implements View.OnClickListener {
    CardView police,driver,user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        police=findViewById(R.id.police);
        driver=findViewById(R.id.driver);
        user=findViewById(R.id.user);
        police.setOnClickListener(this);
        driver.setOnClickListener(this);
        user.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.police:
                startActivity(new Intent(splashscreen.this,Police.class));
                break;
            case R.id.driver:
                startActivity(new Intent(splashscreen.this,Driver.class));
                break;
            case R.id.user:
                startActivity(new Intent(splashscreen.this,User.class));
                break;
        }
    }

}
