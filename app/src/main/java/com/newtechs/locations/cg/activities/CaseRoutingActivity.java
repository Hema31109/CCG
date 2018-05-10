package com.newtechs.locations.cg.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteTta;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;
import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.utilities.Constants;
import com.newtechs.locations.cg.utilities.VehicleData;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class CaseRoutingActivity extends AppCompatActivity {
CoreRouter coreRouter;
MapRoute mapRoute;
MapFragment fragment;
TextView estimatedTime;
Map map;
String locationString;
DatabaseReference myref;
boolean paused = false;
GeoPosition position,userPosition;
PositioningManager positioningManager;
String vehicleNo;
ProgressDialog dialog;
TextView routetext;
    private PositioningManager.OnPositionChangedListener onPositionChangedListener = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
           routetext.setVisibility(View.GONE);
            if (!paused){
                map.setCenter(geoPosition.getCoordinate(), Map.Animation.NONE);
                map.getPositionIndicator().setVisible(true);
                position = geoPosition;
                coreRouter = new CoreRouter();
                RoutePlan routerPlan = new RoutePlan();
                map.setCenter(userPosition.getCoordinate(), Map.Animation.NONE);
                routerPlan.addWaypoint(new RouteWaypoint(geoPosition.getCoordinate()));
                routerPlan.addWaypoint(new RouteWaypoint(userPosition.getCoordinate()));
                RouteOptions routeOptions = new RouteOptions();
                routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                routerPlan.setRouteOptions(routeOptions);
                coreRouter.calculateRoute(routerPlan, new RouteInterface());
            }else{
                Toast.makeText(CaseRoutingActivity.this, "Not able to get current location", Toast.LENGTH_SHORT).show();
                Log.e("UserLocationActivity","Paused is true");
            }

        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

        }
    };
    String[] latLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caserouting);
        routetext = findViewById(R.id.routetext);
        dialog = new ProgressDialog(this);
        checkLocationServices();
        dialog.setMessage("Loading..");
        dialog.setCancelable(false);
        dialog.show();
        routetext.bringToFront();
        routetext.invalidate();
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        estimatedTime = findViewById(R.id.estimatedtime);
        locationString = getIntent().getExtras().getString("location");
        vehicleNo = getIntent().getExtras().getString("vehicleno");
        if (vehicleNo.equalsIgnoreCase("0")) {
            getSupportActionBar().setTitle("Routing");
            findViewById(R.id.fab).setVisibility(View.GONE);
        }
            else
            getSupportActionBar().setTitle("Routing - "+vehicleNo);
        fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapsfragment);
        latLng = locationString.split(",");
        boolean success = MapSettings.setIsolatedDiskCacheRootPath(
                getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.here.android.tut.MapService");
        if (!success) {
            Log.e("UserLocationActivity", "IsolatedDiskCacheNotSet");
        } else {
            fragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    if (error == Error.NONE) {
                        positioningManager = PositioningManager.getInstance();
                        positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(onPositionChangedListener));
                        map = fragment.getMap();
                        userPosition = new GeoPosition(new GeoCoordinate(Double.valueOf(latLng[0]),Double.valueOf(latLng[1])));
                    } else {
                        Log.e("Error", error.getStackTrace());
                    }
                    dialog.dismiss();
                }

            });
        }


        }

    public void updateVerifyInfo(View view) {
        Toast.makeText(this, "Verification.", Toast.LENGTH_SHORT).show();
        FloatingActionButton btn = findViewById(R.id.fab);
        btn.setImageResource(R.drawable.ic_beenhere_black_24dp);
        DatabaseReference reference;
        if (!vehicleNo.equalsIgnoreCase("0")) {
            reference = FirebaseDatabase.getInstance().getReference(Constants.EMERGENCY_VEHICLE_LIST_REFERENCE);
        }

    }


    private class RouteInterface implements CoreRouter.Listener {

        @Override
        public void onProgress(int i) {

        }

        @Override
        public void onCalculateRouteFinished(List<RouteResult> list, RoutingError routingError) {
            if (routingError == RoutingError.NONE) {
                // Render the route on the map
                Route r = list.get(0).getRoute();
                Log.e("SublegCount",r.getSublegCount()+"");
                mapRoute = new MapRoute(r);
                mapRoute.setTrafficEnabled(true);
                map.addMapObject(mapRoute);
                RouteTta tt = r.getTta(Route.TrafficPenaltyMode.OPTIMAL,r.getSublegCount()>=0?r.getSublegCount()-1:0);
                long timeInSeconds = tt.getDuration();
                long timeInMinutes = timeInSeconds/60;
                estimatedTime.setText("Estimated Time: "+timeInMinutes+"mins");
            }
            else {
                Toast.makeText(CaseRoutingActivity.this, "Retry Once", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        if (positioningManager != null){
            positioningManager.start(PositioningManager.LocationMethod.NETWORK);
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

        if(!gps_enabled && !network_enabled) {
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
                    Toast.makeText(CaseRoutingActivity.this, "Enable Location Services", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
    }
}
