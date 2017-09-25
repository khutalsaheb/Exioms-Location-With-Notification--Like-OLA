package com.sa.exioms;

/**
 * @author Created by Dell on 22-Aug-17.
 */


import android.*;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import static com.sa.exioms.MapsActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class FindLocation extends Service implements LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 25;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 2;
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    int flag = 0;
    Location location;
    Notification notification;
    Location currentlocation = new Location("");
    Location dest_location = new Location("");
    float distance;
    NotificationManager notifier;
    double latitude;
    double longitude;
    private Context Context;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i("Tag", "on create");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.i("Indestroy", "destroyed");
        flag = 0;
        stopSelf();
        stopUsingGPS();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        // TODO Auto-generated method stub
        Context = this;
        Log.i("tag", "on start");
        currentlocation = getLocation(Context);

        Double msg = currentlocation.getLatitude();
        Log.i("my long", msg.toString());

        Double dest_lat = intent.getDoubleExtra("Lat", 0.0);
        Double dest_lon = intent.getDoubleExtra("Lng", 0.0);
        Log.i("get lat", dest_lat.toString());
        Log.i("get lon", dest_lon.toString());

        this.dest_location.setLatitude(dest_lat);
        this.dest_location.setLongitude(dest_lon);
        Log.i("get lon", dest_lon.toString());

        return START_NOT_STICKY;
    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) Context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions((Activity) Context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions((Activity)Context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {

            return true;
        }
    }
    public Location getLocation(Context Context) {

        try {
            locationManager = (LocationManager) Context
                    .getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.i("No gps and No Network ",
                        "No gps and No Network is enabled enable either one of them");
                Toast.makeText(this, "Enable either Network or GPS",
                        Toast.LENGTH_LONG).show();
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
checkLocationPermission();
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(FindLocation.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {

        currentlocation = getLocation(Context);
        Log.i("Tag", "location changed");
        distance = currentlocation.distanceTo(dest_location);
        Log.i("Tag", "" + distance);
        if (flag == 0) {
            if ((distance / 1000) < 1) {
                Log.i("Distance", "dist. b/w < 1km");
                Log.d("location", "" + distance);

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(  R.mipmap.ic_launcher)
                                .setContentTitle( "You are within 1 km from ur destination!!")
                                .setContentText("You are "
                                        + distance + " meters "
                                        + " away from your point of interest ");


                Intent notificationIntent = new Intent(this, MapsActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                // Add as notification
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(11, builder.build());

                Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                vi.vibrate(1000);
                flag = 1;

                onDestroy();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}