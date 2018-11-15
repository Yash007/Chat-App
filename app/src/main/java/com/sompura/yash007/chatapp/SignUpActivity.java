package com.sompura.yash007.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class SignUpActivity extends AppCompatActivity {

    private Button signUpButton;
    private Button signUpCancelButton;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText phone;
    private EditText password;
    private EditText confirmPassword;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        signUpButton = (Button) findViewById(R.id.signUpButton);
        signUpCancelButton = (Button) findViewById(R.id.signUpCancelButton);

        firstName = (EditText) findViewById(R.id.signUpFirstNameEditText);
        lastName = (EditText) findViewById(R.id.signUpLastNameEditText);
        email = (EditText) findViewById(R.id.signUpEmailEditText);
        password = (EditText) findViewById(R.id.signUpPasswordEditText);
        confirmPassword = (EditText) findViewById(R.id.signUpConfirmPasswordEditText);
        phone = (EditText) findViewById(R.id.signUpPhoneNumberEditText);

        signUpCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = "pass";
                Boolean pass = true;


                if(firstName.getText().toString() != null && firstName.getText().toString().length() == 0)  {
                    pass = false;
                    temp = "Please enter firstname";
                }

                if(lastName.getText().toString() != null && lastName.getText().toString().length() == 0 && pass == true)  {
                    pass = false;
                    temp = "Please enter lastname";
                }

                if(phone.getText().toString() != null && phone.getText().toString().length() == 0 && pass == true)  {
                    pass = false;
                    temp = "Please enter phonenumber";
                }

                if(email.getText().toString() != null && email.getText().toString().length() == 0 && pass == true)  {
                    pass = false;
                    temp = "Please enter email";
                }

                if(password.getText().toString() != null && password.getText().toString().length() == 0 && pass == true)  {
                    pass = false;
                    temp = "Please enter password";
                }

                if(confirmPassword.getText().toString() != null && confirmPassword.getText().toString().length() == 0 && pass == true)  {
                    pass = false;
                    temp = "Please enter confirm password";
                }

                if(password.getText().toString().equals(confirmPassword.getText().toString()) == false && pass == true) {
                    pass = false;
                    temp = "Both password must be same";
                }

                if(pass == true)    {
                    new SignUpWeb(
                            firstName.getText().toString(),
                            lastName.getText().toString(),
                            email.getText().toString(),
                            phone.getText().toString(),
                            password.getText().toString()
                    ).execute();
                }
                else    {
                    Toast.makeText(SignUpActivity.this,temp,Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public class SignUpWeb extends AsyncTask<String, Void, String> {

        String firstName, lastName, email, phone, password;
        public SignUpWeb(String firstName, String lastName, String email, String phone, String password)  {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.password = password;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignUpActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... arg0) {
            try {
                //URL url = new URL("https://studytutorial.in/post.php");
                URL url = new URL(Config.signUp);

                JSONObject postDataParams = new JSONObject();

                postDataParams.put("uFirstName",firstName);
                postDataParams.put("uLastName",lastName);
                postDataParams.put("uEmail",email);
                postDataParams.put("uMobile",phone);
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
            try {
                JSONObject jsonObject = new JSONObject(result);
                status = jsonObject.getString("result");
                message = jsonObject.getString("message");


            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();

            if(status.equals("Success") == true) {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
            else    {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }


        }
    }

}
