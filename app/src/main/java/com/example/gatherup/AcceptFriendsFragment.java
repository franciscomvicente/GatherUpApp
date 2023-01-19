package com.example.gatherup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.Utils.FindFriendsModel;
import com.example.gatherup.Utils.RequestFriendsAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AcceptFriendsFragment extends Fragment implements RequestFriendsAdapter.OnListItemClicked{

    private EditText inputAccept;
    private RecyclerView outputAccept;

    private FirebaseFirestore store;
    private RequestFriendsAdapter adapter;
    private Query query;
    private PagingConfig config;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userID;
    private ArrayList friendRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_friends_accept, container, false);

        inputAccept = view.findViewById(R.id.inputAccept);
        outputAccept = view.findViewById(R.id.outputFriendRequest);
        store = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();



        Query updated = store.collection("Users").document(userID).collection("Friends").whereEqualTo("Status", "Requested");


        inputAccept.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchPeople = s.toString();
                /*SearchPeople(searchPeople);*/
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        updated.addSnapshotListener((value, error) -> {
            if (value != null) {
                Filter();
            }
        });
        return view;
    }


    @Override
    public void onItemClick(DocumentSnapshot snapshot, int position) {
        Log.d("ItemCLIECK","ItemCLicked" + position + "AND THE ID:" + snapshot.getId());

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle p = new Bundle();
        p.putString("User_key", snapshot.getId());

        profileFragment.setArguments(p);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MainFragment, profileFragment).addToBackStack("friendsrequest_list").commit();
    }


    public void Filter() {
        Query queryAux = store.collection("Users").document(userID).collection("Friends").whereEqualTo("Status", "Requested");
        friendRequests = new ArrayList<>();
        System.out.println("");
        queryAux.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    System.out.println("document.getData(): " + document.getData());
                    String eventReference = document.getId();
                    System.out.println(eventReference);
                    //if(eventIDs.size() < 10) {
                    friendRequests.add(eventReference);
                    //}
                }
                Query();
            } else {
                Log.d("TAG", "Error getting event references: ", task.getException());
            }
        });

    }

    public void Query() {
        if (!friendRequests.isEmpty()) {
            query = store.collection("Users").whereIn(FieldPath.documentId(), friendRequests);
            config = new PagingConfig(3);

            FirestorePagingOptions<FindFriendsModel> options = new FirestorePagingOptions.Builder<FindFriendsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<FindFriendsModel>() {
                @NonNull
                @Override
                public FindFriendsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                    FindFriendsModel findFriendsModel = snapshot.toObject(FindFriendsModel.class);
                    String userID = snapshot.getId();
                    System.out.println(userID);
                    findFriendsModel.setUserID(userID);
                    return findFriendsModel;
                }
            }).build();

            adapter = new RequestFriendsAdapter(options, this);
            outputAccept.setLayoutManager(new LinearLayoutManager(getContext()));
            outputAccept.setAdapter(adapter);

        } else {
            adapter = new RequestFriendsAdapter(new FirestorePagingOptions.Builder<FindFriendsModel>().setLifecycleOwner(this).setQuery(store.collection("Users").document(userID).collection("Friends").whereEqualTo("Status", "wherewhere"), new PagingConfig(3), snapshot -> null).build(),this);
            outputAccept.setLayoutManager(new LinearLayoutManager(getContext()));
            outputAccept.setAdapter(adapter);
        }

    }

    /*private void SearchPeople(String searchPeople) {
        query = store.collection("Users").document(userID).collection("Friends").whereEqualTo("Status","Requested").startAt(searchPeople).endAt(searchPeople + "\uf8ff");

        FirestorePagingOptions<FindFriendsModel> options = new FirestorePagingOptions.Builder<FindFriendsModel>().setLifecycleOwner(this).setQuery(query, config, snapshot -> {
            FindFriendsModel findFriendsModel = snapshot.toObject(FindFriendsModel.class);
            String userID = snapshot.getId();
            findFriendsModel.setUserID(userID);
            return findFriendsModel;
        }).build();

        adapter = new RequestFriendsAdapter(options, this);
        outputAccept.setAdapter(adapter);
    }*/


}