package ru.slatinin.backgroundlocationtracker;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.ParseException;

import static android.content.Context.MODE_PRIVATE;
import static ru.slatinin.backgroundlocationtracker.LocationService.LOCATION_SLA;
import static ru.slatinin.backgroundlocationtracker.LocationService.SHARED_PREFS;
import static ru.slatinin.backgroundlocationtracker.LocationService.formatMillisToHours;

public class MyWorker extends Worker implements LocationListener {

    public static final String TAG = "MyWorker";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private LocationManager locationManager;

    private Context mContext;

    /**
     * Callback for changes in location.
     */

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Done");

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 180000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 5, this);
        }

        return Result.success();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
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
}
