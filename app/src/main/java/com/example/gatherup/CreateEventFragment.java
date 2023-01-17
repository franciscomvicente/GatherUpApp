package com.example.gatherup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment implements MainActivity.LocationCallback {

    ImageView inputCreateEvent_EventPhoto;
    BottomNavigationView bottomNavigationView;
    Button btnCreateEvent;
    EditText inputCreateEvent_Title, inputCreateEvent_Date, inputCreateEvent_Local, inputCreateEvent_Description, inputCreateEvent_Hours;
    NumberPicker inputCreateEvent_MaxCapacity, inputCreateEvent_Duration;
    Switch inputCreateEvent_PrivateEvent;
    String address;
    Spinner inputCreateEvent_Theme;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;
    String eventID;

    private Location location;

    Double longitude;
    Double latitude;

    StorageReference storageReference;
    private Uri imageUri;
    private String[] pickerVals;
    private String[] spinnerVals;
    private ArrayAdapter<String> spinnerArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        inputCreateEvent_EventPhoto = view.findViewById(R.id.inputCreateEvent_EventPhoto);
        inputCreateEvent_Title = view.findViewById(R.id.inputCreateEvent_Title);
        inputCreateEvent_MaxCapacity = view.findViewById(R.id.inputCreateEvent_MaxCapacity);
        inputCreateEvent_Theme = view.findViewById(R.id.inputCreateEvent_Theme);
        inputCreateEvent_Date = view.findViewById(R.id.inputCreateEvent_Date);
        inputCreateEvent_Duration = view.findViewById(R.id.inputCreateEvent_Duration);
        inputCreateEvent_Local = view.findViewById(R.id.inputCreateEvent_Local);
        inputCreateEvent_Description = view.findViewById(R.id.inputCreateEvent_Description);
        inputCreateEvent_PrivateEvent = view.findViewById(R.id.inputCreateEvent_PrivateEvent);
        inputCreateEvent_Hours = view.findViewById(R.id.inputCreateEvent_Hours);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        pickerVals = new String[]{"0:05m", "0:10m", "0:15m", "0:30m", "1h:00", "1h:30", "2h:00", "3h:00", "5h:00", "7h:00", "12h:00", "24h:00"};
        inputCreateEvent_Duration.setMinValue(0);
        inputCreateEvent_Duration.setMaxValue(11);
        inputCreateEvent_Duration.setDisplayedValues(pickerVals);

        inputCreateEvent_MaxCapacity.setMinValue(1);
        inputCreateEvent_MaxCapacity.setMaxValue(100);

        spinnerVals = new String[]{"Festa", "Desporto", "Refeição", "Convívio", "Outros"};
        spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerVals);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputCreateEvent_Theme.setAdapter(spinnerArrayAdapter);
        inputCreateEvent_Theme.setPrompt("Select an Theme");

        btnCreateEvent.setOnClickListener(v -> {
            if (TextUtils.isEmpty(inputCreateEvent_Title.getText().toString())) {
                inputCreateEvent_Title.setError("Need to set a Title");
            } else if (inputCreateEvent_Theme.getSelectedItem().toString().equals("Select an Theme")) {
                Toast.makeText(getContext(), "Need to choose a Theme", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(inputCreateEvent_Date.getText().toString())) {
                inputCreateEvent_Date.setError("Need to choose a Date");
            } else if (TextUtils.isEmpty(inputCreateEvent_Local.getText().toString())) {
                inputCreateEvent_Local.setError("Need to choose a Local");
            } else if (TextUtils.isEmpty(inputCreateEvent_Description.getText().toString())) {
                inputCreateEvent_Description.setError("Need to set a Description");
            } else if (TextUtils.isEmpty(inputCreateEvent_Hours.getText().toString())) {
                inputCreateEvent_Hours.setError("Need to choose a Hour");
            } else {
                PerformCreation();
                Fragment mapsFragment = new MapsFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                bottomNavigationView.setSelectedItemId(R.id.map);
                ft.replace(R.id.MainFragment, mapsFragment).commit();
            }
        });

        inputCreateEvent_EventPhoto.setOnClickListener(v -> {
            Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            someActivityResultLauncher.launch(openGallery);
        });

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        inputCreateEvent_Date.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_MinWidth, (view12, year1, month1, dayOfMonth) -> {
                month1 = month1 + 1;
                String date = dayOfMonth + "/" + month1 + "/" + year1;
                inputCreateEvent_Date.setText(date);
            }, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minutes = calendar.get(Calendar.MINUTE);

        inputCreateEvent_Hours.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_MinWidth, (view1, hourOfDay, minute) -> {
                String hour1 = hourOfDay + ":" + minute;
                inputCreateEvent_Hours.setText(hour1);
            }, hour, minutes, true);
            timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            timePickerDialog.show();

        });

        inputCreateEvent_Local.setOnClickListener(v -> localPicker());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.setCallback(this);

        location = activity.getCurrentLocation();
        if (location == null) {
            location = new Location("");
            location.setLatitude(38.756435);
            location.setLongitude(-9.156538);
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();                             //ORIGEM
                imageUri = data.getData();
                Picasso.get().load(imageUri).into(inputCreateEvent_EventPhoto);
            }
        }
    });

    private void PerformCreation() {
        String title = inputCreateEvent_Title.getText().toString().trim();
        Integer max_capacity = inputCreateEvent_MaxCapacity.getValue();
        String theme = inputCreateEvent_Theme.getSelectedItem().toString();
        String date = inputCreateEvent_Date.getText().toString().trim();
        String duration = pickerVals[inputCreateEvent_Duration.getValue()];
        String description = inputCreateEvent_Description.getText().toString().trim();
        String hours = inputCreateEvent_Hours.getText().toString().trim();
        Integer subscribed = 1;

        //Timestamp
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Date parsedDate = null;
        try {
            parsedDate = format.parse(date + " " + hours);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp timestamp = new Timestamp(parsedDate.getTime());
        format.format(timestamp);


        //PRIVATE OR NOT
        boolean private_event;
        if (inputCreateEvent_PrivateEvent.isChecked()) {
            private_event = true;
        } else {
            private_event = false;
        }

        //GET EVENT KEY
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events");
        eventID = eventRef.push().getKey();

        //SAVE EVENT DOCUMENT
        DocumentReference documentEventReference = store.collection("Events").document(eventID);
        Map<String, Object> event = new HashMap<>();
        event.put("Title", title);
        event.put("MaxCapacity", max_capacity);
        event.put("Theme", theme);
        event.put("Duration", duration);
        event.put("Local", new GeoPoint(latitude, longitude));
        event.put("Address", address);
        event.put("Date", timestamp);
        event.put("Description", description);
        event.put("Private", private_event);
        event.put("CreatorID", userID);
        event.put("Subscribed", subscribed);
        documentEventReference.set(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + eventID + "EventCreated");
                DocumentReference userRef = store.collection("Users").document(userID);
                DocumentReference documentParticipantsReference = store.collection("Events").document(eventID).collection("Participants").document(userID);
                Map<String, Object> participant = new HashMap<>();
                participant.put("Participant", userRef);
                documentParticipantsReference.set(participant).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("TAG", "onSuccess:" + userID + "add as participant of event" + eventID);
                    }
                });
            }
        });

        //SAVE EVENT IN USER EVENT LIST
        DocumentReference ref = store.collection("Events").document(eventID);
        DocumentReference documentReference = store.collection("Users").document(userID).collection("Events").document(eventID);
        Map<String, Object> user = new HashMap<>();
        user.put("Referência", ref);       //VERIFICAR ---------------
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + eventID + "associated" + userID);
            }
        });

        if (!(imageUri == null)) {
            uploadImageToFirebase(imageUri);
        } else {
            uploadDeafultImage();
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference file = storageReference.child("Events/" + eventID + "/eventPhoto.jpg");
        file.putFile(imageUri);
    }

    private void uploadDeafultImage() {
        StorageReference file = storageReference.child("Events/" + eventID + "/eventPhoto.jpg");
        Uri image = Uri.parse("android.resource://com.example.gatherup/drawable/" + R.drawable.defaultimageevent);
        file.putFile(image);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void clearFields() {
        inputCreateEvent_EventPhoto.setImageResource(0);
        inputCreateEvent_Title.setText("");
        inputCreateEvent_Theme.setSelection(0);
        inputCreateEvent_Date.setText("");
        inputCreateEvent_Duration.setValue(0);
        inputCreateEvent_MaxCapacity.setValue(0);
        inputCreateEvent_Local.setText("");
        inputCreateEvent_Description.setText("");
        inputCreateEvent_PrivateEvent.setText("");
    }

    private void localPicker() {
        Intent intent = new PlacePicker.IntentBuilder()
                .setLatLong(location.getLatitude(), location.getLongitude())//(38.756435, -9.156538)
                .showLatLong(true)
                .setMapType(MapType.NORMAL)
                .hideMarkerShadow(true)
                .setFabColor(R.color.MiddleBlue)
                .setMarkerImageImageColor(R.color.MiddleBlue)
                .setBottomViewColor(R.color.SoftBlue)
                .setPrimaryTextColor(R.color.MiddleBlue) // Change text color of Shortened Address
                .setSecondaryTextColor(R.color.LightBlue) // Change text color of full Address
                .disableMarkerAnimation(true)
                .build(getActivity());
        pickeractivity.launch(intent);
    }


    ActivityResultLauncher<Intent> pickeractivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                AddressData addressData = result.getData().getParcelableExtra(Constants.ADDRESS_INTENT);
                address = addressData.component3().get(0).getAddressLine(0);
                longitude = addressData.getLongitude();
                latitude = addressData.getLatitude();
                inputCreateEvent_Local.setText(address);
            } catch (Exception e) {
                System.out.println("ERRO");
                Log.e("MainActivity", e.getMessage());
            }
        }

    });

    @Override
    public void onLocationChanged(Location currentLocation) {
        location = currentLocation;
        System.out.println("Mudei de sitio");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity activity = (MainActivity) getActivity();
        activity.setCallback(null);
    }
}