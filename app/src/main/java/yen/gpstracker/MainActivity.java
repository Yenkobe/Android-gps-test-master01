package yen.gpstracker;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//---------------------//


public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private Button Maplocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startService(new Intent(this, GpsTrackerService.class) ); //Start Service;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Maplocation=(Button)findViewById(R.id.Maplocation);

        Maplocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent inten=new Intent(MainActivity.this,MainActivity.class);
                 startActivity(inten);
            }
        });
        FirebaseDatabase.getInstance().setPersistenceEnabled(true); // persistent - from offline
    }

    //----------- request permission for sdk >=21 --------------//
    public boolean requestApplicationPermissions() {
        final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
        int permissionAccessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestApplicationPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

                String time = String.valueOf(location.getTime());

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); //Get Reference database
                Map<String, String> coordinates = new HashMap<>();
                coordinates.put("lat", String.valueOf(location.getLatitude()) );
                coordinates.put("lon", String.valueOf(location.getLatitude()) );
                coordinates.put("time", time );
                databaseReference.child("bus_data").child(time).setValue(coordinates);
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
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                //status in GPS
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                //status in Net (wifi or cell satelites)
            }
        }
    };

    //Open setting activity
    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

}
