package android.example.delice.Adapter;

import android.content.Context;
import android.content.Intent;
import android.example.delice.ChatActivity;
import android.example.delice.Model.Chat;
import android.example.delice.Model.User;
import android.example.delice.R;
import android.util.Log;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private Context mcontext;

    public ChatAdapter(List<Chat> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        mcontext = parent.getContext();
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        Chat chat = chatList.get(position);

        Log.w("chat adapter",chat.isLastMessageIsSeen());
        Log.w("chat adapter",chat.getLastMessageFrom());

        userDetails(holder.profileImage,holder.username,chat.getId());

        holder.message.setText(chat.getLastMessage());


        if(!chat.getLastMessageFrom().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !chat.isLastMessageIsSeen().equalsIgnoreCase("false")){
            holder.newMessage.setVisibility(View.VISIBLE);
        }

        else{
            holder.newMessage.setVisibility(View.GONE);
        }

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        Intent intent = new Intent(mcontext, ChatActivity.class);
                        intent.putExtra("userId", chat.getId());
                        intent.putExtra("source", "dm");

                        mcontext.startActivity(intent);
            }
        });

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mcontext, ChatActivity.class);
                intent.putExtra("userId", chat.getId());
                intent.putExtra("source", "dm");

                mcontext.startActivity(intent);
            }
        });

    }

    private void userDetails(ImageView profileImage, TextView username, String id) {

        FirebaseDatabase.getInstance().getReference("Users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                username.setText(user.getUsername());
                Glide.with(mcontext).load(user.getImageurl()).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage,newMessage;
        TextView message,username;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.image_profile);
            newMessage =itemView.findViewById(R.id.new_messages);
            message = itemView.findViewById(R.id.message);
            username = itemView.findViewById(R.id.username);
        }
    }
}
