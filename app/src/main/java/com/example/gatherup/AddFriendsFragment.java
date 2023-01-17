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
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AddFriendsFragment extends Fragment implements FindFriendsAdapter.OnListItemClicked{

    private EditText inputPeople;
    private RecyclerView outputPeople;

    private FirebaseFirestore store;
    private FindFriendsAdapter adapter;
    private Query query;
    private PagingConfig config;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_add_friends, container, false);

        inputPeople = view.findViewById(R.id.inputPeople);
        outputPeople = view.findViewById(R.id.outputPeople);
        store = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();

        query = store.collection("Users").whereNotEqualTo(FieldPath.documentId(), userID);

        inputPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchPeople = s.toString();
                SearchPeople(searchPeople);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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

        adapter = new FindFriendsAdapter(options, this);
        outputPeople.setHasFixedSize(true);
        outputPeople.setLayoutManager(new LinearLayoutManager(getContext()));
        outputPeople.setAdapter(adapter);

        return view;
    }

    private void SearchPeople(String searchPeople) {
        query = store.collection("Users").orderBy("Username").startAt(searchPeople).endAt(searchPeople+"\uf8ff");

        FirestorePagingOptions<FindFriendsModel> options = new FirestorePagingOptions.Builder<FindFriendsModel>().setLifecycleOwner(this).setQuery(query, config, snapshot -> {
            FindFriendsModel findFriendsModel = snapshot.toObject(FindFriendsModel.class);
            String userID = snapshot.getId();
            findFriendsModel.setUserID(userID);
            return findFriendsModel;
        }).build();

        adapter = new FindFriendsAdapter(options, this);
        outputPeople.setAdapter(adapter);
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