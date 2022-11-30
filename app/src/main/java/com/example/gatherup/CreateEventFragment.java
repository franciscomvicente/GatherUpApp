package com.example.gatherup;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class CreateEventFragment extends Fragment {

    private Button createEventButton;
    private DatePickerDialog datePickerDialog;
    private Button btnDate;
    private String date;


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

        initDatePicker();
        btnDate = view.findViewById(R.id.CEButtonDate);
        btnDate.setText(getTodaysDate());

        btnDate.setOnClickListener(view1 -> {
            datePickerDialog.show();
        });


        return view;
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int year = cal.get(Calendar.YEAR);

        return makeDateString(day, month, year);
    }

    private void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int day, int month, int year) {
                month = month + 1;
                date = makeDateString(day, month, year);
                btnDate.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getActivity(), style, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year){
        return day + " " + getMonthFormat(month) + " " + year;
    }

    private String getMonthFormat(int s)
    {
        switch(s){
            case 1:
                return "JAN";

            case 2:
                return "FEB";

            case 3:
                return "MAR";

            case 4:
                return "APR";

            case 5:
                return "MAY";

            case 6:
                return "JUN";

            case 7:
                return "JUL";

            case 8:
                return "AUG";

            case 9:
                return "SEP";

            case 10:
                return "OCT";

            case 11:
                return "NOV";

            case 12:
                return "DEC";
        }
        return "JAN";
    }



    public void CreateEvent(View view) {
    }
}