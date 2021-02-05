package ru.slatinin.backgroundlocationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationService extends Service implements LocationListener {

    public static final String SHARED_PREFS = "slatinin";
    public static final String LOCATION_SLA = "slatinin_location";
    public static final String IS_NOTIFIED_ABOUT_BATTERY_SAVE = "slatinin_xiaomi.battery.save";
    private Binder binder;
    private LocationManager locationManager;
    private String locations = "";

    public final static String NOTIFICATION_CHANEL_ID = "LocationServiceSlatinin";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, NOTIFICATION_CHANEL_ID)
                    .setContentTitle("Получаем местопложение")
                    .setContentText("Мы постоянно получаем ваше местоположение")
                    .setSmallIcon(R.drawable.ic_computer)
                    .setContentIntent(pendingIntent)
                    .setTicker("ticker")
                    .build();
        }
        startForeground(1411, notification);
        binder = new Binder();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
        locationCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria locationCriteria = new Criteria();
            locationCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
            locationCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
            String provider = locationManager.getBestProvider(locationCriteria, true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider, 20000, 0, this);
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String prefAddress = sharedPreferences.getString(LOCATION_SLA, "");
        prefAddress += formatMillisToHours(System.currentTimeMillis()) + ": " + location.getProvider() + location.getLatitude() + "\n";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOCATION_SLA, prefAddress);
        editor.apply();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    public class Binder extends android.os.Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public static String formatMillisToHours(long millis) {
        @SuppressLint("SimpleDateFormat")
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(millis);
        return formatter.format(date);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }
}
