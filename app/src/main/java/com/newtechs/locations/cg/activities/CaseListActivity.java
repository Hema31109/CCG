package com.newtechs.locations.cg.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.utilities.Constants;
import com.newtechs.locations.cg.utilities.VehicleData;
import com.newtechs.locations.cg.viewholders.SharedLocationsPoliceViewHolder;

public class CaseListActivity extends AppCompatActivity {
private DatabaseReference databaseReference;
private FirebaseRecyclerAdapter adapter;
private RecyclerView recyclerView;
    int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_list);
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        flag = getIntent().getExtras().getInt("flag");
        if(flag==1){
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERLOCATION_LIST_REFERENCE);
            getSupportActionBar().setTitle("View User Locations");
        }else{
            getSupportActionBar().setTitle("Traffic Police");
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.EMERGENCY_VEHICLE_LIST_REFERENCE);
        }
        recyclerView = findViewById(R.id.recyclerviewlist);
        setUpFirebaseAdapter();
    }
    private void setUpFirebaseAdapter() {
        adapter = new FirebaseRecyclerAdapter<VehicleData, SharedLocationsPoliceViewHolder>
                (VehicleData.class, R.layout.policecaserowdata, SharedLocationsPoliceViewHolder.class,
                        databaseReference) {

            @Override
            protected void populateViewHolder(SharedLocationsPoliceViewHolder viewHolder,
                                              VehicleData model, int position) {
                viewHolder.bindData(model,flag);
            }
        };
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }



}
