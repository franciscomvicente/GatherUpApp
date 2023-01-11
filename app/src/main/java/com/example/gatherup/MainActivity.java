package com.example.gatherup;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

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


}