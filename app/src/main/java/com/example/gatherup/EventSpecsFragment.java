package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class EventSpecsFragment extends Fragment {

    Button criarEventoButton;
    ImageView chatGrupoButton;

    public EventSpecsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_specs, container, false);

        criarEventoButton = view.findViewById(R.id.ESJoinEvent);
        chatGrupoButton= view.findViewById(R.id.ESGroupChat);

        criarEventoButton.setOnClickListener(view1 -> {
            Fragment mapFragment = new MapsFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, mapFragment).commit();
        });

        chatGrupoButton.setOnClickListener(view1 -> {
            Fragment chatFragment = new ChatFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, chatFragment).commit();
        });

        return view;
    }
}