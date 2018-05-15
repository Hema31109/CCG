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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.routing.RouteOptions;
import com.newtechs.locations.cg.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity which launches map view and handles Android run-time requesting permission.
 */
public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private MapFragmentView m_mapFragmentView;
    String userlocation,destlocation,title,distance,vno;
    Toolbar toolbar;
    boolean success;
    public static RouteOptions.TransportMode transportMode = RouteOptions.TransportMode.CAR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advancednavigation);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Routing...");
        Bundle i = getIntent().getExtras();
        userlocation = i.getString("userlocation");
        destlocation = i.getString("destinationlocation");
        vno = i.getString("vno");
        title = i.getString("title");
        setTitle(title);
        distance = i.getString("distance");
        requestPermissions();
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    private void requestPermissions() {

        final List<String> requiredSDKPermissions = new ArrayList<String>();
        requiredSDKPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requiredSDKPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredSDKPermissions.add(Manifest.permission.INTERNET);
        requiredSDKPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        requiredSDKPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);

        ActivityCompat.requestPermissions(this,
                requiredSDKPermissions.toArray(new String[requiredSDKPermissions.size()]),
                REQUEST_CODE_ASK_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {

                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /**
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                permissions[index])) {
                            Toast.makeText(this,
                                    "Required permission " + permissions[index]
                                            + " not granted. Please go to settings and turn on for sample app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Required permission " + permissions[index] + " not granted",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }

                /**
                 * All permission requests are being handled.Create map fragment view.Please note
                 * the HERE SDK requires all permissions defined above to operate properly.
                 */


                success = MapSettings.setIsolatedDiskCacheRootPath(
                        getExternalFilesDir(null) + File.separator + ".here-maps",
                        "com.here.android.tut.MapService");
                m_mapFragmentView = new MapFragmentView(vno,this,success,userlocation,destlocation,title,distance);

                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onDestroy(){
        m_mapFragmentView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        m_mapFragmentView.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selectconnectivity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.walk:
                transportMode =  RouteOptions.TransportMode.PEDESTRIAN;
                break;
         case R.id.car:
                transportMode = RouteOptions.TransportMode.CAR;
                break;
        }
        m_mapFragmentView.onDestroy();
        m_mapFragmentView = new MapFragmentView(vno,this,success,userlocation,destlocation,title,distance);
        return super.onOptionsItemSelected(item);
    }
}
