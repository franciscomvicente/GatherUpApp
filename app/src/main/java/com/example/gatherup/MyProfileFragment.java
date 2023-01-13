package com.example.gatherup;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gatherup.Utils.EventsModel;
import com.example.gatherup.Utils.FirestoreAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.sql.SQLOutput;

public class MyProfileFragment extends Fragment implements FirestoreAdapter.OnListItemClicked{

    ImageView outputProfilePhoto;
    TextView outputUsername, outputBio;
    Button btnEditProfile, btnLogout;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore store;
    String userID;

    StorageReference storageReference;

    RecyclerView recyclerView;
    private FirestoreAdapter adapter;
    RecyclerView recyclerView2;
    private FirestoreAdapter adapter2;


    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        setHasOptionsMenu(true);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        outputUsername = view.findViewById(R.id.outputUsername);
        outputBio = view.findViewById(R.id.outputBio);
        outputProfilePhoto = view.findViewById(R.id.outputProfilePhoto);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileReference = storageReference.child("Users/"+ userID + "/profile.jpg");
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(outputProfilePhoto);
            }
        });

        DocumentReference documentReference = store.collection("Users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {                                  //this
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                outputUsername.setText(value.getString("Username"));
                outputBio.setText(value.getString("Bio"));
            }
        });

        btnLogout.setOnClickListener(v -> PerformLogout());

        btnEditProfile.setOnClickListener(view1 -> {
            Fragment editProfileFragment = new EditPerfilFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, editProfileFragment).commit();
        });



        Timestamp currentTime = Timestamp.now();

        //RecyclerView eventosQueParticipa
        recyclerView = view.findViewById(R.id.listaEventos);
        System.out.println("Antes");
        Query query = store.collection("Users").document(userID).collection("Events").whereGreaterThanOrEqualTo("Date", currentTime);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Get the event reference
                        String eventRef = document.getString("eventRef");
                        // Use the reference to retrieve the actual event data
                        store.document(eventRef).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    EventsModel event = task.getResult().toObject(EventsModel.class);
                                    // Do something with the event data
                                }
                            }
                        });
                    }
                }
            }
        });

        System.out.println("Depois");
        PagingConfig config = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

        //PAGING OPTIONS
        FirestorePagingOptions<EventsModel> options = new FirestorePagingOptions.Builder<EventsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<EventsModel>() {
            @NonNull
            @Override
            public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                System.out.println("1");
                String eventID = snapshot.getId();
                eventsModel.setEventID(eventID);
                return eventsModel;
            }
        }).build();

        adapter = new FirestoreAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        //RecyclerView meusEventos
        recyclerView2 = view.findViewById(R.id.meusEventos);


        //QUERY Meus Eventos
        System.out.println("AS QUERIES COMEÇAM AQUI!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Query query2 = store.collection("Events").whereEqualTo("CreatorID", userID);



        PagingConfig config2 = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

        //PAGING OPTIONS
        FirestorePagingOptions<EventsModel> options2 = new FirestorePagingOptions.Builder<EventsModel>().setLifecycleOwner(this).setQuery(query2, config2, new SnapshotParser<EventsModel>() {
            @NonNull
            @Override
            public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                System.out.println("1");
                String eventID = snapshot.getId();
                eventsModel.setEventID(eventID);
                return eventsModel;
            }
        }).build();

        adapter2 = new FirestoreAdapter(options2, this);

        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView2.setAdapter(adapter2);

        return view;
    }

    private void PerformLogout() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", "false");
        editor.apply();

        Intent intent = new Intent(getContext(),LoginActivity.class);
        startActivity(intent);
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
}