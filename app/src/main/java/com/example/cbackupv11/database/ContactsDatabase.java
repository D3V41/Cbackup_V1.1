package com.example.cbackupv11.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.cbackupv11.Helper.Message;
import com.example.cbackupv11.models.Contact;

import java.util.ArrayList;
import java.util.List;

public final class ContactsDatabase {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ContactsEntry.TABLE_NAME + " (" +
                    ContactsEntry._ID + " INTEGER PRIMARY KEY," +
                    ContactsEntry.FILE_NAME + " TEXT," +
                    ContactsEntry.FILE_DATA + " TEXT," +
                    ContactsEntry.DATE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ContactsEntry.TABLE_NAME;

    ContactsDatabaseHelper dbHelper;
    List<Contact> contactList;
    static ContactsDatabase contactsDatabase;

    private ContactsDatabase(Context context){
        dbHelper = new ContactsDatabaseHelper(context);
        contactList = new ArrayList<Contact>();
    }

    static public ContactsDatabase Instance(Context context){
        if(contactsDatabase == null){
            contactsDatabase = new ContactsDatabase(context);
        }
        return contactsDatabase;
    }

    public long insertData(String filename,String filedata, String date){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactsEntry.FILE_NAME, filename);
        values.put(ContactsEntry.FILE_DATA, filedata);
        values.put(ContactsEntry.DATE, date);
        return db.insert(ContactsEntry.TABLE_NAME, null, values);
    }

    public List<Contact> getContactList(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {BaseColumns._ID,ContactsEntry.FILE_NAME,ContactsEntry.FILE_DATA,ContactsEntry.DATE};
        Cursor cursor =db.query(ContactsEntry.TABLE_NAME,columns,null,null,
                null,null,BaseColumns._ID+" DESC");
        contactList.clear();
        while (cursor.moveToNext())
        {
            String filename = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.FILE_NAME));
            String  filedata = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.FILE_DATA));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.DATE));
            contactList.add(new Contact(filename,filedata,date));
        }
        cursor.close();
        return contactList;
    }

    public  int deleteContact(String filename)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] whereArgs ={filename};
        return db.delete(ContactsEntry.TABLE_NAME ,ContactsEntry.FILE_NAME+" = ?",whereArgs);
    }


    public static class ContactsEntry implements BaseColumns{
        public static final String TABLE_NAME = "Contacts";
        public static final String FILE_NAME = "Filename";
        public static final String FILE_DATA = "Filedata";
        public static final String DATE = "Date";
    }

    public class ContactsDatabaseHelper extends SQLiteOpenHelper{
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "CBackup.db";
        Context context;

        public ContactsDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}


