package com.newtechs.locations.cg.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.newtechs.locations.cg.R;
import com.newtechs.locations.cg.listeners.MarkerClickListener;
import com.newtechs.locations.cg.routing.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NearbyPlacesActivity extends AppCompatActivity {
    private MapFragment fragment;
    private Map map;
    private MarkerClickListener listener;
    private ProgressDialog dialog;
    private String coordinates;
    private boolean isUp = false;
    private LinearLayout infoLayout;
    private TextView titleText,descriptionText,distanceText;
    private String title,destinationcoordinates,distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearbyplaces);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Nearby Places");
        titleText = findViewById(R.id.titleInfo);
        infoLayout = findViewById(R.id.infolayout);
        descriptionText = findViewById(R.id.descriptiontext);
        distanceText = findViewById(R.id.distancetimetext);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();
        coordinates = getIntent().getExtras().getString("coordinates");
        fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapsfragment);
        boolean success = MapSettings.setIsolatedDiskCacheRootPath(
                getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.here.android.tut.MapService");
        if (!success){
            Log.e("UserLocationActivity","IsolatedDiskCacheNotSet");
        }else{
            fragment.init(new OnEngineInitListener(){
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    if (error == Error.NONE) {
                        fragment.addOnMapRenderListener(new OnMapRenderListener() {
                  @Override
                  public void onPreDraw() {

                  }

                  @Override
                  public void onPostDraw(boolean b, long l) {
                      dialog.dismiss();
                  }

                  @Override
                  public void onSizeChanged(int i, int i1) {

                  }

                  @Override
                  public void onGraphicsDetached() {

                  }

                  @Override
                  public void onRenderBufferCreated() {

                  }
              });
                        map = fragment.getMap();
                        fragment.getMapGesture().addOnGestureListener(new MapGesture.OnGestureListener() {
                            @Override
                            public void onPanStart() {

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
                            public boolean onMapObjectsSelected(List<ViewObject> list) {
                                for (ViewObject viewObject : list) {
                                    if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                                        MapObject mapObject = (MapObject) viewObject;
                                        if (mapObject.getType() == MapObject.Type.MARKER) {
                                            MapMarker window_marker = ((MapMarker)mapObject);
                                            String titles = window_marker.getTitle();
                                            String distances = window_marker.getDescription();
                                            titleText.setText(titles);
                                            GeoCoordinate coordinate =window_marker.getCoordinate();
                                            descriptionText.setText("Coordinates: "+coordinate.getLatitude()+","+coordinate.getLongitude());
                                            distanceText.setText(distances);
                                            slideUp(infoLayout);
                                            distance = distances;
                                            destinationcoordinates =coordinate.getLatitude()+","+coordinate.getLongitude();
                                            title = titles;
                                            Log.d("Title->","Title"+window_marker.getTitle());
                                            Log.d("description->","Description"+window_marker.getDescription());
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            }

                            @Override
                            public boolean onTapEvent(PointF pointF) {
                                return false;
                            }

                            @Override
                            public boolean onDoubleTapEvent(PointF pointF) {
                                return false;
                            }

                            @Override
                            public void onPinchLocked() {

                            }

                            @Override
                            public boolean onPinchZoomEvent(float v, PointF pointF) {
                                return false;
                            }

                            @Override
                            public void onRotateLocked() {

                            }

                            @Override
                            public boolean onRotateEvent(float v) {
                                return false;
                            }

                            @Override
                            public boolean onTiltEvent(float v) {
                                return false;
                            }

                            @Override
                            public boolean onLongPressEvent(PointF pointF) {
                                return false;
                            }

                            @Override
                            public void onLongPressRelease() {

                            }

                            @Override
                            public boolean onTwoFingerTapEvent(PointF pointF) {
                                return false;
                            }
                        });
                        String spl []= coordinates.split(",");
                        map.setCenter(new GeoCoordinate(Double.valueOf(spl[0]),Double.valueOf(spl[1])), Map.Animation.NONE);
                        map.getPositionIndicator().setVisible(true);
                        if (checkInternet()) {
                            sendData(coordinates);
                        }else{
                                Snackbar.make(findViewById(R.id.coordinatorlayout),"Check Internet Connection!",Snackbar.LENGTH_LONG).show();
                        }
                    }else{
                        Log.e("Error",error.getStackTrace());
                    }
                    }

            });
        }
    }
    public void sendData(String coordinate)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading");
        dialog.show();
        String url = "https://places.cit.api.here.com/places/v1/discover/explore?app_id=qqLqWZhDd6rkJoTUNHSI&app_code=ZnA8s-QCV5XX3alRa5EHEQ&at="+coordinate+"&cat=hospital-health-care-facility";
        StringRequest stringRequest=new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject rootObject = new JSONObject(response);
                    JSONObject nextObject = rootObject.getJSONObject("results");
                    JSONArray rootArray = nextObject.getJSONArray("items");
                    for(int i=0;i<rootArray.length();i++){
                        JSONObject childObject1 = rootArray.getJSONObject(i);
                        JSONArray positionArray = childObject1.getJSONArray("position");
                        String distance = childObject1.getString("distance");
                        String title = childObject1.getString("title");
                        GeoCoordinate coordinate = new GeoCoordinate(positionArray.getDouble(0),positionArray.getDouble(1));
                        Image image = new Image();
                        try {
                            image.setImageResource(R.drawable.markerimgbig);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        MapMarker marker = new MapMarker(coordinate, image);
                        marker.setTitle(title.toUpperCase());
                        marker.setCoordinate(coordinate);
                        marker.setDescription("Distance:"+distance+"mts");
                        marker.setAnchorPoint(new PointF(image.getWidth() / 2, image.getHeight()));
                        map.addMapObject(marker);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrorMsg",error.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        });
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        if (isUp) {
            slideDown(infoLayout);
        }else{
            super.onBackPressed();
        }
    }

    public void slideUp(View v)
    {
        TranslateAnimation anime=new TranslateAnimation(0,0,v.getHeight(),0);
        anime.setDuration(500);
        anime.setFillAfter(true);
        infoLayout.setVisibility(View.VISIBLE);
        infoLayout.setClickable(true);
        infoLayout.bringToFront();
        infoLayout.invalidate();
        v.startAnimation(anime);
        isUp = true;
    }
    public void slideDown(View v)
    {
        TranslateAnimation anime=new TranslateAnimation(0,0,0,v.getHeight());
        anime.setDuration(500);
        anime.setFillAfter(true);
        v.startAnimation(anime);
        infoLayout.setVisibility(View.GONE);
        isUp = false;
        infoLayout.setClickable(false);
    }

    public void createRoute(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userlocation",coordinates);
        intent.putExtra("destinationlocation",destinationcoordinates);
        intent.putExtra("title",title);
        intent.putExtra("distance",distance);
        intent.putExtra("vno","0");
        HomeScreenActivity.flag = 0;
        HomeScreenActivity.name = title;
        startActivity(intent);
    }

    public boolean checkInternet(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info!=null && info.isAvailable() &&info.isConnected();
    }

}
