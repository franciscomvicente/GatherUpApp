package com.example.gatherup.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatherup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class EventChatAdapter extends RecyclerView.Adapter<EventChatAdapter.HolderEventChat>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 0;
    String senderUid;

    private Context context;
    private ArrayList<EventChatModel> eventChatModels;  // NAO NECESSARIO

    private FirebaseAuth auth;

    public EventChatAdapter(Context context){
        this.context = context;

        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderEventChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_eventchat_right, parent, false);
            return new HolderEventChat(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_eventchat_left, parent, false);
            return new HolderEventChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderEventChat holder, int position) {
        EventChatModel model = new EventChatModel();

        String message  = model.getMessage();
        senderUid = model.getSender();

        holder.outputName.setText(message);

        setUserName(model, holder);
    }

    private void setUserName(EventChatModel model, HolderEventChat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Events");
    }

    @Override
    public int getItemViewType(int position) {
        if(senderUid.equals(auth.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class HolderEventChat extends RecyclerView.ViewHolder{

        private TextView outputName, outputMessage, outputTimestamp;

        public HolderEventChat(@NonNull View itemView) {
            super(itemView);

            outputName = itemView.findViewById(R.id.outputName);
            outputMessage = itemView.findViewById(R.id.outputMessage);
            outputTimestamp = itemView.findViewById(R.id.outputTimestamp);
        }
    }

}
