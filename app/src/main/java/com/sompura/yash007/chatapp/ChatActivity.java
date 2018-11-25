package com.sompura.yash007.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {

    String sId, rId, name, shortName;
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList;
    private static final String TAG = "SearchContactsActivity";
    private ImageButton sendButton;
    private EditText message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sId = getIntent().getExtras().getString("sId","");
        rId = getIntent().getExtras().getString("rId","");
        name = getIntent().getExtras().getString("name","");
        shortName = getIntent().getExtras().getString("shortName","");
        sendButton = (ImageButton) findViewById(R.id.sendMessageButton);
        message = (EditText) findViewById(R.id.sendMessageEditText);

        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.chatScroll);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);

        new LoadChat(sId,rId).execute();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getText().toString() != null && message.getText().toString().length() != 0 && message.getText().toString().isEmpty() == false)   {
                    new SendChat(message.getText().toString(), sId,rId).execute();
                    message.setText("");
                }
            }
        });

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new LoadChat(sId,rId).execute();
                handler.postDelayed(this,10000);
            }
        };

        handler.postDelayed(runnable, 10000);
    }

    private class LoadChat extends AsyncTask<Void, Void, Void> {

        String senderId,receiverId;
        public LoadChat(String senderId, String receiverId) {
            this.senderId = senderId;
            this.receiverId = receiverId;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog


        }
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(Config.loadChat+"&senderId="+senderId+"&receiverId="+receiverId);


            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("chat");
                    contactList.clear();
                    AES.setKey();
                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String message = c.getString("message");
                        String date = c.getString("date");
                        String time = c.getString("time");
                        String sensLevel = c.getString("sensLevel");
                        String senId = c.getString("senderId");
                        String shortName = c.getString("shortName");

                        AES.decrypt(message);
                        message = AES.getDecryptedString();
                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("message",message);
                        contact.put("date",date);
                        contact.put("time",time);
                        contact.put("sensLevel",sensLevel);
                        contact.put("senId",senId);
                        contact.put("shortName",shortName);

                        // adding contact to contact list

                        contactList.add(contact);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    ChatActivity.this, contactList,
                    R.layout.chat_bubble, new String[]{"message","time", "sensLevel","senId","shortName"},
                    new int[]{R.id.messageChatListView,R.id.timeChatListView,
                            R.id.sensLevelChatListView,R.id.senIdChatListView,R.id.shortNameChatListView});

            lv.setAdapter(adapter);
        }

    }

    public class SendChat extends AsyncTask<String, Void, String> {

        String message, senderId, receiverId;

        public SendChat(String message, String senderId, String receiverId)  {
            this.message = message;
            this.senderId = senderId;
            this.receiverId = receiverId;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pDialog = new ProgressDialog(ChatActivity.this);
//            pDialog.setMessage("Please wait...");
//            pDialog.setCancelable(false);
//            pDialog.show();
        }

        protected String doInBackground(String... arg0) {
            try {
                //URL url = new URL("https://studytutorial.in/post.php");
                URL url = new URL(Config.sendChat);

                AES.setKey();
                AES.encrypt(message);
                SensitiveLevel  sensitiveLevel = new SensitiveLevel(message.toLowerCase());
                message = AES.getEncryptedString();
                int level = sensitiveLevel.findSensitiveLevel();

                JSONObject postDataParams = new JSONObject();

                postDataParams.put("message",message);
                postDataParams.put("senderId",senderId);
                postDataParams.put("receiverId",receiverId);
                postDataParams.put("sensLevel",level);

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

            if(status.equals("Success") == true) {
                //Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                new LoadChat(sId,rId).execute();
            }
            else    {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }


        }
    }
}
