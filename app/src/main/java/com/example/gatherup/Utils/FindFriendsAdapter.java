package com.example.gatherup.Utils;

import android.net.Uri;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class FindFriendsAdapter extends FirestorePagingAdapter<FindFriendsModel, FindFriendsAdapter.EventsViewHolder> {

    private OnListItemClicked onListItemClicked;
    private StorageReference profilePhoto;

    public FindFriendsAdapter(@NonNull FirestorePagingOptions<FindFriendsModel> options, FindFriendsAdapter.OnListItemClicked onListItemClicked) {
        super(options);
        this.onListItemClicked = onListItemClicked;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull FindFriendsModel model) {
        holder.list_username.setText(model.getUsername());
        //holder.btnAddFriend.setText(model.getDate());

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
        return new FindFriendsAdapter.EventsViewHolder(view);
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
}
