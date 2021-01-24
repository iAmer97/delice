package android.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.example.instagramclone.Adapter.ChatAdapter;
import android.example.instagramclone.Model.Chat;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DmActivity extends AppCompatActivity {

    RecyclerView chatsRV;
    ChatAdapter chatAdapter;
    LinearLayoutManager linearLayoutManager;
    List<Chat> chatList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_activite);

        chatsRV = findViewById(R.id.chats_rv);
        chatList = new ArrayList<>();

        chatAdapter = new ChatAdapter(chatList);
        linearLayoutManager = new LinearLayoutManager(this);
        chatsRV.setHasFixedSize(true);
        chatsRV.setLayoutManager(linearLayoutManager);
        chatsRV.setAdapter(chatAdapter);

        readChats();
    }

    private void readChats() {

        FirebaseDatabase.getInstance().getReference("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    Log.w("chats",snapshot.toString());
                    chatList.add(chat);
                    chatAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}