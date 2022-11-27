package com.example.gatherup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.gatherup.databinding.FragmentAddFriendsBinding;

public class FriendsListFragment extends Fragment {

    private Button addFriendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        addFriendButton = view.findViewById(R.id.EPAddFriends);

        addFriendButton.setOnClickListener(view1 -> {
            Fragment addFriendsFragment = new AddFriendsFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, addFriendsFragment).commit();
        });


        return view;
    }


}
