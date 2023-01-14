package com.example.gatherup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.Utils.FindFriendsModel;
import com.example.gatherup.Utils.FriendsAdapter;
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

public class FriendsListFragment extends Fragment implements FriendsAdapter.OnListItemClicked {

    private Button btnAddFriend;
    private EditText inputFriendName;
    private RecyclerView outputFriends;

    private FirebaseFirestore store;
    private FriendsAdapter adapter;
    private Query query;
    private PagingConfig config;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userID;
    private ArrayList<String> friends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);

        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        inputFriendName = view.findViewById(R.id.inputFriendName);
        outputFriends = view.findViewById(R.id.outputFriends);
        store = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();
        friends = new ArrayList<>();

        Filter();

        inputFriendName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnAddFriend.setOnClickListener(view1 -> {
            Fragment addFriendsFragment = new AddFriendsFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, addFriendsFragment).commit();
        });

        return view;
    }

    @Override
    public void onItemClick(DocumentSnapshot snapshot, int position) {
        Log.d("ItemCLIECK", "ItemCLicked" + position + "AND THE ID:" + snapshot.getId());

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle p = new Bundle();
        p.putString("User_key", snapshot.getId());

        profileFragment.setArguments(p);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MainFragment, profileFragment).addToBackStack("try").commit();
    }

    public void Filter() {
        Query queryAux = store.collection("Users").document(userID).collection("Friends").whereEqualTo("Status", "Done");
        queryAux.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    System.out.println("document.getData(): " + document.getData());
                    String eventReference = document.getId();
                    System.out.println(eventReference);
                    //if(eventIDs.size() < 10) {
                    friends.add(eventReference);
                    //}
                    System.out.println(friends);
                }
                Query();
            } else {
                Log.d("TAG", "Error getting event references: ", task.getException());
            }
        });

    }

    public void Query() {
        if (!friends.isEmpty()) {
            query = store.collection("Users").whereIn(FieldPath.documentId(), friends);
            System.out.println("2222");
            config = new PagingConfig(3);

            FirestorePagingOptions<FindFriendsModel> options = new FirestorePagingOptions.Builder<FindFriendsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<FindFriendsModel>() {
                @NonNull
                @Override
                public FindFriendsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                    System.out.println("1111");
                    FindFriendsModel findFriendsModel = snapshot.toObject(FindFriendsModel.class);
                    String userID = snapshot.getId();
                    findFriendsModel.setUserID(userID);
                    return findFriendsModel;
                }
            }).build();

            adapter = new FriendsAdapter(options, this);
            outputFriends.setHasFixedSize(true);
            outputFriends.setLayoutManager(new LinearLayoutManager(getContext()));
            outputFriends.setAdapter(adapter);
        } else {
            System.out.println("ERRRRO");
        }
    }
}
