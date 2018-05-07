package com.example.dell.cg;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class Police extends AppCompatActivity {
    ListView listView;
    ArrayList arrayList;
    Button click;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police);
        listView=findViewById(R.id.list);
        arrayList=new ArrayList();
        arrayList.add("hai");
        arrayList.add("hai0");
        arrayList.add("hai");
        arrayList.add("hai0");
        Custom custom=new Custom(Police.this,arrayList);
        listView.setAdapter(custom);
        click=findViewById(R.id.click);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Police.this);
                View view=getLayoutInflater().inflate(R.layout.dialog,null);
                alert.setView(view);
                alert.show();
            }
        });
    }
}
