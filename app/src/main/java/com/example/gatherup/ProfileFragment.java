package com.example.gatherup;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.Utils.EventsModel;
import com.example.gatherup.Utils.FirestoreAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment implements FirestoreAdapter.OnListItemClicked{

    ImageView outputProfilePhoto;
    TextView outputUsername, outputBio;
    RecyclerView recyclerViewAllEvents;
    RecyclerView recylerViewHostEvents;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;

    StorageReference storageReference;

    private FirestoreAdapter adapter;
    private FirestoreAdapter adapter2;
    ArrayList<String> eventIDs;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        String profileID = getArguments().getString("User_key");
        Log.d("TAG","TESTEEEE"+ profileID);

        outputUsername = view.findViewById(R.id.outputSUsername);
        outputBio = view.findViewById(R.id.outputSBio);
        outputProfilePhoto = view.findViewById(R.id.outputSProfilePhoto);
        recyclerViewAllEvents = view.findViewById(R.id.recyclerViewAllEvents);
        recylerViewHostEvents = view.findViewById(R.id.recyclerViewHostEvents);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileReference = storageReference.child("Users/"+ profileID + "/profile.jpg");
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(outputProfilePhoto);
            }
        });

        DocumentReference documentReference = store.collection("Users").document(profileID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {                                  //this
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                outputUsername.setText(value.getString("Username"));
                outputBio.setText(value.getString("Bio"));
            }
        });

        eventIDs = new ArrayList<>();

        Query queryAux = store.collection("Users").document(profileID).collection("Events");

        queryAux.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        System.out.println("document.getData(): " + document.getData());
                        String eventReference = document.getId();
                        if(eventIDs.size() < 10) {
                            eventIDs.add(eventReference);
                            System.out.println(eventReference);
                        }
                    }
                    System.out.println(eventIDs);
                    getIds();
                } else {
                    Log.d("TAG", "Error getting event references: ", task.getException());
                }

            }
        });

        //QUERYHOST
        Query queryHost = store.collection("Events").whereEqualTo("CreatorID", profileID).whereEqualTo("Private", false);

        PagingConfig configHost = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

        //PAGING OPTIONS
        FirestorePagingOptions<EventsModel> options = new FirestorePagingOptions.Builder<EventsModel>().setLifecycleOwner(this).setQuery(queryHost, configHost, new SnapshotParser<EventsModel>() {
            @NonNull
            @Override
            public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                String eventID = snapshot.getId();
                eventsModel.setEventID(eventID);
                return eventsModel;
            }
        }).build();

        adapter2 = new FirestoreAdapter(options, this);

        recylerViewHostEvents.setHasFixedSize(true);
        recylerViewHostEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recylerViewHostEvents.setAdapter(adapter2);

        return view;
    }

    private void setRecyclerViewHostEvents(){

    }


    @Override
    public void onItemClick(DocumentSnapshot snapshot, int position) {
        Log.d("ItemCLIECK","ItemCLicked" + position + "AND THE ID:" + snapshot.getId());

        EventSpecsFragment eventSpecsFragment = new EventSpecsFragment();
        Bundle b = new Bundle();
        b.putString("key", snapshot.getId());

        eventSpecsFragment.setArguments(b);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MainFragment, eventSpecsFragment).addToBackStack("teste").commit();
    }

    private void getIds() {
        if (!eventIDs.isEmpty()) {
            Query query = store.collection("Events").whereIn(FieldPath.documentId(), eventIDs).whereEqualTo("Private", false);

            PagingConfig config = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

            //PAGING OPTIONS
            FirestorePagingOptions<EventsModel> options = new FirestorePagingOptions.Builder<EventsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<EventsModel>() {
                @NonNull
                @Override
                public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                    EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                    String eventID = snapshot.getId();
                    eventsModel.setEventID(eventID);
                    return eventsModel;
                }
            }).build();

            adapter = new FirestoreAdapter(options, this);
            recyclerViewAllEvents.setHasFixedSize(true);
            recyclerViewAllEvents.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewAllEvents.setAdapter(adapter);
        }
    }
}