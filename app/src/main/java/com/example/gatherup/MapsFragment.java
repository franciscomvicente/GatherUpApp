package com.example.gatherup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements LocationListener {
    private GoogleMap mMap;
    LocationManager locationmanager;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    public double latitude;
    public double longitude;
    Marker markername;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    };

    @SuppressLint("MissingPermission") //NAO VERIFICA PERMISSOES, TIRAR DEPOIS
    public void requestlocation(){
        checkLocationPermission();
        locationmanager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 100, this);
    }

    public void checkLocationPermission() { //TESTE verificar o que faz
        int hasWriteStoragePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWriteStoragePermission = getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
                return;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        markername.remove();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        displayLocation(location);
    }

    @SuppressLint("MissingPermission")
    public void displayLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng loc = new LatLng(latitude, longitude);
        markername = mMap.addMarker(new MarkerOptions().position(loc).title("This is Me"));
    }

}