package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MyProfileFragment extends Fragment {

    private Button editProfileButton;


    public MyProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        editProfileButton = view.findViewById(R.id.EditProfileButton);

        editProfileButton.setOnClickListener(view1 -> {
            Fragment editPerfilFragment = new EditPerfilFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, editPerfilFragment).commit();
        });

        return view;
    }

}