package com.example.gatherup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private Location mCurrentLocation;
    private LocationManager locationManager;
    private LocationCallback mCallback;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 100, this);
        mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    MapsFragment mapsFragment = new MapsFragment();
    CreateEventFragment createEventFragment = new CreateEventFragment();
    FriendsListFragment friendsListFragment = new FriendsListFragment();
    MyProfileFragment myProfileFragment = new MyProfileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map:
                getSupportFragmentManager().beginTransaction().replace(R.id.MainFragment, mapsFragment).commit();
                return true;
            case R.id.new_event:
                getSupportFragmentManager().beginTransaction().replace(R.id.MainFragment, createEventFragment).addToBackStack("new_event").commit();
                return true;
            case R.id.friends:
                getSupportFragmentManager().beginTransaction().replace(R.id.MainFragment, friendsListFragment).addToBackStack("friends").commit();
                return true;
            case R.id.perfil:
                getSupportFragmentManager().beginTransaction().replace(R.id.MainFragment, myProfileFragment).addToBackStack("perfil").commit();
                return true;
        }
        return false;
    }

    public interface LocationCallback {
        void onLocationChanged(Location location);
    }

    public void setCallback(LocationCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mCallback != null) {
            mCallback.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("Liguei O GPS");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 10, 100, this);
        mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("Desliguei O GPS");
    }
}