package com.example.gatherup;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.Utils.FindFriendsModel;
import com.example.gatherup.Utils.FriendsAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ParticipantsFragment extends Fragment implements FriendsAdapter.OnListItemClicked {

    String eventID;

    Toolbar toolbar;
    ImageView outputChat_EventPhoto;
    TextView outputChat_Title;
    EditText inputMessage;
    ImageButton btnSendMessage;
    RecyclerView eventParticipants;
    private FriendsAdapter adapter;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    StorageReference storageReference;

    private ArrayList<String> participantsIDs;
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

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        loadGroupInfo();

        participantsIDs = new ArrayList<>();
        //RecyclerView eventosQueParticipa
        eventParticipants = view.findViewById(R.id.eventParticipants);
        System.out.println("Antes");

        Query queryAux = store.collection("Events").document(eventID).collection("Participants");

        queryAux.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        System.out.println("document.getData(): " + document.getData());
                        String eventReference = document.getId();
                        if(participantsIDs.size() < 10) {
                            participantsIDs.add(eventReference);
                            System.out.println(eventReference);
                        }
                    }
                    System.out.println(participantsIDs);
                    getIds();
                } else {
                    Log.d("TAG", "Error getting event references: ", task.getException());
                }

            }
        });

        return view;
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
        eventReference.addSnapshotListener((value, error) -> outputChat_Title.setText(value.getString("Title") + ": Participantes"));
    }

    private void getIds(){
        if(!participantsIDs.isEmpty()){
            Query query = store.collection("Users").whereIn(FieldPath.documentId(), participantsIDs);

            PagingConfig config = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

            //PAGING OPTIONS
            FirestorePagingOptions<FindFriendsModel> options = new FirestorePagingOptions.Builder<FindFriendsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<FindFriendsModel>() {
                @NonNull
                @Override
                public FindFriendsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                    FindFriendsModel eventsModel = snapshot.toObject(FindFriendsModel.class);
                    String participantID = snapshot.getId();
                    eventsModel.setUserID(participantID);
                    return eventsModel;
                }
            }).build();

            adapter = new FriendsAdapter(options, this);
            eventParticipants.setHasFixedSize(true);
            eventParticipants.setLayoutManager(new LinearLayoutManager(getContext()));
            eventParticipants.setAdapter(adapter);
        }
    }


    @Override
    public void onItemClick(DocumentSnapshot snapshot, int position) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle p = new Bundle();
        p.putString("User_key", snapshot.getId());

        profileFragment.setArguments(p);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MainFragment, profileFragment).addToBackStack("try").commit();
    }
}