package com.example.gatherup;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class EventSpecsFragment extends Fragment {

    Button btnEvent;
    ImageView btnGroupChat, outputEventsSpecs_EventPhoto, outputEventsSpecs_UserPhoto;
    TextView outputEventSpecs_Title, outputEventSpecs_Local, outputEventSpecs_Username, outputEventSpecs_Theme,
            outputEventSpecs_Description, outputEventSpecs_Capacity, outputEventSpecs_Duration, outputEventSpecs_Date;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;
    String creatorID;

    private long subscribed;
    private long maxcapacity;

    StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_specs, container, false);

        //GET EVENT_ID
        String eventID = getArguments().getString("key");

        btnEvent = view.findViewById(R.id.btnEvent);
        btnGroupChat = view.findViewById(R.id.btnGroupChat);
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

        //EVENT PHOTO
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference eventPhoto = storageReference.child("Events/" + eventID + "/eventPhoto.jpg");
        eventPhoto.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(outputEventsSpecs_EventPhoto));

        DocumentReference eventReference = store.collection("Events").document(eventID);
        eventReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {                                  //this
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                try {
                    String date = toDate(value);
                    maxcapacity = value.getLong("MaxCapacity");
                    subscribed = value.getLong("Subscribed");
                    String capacity = subscribed + "/" + maxcapacity;

                    outputEventSpecs_Title.setText(value.getString("Title"));
                    outputEventSpecs_Local.setText(value.getString("Address"));
                    outputEventSpecs_Theme.setText(value.getString("Theme"));
                    outputEventSpecs_Description.setText(value.getString("Description"));
                    outputEventSpecs_Capacity.setText(capacity);
                    outputEventSpecs_Duration.setText(value.getString("Duration"));
                    outputEventSpecs_Date.setText(date);

                    creatorID = value.getString("CreatorID");
                    DocumentReference creatorReference = store.collection("Users").document(creatorID);
                    creatorReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot valueCreator, @Nullable FirebaseFirestoreException error) {
                            outputEventSpecs_Username.setText(valueCreator.getString("Username"));
                        }
                    });
                    StorageReference creatorPhoto = storageReference.child("Users/" + creatorID + "/profile.jpg");
                    creatorPhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(outputEventsSpecs_UserPhoto);
                        }
                    });
                }catch (Exception e){
                    System.out.println("ERROR");
                }
            }
        });

        CheckRegistered(eventID);

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

    private void JoinEvent(String eventID) {
        DocumentReference ref = store.collection("Events").document(eventID);
        DocumentReference documentReference = store.collection("Users").document(userID).collection("Events").document(eventID);
        Map<String, Object> user = new HashMap<>();
        user.put("Referência", ref);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + eventID + "associated" + userID);
            }
        });
        //add +1 to subscribed
        ref.update("Subscribed", FieldValue.increment(1));

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

        CheckRegistered(eventID);
    }

    private void LeaveEvent(String eventID) {
        DocumentReference documentReference = store.collection("Users").document(userID).collection("Events").document(eventID);
        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + eventID + "disassociated" + userID);
            }
        });

        store.collection("Events").document(eventID).collection("Participants").document(userID).delete();
        store.collection("Events").document(eventID).update("Subscribed", FieldValue.increment(-1));

        CheckRegistered(eventID);
    }

    private void EditEvent(String eventID) {
        EditEventFragment editEventFragment = new EditEventFragment();
        Bundle b = new Bundle();
        b.putString("key", eventID);
        editEventFragment.setArguments(b);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MainFragment, editEventFragment).commit();
    }

    private void CheckRegistered(String eventID) {
        DocumentReference docIdRef = store.collection("Users").document(userID).collection("Events").document(eventID);
        docIdRef.get().addOnCompleteListener(task -> {
            String TAG = "Teste";
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (creatorID.equals(userID)) {
                        btnEvent.setText("Edit Event");
                        btnEvent.setOnClickListener(v -> EditEvent(eventID));
                    } else {
                        btnEvent.setText("Leave Event");
                        btnEvent.setOnClickListener(v -> LeaveEvent(eventID));
                    }
                } else {
                    if (subscribed < maxcapacity) {
                        if (getSubscribed(eventID) < maxcapacity) {
                            btnEvent.setText("Join Event");
                            btnEvent.setOnClickListener(v -> JoinEvent(eventID));
                        } else {
                            Toast.makeText(getContext(), "Event Already Full", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btnEvent.setText("Event Full");
                    }
                }
            } else {
                Log.d(TAG, "Failed with: ", task.getException());
            }
        });
    }

    private String toDate(DocumentSnapshot value) {

        Timestamp timestamp = value.getTimestamp("Date");
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = sfd.format(timestamp.toDate());
        String date = (String) time;

        return date;

    }

    private long getSubscribed(String eventID) {
        store.collection("Events").document(eventID).addSnapshotListener((value, error) -> subscribed = value.getLong("Subscribed"));
        return subscribed;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}