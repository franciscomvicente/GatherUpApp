package com.example.gatherup;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.Utils.EventsModel;
import com.example.gatherup.Utils.FirestoreAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class EventListFragment extends Fragment implements FirestoreAdapter.OnListItemClicked{

    private FirebaseFirestore store;
    private RecyclerView outputEvents;

    private FirestoreAdapter adapter;


    private Double latitude;
    private Double longitude;
    private Location location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        if (getArguments() != null) {
            location = getArguments().getParcelable("last_known_location");
        }

        outputEvents = view.findViewById(R.id.outputEvents);
        store = FirebaseFirestore.getInstance();

        Timestamp currentTime = Timestamp.now();

        //QUERY
        Query query = store.collection("Events").whereGreaterThan("Date",currentTime);

        PagingConfig config = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

        //PAGING OPTIONS
        FirestorePagingOptions<EventsModel> options = new FirestorePagingOptions.Builder<EventsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<EventsModel>() {
            @NonNull
            @Override
            public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                String eventID = snapshot.getId();
                eventsModel.setEventID(eventID);
                eventsModel.setLocation(location);
                return eventsModel;
            }
        }).build();

        adapter = new FirestoreAdapter(options, this);

        outputEvents.setHasFixedSize(true);
        outputEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        outputEvents.setAdapter(adapter);

        return view;
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