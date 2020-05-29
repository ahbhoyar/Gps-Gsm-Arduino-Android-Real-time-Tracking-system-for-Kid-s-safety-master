package com.example.dell.gps_test;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.dell.gps_test.Config.KEY_PASSWORD;
import static com.example.dell.gps_test.Config.KEY_USERNAME;
import static com.example.dell.gps_test.Config.LOGIN_URL;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    public ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;
    private ProgressBar pd;
    private View progressView;
    private AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getApplicationContext().getSharedPreferences("myPref", MODE_PRIVATE);
        if (sharedPreferences.contains("username")) {
            startActivity(new Intent(this, MapView.class));

        }


        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);
        _signupLink = findViewById(R.id.link_signup);


        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Check()) {
                    login();
                }
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Signup.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);


            }

        });


    }

    private void login() {
        Log.d(TAG, "Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(Login.this,
                R.style.AppTheme);

        final String Email = _emailText.getText().toString();
        final String Password = _passwordText.getText().toString();

        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("response");
                    JSONObject data = result.getJSONObject(0);

                    String name = data.getString("status");
                    String studentId   = data.getString("studentId");
                if (name.equals("success")) {

                    SharedPreferences.Editor e = sharedPreferences.edit();
                    e.putString("username", Email);
                    e.putString("password", Password);
                    e.putString("studentId", studentId);
                    e.commit();

                    Toast.makeText(getApplicationContext(), "Login Succesfully", Toast.LENGTH_LONG).show();

                    progressDialog.dismiss();

                    Intent intent = new Intent(getApplicationContext(), MapView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);


                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(),
                            R.style.Theme_AppCompat_Dialog_Alert);
                    builder.setMessage(response)
                            .setTitle("Login Error")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Intent intentFail = new Intent(getApplicationContext(), Login.class);
                    startActivity(intentFail);



                }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.Theme_AppCompat_Dialog_Alert);
                builder.setMessage(error.toString())
                        .setTitle("Error")
                        .setPositiveButton(android.R.string.ok, null);
                         AlertDialog dialog = builder.create();
                         dialog.show();
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                _loginButton.setEnabled(true);
                progressDialog.dismiss();

            }

        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERNAME, Email);
                map.put(KEY_PASSWORD, Password);
                return map;


            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();

            }
        }

    }

    @Override
    public void onBackPressed() {

        moveTaskToBack(false);
    }


    public Boolean Check() {
        ConnectivityManager cn = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        if (nf != null && nf.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection.!",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }


}
