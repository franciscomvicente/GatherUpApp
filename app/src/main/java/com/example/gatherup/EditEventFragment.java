package com.example.gatherup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditEventFragment extends Fragment {

    Button btnConfirmEdit, btnDeleteEvent;
    ImageView btnGroupChat, inputEventsSpecs_EventPhoto;
    TextView inputEventSpecs_Title, inputEventSpecs_Local, inputEventSpecs_Description, inputEventSpecs_Date, inputEditEvent_Hours;
    NumberPicker inputEventSpecs_Capacity, inputEventSpecs_Duration;
    Switch inputEditEvent_PrivateEvent;
    Spinner inputEventSpecs_Theme;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;

    String pickerVals[];
    private String[] spinnerVals;
    private ArrayAdapter<String> spinnerArrayAdapter;

    StorageReference storageReference;

    String address;
    GeoPoint geoPoint;
    Double longitude;
    Double latitude;
    String eventID;

    BottomNavigationView bottomNavigationView;
    private Uri imageUri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);
        //GET EVENT_ID
        eventID = getArguments().getString("key");
        Log.d("TAG", "TESTEEEE" + eventID);


        associateLayoutElements(view);
        load();

        pickerVals = new String[]{"0:05m", "0:10m", "0:15m", "0:30m", "1h:00", "1h:30", "2h:00", "3h:00", "5h:00", "7h:00", "12h:00", "24h:00"};
        inputEventSpecs_Duration.setMinValue(0);
        inputEventSpecs_Duration.setMaxValue(11);
        inputEventSpecs_Duration.setDisplayedValues(pickerVals);

        inputEventSpecs_Capacity.setMinValue(1);
        inputEventSpecs_Capacity.setMaxValue(100);

        spinnerVals = new String[]{"Festa", "Desporto", "Refeição", "Convívio", "Outros"};
        spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerVals);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputEventSpecs_Theme.setAdapter(spinnerArrayAdapter);

        btnConfirmEdit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(inputEventSpecs_Title.getText().toString())) {
                inputEventSpecs_Title.setError("Need to set a Title");
            } else if (inputEventSpecs_Theme.getSelectedItem().toString().equals("Select an Theme")) {
                Toast.makeText(getContext(), "Need to choose a Theme", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(inputEventSpecs_Date.getText().toString())) {
                inputEventSpecs_Date.setError("Need to choose a Date");
            } else if (TextUtils.isEmpty(inputEventSpecs_Local.getText().toString())) {
                inputEventSpecs_Local.setError("Need to choose a Local");
            } else if (TextUtils.isEmpty(inputEventSpecs_Description.getText().toString())) {
                inputEventSpecs_Description.setError("Need to set a Description");
            } else if (TextUtils.isEmpty(inputEditEvent_Hours.getText().toString())) {
                inputEditEvent_Hours.setError("Need to choose a Hour");
            } else {
                PerformCreation();
                Fragment mapsFragment = new MapsFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                //bottomNavigationView.setSelectedItemId(R.id.map);
                ft.replace(R.id.MainFragment, mapsFragment).commit();
            }
        });

        inputEventsSpecs_EventPhoto.setOnClickListener(v -> {
            Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            someActivityResultLauncher.launch(openGallery);
        });

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        inputEventSpecs_Date.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_MinWidth, (view12, year1, month1, dayOfMonth) -> {
                month1 = month1 + 1;
                String date = dayOfMonth + "/" + month1 + "/" + year1;
                inputEventSpecs_Date.setText(date);
            }, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minutes = calendar.get(Calendar.MINUTE);

        inputEditEvent_Hours.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_MinWidth, (view1, hourOfDay, minute) -> {
                String hour1 = hourOfDay + ":" + minute;
                inputEditEvent_Hours.setText(hour1);
            }, hour, minutes, true);
            timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            timePickerDialog.show();

        });

        inputEventSpecs_Local.setOnClickListener(v -> localPicker());

        DeleteEvent();

        return view;
    }

    public void associateLayoutElements(View view) {
        btnConfirmEdit = view.findViewById(R.id.btnEditEvent);
        btnGroupChat = view.findViewById(R.id.btnGroupChat);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        inputEventsSpecs_EventPhoto = view.findViewById(R.id.inputEditEvent_EventPhoto);
        inputEventSpecs_Title = view.findViewById(R.id.inputEditEvent_Title);
        inputEventSpecs_Local = view.findViewById(R.id.inputEditEvent_Local);
        inputEventSpecs_Theme = view.findViewById(R.id.inputEditEvent_Theme);
        inputEventSpecs_Description = view.findViewById(R.id.inputEditEvent_Description);
        inputEventSpecs_Capacity = view.findViewById(R.id.inputEditEvent_MaxCapacity);
        inputEventSpecs_Duration = view.findViewById(R.id.inputEditEvent_Duration);
        inputEventSpecs_Date = view.findViewById(R.id.inputEditEvent_Date);
        inputEditEvent_Hours = view.findViewById(R.id.inputEditEvent_Hours);
        inputEditEvent_PrivateEvent = view.findViewById(R.id.inputEditEvent_PrivateEvent);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();
    }


    private void load() {
        //EVENT PHOTO
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference eventPhoto = storageReference.child("Events/" + eventID + "/eventPhoto.jpg");
        eventPhoto.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(inputEventsSpecs_EventPhoto));

        DocumentReference eventReference = store.collection("Events").document(eventID);
        eventReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {                                  //this
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                try {
                    String date = toDate(value)[0];
                    String hours = toDate(value)[1];

                    pickerVals = new String[]{"0:05m", "0:10m", "0:15m", "0:30m", "1h:00", "1h:30", "2h:00", "3h:00", "5h:00", "7h:00", "12h:00", "24h:00"};
                    inputEventSpecs_Duration.setMinValue(0);
                    inputEventSpecs_Duration.setMaxValue(11);
                    inputEventSpecs_Duration.setDisplayedValues(pickerVals);

                    String duration = value.getString("Duration");
                    int i = 0;
                    for (String val : pickerVals) {
                        if (val.equals(duration)) {
                            inputEventSpecs_Duration.setValue(i);
                        }
                        i++;
                    }
                    String selected = value.getString("Theme");
                    int j = 0;
                    for (String val : spinnerVals) {
                        if (val.equals(selected)) {
                            inputEventSpecs_Theme.setSelection(j);
                        }
                        j++;
                    }

                    inputEventSpecs_Title.setText(value.getString("Title"));
                    inputEventSpecs_Local.setText(value.getString("Address"));
                    inputEventSpecs_Description.setText(value.getString("Description"));
                    inputEventSpecs_Capacity.setValue((Integer) Objects.requireNonNull(value.getLong(("MaxCapacity"))).intValue());
                    inputEventSpecs_Date.setText(date);
                    inputEditEvent_Hours.setText(hours);

                    geoPoint = value.getGeoPoint("Local");
                    latitude = geoPoint.getLatitude();
                    longitude = geoPoint.getLongitude();
                    address = value.getString("Address");

                }catch (Exception e){
                    System.out.println(e);
                }
            }
        });
    }

    private String[] toDate(DocumentSnapshot value) {
        Timestamp timestamp = value.getTimestamp("Date");
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = sfd.format(timestamp.toDate());
        String[] splitStr = ((String) time).split("\\s+");

        return new String[]{splitStr[0], splitStr[1]};
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();                             //ORIGEM
                imageUri = data.getData();
                Picasso.get().load(imageUri).into(inputEventsSpecs_EventPhoto);
            }
        }
    });

    private void PerformCreation() {
        String title = inputEventSpecs_Title.getText().toString().trim();
        Integer max_capacity = inputEventSpecs_Capacity.getValue();
        String theme = inputEventSpecs_Theme.getSelectedItem().toString();
        String date = inputEventSpecs_Date.getText().toString().trim();
        String duration = pickerVals[inputEventSpecs_Duration.getValue()];
        String description = inputEventSpecs_Description.getText().toString().trim();
        String hours = inputEditEvent_Hours.getText().toString().trim();

        //Timestamp
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Date parsedDate = null;
        try {
            parsedDate = format.parse(date + " " + hours);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        format.format(timestamp);


        //PRIVATE OR NOT
        boolean private_event;
        if (inputEditEvent_PrivateEvent.isChecked()) {
            private_event = true;
        } else {
            private_event = false;
        }

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
        documentEventReference.update(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + eventID + "EventChanged");
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


    private void localPicker() {
        Intent intent = new PlacePicker.IntentBuilder()
                .setLatLong(38.756435, -9.156538)
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
                inputEventSpecs_Local.setText(address);
            } catch (Exception e) {
                System.out.println("ERRO");
                Log.e("MainActivity", e.getMessage());
            }
        }

    });

    private void DeleteEvent() {
        btnDeleteEvent.setOnClickListener((view -> {
            store.collection("Events").document(eventID).delete();

            MyProfileFragment myProfileFragment = new MyProfileFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, myProfileFragment).commit();
        }));
    }
}

