package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.gatherup.databinding.FragmentAddFriendsBinding;

public class FriendsListFragment extends Fragment {

    private Button btnAddFriend;
    private EditText inputFriendName;
    private RecyclerView outputFriends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);


        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        inputFriendName = view.findViewById(R.id.inputFriendName);
        outputFriends = view.findViewById(R.id.outputFriends);

        outputFriends.setHasFixedSize(true);
        outputFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        //outputFriends.setAdapter(adapter);

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


}
