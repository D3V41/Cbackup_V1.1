package com.example.cbackupv11.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.cbackupv11.R;
import com.example.cbackupv11.adapter.ContactAdapter;
import com.example.cbackupv11.database.ContactsDatabase;
import com.example.cbackupv11.models.Contact;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private ListView contactListView;
    private List<Contact> contacts;
    private ContactsDatabase dbInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);

        AdView mAdView = root.findViewById(R.id.contacts_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        contactListView = root.findViewById(R.id.contactlist);
        contacts = new ArrayList<Contact>();
        dbInstance = ContactsDatabase.Instance(getContext());

        showContactsList();

        return root;
    }

    public void showContactsList(){
        contacts = dbInstance.getContactList();
        ContactAdapter contactAdapter = new ContactAdapter(getContext(),contacts);
        contactListView.setAdapter(contactAdapter);
    }
}