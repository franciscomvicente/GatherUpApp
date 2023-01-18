package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class FiltersFragment extends Fragment {
    private Spinner filterByTheme;
    private Button btnconfirmFilter, btnClearFilter;
    private String[] spinnerVals;
    private String theme;
    private ArrayAdapter<String> spinnerArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filters, container, false);

        filterByTheme = view.findViewById(R.id.filterByTheme);
        btnconfirmFilter = view.findViewById(R.id.btnConfirmFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);

        spinnerVals = new String[]{"Festa", "Desporto", "Refeição", "Convívio", "Outros"};
        spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerVals);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterByTheme.setAdapter(spinnerArrayAdapter);
        filterByTheme.setPrompt("Select an Theme");


        btnconfirmFilter.setOnClickListener(view1 -> {
            theme = filterByTheme.getSelectedItem().toString();
            if(theme != null){
                MapsFragment mapsFragment = new MapsFragment();
                Bundle b = new Bundle();
                b.putString("filter", theme);

                mapsFragment.setArguments(b);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.MainFragment, mapsFragment).commit();
            }
            else{
                MapsFragment mapsFragment = new MapsFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.MainFragment, mapsFragment).commit();
            }
        });

        btnClearFilter.setOnClickListener(view1 -> {
            theme = null;
            MapsFragment mapsFragment = new MapsFragment();
            Bundle b = new Bundle();
            b.putString("filter", theme);

            mapsFragment.setArguments(b);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, mapsFragment).commit();
        });

        return view;
    }
}