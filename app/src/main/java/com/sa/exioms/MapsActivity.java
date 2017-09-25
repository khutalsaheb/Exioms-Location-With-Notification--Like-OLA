package com.sa.exioms;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,

        View.OnClickListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleMap mMap;
    Button btn_BookRide, btn_Refresh;
    Intent intent;
    private double longitude;
    private double latitude;
    private double fromLongitude;
    private double fromLatitude;
    private GoogleApiClient googleApiClient;
    private EditText buttonSetTo, buttonSetFrom;
    private TextView current, destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        setContentView(R.layout.activity_maps);
        buildGoogleApiClient();
        buttonSetFrom = (EditText) findViewById(R.id.et_currentaddress);
        buttonSetTo = (EditText) findViewById(R.id.et_destination);
        btn_BookRide = (Button) findViewById(R.id.bookride);
        btn_BookRide.setOnClickListener(this);
        btn_Refresh = (Button) findViewById(R.id.refresh);
        btn_Refresh.setOnClickListener(this);
        buttonSetTo.setOnClickListener(this);
        current = (TextView) findViewById(R.id.current);
        destination = (TextView) findViewById(R.id.destination);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API)
                .build();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    //Getting current location
    private void getCurrentLocation() {
        checkLocationPermission();

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder();
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    buttonSetFrom.setText(strReturnedAddress.toString());

                } else {
                    buttonSetFrom.setText("No Address returned!");
                }
            } catch (IOException e) {
                buttonSetFrom.setText("Cannot get Address!");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // recreate();

    }

    @Override
    public void onConnected(Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // Do your stuff here...
                if (resultCode == Activity.RESULT_OK) {

                    Place place = PlaceAutocomplete.getPlace(MapsActivity.this, data);
                    Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());
                    buttonSetTo.setText(place.getAddress());

                    LatLng queriedLocation = place.getLatLng();
                    Log.d("Latitude is", "" + queriedLocation.latitude);
                    Log.d("Longitude is", "" + queriedLocation.longitude);
                    fromLatitude = queriedLocation.latitude;
                    fromLongitude = queriedLocation.longitude;

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e("Tag", status.getStatusMessage());

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "You have cancel Search", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onClick(View v) {

        if (v == btn_BookRide) {

            if (buttonSetTo.getText().toString().length() == 0) {
                Toast.makeText(MapsActivity.this, "PLEASE ENTER DESTINATION POINT", Toast.LENGTH_LONG).show();

            } else if (buttonSetFrom.getText().toString().length() == 0) {
                Toast.makeText(MapsActivity.this, "PLEASE ENTER CURRENT POINT", Toast.LENGTH_LONG).show();

            } else {
                buttonSetFrom.setVisibility(View.GONE);
                buttonSetTo.setVisibility(View.GONE);
                btn_Refresh.setVisibility(View.GONE);
                current.setVisibility(View.GONE);
                destination.setVisibility(View.GONE);
                Log.d("Lat", String.valueOf(fromLatitude));
                Log.d("Lng", String.valueOf(fromLongitude));
                intent = new Intent(getBaseContext(), FindLocation.class);
                intent.putExtra("Lat", fromLatitude);
                intent.putExtra("Lng", fromLongitude);

                startService(intent);
            }
        }
        if (v == btn_Refresh) {
            recreate();
        }
        if (v == buttonSetTo) {
            try {

                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().
                        setTypeFilter(Place.TYPE_COUNTRY).setCountry("IN").build();
                Intent intent =
                        new PlaceAutocomplete
                                .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .setFilter(typeFilter)
                                .build(this);
                startActivityForResult(intent, 1);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException ignored) {

            }
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {

            return true;
        }
    }


}