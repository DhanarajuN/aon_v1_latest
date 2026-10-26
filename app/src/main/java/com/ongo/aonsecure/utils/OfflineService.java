package com.ongo.aonsecure.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;


import com.ongo.aonsecure.activity.LoginActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import pdf.app.library.utils.Constants;

public class OfflineService extends Service {
    public static final int notify = 5000;  //interval between two services(Here Service run every 5 seconds)
    int count = 0;  //number of times service is display
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Toast.makeText(this, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast
                    checkInternetConnection();

                }
            });

        }

    }

    private void checkInternetConnection() {
        ConnectivityManager mgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();

        if (netInfo != null) {
            if (netInfo.isConnected()) {
                availableNet();
            } else {
                netNotAvailable();
            }
        } else {
            netNotAvailable();
        }
    }

    private void netNotAvailable() {
        String dateStr = SharedPref.INSTANCE.read(Constants.PREF_ONLINE_TIME, "");
        if (dateStr != null && !dateStr.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            String currentDateStr = convertDateToStr(cal.getTime());
            Date previousDate = convertStrToDate(dateStr);
            Date currentDate = convertStrToDate(currentDateStr);
            long secs = (currentDate.getTime() - previousDate.getTime()) / 1000;
            long hours = secs / 3600;
            secs = secs % 3600;
            long mins = secs / 60;
            secs = secs % 60;

            if (mins >= Constants.OFF_TIME_IN_MINS) {
                SharedPref.INSTANCE.initialize(getApplicationContext());
                SharedPref.INSTANCE.write(Constants.PREF_LOGGED_IN_EMAIL, "");
                SharedPref.INSTANCE.write(Constants.PREF_ONLINE_TIME, "");

                Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);
                Toast.makeText(getApplicationContext(), "Please use internet", Toast.LENGTH_LONG).show();
            }


        }
    }

    private void availableNet() {
        Calendar cal = Calendar.getInstance();
        SharedPref.INSTANCE.initialize(getApplicationContext());
        SharedPref.INSTANCE.write(Constants.PREF_ONLINE_TIME, convertDateToStr(cal.getTime()));

    }

    private String convertDateToStr(Date date) {
        String convertedDate = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        convertedDate = simpleDateFormat.format(date);
        return convertedDate;
    }

    private Date convertStrToDate(String date) {
        Date convertedDate = null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            convertedDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertedDate;


    }
}