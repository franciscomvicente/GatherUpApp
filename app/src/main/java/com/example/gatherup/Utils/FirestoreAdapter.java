package com.example.gatherup.Utils;

import android.location.Location;
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
import com.google.firebase.firestore.GeoPoint;

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
        holder.list_distance.setText(distance(model.getLocation(),model.getLocal()));
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

    private String distance(Location location, GeoPoint local) {
        String distancia;
        float dist;
        android.location.Location location1 = new android.location.Location("provider");
        location1.setLatitude(location.getLatitude());
        location1.setLongitude(location.getLongitude());

        android.location.Location location2 = new android.location.Location("provider");
        location2.setLatitude(local.getLatitude() );
        location2.setLongitude(local.getLongitude());
        dist = location1.distanceTo(location2);
        dist = Math.round(dist * 10) / 10.0f;
        distancia = dist + "M";
        if(dist > 999){
            dist = dist / 1000;
            dist = Math.round(dist * 10) / 10.0f;
            distancia = dist + "Km";
        }
        return distancia;
    }
}
