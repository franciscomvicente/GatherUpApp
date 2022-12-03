package com.example.gatherup;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ChatFragment extends Fragment {

    String eventID;

    Toolbar toolbar;
    ImageView outputChat_EventPhoto;
    TextView outputChat_Title;
    EditText inputMessage;
    ImageButton btnSendMessage;
    RecyclerView chatRv;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    StorageReference storageReference;

    String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        //GET EVENT_ID
        eventID = getArguments().getString("EventID");
        Log.d("TAG","TESTEEEE"+ eventID);

        toolbar = view.findViewById(R.id.toolbar);
        outputChat_EventPhoto = view.findViewById(R.id.outputChat_EventPhoto);
        outputChat_Title = view.findViewById(R.id.outputChat_Title);
        inputMessage = view.findViewById(R.id.inputMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        chatRv = view.findViewById(R.id.chatRv);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        loadGroupInfo();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessage.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(getContext(), "Can't send empty message", Toast.LENGTH_SHORT).show();
                }else {
                    sendMessage(message);
                }
            }
        });


        return view;
    }

    private void sendMessage(String message) {
        //time
        String timestamp = ""+System.currentTimeMillis();
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("Sender", ""+userID);
        hashMap.put("Message", ""+message);
        hashMap.put("Timestamp", ""+timestamp);

        DocumentReference documentReference = store.collection("Events").document(eventID).collection("Messages").document(timestamp);
        documentReference.set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //message sent
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();  //GET ACTIVITY CONTEXT
            }
        });
    }

    private void loadGroupInfo() {
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference eventPhoto = storageReference.child("Events/"+ eventID + "/eventPhoto.jpg");
        eventPhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(outputChat_EventPhoto);
            }
        });

        DocumentReference eventReference = store.collection("Events").document(eventID);
        eventReference.addSnapshotListener((value, error) -> outputChat_Title.setText(value.getString("Title")));
    }
}