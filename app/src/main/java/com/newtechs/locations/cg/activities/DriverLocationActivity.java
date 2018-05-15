package com.newtechs.locations.cg.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.mapping.TrafficEvent;
import com.here.android.mpa.urbanmobility.Alert;
import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.utilities.Constants;
import com.newtechs.locations.cg.utilities.VehicleData;

import java.io.File;
import java.lang.ref.WeakReference;

public class DriverLocationActivity extends AppCompatActivity {
private MapFragment fragment;
private boolean paused = false;
private PositioningManager positioningManager;
private Map map;
private GeoPosition gePosition;
private LocationDataSourceHERE here;
public static GeoCoordinate geoCoordinate;
private DatabaseReference myRef,currentRef;
private FirebaseDatabase database;
TextView positiontext;
private ProgressDialog dialog;
    private PositioningManager.OnPositionChangedListener onPositionChangedListener = new PositioningManager.OnPositionChangedListener() {
    @Override
    public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
        dialog.dismiss();
        if (!paused){
            map.setCenter(geoPosition.getCoordinate(), Map.Animation.NONE);
            map.getPositionIndicator().setVisible(true);
            gePosition = geoPosition;
            geoCoordinate = geoPosition.getCoordinate();
            positiontext.setVisibility(View.GONE);
        }else{

        }

    }

    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {
    }
};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);
        positiontext = findViewById(R.id.positiontext);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();
        checkLocationServices();
        positiontext.bringToFront();
        positiontext.invalidate();
        if (!checkInternet()){
            Snackbar.make(findViewById(R.id.coordinatorlayout),"Check Internet Connection!",Snackbar.LENGTH_LONG).show();
        }
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(Constants.EMERGENCY_VEHICLE_LIST_REFERENCE);
        fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapsfragment);
        boolean success = MapSettings.setIsolatedDiskCacheRootPath(
                getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.here.android.tut.MapService");

        if (!success){
            Log.e("UserLocationActivity","IsolatedDiskCacheNotSet");
        }else {
            fragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    if (error == Error.NONE) {
                        map = fragment.getMap();
                        positioningManager = PositioningManager.getInstance();
                        positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(onPositionChangedListener));
                        here = LocationDataSourceHERE.getInstance();
                        if (here!=null){
                            positioningManager = PositioningManager.getInstance();
                            positioningManager.setDataSource(here);
                            positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(onPositionChangedListener));
                            if (positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)){
                                Toast.makeText(DriverLocationActivity.this, "Position Update Started!", Toast.LENGTH_SHORT).show();
                                positiontext.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(DriverLocationActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(DriverLocationActivity.this, "Here Data Null!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("UserLocationActivity", "Cannot Initialize MapFragment" + "\n" + error.getDetails());
                    }
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        if (positioningManager != null){
            if (!positioningManager.isActive())
            positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR);
        }
    }

    @Override
    protected void onPause() {
        if (positioningManager!=null){
            positioningManager.stop();
        }
        super.onPause();
        paused = true;
    }

    @Override
    protected void onDestroy() {
        if (positioningManager!=null){
            positioningManager.removeListener(onPositionChangedListener);
        }
        map=null;
        super.onDestroy();
    }

    public void searchUserLocation(View view) {
        Intent intent = new Intent(this,CaseListActivity.class);
        intent.putExtra("flag",1);
        startActivity(intent);
    }

    public void viewNearbyHospitals(View view) {
        if (gePosition==null){
            Toast.makeText(this, "Wait until current position is fetched.", Toast.LENGTH_SHORT).show();
            return;
        }
        GeoCoordinate coordinate = gePosition.getCoordinate();
        String locations = coordinate.getLatitude()+","+coordinate.getLongitude();
        Intent intent = new Intent(this,NearbyPlacesActivity.class);
        intent.putExtra("coordinates",locations);
        startActivity(intent);
    }

    public void share(View view) {
        DatabaseReference yRef=myRef.push();
        VehicleData data=new VehicleData();
        GeoCoordinate coordinate = gePosition.getCoordinate();
        data.location = coordinate.getLatitude()+","+coordinate.getLongitude();
        data.verifiedtext = "NO";
        String randvalue = Build.ID;
        data.vehicleNo = ("TN 23 "+randvalue).substring(0,10);
        yRef.setValue(data);
        Toast.makeText(this, "Location Shared Successfully", Toast.LENGTH_LONG).show();
    }
    public void checkLocationServices(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled||!network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Toast.makeText(DriverLocationActivity.this, "Enable Location Services", Toast.LENGTH_SHORT).show();
                }
            });
           final AlertDialog d =  dialog.create();
           d.setOnShowListener(new DialogInterface.OnShowListener() {
               @Override
               public void onShow(DialogInterface dialog) {
                   d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                   d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
               }
           });
            d.show();
        }
    }

    public void searchTraffic(View view) {
        MapTrafficLayer layer = map.getMapTrafficLayer();
        layer.setDisplayFilter(TrafficEvent.Severity.VERY_HIGH);
        map.setTrafficInfoVisible(true);
    }

    public boolean checkInternet(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info!=null && info.isAvailable() &&info.isConnected();
    }


}
