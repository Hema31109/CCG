package com.newtechs.locations.cg.activities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
    public static int route_flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_list);
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Driver Locations List");
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
        if (!checkInternet()){
            Snackbar.make(findViewById(R.id.coordinatorlayout),"Check Internet Connection!",Snackbar.LENGTH_LONG).show();
        }
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

    public boolean checkInternet(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info!=null && info.isAvailable() &&info.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selectrouteoption,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.navigate:
                route_flag = 1;
                break;
            case R.id.simulate:
                route_flag = 0;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        route_flag = 0;
    }
}
