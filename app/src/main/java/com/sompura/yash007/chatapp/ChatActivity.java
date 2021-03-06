package com.sompura.yash007.chatapp;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ChatActivity extends AppCompatActivity {

    String sId, rId, name, shortName;
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList;
    private static final String TAG = "SearchContactsActivity";
    private ImageButton sendButton;
    private EditText message;
    private static final String KEY_NAME = "chatAppKey";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    public Dialog dialog;
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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView sensLevel = (TextView) view.findViewById(R.id.sensLevelChatListView);
                String level = sensLevel.getText().toString();
                TextView senderId = (TextView) view.findViewById(R.id.senIdChatListView);
                TextView mId = (TextView) view.findViewById(R.id.mId);

                if(Integer.parseInt(level) == 1 && senderId.getText().toString().equals(sId) == false)    {
                    //Dialog code here
                    dialog = new Dialog(ChatActivity.this);
                    dialog.setContentView(R.layout.dialog_fingerprint);
                    dialog.getWindow().getAttributes().width = LinearLayout.LayoutParams.MATCH_PARENT;
                    dialog.show();

                    Button logout = dialog.findViewById(R.id.dialogFingerPrintLogout);
                    logout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                    fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

                    generateKey();

                    if (cipherInit()) {
                        cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintLoginHandler helper = new FingerprintLoginHandler(ChatActivity.this, dialog, mId.getText().toString());

                        helper.startAuth(fingerprintManager, cryptoObject);

                    }
                }
            }
        });
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
                        String mId = c.getString("mId");

                        if(Integer.parseInt(sensLevel) == 1 && senId.equals(sId) == false)  {
                            message = "Confidential message!! Tap to view";
                        }
                        else    {
                            AES.decrypt(message);
                            message = AES.getDecryptedString();
                        }
                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("message",message);
                        contact.put("date",date);
                        contact.put("time",time);
                        contact.put("sensLevel",sensLevel);
                        contact.put("senId",senId);
                        contact.put("shortName",shortName);
                        contact.put("mId",mId);

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
                    R.layout.chat_bubble, new String[]{"message","time", "sensLevel","senId","shortName","mId"},
                    new int[]{R.id.messageChatListView,R.id.timeChatListView,
                            R.id.sensLevelChatListView,R.id.senIdChatListView,R.id.shortNameChatListView,R.id.mId});

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

    //Fingerprint METHODS WILL BE HERE
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected  void generateKey()   {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        }
        catch (Exception e) {
            Log.d("Sporites",e.toString());
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        }
        catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            Log.d("Sporties",e.toString());
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}
