package com.example.dell.gps_test;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.dell.gps_test.Config.DEST_LOCATION;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String[] mapTypeStrings = {"Normal", "Hybrid", "Satellite", "Terrain"};
    public double lat, lng;
    public float speed;
    SharedPreferences sp;
    String latitude = "";
    String longitude = "";
    String spd = "";
    String origin = "";
     FrameLayout progressBarHolder;
     AlphaAnimation inAnimation;
     AlphaAnimation outAnimation;
    private GoogleMap mMap;
    private List<LatLng> points;
    private int lastMapType = 1;
    private BroadcastReceiver broadcastReceiver;
    private TextView txtMessage;

    private static MainActivity instance;

    public MainActivity getInstance(){
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0358B2")));
        sp = getSharedPreferences("myPref", MODE_PRIVATE);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        getData();
        txtMessage = findViewById(R.id.txt_push);



/** Either on the constructor or the 'OnCreate' method, you should add: */
        instance = this;

    }


//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu,menu);
//        return super.onCreateOptionsMenu(menu);
//    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.map_routes_clear:
//
//             Toast.makeText(getApplicationContext(),"select",Toast.LENGTH_LONG).show();
//
//                break;
//
//            case R.id.map_type:
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
//                alertDialogBuilder.setTitle("Choose map type");
//
//                alertDialogBuilder.setSingleChoiceItems(mapTypeStrings, lastMapType, new DialogInterface.OnClickListener() {
//                    @Override public void onClick( DialogInterface dialog, int which ) {
//                        dialog.dismiss();
//
//                        if (which == lastMapType)
//                            return;
//
//                        lastMapType = which;
//
//                        switch (which) {
//                            case 0:
//                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                                break;
//                            case 1:
//                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//                                break;
//                            case 2:
//                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//                                break;
//                            case 3:
//                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//                                break;
//                        }
//                    }
//                });
//                alertDialogBuilder.setNegativeButton("Cancel", null);
//                alertDialogBuilder.show();
//                break;
//
//    }
//        return true;
//    }

    private void getData() {
        StringRequest stringRequest = new StringRequest(Config.DATA_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showJSON(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void showJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config.JSON_ARRAY);
            JSONObject geoData = result.getJSONObject(0);
            latitude = geoData.getString(Config.KEY_LAT);
            longitude = geoData.getString(Config.KEY_LNG);
            spd = geoData.getString(Config.KEY_SPEED);
            origin = latitude + "," + longitude;

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            new GetDirection().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            lat = Double.parseDouble(latitude);
            lng = Double.parseDouble(longitude);
            speed = Float.parseFloat(spd);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Bus No. 2").snippet("Your Son is here.").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
        mMap.setMaxZoomPreference(20);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    class GetDirection extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... strings) {

            String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + DEST_LOCATION + "&sensor=false";
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection httpconn = (HttpURLConnection) url
                        .openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(httpconn.getInputStream()),
                            8192);
                    String strLine = null;
                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }
                String jsonOutput = response.toString();

                JSONObject jsonObject = new JSONObject(jsonOutput);

                // routesArray contains ALL routes
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                // Grab the first route
                JSONObject route = routesArray.getJSONObject(0);

                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyline = poly.getString("points");
                points = decodePoly(polyline);


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(String file_url) {
            for (int i = 0; i < points.size() - 1; i++) {
                LatLng src = points.get(i);
                LatLng dest = points.get(i + 1);
                try {
                    //here is where it will draw the polyline in your map
                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(src.latitude, src.longitude),
                                    new LatLng(dest.latitude, dest.longitude))
                            .width(2).color(Color.RED).geodesic(true));
                } catch (NullPointerException e) {
                    Log.e("Error", "NullPointerException onPostExecute: " + e.toString());
                } catch (Exception e2) {
                    Log.e("Error", "Exception onPostExecute: " + e2.toString());
                }

            }
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

        }
    }


}
