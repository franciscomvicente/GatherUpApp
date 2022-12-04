package com.example.gatherup;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class EventSpecsFragment extends Fragment {

    Button btnJoinEvent;
    ImageView btnGroupChat, outputEventsSpecs_EventPhoto, outputEventsSpecs_UserPhoto;
    TextView outputEventSpecs_Title, outputEventSpecs_Local, outputEventSpecs_Username, outputEventSpecs_Theme,
            outputEventSpecs_Description, outputEventSpecs_Capacity, outputEventSpecs_Duration, outputEventSpecs_Date;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;
    String creatorID;

    StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_specs, container, false);

        //GET EVENT_ID
        String eventID = getArguments().getString("key");
        Log.d("TAG","TESTEEEE"+ eventID);

        btnJoinEvent = view.findViewById(R.id.btnJoinEvent);
        btnGroupChat= view.findViewById(R.id.btnGroupChat);
        outputEventsSpecs_EventPhoto = view.findViewById(R.id.outputEventSpecs_EventPhoto);
        outputEventsSpecs_UserPhoto = view.findViewById(R.id.outputEventSpecs_UserPhoto);
        outputEventSpecs_Title = view.findViewById(R.id.outputEventSpecs_Title);
        outputEventSpecs_Local = view.findViewById(R.id.outputEventSpecs_Local);
        outputEventSpecs_Username = view.findViewById(R.id.outputEventSpecs_Username);
        outputEventSpecs_Theme = view.findViewById(R.id.outputEventSpecs_Theme);
        outputEventSpecs_Description = view.findViewById(R.id.outputEventSpecs_Description);
        outputEventSpecs_Capacity = view.findViewById(R.id.outputEventSpecs_Capacity);
        outputEventSpecs_Duration = view.findViewById(R.id.outputEventSpecs_Duration);
        outputEventSpecs_Date = view.findViewById(R.id.outputEventSpecs_Date);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference eventPhoto = storageReference.child("Events/"+ eventID + "/eventPhoto.jpg");
        eventPhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(outputEventsSpecs_EventPhoto);
            }
        });

        DocumentReference eventReference = store.collection("Events").document(eventID);
        eventReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {                                  //this
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                outputEventSpecs_Title.setText(value.getString("Title"));
                outputEventSpecs_Local.setText(value.getString("Local"));
                //outputEventSpecs_Username.setText(value.getString("Username")); REVER
                outputEventSpecs_Theme.setText(value.getString("Theme"));
                outputEventSpecs_Description.setText(value.getString("Description"));
                outputEventSpecs_Capacity.setText(String.valueOf(Objects.requireNonNull(value.getLong("MaxCapacity")).intValue()));
                outputEventSpecs_Duration.setText(value.getString("Duration"));
                outputEventSpecs_Date.setText(value.getString("Date"));

                creatorID = value.getString("CreatorID");
                DocumentReference creatorReference = store.collection("Users").document(creatorID);
                creatorReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot valueCreator, @Nullable FirebaseFirestoreException error) {
                        outputEventSpecs_Username.setText(valueCreator.getString("Username"));
                    }
                });
                StorageReference creatorPhoto = storageReference.child("Users/"+ creatorID + "/profile.jpg");
                creatorPhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(outputEventsSpecs_UserPhoto);
                    }
                });
            }
        });

        btnJoinEvent.setOnClickListener(view1 -> {

        });

        btnGroupChat.setOnClickListener(view1 -> {
            Fragment chatFragment = new ChatFragment();
            Bundle b = new Bundle();
            b.putString("EventID", eventID);

            chatFragment.setArguments(b);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, chatFragment).addToBackStack(null).commit(); // addToBackStack(null) back to last fragment with bugs
        });

        return view;
    }
}