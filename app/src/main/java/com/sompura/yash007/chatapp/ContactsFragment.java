package com.sompura.yash007.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class ContactsFragment extends Fragment {

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
    }
}
