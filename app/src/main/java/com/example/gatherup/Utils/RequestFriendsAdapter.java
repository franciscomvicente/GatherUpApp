package com.example.gatherup.Utils;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.R;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class RequestFriendsAdapter extends FirestorePagingAdapter<FindFriendsModel, RequestFriendsAdapter.EventsViewHolder> {
    private RequestFriendsAdapter.OnListItemClicked onListItemClicked;
    private StorageReference profilePhoto;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore store;
    private String userID;

    public RequestFriendsAdapter(@NonNull FirestorePagingOptions<FindFriendsModel> options, RequestFriendsAdapter.OnListItemClicked onListItemClicked) {
        super(options);
        this.onListItemClicked = onListItemClicked;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull FindFriendsModel model) {
        holder.btnAcceptFriendRequest.setBackgroundColor(0xFF24D74E);
        holder.btnRejectFriendRequest.setBackgroundColor(0xFFFF0000);


        holder.btnAcceptFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AcceptFriendRequest(model.getUserID());
            }
        });

        holder.btnRejectFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveFriendRequest(model.getUserID());
            }
        });
        //Check(holder, model.getUserID());

        profilePhoto = FirebaseStorage.getInstance().getReference().child("Users/" + model.getUserID() + "/profile.jpg");
        profilePhoto.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(holder.list_UserPhoto));
        holder.list_username.setText(model.getUsername());
    }



    @NonNull
    @Override
    public RequestFriendsAdapter.EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_requested_friend, parent, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        return new RequestFriendsAdapter.EventsViewHolder(view);
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView list_username;
        private ImageView list_UserPhoto;
        private Button btnAcceptFriendRequest;
        private Button btnRejectFriendRequest;


        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            list_username = itemView.findViewById(R.id.list_usernameRequest);
            list_UserPhoto = itemView.findViewById(R.id.list_UserPhotoRequest);
            btnAcceptFriendRequest = itemView.findViewById(R.id.btnAddFriendRequest);
            btnRejectFriendRequest = itemView.findViewById(R.id.btnRejectFriendRequest);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onListItemClicked.onItemClick(getItem(getBindingAdapterPosition()), getBindingAdapterPosition());
        }
    }

    public interface OnListItemClicked {
        void onItemClick(DocumentSnapshot snapshot, int position);
    }

    private void AcceptFriendRequest(String friendID) {
        DocumentReference documentReferenceacceptfriend = store.collection("Users").document(friendID).collection("Friends").document(userID);
        Map<String, Object> friendrequestacceptfriend = new HashMap<>();
        friendrequestacceptfriend.put("Status", "Done");
        documentReferenceacceptfriend.update(friendrequestacceptfriend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + friendID + "Friend Requested Invited");
            }
        });

        DocumentReference documentReferenceacceptuser = store.collection("Users").document(userID).collection("Friends").document(friendID);
        Map<String, Object> friendrequestacceptuser = new HashMap<>();
        friendrequestacceptuser.put("Status", "Done");
        documentReferenceacceptuser.update(friendrequestacceptuser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + friendID + "Friend Requested Invited");
            }
        });
    }

    private void RemoveFriendRequest(String friendID) {
        DocumentReference documentReferencefriend = store.collection("Users").document(friendID).collection("Friends").document(userID);
        documentReferencefriend.delete();

        DocumentReference documentReferenceuser = store.collection("Users").document(userID).collection("Friends").document(friendID);
        documentReferenceuser.delete();
    }

}
