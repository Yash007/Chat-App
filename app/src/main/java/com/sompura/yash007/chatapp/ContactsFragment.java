package com.sompura.yash007.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ContactsFragment extends Fragment {

    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList;
    private static final String TAG = "SearchContactsActivity";

    FloatingActionButton addContacts;
    Activity context;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        addContacts = (FloatingActionButton) context.findViewById(R.id.addContactsFloatingButton);

        addContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, SearchContactsActivity.class));
            }
        });

        contactList = new ArrayList<>();
        lv = (ListView) context.findViewById(R.id.contactList);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Config.prefName,Context.MODE_PRIVATE);
        final String uId = sharedPreferences.getString("uId","");
        new ListContacts(uId).execute();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView receiverId = (TextView) view.findViewById(R.id.contactIdListView);
                TextView receiverName = (TextView) view.findViewById(R.id.nameListView);
                TextView receiverShortName = (TextView) view.findViewById(R.id.shortNameListView);

                String rId, name, shortName;
                rId = receiverId.getText().toString();
                name = receiverName.getText().toString();
                shortName = receiverShortName.getText().toString();

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("sId",uId);
                intent.putExtra("rId",rId);
                intent.putExtra("name",name);
                intent.putExtra("shortName",shortName);

                context.startActivity(intent);
            }
        });
    }

    private class ListContacts extends AsyncTask<Void, Void, Void> {

        String uId;
        public ListContacts(String uId) {
            this.uId = uId;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(Config.listContacts+"&sourceId="+uId);


            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("contacts");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString("destinationId");
                        String name = c.getString("cName");
                        String email = c.getString("cEmail");
                        String shortName = c.getString("cShortName");
                        String phone = c.getString("cMobile");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("name",name);
                        contact.put("email",email);
                        contact.put("shortName",shortName);
                        contact.put("phone",phone);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    context, contactList,
                    R.layout.contacts_listview, new String[]{"id","name", "email","phone","shortName"},
                    new int[]{R.id.contactIdListView,R.id.nameListView,
                            R.id.emailListView,R.id.phoneListView,R.id.shortNameListView});

            lv.setAdapter(adapter);
        }

    }
}
