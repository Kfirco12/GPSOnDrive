package com.example.cohenkfi.gpsondrive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private double latitude = 0;
    private double longitude = 0;
    private EditText x;
    private EditText y;
    private EditText phone;
    private TextView last;
    private Button insert;
    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private String phone_number = "";
    private Location mCurrentLocation;
    private Location mLastLocation = null;
    private Location friend_location;
    private SmsManager smsManager = SmsManager.getDefault();
    private boolean valid = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        ActivityCompat.requestPermissions(this
                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE},
                MY_PERMISSIONS_REQUEST_LOCATION);


        x = (EditText) findViewById(R.id.x);
        y = (EditText) findViewById(R.id.y);
        phone = (EditText) findViewById(R.id.phone_num);

        insert = (Button) findViewById(R.id.insert);
        last = (TextView) findViewById(R.id.last_location);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inform the user the button has been clicked

                try {
                    latitude = Double.parseDouble(y.getText().toString());
                    longitude = Double.parseDouble(x.getText().toString());
                    phone_number = phone.getText().toString();
                    last.setText("");
                    valid = true;
                } catch (Exception e) {
                    last.setText("You have a problem with one or more of the fields.");
                    x.setText("");
                    y.setText("");
                    phone.setText("");
                    valid = false;
                }
                if (valid) {
                    mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            //addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            friend_location = new Location("b");
                            friend_location.setLatitude(latitude);
                            friend_location.setLongitude(longitude);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000,
                                10, mLocationListener);

                    } else {
                        last.setText("Premission denied, app won't work");
                    }
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w("MainActivity", "Permissions was granteed");
                } else {
                    Log.e("MainActivity", "Permissions was denied");
                }
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,(com.google.android.gms.location.LocationListener)this);
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            Toast.makeText(this, "No permissions", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (mLastLocation == null)
                mLastLocation = location;
            mCurrentLocation = location;

            double distance = friend_location.distanceTo(mCurrentLocation);

            mLastLocation = location;
            last.setText("" + distance);

            smsManager.sendTextMessage(phone_number, null, "The distance between us is: " + distance, null, null);


        }


        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


}


