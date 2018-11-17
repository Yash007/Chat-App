package com.sompura.yash007.chatapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChatActivity extends AppCompatActivity {

    String sId, rId, name, shortName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sId = getIntent().getExtras().getString("sId","");
        rId = getIntent().getExtras().getString("rId","");
        name = getIntent().getExtras().getString("name","");
        shortName = getIntent().getExtras().getString("shortName","");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);


    }
}
