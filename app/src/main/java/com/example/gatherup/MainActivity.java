package com.example.gatherup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.map);
    }

    MapsFragment mapsFragment = new MapsFragment();
    EditPerfilFragment teste = new EditPerfilFragment();
    MyProfileFragment myprofile = new MyProfileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map:
                getSupportFragmentManager().beginTransaction().replace(R.id.Fragment, mapsFragment).commit();
                return true;
            case R.id.new_event:
                getSupportFragmentManager().beginTransaction().replace(R.id.Fragment, teste).commit();
                return true;
            case R.id.friends:
                getSupportFragmentManager().beginTransaction().replace(R.id.Fragment, myprofile).commit();
                return true;
            case R.id.perfil:
                //getSupportFragmentManager().beginTransaction().replace(R.id.Fragment, thirdFragment).commit();
                return true;
        }
        return false;
    }
}