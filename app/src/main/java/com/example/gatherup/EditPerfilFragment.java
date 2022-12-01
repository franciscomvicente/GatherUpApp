package com.example.gatherup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;


public class EditPerfilFragment extends Fragment {

    Button saveProfileButton;
    ImageView inputProfilePhoto;
    TextView inputDatePicker;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;

    StorageReference storageReference;

    DatePickerDialog.OnDateSetListener setListener;

    public EditPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_perfil, container, false);

        saveProfileButton = view.findViewById(R.id.SaveProfile);
        inputProfilePhoto = view.findViewById(R.id.inputProfilePhoto);
        inputDatePicker = view.findViewById(R.id.inputDatePicker);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileReference = storageReference.child("Users/"+ userID + "/profile.jpg");
        profileReference.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(inputProfilePhoto));

        saveProfileButton.setOnClickListener(view1 -> {
            Fragment myProfileFragment = new MyProfileFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, myProfileFragment).commit();
        });

        inputProfilePhoto.setOnClickListener(v -> {
            Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            someActivityResultLauncher.launch(openGallery);
        });

        //DATE PICKER
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        /*
        inputDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(), android.R.style.Theme_Holo_Dialog_MinWidth, setListener, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        //LISTENER FOR DATE PICKER
        setListener = (view12, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date = day+"/"+ month1 +"/"+ year1;
            inputDatePicker.setText(date);
        };

         */

        inputDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_MinWidth,(view12, year1, month1, dayOfMonth) -> {
                month1 = month1 +1;
                String date = day+"/"+ month1 +"/"+ year1;
                inputDatePicker.setText(date);
            }, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        return view;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();                             //ORIGEM
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    });

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference file = storageReference.child("Users/"+ userID + "/profile.jpg");
        file.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(inputProfilePhoto);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show(); //VERIFICAR
            }
        });
    }
}