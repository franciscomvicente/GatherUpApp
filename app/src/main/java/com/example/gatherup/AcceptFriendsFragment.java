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

import com.example.gatherup.Utils.FindFriendsAdapter;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_friends_accept, container, false);

        inputAccept = view.findViewById(R.id.inputAccept);
        outputAccept = view.findViewById(R.id.outputFriendRequest);
        store = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();

        query = store.collection("Users").document(userID).collection("Friends").whereEqualTo("Status", "Requested");


        config = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO
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
        outputAccept.setHasFixedSize(true);
        outputAccept.setLayoutManager(new LinearLayoutManager(getContext()));
        outputAccept.setAdapter(adapter);

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
        ft.replace(R.id.MainFragment, profileFragment).addToBackStack("try").commit();
    }


}