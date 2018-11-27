package com.sompura.yash007.chatapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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


@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintLoginHandler extends FingerprintManager.AuthenticationCallback {
    private CancellationSignal cancellationSignal;
    private Activity appContext;
    public Dialog dialog;
    public String mId;
    private ProgressDialog pDialog;
    public String message;
    public TextView message1;

    public FingerprintLoginHandler(Activity context, Dialog dialog, String mId) {
        appContext = context;
        this.dialog = dialog;
        this.mId = mId;
        message = null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void startAuth(FingerprintManager manager,
                          FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                android.Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        Toast.makeText(appContext,
                "Authentication error\n" + errString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        Toast.makeText(appContext,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext,
                "Authentication failed.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        //update method for adding fingerprint

        onAuthSuccess();
    }

    public void onAuthSuccess() {

        dialog.dismiss();

        new GetMessage(mId).execute();


        final Dialog dialog1 = new Dialog(appContext);
        dialog1.setContentView(R.layout.dialog_message);
        dialog1.getWindow().getAttributes().width = LinearLayout.LayoutParams.MATCH_PARENT;
        dialog1.show();

        message1 = (TextView) dialog1.findViewById(R.id.dialogMessage);



        Button logout = dialog1.findViewById(R.id.dialogMessageClose);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
            }
        });

    }

    public class GetMessage extends AsyncTask<String, Void, String> {

        String mId;
        public GetMessage(String mId)  {
            this.mId = mId;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(appContext);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... arg0) {
            try {
                //URL url = new URL("https://studytutorial.in/post.php");
                URL url = new URL(Config.getMessage);

                JSONObject postDataParams = new JSONObject();

                postDataParams.put("mId",mId);

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
            Log.d("TAG1111111111",result);
            JSONObject profile;
            JSONObject jsonObject;
            JSONArray params;
            try {
                jsonObject = new JSONObject(result);
                message = jsonObject.getString("message");
                AES.setKey();
                AES.decrypt(message);
                message = AES.getDecryptedString();
                Log.d("TAG1111111111",message);
                message1.setText(message);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }


}
