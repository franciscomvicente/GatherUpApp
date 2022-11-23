package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CreateEventFragment extends Fragment {

    private Button createEventButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        createEventButton = view.findViewById(R.id.CECreateEvent);

        createEventButton.setOnClickListener(view1 -> {
            Fragment mapsFragment = new MapsFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, mapsFragment).commit();
        });

        return view;
    }

    public void CreateEvent(View view) {
    }
}