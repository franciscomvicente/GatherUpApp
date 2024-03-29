package com.example.gatherup.Utils;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class FriendsAdapter extends FirestorePagingAdapter<FindFriendsModel, FriendsAdapter.EventsViewHolder> {
    private FriendsAdapter.OnListItemClicked onListItemClicked;
    private StorageReference profilePhoto;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore store;
    private String userID;

    public FriendsAdapter(@NonNull FirestorePagingOptions<FindFriendsModel> options, FriendsAdapter.OnListItemClicked onListItemClicked) {
        super(options);
        this.onListItemClicked = onListItemClicked;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull FindFriendsModel model) {
        holder.list_username.setText(model.getUsername());
        holder.btnAddFriend.setText("Remove Friend");
        holder.btnAddFriend.setBackgroundColor(0xFFFF0000);
        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveFriend(model.getUserID());
            }
        });
        //Check(holder, model.getUserID());

        profilePhoto = FirebaseStorage.getInstance().getReference().child("Users/" + model.getUserID() + "/profile.jpg");
        profilePhoto.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(holder.list_UserPhoto));
    }

    @NonNull
    @Override
    public FriendsAdapter.EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend_single, parent, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        store = FirebaseFirestore.getInstance();
        userID = user.getUid();

        return new FriendsAdapter.EventsViewHolder(view);
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

    private void RemoveFriend(String friendID) {
        DocumentReference documentReferencefriend = store.collection("Users").document(friendID).collection("Friends").document(userID);
        documentReferencefriend.delete();

        DocumentReference documentReferenceuser = store.collection("Users").document(userID).collection("Friends").document(friendID);
        documentReferenceuser.delete();
    }
}
