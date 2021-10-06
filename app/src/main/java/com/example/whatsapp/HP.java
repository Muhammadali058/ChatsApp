package com.example.whatsapp;

import static android.content.Context.TELEPHONY_SERVICE;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HP {

    public static Spannable removeUnderline(String link){
        Spannable s = (Spannable) Html.fromHtml(link);
        for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
            s.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0);
        }

        return  s;
    }

    public static String getCountryISO(Context context){
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (!telephonyManager.getNetworkCountryIso().toString().equals("")) {
                iso = telephonyManager.getNetworkCountryIso().toString();
            }
        }

//        return CountryToPhonePrefix.getPhone(iso);
        return "+92";
    }

    public static String getFormatedTime(long time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        return simpleDateFormat.format(new Date(time));
    }

    public static String[] getDividedMessage(String message){
        String[] dividedMessage = new String[2];
        int length = 25;
        if(message.length() > length){
            dividedMessage[0] = message.substring(0, message.length() - length);
            dividedMessage[1] = message.substring(message.length() - length, message.length());
        }else {
            dividedMessage[1] = message;
        }

        return  dividedMessage;
    }

    public static String getVideoPathFromURI(Context context, Uri uri) {

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String result;

        // for API 19 and above
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {


            cursor.moveToFirst();
            String image_id = cursor.getString(0);
            image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);

        }

        cursor.moveToFirst();
        result = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();

        return result;
    }

    public static String getImagePathFromURI(Context context, Uri uri) {

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String result;

        // for API 19 and above
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {


            cursor.moveToFirst();
            String image_id = cursor.getString(0);
            image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);

        }

        cursor.moveToFirst();
        result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return result;
    }
}
