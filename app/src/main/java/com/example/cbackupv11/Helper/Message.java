package com.example.cbackupv11.Helper;

import android.content.Context;
import android.widget.Toast;

public class Message {
    public static void showMessage(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
}
