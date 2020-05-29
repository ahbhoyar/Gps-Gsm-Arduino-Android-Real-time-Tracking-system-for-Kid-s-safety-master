package com.example.dell.gps_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.dell.gps_test.Config.DRV_INFO;

public class driver extends AppCompatActivity {


    String drvfname;
    String drvlname;
    String drvemail;
    String drvphone;
    String drvlic;
    String drvadd;
    String busid;
    String routeid;
    String fullname;

    private TextView drvName;
    private TextView drvEmail;
    private TextView drvPhone;

    private TextView BusId;
    private TextView RouteId;

    private TextView drvLic;
    private TextView drvAdd;
    private Button call;
    SharedPreferences sp;
    String stdId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        sp = getSharedPreferences("myPref", MODE_PRIVATE);
        stdId =  sp.getString("studentId","0");

        drvName = findViewById(R.id.drvName);
        drvEmail = findViewById(R.id.drvEmail);
        drvPhone = findViewById(R.id.drvPhone);
        drvLic = findViewById(R.id.drvLic);
        drvAdd = findViewById(R.id.drvAdd);
        BusId = findViewById(R.id.BusNo);
        RouteId = findViewById(R.id.RouteId);
        call = findViewById(R.id.call);

        try {
            getDriverDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + drvphone));
                startActivity(callIntent);


            }
        });

    }


    private void getDriverDetail() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,DRV_INFO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                showDriverDetail(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(driver.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> map = new HashMap<String, String>();
            map.put("studentId", stdId);
            return map;

        }

        };



        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showDriverDetail(String response) {

        try {


            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config.DRVINFO);
            JSONObject data = result.getJSONObject(0);
            drvfname = data.getString("firstName");
            drvlname = data.getString("lastName");
            drvemail = data.getString("email");
            drvphone = data.getString("phone");
            drvlic = data.getString("driverLic");
            drvadd = data.getString("driverLoc");
            busid = data.getString("busId");
            routeid = data.getString("busNo");
            fullname = drvfname + " " + drvlname;
            drvName.setText(fullname);
            drvEmail.setText(drvemail);
            drvPhone.setText(drvphone);
            BusId.setText(busid);
            RouteId.setText(routeid);
            drvLic.setText(drvlic);
            drvAdd.setText(drvadd);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}