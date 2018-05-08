package com.newtechs.locations.cg;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class Police extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList arrayList1,arrayList2,arrayList3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police);
        arrayList1=new ArrayList();
        arrayList2=new ArrayList();
        arrayList3=new ArrayList();
        recyclerView=findViewById(R.id.recycler);
        for (int i=0;i<=5;i++)
        {
            arrayList1.add(i+1);
        }
        arrayList2.add("Rajhasthan");
        arrayList2.add("Delhi");
        arrayList2.add("Mumbai");
        arrayList2.add("Rajhasthan");
        arrayList2.add("Rajhasthan");
        arrayList2.add("Rajhasthan");


        arrayList3.add("11:00");
        arrayList3.add("09:00");
        arrayList3.add("07:00");
        arrayList3.add("07:00");
        arrayList3.add("07:00");
        arrayList3.add("07:00");

        MyRecycler myRecycler=new MyRecycler(Police.this,arrayList1,arrayList2,arrayList3);
        recyclerView.setAdapter(myRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//                       AlertDialog.Builder alert = new AlertDialog.Builder(Police.this);
//                View view=getLayoutInflater().inflate(R.layout.dialog,null);
//                alert.setView(view);
//                alert.show();
    }
}
