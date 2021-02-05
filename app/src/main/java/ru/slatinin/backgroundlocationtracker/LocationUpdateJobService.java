package ru.slatinin.backgroundlocationtracker;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static ru.slatinin.backgroundlocationtracker.LocationService.LOCATION_SLA;
import static ru.slatinin.backgroundlocationtracker.LocationService.SHARED_PREFS;
import static ru.slatinin.backgroundlocationtracker.LocationService.formatMillisToHours;

public class LocationUpdateJobService extends JobService implements LocationListener {

    private LocationManager locationManager;

    @Override
    public boolean onStartJob(JobParameters params) {
        requestUpdates();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        locationManager.removeUpdates(this);
        return false;
    }

    private void requestUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
        locationCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 180000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 5, this);
        }
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
}
