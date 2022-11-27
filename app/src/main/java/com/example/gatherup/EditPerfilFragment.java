package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class EditPerfilFragment extends Fragment {

    Button saveProfileButton;

    public EditPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_perfil, container, false);
        saveProfileButton = view.findViewById(R.id.SaveProfile);

        saveProfileButton.setOnClickListener(view1 -> {
            Fragment myProfileFragment = new MyProfileFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, myProfileFragment).commit();
        });

        return view;
    }
}