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

public class FindFriendsAdapter extends FirestorePagingAdapter<FindFriendsModel, FindFriendsAdapter.EventsViewHolder> {

    private OnListItemClicked onListItemClicked;
    private StorageReference profilePhoto;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore store;
    private String userID;

    public FindFriendsAdapter(@NonNull FirestorePagingOptions<FindFriendsModel> options, FindFriendsAdapter.OnListItemClicked onListItemClicked) {
        super(options);
        this.onListItemClicked = onListItemClicked;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull FindFriendsModel model) {
        holder.list_username.setText(model.getUsername());
        Check(holder, model.getUserID());

        profilePhoto = FirebaseStorage.getInstance().getReference().child("Users/" + model.getUserID() + "/profile.jpg");
        profilePhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.list_UserPhoto);
            }
        });
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend_single, parent, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        return new FindFriendsAdapter.EventsViewHolder(view);
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView list_username;
        private ImageView list_UserPhoto;
        private Button btnAddFriend;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            list_username = itemView.findViewById(R.id.list_username);
            list_UserPhoto = itemView.findViewById(R.id.list_UserPhoto);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);

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

    private void Check(EventsViewHolder holder, String friendID) {
        DocumentReference docIdRef = store.collection("Users").document(userID).collection("Friends").document(friendID);
        docIdRef.get().addOnCompleteListener(task -> {
            String TAG = "Teste";
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (document.getString("Status").equals("Done")) {
                        holder.btnAddFriend.setText("Remove Friend");
                        holder.btnAddFriend.setBackgroundColor(0xFFFF0000);
                        holder.btnAddFriend.setOnClickListener(v -> RemoveFriend(holder,friendID));
                    } else if (document.getString("Status").equals("Requesting")) {
                        holder.btnAddFriend.setText("Requested");
                        holder.btnAddFriend.setBackgroundColor(0xFF2CD0D5);
                        holder.btnAddFriend.setOnClickListener(v -> RemoveFriend(holder,friendID));
                    } else if (document.getString("Status").equals("Requested")){
                        holder.btnAddFriend.setText("Accept");
                        holder.btnAddFriend.setBackgroundColor(0xFF24D74E);
                        holder.btnAddFriend.setOnClickListener(v -> AcceptFriend(holder,friendID));
                    }
                } else {
                    holder.btnAddFriend.setText("Add Friend");
                    holder.btnAddFriend.setBackgroundColor(0xFF437FC7);
                    holder.btnAddFriend.setOnClickListener(v -> AddFriend(holder,friendID));
                }
            } else {
                Log.d(TAG, "Failed with: ", task.getException());
            }
        });
    }

    private void RemoveFriend(EventsViewHolder holder,String friendID) {
        DocumentReference documentReferencefriend = store.collection("Users").document(friendID).collection("Friends").document(userID);
        documentReferencefriend.delete();

        DocumentReference documentReferenceuser = store.collection("Users").document(userID).collection("Friends").document(friendID);
        documentReferenceuser.delete();

        Check(holder,friendID);
    }

    private void AddFriend(EventsViewHolder holder,String friendID) {
        DocumentReference documentReferencefriend = store.collection("Users").document(friendID).collection("Friends").document(userID);
        Map<String, Object> friendrequestfriend = new HashMap<>();
        friendrequestfriend.put("Status", "Requested");
        documentReferencefriend.set(friendrequestfriend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + friendID + "Friend Requested Invited");
            }
        });

        DocumentReference documentReferenceuser = store.collection("Users").document(userID).collection("Friends").document(friendID);
        Map<String, Object> friendrequestuser = new HashMap<>();
        friendrequestuser.put("Status", "Requesting");
        documentReferenceuser.set(friendrequestuser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "onSuccess:" + friendID + "Friend Requested Invited");
            }
        });

        Check(holder,friendID);
    }

    private void AcceptFriend(EventsViewHolder holder,String friendID){
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

        Check(holder,friendID);
    }
}
