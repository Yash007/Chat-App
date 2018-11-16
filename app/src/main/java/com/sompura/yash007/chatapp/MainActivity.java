package com.sompura.yash007.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView signUpText;
    private EditText id, password;
    private Button loginButton;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        signUpText = (TextView) findViewById(R.id.loginSignUpText);

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });

        id = (EditText) findViewById(R.id.loginIdEditText);
        password = (EditText) findViewById(R.id.loginPasswordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = "pass";
                Boolean pass = true;

                if(id.getText().toString() != null && id.getText().toString().length() == 0)  {
                    pass = false;
                    temp = "Please enter Email";
                }

                if(password.getText().toString() != null && password.getText().toString().length() == 0 && pass == true)  {
                    pass = false;
                    temp = "Please enter Password";
                }

                if(pass == true)    {
                    new LoginWeb(
                            id.getText().toString(),
                            password.getText().toString()
                    ).execute();
                }
                else    {
                    Toast.makeText(MainActivity.this,temp,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public class LoginWeb extends AsyncTask<String, Void, String> {

        String id, password;
        public LoginWeb(String id, String password)  {
            this.id = id;
            this.password = password;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... arg0) {
            try {
                //URL url = new URL("https://studytutorial.in/post.php");
                URL url = new URL(Config.login);

                JSONObject postDataParams = new JSONObject();

                postDataParams.put("uEmail",id);
                postDataParams.put("uPassword",password);

                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "Application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","Application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(postDataParams.toString());

                os.flush();
                os.close();

                int responseCode=conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {
                        Log.e("+++++", "line: "+line);
                        sb.append(line);
                        //break;
                    }
                    in.close();
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e) {
                Log.e("~~~", e.toString());
                return new String("Exception: " + e.getMessage());
            }

        }


        @Override
        protected void onPostExecute(String result) {
            Log.d("TAG",result);
            String status = null;
            String message = null;
            JSONObject profile;
            JSONObject jsonObject;
            JSONArray params;
            try {
                jsonObject = new JSONObject(result);
                status = jsonObject.getString("result");
                message = jsonObject.getString("message");

                if(status.equals("Success") == true) {


                    //params = jsonObject.getJSONArray("profile");
                    profile = jsonObject.getJSONObject("profile");

                    SharedPreferences.Editor sharedPreferences = getSharedPreferences(Config.prefName,MODE_PRIVATE).edit();

                    sharedPreferences.putString("id",id);
                    sharedPreferences.putString("password",password);
                    sharedPreferences.putString("firstName",profile.getString("uFirstName"));
                    sharedPreferences.putString("lastName",profile.getString("uLastName"));
                    sharedPreferences.putString("mobile",profile.getString("uMobile"));
                    sharedPreferences.putBoolean("keepLogin",true);
                    sharedPreferences.commit();
                    sharedPreferences.apply();

                    startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                }
                else    {
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }
}
