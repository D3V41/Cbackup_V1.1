package com.example.cbackupv11.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cbackupv11.R;
import com.example.cbackupv11.database.ContactsDatabase;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupFragment extends Fragment implements View.OnClickListener {

    private Button backupBtn;
    private TextView totalContacttxt,contactNametxt;
    private ProgressBar progressBar;
    private StringBuilder FILE_DATA;
    private ContactsDatabase dbInstance;
    private InterstitialAd mInterstitialAd;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_backup, container, false);

        AdView mAdView = root.findViewById(R.id.backup_adView);
        backupBtn = root.findViewById(R.id.btnBackup);
        totalContacttxt = root.findViewById(R.id.textTotalContacts);
        contactNametxt = root.findViewById(R.id.textContactName);
        progressBar = root.findViewById(R.id.progressBar);
        FILE_DATA = new StringBuilder();

        dbInstance = ContactsDatabase.Instance(getContext());

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(getContext(),"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });

        backupBtn.setOnClickListener(this);

        return root;
    }


    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            getTotalContacts();
            new LongOperation().execute();
        } else {
            askPermission();
        }
    }


    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected String doInBackground(String... params) {
            getVCF();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            String filedata = FILE_DATA.toString();
            String filename = "Contacts-"+System.nanoTime()+".vcf";
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm E, dd-MM");
            String date = myDateObj.format(myFormatObj);
            dbInstance.insertData(filename,filedata,date);
            FILE_DATA = new StringBuilder();
            successMessage();
        }

    }

    public void getVCF()
    {
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
        phones.moveToFirst();
        for(int i =0;i<phones.getCount();i++)
        {
            @SuppressLint("Range")
            String lookupKey =  phones.getString(phones.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
            AssetFileDescriptor fd;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    contactNametxt.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(207,141,255)));
                    progressBar.setMax(phones.getCount());
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });
            try
            {
                fd = getActivity().getContentResolver().openAssetFileDescriptor(uri, "r");
                FileInputStream fis = fd.createInputStream();
                byte[] buf = readBytes(fis);
                fis.read(buf);
                String VCard = new String(buf);
                FILE_DATA.append(VCard);
                @SuppressLint("Range")
                final String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                final int pint = i;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactNametxt.setText(name);
                        progressBar.setProgress(pint);
                    }
                });
                phones.moveToNext();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactNametxt.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void getTotalContacts(){
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
        totalContacttxt.setText(new StringBuilder().append("Total Contacts: ").append(phones.getCount()).toString());
    }

    private void askPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please give Contacts read permission to generate backup\nPermissions>Contacts>Allow")
                .setTitle("Permission Access");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(),
                        null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void successMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Backup created successfully");
        builder.setCancelable(false);
        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateContactList();
                        getFullScreenAd();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void getFullScreenAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                @Override
                public void onAdDismissedFullScreenContent() {
                    viewPager = (ViewPager) getActivity().findViewById(
                            R.id.viewPager);
                    viewPager.setCurrentItem(1);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    viewPager = (ViewPager) getActivity().findViewById(
                            R.id.viewPager);
                    viewPager.setCurrentItem(1);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null;
                }
            });
        } else {
            viewPager = (ViewPager) getActivity().findViewById(
                    R.id.viewPager);
            viewPager.setCurrentItem(1);
        }
    }

    public void updateContactList(){
        ContactsFragment contactsFragment = (ContactsFragment)
                getFragmentManager().getFragments().get(1);
        contactsFragment.showContactsList();
    }
}