package com.example.gatherup.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.R;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;

public class FirestoreAdapter extends FirestorePagingAdapter<EventsModel, FirestoreAdapter.EventsViewHolder> {

    private OnListItemClicked onListItemClicked;

    public FirestoreAdapter(@NonNull FirestorePagingOptions<EventsModel> options, OnListItemClicked onListItemClicked) {
        super(options);
        this.onListItemClicked = onListItemClicked;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull EventsModel model) {
        String[] array = toDate(model);
        holder.list_title.setText(model.getTitle());
        holder.list_date.setText(array[0]);
        holder.list_hour.setText(array[1]);
        holder.list_distance.setText(model.getDistance());
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single, parent, false);
        return new EventsViewHolder(view);
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView list_title, list_date, list_hour, list_distance;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            list_distance = itemView.findViewById(R.id.list_distance);
            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);
            list_hour = itemView.findViewById(R.id.list_hours);

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

    private String[] toDate(EventsModel model){
        Timestamp timestamp = model.getDate();
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = sfd.format(timestamp.toDate());
        String[] splitStr = ((String) time).split("\\s+");

        return new String[]{ splitStr[0], splitStr[1] };
    }
}
