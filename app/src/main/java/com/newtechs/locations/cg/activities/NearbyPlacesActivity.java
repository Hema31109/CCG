package com.newtechs.locations.cg.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.newtechs.locations.cg.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class NearbyPlacesActivity extends AppCompatActivity {
    private MapFragment fragment;
    private Map map;
    private ProgressDialog dialog;
    private String coordinates;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_police);
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
                        String spl []= coordinates.split(",");
                        map.setCenter(new GeoCoordinate(Double.valueOf(spl[0]),Double.valueOf(spl[1])), Map.Animation.NONE);
                        map.getPositionIndicator().setVisible(true);
                        sendData(coordinates);
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
                        marker.setDescription("Title:"+title+"\n"+"Distance:"+distance+"mts");
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
}
