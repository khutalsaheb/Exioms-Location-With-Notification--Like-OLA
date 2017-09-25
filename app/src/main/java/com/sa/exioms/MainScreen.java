package com.sa.exioms;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class MainScreen extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {// Splash screen timer
    private static final String TAG = MainScreen.class.getSimpleName();

    ImageView start;
    boolean isConnected = ConnectivityReceiver.isConnected();
    Dialog alertDialog;
    Intent intent;
    private boolean mIsBackButtonPressed;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        start = (ImageView) findViewById(R.id.screen);
        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                if (!mIsBackButtonPressed) {
                    if (!isConnected) {
                        checkConnection();
                        Toast.makeText(getApplicationContext(), "Sorry! Not connected to Internet.", Toast.LENGTH_LONG).show();
                        //      recreate();
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                    } else {
                        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            Toast.makeText(getApplicationContext(), "Sorry! Not connected to Location. ", Toast.LENGTH_LONG).show();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
                            builder.setTitle("Location Services Not Active");
                            builder.setMessage("Please enable Location Services and GPS");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Show location settings when the user acknowledges the alert dialog
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();


                        } else {

                            checkConnection();
                            intent = new Intent(MainScreen.this,
                                    MapsActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                } else {
                    checkConnection();
                    intent = new Intent(MainScreen.this,
                            MainScreen.class);
                    startActivity(intent);
                    finish();
                }


            }
        }, SPLASH_TIME_OUT);
    }


    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        if (isConnected) {
            Toast.makeText(getApplicationContext(), "connected to Internet.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Sorry! Not connected to Internet.", Toast.LENGTH_LONG).show();

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
        Log.d(TAG, "In the onRestart() event");
    }

    public void onStart() {
        super.onStart();
        Log.d(TAG, "In the onStart() event");
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "In the onPause() event");
    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "In the onStop() event");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In the onDestroy() event");
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }


    @Override
    public void onBackPressed() {
        mIsBackButtonPressed = true;
        super.onBackPressed();
        recreate();
    }
}