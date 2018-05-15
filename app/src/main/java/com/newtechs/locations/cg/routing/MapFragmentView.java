/*
 * Copyright (c) 2011-2018 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.newtechs.locations.cg.routing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteTta;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ReverseGeocodeRequest2;
import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.activities.CaseRoutingActivity;
import com.newtechs.locations.cg.activities.HomeScreenActivity;
import com.newtechs.locations.cg.activities.NearbyPlacesActivity;
import com.newtechs.locations.cg.services.ForegroundService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Besides the turn-by-turn navigation example app, This app covers 2 other common use cases:
 * - usage of MapUpdateMode#RoadView during navigation and its interactions with user gestures.
 * - using a MapMarker as position indicator and how to make the movements smooth and
 * synchronized with map movements.
 */
public class MapFragmentView {
    private MapFragment m_mapFragment;
    private Map m_map;
    Double lat;
    Double lon;
    private MapMarker m_positionIndicatorFixed = null;
    private PointF m_mapTransformCenter;
    private boolean m_returningToRoadViewMode = false;
    private double m_lastZoomLevelInRoadViewMode = 0.0;
    private Activity m_activity;
    String userlocation;
    String destlocation;
    String title;
    TextView estimatedTime,routetext,instructiontext;
    String distance;
    String vehicleNo;

boolean success;
    public MapFragmentView(String vehicleNo,Activity activity, boolean success, String userlocation, String destlocation, String title, String distance) {
        m_activity = activity;
        this.vehicleNo =vehicleNo;
        this.success = success;
        this.title = title;
        this.userlocation = userlocation;
        this.destlocation =destlocation;
        this.distance=distance;
        initMapFragment();
    }

    private void initMapFragment() {
        m_mapFragment = (MapFragment) m_activity.getFragmentManager()
                .findFragmentById(R.id.mapsfragment);
        // Set path of isolated disk cache
        routetext = m_activity.findViewById(R.id.routetext);
        estimatedTime = m_activity.findViewById(R.id.estimatedtime);
        instructiontext =m_activity.findViewById(R.id.instructiontext);
        if (!success) {
            // Setting the isolated disk cache was not successful, please check if the path is valid and
            // ensure that it does not match the default location
            // (getExternalStorageDirectory()/.here-maps).
            // Also, ensure the provided intent name does not match the default intent name.
            Log.e("Error","Not found");
        } else {
            if (m_mapFragment != null) {

            /* Initialize the MapFragment, results will be given via the called back. */
                m_mapFragment.init(new OnEngineInitListener() {
                    @Override
                    public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {

                        if (error == OnEngineInitListener.Error.NONE) {
                            m_mapFragment.getMapGesture().addOnGestureListener(gestureListener, 100, true);
                            // retrieve a reference of the map from the map fragment
                            m_map = m_mapFragment.getMap();
                            if (vehicleNo.equalsIgnoreCase("0"))
                            m_map.setZoomLevel(13);
                            else
                                m_map.setZoomLevel(19);
                            m_map.addTransformListener(onTransformListener);

                            PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
                            final RoutePlan routePlan = new RoutePlan();
                            routetext.setVisibility(View.VISIBLE);
                            // these two waypoints cover suburban roads
                            String userLatLng[]=userlocation.split(",");
                            final String destLatLng[]=destlocation.split(",");
                            if (vehicleNo.equalsIgnoreCase("0")) {
                                lat = Double.valueOf(destLatLng[0]);
                                lon = Double.valueOf(destLatLng[1]);
                                GeoCoordinate coordinate = new GeoCoordinate(Double.valueOf(userLatLng[0]), Double.valueOf(userLatLng[1]));
                                routePlan.addWaypoint(new RouteWaypoint(coordinate));
                                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.valueOf(destLatLng[0]), Double.valueOf(destLatLng[1]))));
                                triggerRevGeocodeRequest(coordinate);
                            }else{
                                lat = Double.valueOf(userLatLng[0]);
                                lon = Double.valueOf(userLatLng[1]);
                                GeoCoordinate coordinate =new GeoCoordinate(Double.valueOf(destLatLng[0]), Double.valueOf(destLatLng[1]));
                                routePlan.addWaypoint(new RouteWaypoint(coordinate));
                                 routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.valueOf(userLatLng[0]), Double.valueOf(userLatLng[1]))));
                                triggerRevGeocodeRequest(coordinate);
                            }
                            try {

                                // calculate a route for navigation
                                CoreRouter coreRouter = new CoreRouter();
                                RouteOptions routeOptions = new RouteOptions();
                                routeOptions.setTransportMode(MainActivity.transportMode);
                                routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                                routePlan.setRouteOptions(routeOptions);
                                coreRouter.calculateRoute(routePlan, new CoreRouter.Listener() {
                                    @Override
                                    public void onCalculateRouteFinished(List<RouteResult> list,
                                                                         RoutingError routingError) {
                                        routetext.setVisibility(View.GONE);
                                        if (routingError == RoutingError.NONE) {
                                            Route route = list.get(0).getRoute();
                                            RouteTta tt = route.getTta(Route.TrafficPenaltyMode.OPTIMAL,route.getSublegCount()>0&&route.getSublegCount()!=1?1:0);
                                            long timeInSeconds = tt.getDuration();
                                            long timeInMinutes = timeInSeconds/60;
                                            estimatedTime.append("Total Estimated Time: "+timeInMinutes+"mins"+"\n");

                                            MapRoute mapRoute = new MapRoute(route);
                                            mapRoute.setManeuverNumberVisible(true);
                                            m_map.addMapObject(mapRoute);
                                            // move the map to the first waypoint which is starting point of
                                            // the route
                                            m_map.setCenter(routePlan.getWaypoint(0).getNavigablePosition(),
                                                    Map.Animation.NONE);

                                            // setting MapUpdateMode to RoadView will enable automatic map
                                            // movements and zoom level adjustments
                                            NavigationManager.getInstance().setMapUpdateMode
                                                    (NavigationManager.MapUpdateMode.ROADVIEW);

                                            // adjust tilt to show 3D view
                                            m_map.setTilt(80);

                                            // adjust transform center for navigation experience in portrait
                                            // view
                                            m_mapTransformCenter = new PointF(m_map.getTransformCenter().x, (m_map
                                                    .getTransformCenter().y * 85 / 50));
                                            m_map.setTransformCenter(m_mapTransformCenter);
                                            m_map.setTrafficInfoVisible(true);
                                            m_map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.FLOW, true);
                                            // create a map marker to show current position
                                            Image icon = new Image();
                                            m_positionIndicatorFixed = new MapMarker();
                                            try {
                                                icon.setImageResource(R.drawable.markerimgbig);
                                                m_positionIndicatorFixed.setIcon(icon);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            m_positionIndicatorFixed.setVisible(true);
                                            m_positionIndicatorFixed.setCoordinate(m_map.getCenter());
                                            m_map.addMapObject(m_positionIndicatorFixed);
                                            MapMarker marker = new MapMarker(new GeoCoordinate(lat,lon),icon);
                                            marker.setVisible(true);
                                            m_map.addMapObject(marker);
                                            m_mapFragment.getPositionIndicator().setVisible(false);

                                            NavigationManager.getInstance().setMap(m_map);

                                            // listen to real position updates. This is used when RoadView is
                                            // not active.4
                                            instructiontext.setVisibility(View.VISIBLE);
                                            instructiontext.bringToFront();
                                            instructiontext.invalidate();
                                            PositioningManager.getInstance().addListener(
                                                    new WeakReference<PositioningManager.OnPositionChangedListener>(
                                                            mapPositionHandler));
                                            NavigationManager.getInstance().addNewInstructionEventListener(new WeakReference<NavigationManager.NewInstructionEventListener>(new NavigationManager.NewInstructionEventListener() {
                                                @Override
                                                public void onNewInstructionEvent() {
                                                    Maneuver maneuver= NavigationManager.getInstance().getNextManeuver();
                                                    Maneuver.Turn turn = maneuver.getTurn();
                                                    String turnName=turn.name();
                                                    int distance = maneuver.getDistanceFromPreviousManeuver();
                                                    String nextRoadName = maneuver.getNextRoadName();
                                                    Log.e("Instruction",turnName+"\n"+distance+"\n"+nextRoadName);
                                                    if (vehicleNo.equalsIgnoreCase("0")){
                                                        instructiontext.setText("Take a "+turnName+" in "+distance+"mts");
                                                    }else{
                                                        instructiontext.setText("Will take a "+turnName+" in "+distance+"mts");
                                                    }
                                                }
                                            }));
                                            NavigationManager.getInstance().addNavigationManagerEventListener(new WeakReference<NavigationManager.NavigationManagerEventListener>(new NavigationManager.NavigationManagerEventListener() {
                                                @Override
                                                public void onEnded(NavigationManager.NavigationMode navigationMode) {
                                                    stopForegroundService();
                                                    instructiontext.setText("Destination Reached!");
                                                    Toast.makeText(m_activity, "Destination Reached", Toast.LENGTH_SHORT).show();
                                                }
                                            }));
//                                             listen to updates from RoadView which tells you where the map
//                                             center should be situated. This is used when RoadView is active.
                                            NavigationManager.getInstance().getRoadView().addListener(new
                                                    WeakReference<NavigationManager.RoadView.Listener>(roadViewListener));

                                            // start navigation simulation travelling at 13 meters per second
                                            startForegroundService();
                                            NavigationManager.getInstance().simulate(route, 13);
                                        } else {
                                            Toast.makeText(m_activity,
                                                    "Error:route calculation returned error code: " + routingError,
                                                    Toast.LENGTH_LONG).show();

                                        }
                                    }

                                    @Override
                                    public void onProgress(int i) {

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "ERROR: Cannot initialize Map with error " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }


        m_mapFragment.addOnMapRenderListener(new OnMapRenderListener() {
            @Override
            public void onPreDraw() {
                if (m_positionIndicatorFixed != null) {
                    if (NavigationManager.getInstance()
                            .getMapUpdateMode().equals(NavigationManager.MapUpdateMode.ROADVIEW)) {
                        if (!m_returningToRoadViewMode) {
                            // when road view is active, we set the position indicator to align
                            // with the current map transform center to synchronize map and map
                            // marker movements.
                            m_positionIndicatorFixed.setCoordinate(m_map.pixelToGeo(m_mapTransformCenter));
                        }
                    }
                }
            }

            @Override
            public void onPostDraw(boolean var1, long var2) {
            }

            @Override
            public void onSizeChanged(int var1, int var2) {
            }

            @Override
            public void onGraphicsDetached() {
            }

            @Override
            public void onRenderBufferCreated() {
            }
        });

    }

    // listen for positioning events
    private PositioningManager.OnPositionChangedListener mapPositionHandler = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod method, GeoPosition position,
                                      boolean isMapMatched) {
            if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager
                    .MapUpdateMode.NONE) && !m_returningToRoadViewMode)
                // use this updated position when map is not updated by RoadView.
                m_positionIndicatorFixed.setCoordinate(position.getCoordinate());
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod method,
                PositioningManager.LocationStatus status) {

        }
    };

    private void pauseRoadView() {
        // pause RoadView so that map will stop moving, the map marker will use updates from
        // PositionManager callback to update its position.

        if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager.MapUpdateMode.ROADVIEW)) {
            NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
            NavigationManager.getInstance().getRoadView().removeListener(roadViewListener);
            m_lastZoomLevelInRoadViewMode = m_map.getZoomLevel();
        }
    }

    private void resumeRoadView() {
        // move map back to it's current position.
        m_map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(), Map
                        .Animation.BOW, m_lastZoomLevelInRoadViewMode, Map.MOVE_PRESERVE_ORIENTATION,
                80);
        // do not start RoadView and its listener until the map movement is complete.
        m_returningToRoadViewMode = true;
    }

    // application design suggestion: pause roadview when user gesture is detected.
    private MapGesture.OnGestureListener gestureListener = new MapGesture.OnGestureListener() {
        @Override
        public void onPanStart() {
            pauseRoadView();
        }

        @Override
        public void onPanEnd() {
        }

        @Override
        public void onMultiFingerManipulationStart() {
        }

        @Override
        public void onMultiFingerManipulationEnd() {
        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> objects) {
            return false;
        }

        @Override
        public boolean onTapEvent(PointF p) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF p) {
            return false;
        }

        @Override
        public void onPinchLocked() {
        }

        @Override
        public boolean onPinchZoomEvent(float scaleFactor, PointF p) {
            pauseRoadView();
            return false;
        }

        @Override
        public void onRotateLocked() {
        }

        @Override
        public boolean onRotateEvent(float rotateAngle) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float angle) {
            pauseRoadView();
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF p) {
            return false;
        }

        @Override
        public void onLongPressRelease() {
        }

        @Override
        public boolean onTwoFingerTapEvent(PointF p) {
            return false;
        }
    };

    final private NavigationManager.RoadView.Listener roadViewListener = new NavigationManager.RoadView.Listener() {
        @Override
        public void onPositionChanged(GeoCoordinate geoCoordinate) {
            // an active RoadView provides coordinates that is the map transform center of it's
            // movements.
            m_mapTransformCenter = m_map.projectToPixel
                    (geoCoordinate).getResult();
        }
    };

    final private Map.OnTransformListener onTransformListener = new Map.OnTransformListener() {
        @Override
        public void onMapTransformStart() {
        }

        @Override
        public void onMapTransformEnd(MapState mapsState) {
            // do not start RoadView and its listener until moving map to current position has
            // completed
            if (m_returningToRoadViewMode) {
                NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode
                        .ROADVIEW);
                NavigationManager.getInstance().getRoadView().addListener(new
                        WeakReference<NavigationManager.RoadView.Listener>(roadViewListener));
                m_returningToRoadViewMode = false;
            }
        }

    };

    public void onDestroy() {
        m_map.removeMapObject(m_positionIndicatorFixed);
        NavigationManager.getInstance().stop();
        PositioningManager.getInstance().stop();
        estimatedTime.setText("");
    }

    public void onBackPressed() {
        if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager
                .MapUpdateMode.NONE)) {
            resumeRoadView();
        } else {
            m_activity.finish();
        }
    }
    private void triggerRevGeocodeRequest(GeoCoordinate coordinate) {
        /* Create a ReverseGeocodeRequest object with a GeoCoordinate. */
        ReverseGeocodeRequest2 revGecodeRequest = new ReverseGeocodeRequest2(coordinate);
        revGecodeRequest.execute(new ResultListener<Location>() {
            @Override
            public void onCompleted(Location location, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    /*
                     * From the location object, we retrieve the address and display to the screen.
                     * Please refer to HERE Android SDK doc for other supported APIs.
                     */
                    //instructiontext.setVisibility(View.VISIBLE);
                    //instructiontext.setText("Destination:"+location.getAddress().toString());
                    Toast.makeText(m_activity, "Destination:"+location.getAddress().toString(), Toast.LENGTH_SHORT).show();
                    if (HomeScreenActivity.flag == 1)
                        estimatedTime.append("Destination:"+location.getAddress().toString().replace("\n",",")+"\n");
                    else
                        estimatedTime.append("Destination:"+HomeScreenActivity.name+"\n"+location.getAddress().toString().replace("\n",",")+"\n");
                } else {
                    Toast.makeText(m_activity, "Reverse Geocoding not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean m_foregroundServiceStarted;
    private void startForegroundService() {
        if (!m_foregroundServiceStarted) {
            m_foregroundServiceStarted = true;
            Intent startIntent = new Intent(m_activity, ForegroundService.class);
            startIntent.setAction(ForegroundService.START_ACTION);
            m_activity.startService(startIntent);
        }
    }

    private void stopForegroundService() {
        if (m_foregroundServiceStarted) {
            m_foregroundServiceStarted = false;
            Intent stopIntent = new Intent(m_activity, ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOP_ACTION);
            m_activity.startService(stopIntent);
        }
    }
}
