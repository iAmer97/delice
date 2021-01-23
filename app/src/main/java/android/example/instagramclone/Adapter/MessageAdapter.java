package android.example.instagramclone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.example.instagramclone.Model.Message;
import android.example.instagramclone.R;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messagesList;
    //private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    private Context mcontext;

    public MessageAdapter(List<Message> messagesList){
        this.messagesList = messagesList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item,parent,false);

        mcontext = parent.getContext();

        //mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        //String messageSenderID = mAuth.getCurrentUser().getUid();
        String messageSenderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Message message = messagesList.get(position);

        String fromUserID = message.getFrom();
        String messageType = message.getType();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    String image = snapshot.child("imageurl").getValue().toString();

                    Glide.with(mcontext).load(image).into(holder.imageProfile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(messageType.equals("text")){
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.imageProfile.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(messageSenderID)){
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.senderMessageText.setGravity(Gravity.LEFT);
                holder.senderMessageText.setText(message.getMessage());
            }
            else{

                holder.senderMessageText.setVisibility(View.INVISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.imageProfile.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.receiverMessageText.setGravity(Gravity.LEFT);
                holder.receiverMessageText.setText(message.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView senderMessageText, receiverMessageText;
        private ImageView imageProfile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message);
            receiverMessageText = itemView.findViewById(R.id.receiver_message);
            imageProfile = itemView.findViewById(R.id.image_profile);
        }
    }
}
