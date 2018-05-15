package com.newtechs.locations.cg.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
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
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteTta;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;
import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.services.ForegroundService;
import com.newtechs.locations.cg.utilities.Constants;
import com.newtechs.locations.cg.utilities.VehicleData;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class CaseRoutingActivity extends AppCompatActivity {
CoreRouter coreRouter;
MapRoute mapRoute;
MapFragment fragment;
TextView estimatedTime;
Map map;
GeoPosition destPosition,userPosition;
String vehicleNo;
TextView instructionText;
public GeoCoordinate coordinatess;
TextView routetext;
String userlocation,destlocation,title,distance,vno;
boolean val = true;
    private boolean m_foregroundServiceStarted;

    private void startForegroundService() {
        if (!m_foregroundServiceStarted) {
            m_foregroundServiceStarted = true;
            Intent startIntent = new Intent(this, ForegroundService.class);
            startIntent.setAction(ForegroundService.START_ACTION);
            startService(startIntent);
        }
    }

    private void stopForegroundService() {
        if (m_foregroundServiceStarted) {
            m_foregroundServiceStarted = false;
            Intent stopIntent = new Intent(this, ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOP_ACTION);
           startService(stopIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caserouting);
        routetext = findViewById(R.id.routetext);
        instructionText = findViewById(R.id.instructiontext);
        checkLocationServices();
        routetext.bringToFront();
        routetext.invalidate();
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        estimatedTime = findViewById(R.id.estimatedtime);
        Bundle i = getIntent().getExtras();
        userlocation = i.getString("userlocation");
        destlocation = i.getString("destinationlocation");
        vno = i.getString("vno");
        title = i.getString("title");
        distance = i.getString("distance");
        vehicleNo =vno;
        if (vehicleNo.equalsIgnoreCase("0")) {
            getSupportActionBar().setTitle("Routing");
            coordinatess = DriverLocationActivity.geoCoordinate;
        }
            else
            getSupportActionBar().setTitle("Routing - "+vehicleNo);
        fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapsfragment);
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
//                        positioningManager = PositioningManager.getInstance();
//                        positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(onPositionChangedListener));
                        map = fragment.getMap();
                        coreRouter = new CoreRouter();
                        RoutePlan routerPlan = new RoutePlan();
                        String userLatLng[]=userlocation.split(",");
                        userPosition = new GeoPosition(new GeoCoordinate(Double.valueOf(userLatLng[0]),Double.valueOf(userLatLng[1])));
                        final String destLatLng[]=destlocation.split(",");
                        destPosition = new GeoPosition(new GeoCoordinate(Double.valueOf(destLatLng[0]),Double.valueOf(destLatLng[1])));
                   //     if (vehicleNo.equalsIgnoreCase("0")) {
//                            map.setCenter(destPosition.getCoordinate(), Map.Animation.BOW);
//                            map.getPositionIndicator().setVisible(true);
//                            routerPlan.addWaypoint(new RouteWaypoint(userPosition.getCoordinate()));
//                            routerPlan.addWaypoint(new RouteWaypoint(destPosition.getCoordinate()));
//                        }else{
                            map.setCenter(userPosition.getCoordinate(), Map.Animation.BOW);
                            map.getPositionIndicator().setVisible(true);
                            routerPlan.addWaypoint(new RouteWaypoint(destPosition.getCoordinate()));
                            routerPlan.addWaypoint(new RouteWaypoint(userPosition.getCoordinate()));
                        //}
                        RouteOptions routeOptions = new RouteOptions();
                        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                        routerPlan.setRouteOptions(routeOptions);
                        coreRouter.calculateRoute(routerPlan, new RouteInterface());
                        map.setZoomLevel(15);
                        val = false;
                        Image image = new Image();
                        try {
                            image.setImageResource(R.drawable.markerimgbig);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        MapMarker marker = new MapMarker(routerPlan.getWaypoint(1).getNavigablePosition(), image);
                        marker.setDescription("Destination");
                        marker.setAnchorPoint(new PointF(image.getWidth() / 2, image.getHeight()));
                        map.addMapObject(marker);


                        //                        here = LocationDataSourceHERE.getInstance();
//                        if (here!=null){
//                            positioningManager = PositioningManager.getInstance();
//                            positioningManager.setDataSource(here);
//                            positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(onPositionChangedListener));
//                            if (positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)){
//                                Toast.makeText(CaseRoutingActivity.this, "Position Update Started!", Toast.LENGTH_SHORT).show();
//                            }else{
//                                Toast.makeText(CaseRoutingActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                            }
//                        }else{
//                            Toast.makeText(CaseRoutingActivity.this, "Here Data Null!", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        Log.e("Error", error.getStackTrace());
                    }
                }

            });
        }


        }

//    public void updateVerifyInfo(View view) {
//        Toast.makeText(this, "Verification.", Toast.LENGTH_SHORT).show();
//        FloatingActionButton btn = findViewById(R.id.fab);
//        btn.setImageResource(R.drawable.ic_beenhere_black_24dp);
//        DatabaseReference reference;
//        if (!vehicleNo.equalsIgnoreCase("0")) {
//            reference = FirebaseDatabase.getInstance().getReference(Constants.EMERGENCY_VEHICLE_LIST_REFERENCE);
//        }
//
//    }


    private class RouteInterface implements CoreRouter.Listener {

        @Override
        public void onProgress(int i) {

        }

        @Override
        public void onCalculateRouteFinished(List<RouteResult> list, RoutingError routingError) {
            routetext.setVisibility(View.GONE);
            if (routingError == RoutingError.NONE) {
                Log.e("Routes",list.size()+"");
                for (RouteResult rr:list)
                Log.e("RouteResult",rr.getRoute().getTta(Route.TrafficPenaltyMode.OPTIMAL,rr.getRoute().getSublegCount()>=0?rr.getRoute().getSublegCount()-1:0).getDuration()+"");
                Route r = list.get(0).getRoute();
                Log.e("SublegCount",r.getSublegCount()+"");
                mapRoute = new MapRoute(r);
                mapRoute.setManeuverNumberVisible(true);
                mapRoute.setTrafficEnabled(true);
                map.addMapObject(mapRoute);
                RouteTta tt = r.getTta(Route.TrafficPenaltyMode.OPTIMAL,r.getSublegCount()>=0?r.getSublegCount()-1:0);
                long timeInSeconds = tt.getDuration();
                long timeInMinutes = timeInSeconds/60;
                estimatedTime.setText("Estimated Time: "+timeInMinutes+"mins");
                final NavigationManager manager = NavigationManager.getInstance();
                map.getPositionIndicator().setVisible(true);
                manager.setMap(map);
                GeoBoundingBox gbb =r.getBoundingBox();
                map.zoomTo(gbb, Map.Animation.NONE,
                        Map.MOVE_PRESERVE_ORIENTATION);
                manager.startNavigation(r);
                map.setTilt(90);
                startForegroundService();
                instructionText.setVisibility(View.VISIBLE);
                instructionText.bringToFront();
                instructionText.invalidate();
                manager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
                manager.addNewInstructionEventListener(new WeakReference<NavigationManager.NewInstructionEventListener>(new NavigationManager.NewInstructionEventListener() {
                    @Override
                    public void onNewInstructionEvent() {
                        Maneuver maneuver= manager.getNextManeuver();
                        Maneuver.Turn turn = maneuver.getTurn();
                        String turnName=turn.name();
                        int distance = maneuver.getDistanceFromPreviousManeuver();
                        String nextRoadName = maneuver.getNextRoadName();
                        if (vehicleNo.equalsIgnoreCase("0")){
                            instructionText.setText("Take a "+turnName+"to the "+nextRoadName+" in "+distance+"mts");
                        }
                        instructionText.setText("Will take a "+turnName+"to the "+nextRoadName+" in "+distance+"mts");
                    }
                }));
                manager.addNavigationManagerEventListener(new WeakReference<NavigationManager.NavigationManagerEventListener>(new NavigationManager.NavigationManagerEventListener() {
                    @Override
                    public void onEnded(NavigationManager.NavigationMode navigationMode) {
                        stopForegroundService();
                        Toast.makeText(CaseRoutingActivity.this, "Navigation Ended!", Toast.LENGTH_SHORT).show();
                        instructionText.setText("Destination Reached!");
                    }
                }));
            }
            else {
                Toast.makeText(CaseRoutingActivity.this, "Retry Once", Toast.LENGTH_SHORT).show();
            }
        }
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
