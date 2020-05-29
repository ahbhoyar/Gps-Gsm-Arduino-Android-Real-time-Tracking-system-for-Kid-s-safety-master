package com.example.dell.gps_test;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dell.gps_test.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.dell.gps_test.Config.DATA_URL;
import static com.example.dell.gps_test.Config.KEY_PASSWORD;
import static com.example.dell.gps_test.Config.KEY_USERNAME;
import static com.example.dell.gps_test.Config.STD_INFO;


public class MapView extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener {
    private final String[] mapTypeStrings = {"Normal", "Hybrid", "Satellite", "Terrain"};
    public double lat, lng;
    public double stdlat, stdlng;
    public String spd;
    SharedPreferences sp;
    String latitude = "";
    String longitude = "";
    String stdlatitude = "";
    String stdlongitude = "";
    String latitude1 = "";
    String longitude1 = "";
    String stdlatitude1 = "";
    String stdlongitude1 = "";
    String speed = "";
    String origin = "";
    String name;
    String regId;
    String loc;
    String dest;
    String des;
    int alert;
    Marker marker;
    FrameLayout progressBarHolder;
    Context context;
    private int lastMapType = 1;
    private GoogleMap mMap;
    private List<LatLng> latLngList;
    private TextView distance;
    private List<LatLng> points;
    private TextView textView;
    private TextView tName;
    private TextView tRegId;
    private TextView location;
    private Button btnSch, btnDrv;
    private List<Marker> originMarker = new ArrayList<>();
    private List<Marker> destination = new ArrayList<>();
    private List<Polyline> polylinePath = new ArrayList<>();
    private ProgressBar pd;
    String stdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        sp = getSharedPreferences("myPref", MODE_PRIVATE);
        stdId =  sp.getString("studentId","0");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(" Kids Tracker");

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        // collapsingToolbarLayout.setTitle("Kids Tracking");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // distance = (TextView) findViewById(R.id.ad);
        tName = findViewById(R.id.std);
        tRegId = findViewById(R.id.regId);
        location = findViewById(R.id.location);
        btnSch = findViewById(R.id.sch);
        btnDrv = findViewById(R.id.drv);
        latLngList = new ArrayList<LatLng>();


        try {

                getStudentDetail();

                getServerData();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendRequest() {
        try {

            new DirectionFinder(this, origin, des).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getStudentDetail() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,STD_INFO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                showStudentDetail(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "Internet Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Side Error!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();
                }
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> map = new HashMap<String, String>();
            map.put("studentId", stdId);
            return map;

        }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void showStudentDetail(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config.STDINFO);
            JSONObject data = result.getJSONObject(0);

            name = data.getString("name")+" "+data.getString("last");
            regId = data.getString("class");
            loc = data.getString("busNo");
            dest = data.getString("location");
            stdlatitude = data.getString("lat");
            stdlongitude = data.getString("lon");
            stdlatitude1 = stdlatitude.substring(0, 7);
            stdlongitude1 = stdlongitude.substring(0, 7);
            des = stdlatitude1 + "," + stdlongitude1;
            stdlat = Double.parseDouble(stdlatitude);
            stdlng = Double.parseDouble(stdlongitude);


            tName.setText(name);
            tRegId.setText("Class-" + regId);
            location.setText(dest);

            btnSch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   // sendRequest();

                }
            });
            btnDrv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent drv = new Intent(getApplicationContext(), driver.class);
                    startActivity(drv);

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void getServerData() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DATA_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                showJSON(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "Internet Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Side Error!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();
                }


                Toast.makeText(MapView.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("busNo", stdId);
                map.put("studentId", stdId);
                return map;
        }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

        SystemClock.sleep(1000);
        looper();
    }

    private void showJSON(String response) {


        try {

            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config.JSON_ARRAY);
            JSONObject geoData = result.getJSONObject(0);
            latitude = geoData.getString(Config.KEY_LAT);
            longitude = geoData.getString(Config.KEY_LNG);

            latitude1 = latitude.substring(0, 7);
            longitude1 = longitude.substring(0, 7);
            alert = Integer.parseInt(geoData.getString("stop"));
            //spd = geoData.getString(Config.KEY_SPEED);

            // new MainActivity.GetDirection();


        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            lat = Double.parseDouble(latitude);
            lng = Double.parseDouble(longitude);
            origin = latitude1 + "," + longitude1;
            // speed = Float.parseFloat(spd);
            location.setText(latlongAddress(lat, lng));
            //sendRequest();


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        if (mMap == null || latitude == null) return;

        mMap.clear();


        LatLng latlng = new LatLng(lat, lng);

        mMap.addMarker(new MarkerOptions()

                .position(latlng).title(latlongAddress(lat, lng))
                .title("Bus No. 1")
                .snippet("Your Son is here.")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16f));


        if (alert == 0) {
            addNotification();
        }
        if ((stdlatitude1.equals(latitude1)) || (stdlongitude1.equals(longitude1))) {
            reachNotification();
        }

    }

    public String latlongAddress(double lt, double ln) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        String ad = "";
        try {
            addresses = geocoder.getFromLocation(lt, ln, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                ad = address.getAddressLine(0) +
                        "," + address.getLocality() +
                        "," + address.getAdminArea();


            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return ad;
    }

    private void looper() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getServerData();
                Log.d("Log", "inside looper");

            }
        }, 10000);

    }


    private void addNotification() {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.map_marker_dark)
                .setContentTitle("Warning")
                .setContentText("Please call school authority")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("School Bus has been stopped from Last 5 minute"))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        Intent notificationIntent = new Intent(this, MapView.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());

    }

    private void reachNotification() {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.end_green)
                .setContentTitle("Alert")
                .setContentText("School Vehicle Arrived at your Stop")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Please Get Your Child "))
                .setSound(sound)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        Intent notificationIntent = new Intent(this, MapView.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(001, builder.build());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main
                , menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_map_type:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapView.this);
                alertDialogBuilder.setTitle("Choose map type");

                alertDialogBuilder.setSingleChoiceItems(mapTypeStrings, lastMapType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (which == lastMapType)
                            return;

                        lastMapType = which;

                        switch (which) {
                            case 0:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                        }
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", null);
                alertDialogBuilder.show();
                break;

        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        if (marker == null) {

            mMap = googleMap;
             LatLng school = new LatLng(21.1417 ,79.1336);
             mMap.addMarker(new MarkerOptions().position(school).title("St. Xavier School ").snippet("Your Son is here.").icon(BitmapDescriptorFactory.fromResource(R.drawable.school)));
             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(school,15));


            //  mMap.getUiSettings().setZoomControlsEnabled(true);
            //   mMap.getUiSettings().setAllGesturesEnabled(true);
            //  mMap.getUiSettings().setZoomControlsEnabled(true);
            //   mMap.getUiSettings().setTiltGesturesEnabled(true);
            //   mMap.getUiSettings().setMapToolbarEnabled(true);
            //    mMap.moveCamera(CameraUpdateFactory.zoomBy(15));

        }


    }

    @Override
    protected void onResume() {

        super.onResume();
        try {
            getStudentDetail();
            getServerData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            getStudentDetail();
            getServerData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        // moveTaskToBack(false);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        //finish();
    }


    @Override
    public void onDirectionFinderStart() {

        if (marker != null) {
            marker.remove();
        }

        if (originMarker != null) {
            for (Marker marker : originMarker) {
                marker.remove();
            }
        }

        if (destination != null) {
            for (Marker marker : destination) {
                marker.remove();
            }
        }

        if (polylinePath != null) {
            for (Polyline polyline : polylinePath) {
                polyline.remove();
            }
        }


    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {

        polylinePath = new ArrayList<>();
        originMarker = new ArrayList<>();
        destination = new ArrayList<>();

        for (Route routes : route) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.startLocation, 17));

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom(15)
                    .bearing(28)
                    .tilt(45)
                    .build()
            ));


            ((TextView) findViewById(R.id.a)).setText(routes.duration.text);
            ((TextView) findViewById(R.id.b)).setText(routes.distance.text);

            originMarker.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))
                    .title(routes.startAddress)
                    .position(routes.startLocation)));


            destination.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.home))
                    .title(routes.endAddress)
                    .position(routes.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < routes.points.size(); i++)
                polylineOptions.add(routes.points.get(i));

            polylinePath.add(mMap.addPolyline(polylineOptions));
        }


    }


    public Boolean Check() {
        ConnectivityManager cn = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        if (nf != null && nf.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast.makeText(context, "No internet connection.!",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }






}











