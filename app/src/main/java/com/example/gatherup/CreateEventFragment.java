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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    ImageView inputCreateEvent_EventPhoto;
    BottomNavigationView bottomNavigationView;
    Button btnCreateEvent;
    EditText inputCreateEvent_Title, inputCreateEvent_Theme, inputCreateEvent_Date, inputCreateEvent_Local, inputCreateEvent_Description, inputCreateEvent_Hours;
    NumberPicker inputCreateEvent_MaxCapacity, inputCreateEvent_Duration;
    Switch inputCreateEvent_PrivateEvent;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;
    String eventID;

    Double longitude;
    Double latitude;

    StorageReference storageReference;
    private Uri imageUri;
    private String[] pickerVals;

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

        btnCreateEvent.setOnClickListener(v -> {
            if (TextUtils.isEmpty(inputCreateEvent_Title.getText().toString())) {
                inputCreateEvent_Title.setError("Your message");
            } else if (TextUtils.isEmpty(inputCreateEvent_Theme.getText().toString())) {
                inputCreateEvent_Title.setError("Your message");
            } else if (TextUtils.isEmpty(inputCreateEvent_Date.getText().toString())) {
                inputCreateEvent_Title.setError("Your message");
            } else if (TextUtils.isEmpty(inputCreateEvent_Local.getText().toString())) {
                inputCreateEvent_Title.setError("Your message");
            } else if (TextUtils.isEmpty(inputCreateEvent_Description.getText().toString())) {
                inputCreateEvent_Title.setError("Your message");
            } else if (TextUtils.isEmpty(inputCreateEvent_Hours.getText().toString())) {
                inputCreateEvent_Title.setError("Your message");
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
        String theme = inputCreateEvent_Theme.getText().toString().trim();
        String date = inputCreateEvent_Date.getText().toString().trim();
        String duration = pickerVals[inputCreateEvent_Duration.getValue()];
        String description = inputCreateEvent_Description.getText().toString().trim();
        String hours = inputCreateEvent_Hours.getText().toString().trim();

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
        event.put("Date", date);
        event.put("Duration", duration);
        event.put("Local", new GeoPoint(latitude,longitude));
        event.put("Description", description);
        event.put("Private", private_event);
        event.put("Hours", hours);
        event.put("CreatorID", userID);
        documentEventReference.set(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + eventID + "EventCreated");
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
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference file = storageReference.child("Events/" + eventID + "/eventPhoto.jpg");
        file.putFile(imageUri);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void clearFields() {
        inputCreateEvent_EventPhoto.setImageResource(0);
        inputCreateEvent_Title.setText("");
        inputCreateEvent_Theme.setText("");
        inputCreateEvent_Date.setText("");
        inputCreateEvent_Duration.setValue(0);
        inputCreateEvent_MaxCapacity.setValue(0);
        inputCreateEvent_Local.setText("");
        inputCreateEvent_Description.setText("");
        inputCreateEvent_PrivateEvent.setText("");
        inputCreateEvent_Hours.setText("");
    }

    private void localPicker() {
        Intent intent = new PlacePicker.IntentBuilder()
                .setLatLong(40.748672, -73.985628)
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
        System.out.println("2");
        pickeractivity.launch(intent);
    }


    ActivityResultLauncher<Intent> pickeractivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                AddressData addressData = result.getData().getParcelableExtra(Constants.ADDRESS_INTENT);
                System.out.println(addressData);
                longitude = addressData.getLongitude();
                latitude = addressData.getLatitude();
                inputCreateEvent_Local.setText(addressData.toString());
            } catch (Exception e) {
                System.out.println("ERRO");
                Log.e("MainActivity", e.getMessage());
            }
        }

    });
}