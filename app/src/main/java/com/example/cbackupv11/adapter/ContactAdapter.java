package com.example.cbackupv11.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.cbackupv11.R;
import com.example.cbackupv11.database.ContactsDatabase;
import com.example.cbackupv11.models.Contact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class ContactAdapter extends BaseAdapter {
    Context context;
    List<Contact> contactList;
    LayoutInflater inflter;
    private ContactsDatabase dbInstance;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
        dbInstance = ContactsDatabase.Instance(context);
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.contacts_listview, null);

        TextView filename = view.findViewById(R.id.textFileName);
        TextView date = view.findViewById(R.id.textDate);
        ImageView delete = view.findViewById(R.id.imgDelete);
        ImageView share = view.findViewById(R.id.imgShare);

        filename.setText(contactList.get(i).getFileName());
        date.setText(contactList.get(i).getDate());

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbInstance.deleteContact(filename.getText().toString());
                contactList.remove(i);
                notifyDataSetChanged();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCache();
                shareFile(i);
            }
        });

        return view;
    }

    public void clearCache(){
        try{
            File cache = context.getExternalCacheDir();
            String[] children = cache.list();
            for (String child: children) {
                File fchild = new File(cache,child);
                fchild.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shareFile(int i){
        File tempFile = null;
        try {
            tempFile = File.createTempFile("Contacts-", ".vcf",context.getExternalCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(contactList.get(i).getFileData().getBytes());
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(context, "com.example.cbackupv11.fileprovider", tempFile));
        sendIntent.setType("text/x-vcard");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }
}
