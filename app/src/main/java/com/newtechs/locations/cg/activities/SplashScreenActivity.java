package com.newtechs.locations.cg.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.newtechs.locations.cg.R;

public class SplashScreenActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        imageView=findViewById(R.id.image);
       Thread thread=new Thread(){
            @Override
            public void run() {
                 try {
                    sleep(3000);
                    Intent intent=new Intent(getApplicationContext(),HomeScreenActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    }

