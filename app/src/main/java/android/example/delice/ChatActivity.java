package android.example.delice;

import android.content.Context;
import android.example.delice.Adapter.MessageAdapter;
import android.example.delice.Model.Message;
import android.example.delice.Model.User;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolBar;
    private ImageButton SendMessageButton, SendImageButton;
    private EditText userMessageInput;

    private RecyclerView userMessagesList;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private String messageRceiverID, messageReceiverName, messageSenderId,source;

    private TextView receiverName;
    private ImageView receiverProfileImage;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();

        messageSenderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageRceiverID = getIntent().getStringExtra("userId");
        source = getIntent().getStringExtra("source");
        //messageReceiverName = getIntent().getStringExtra("username");

        initializeViews();
        
        displayReceiverInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });
        
        fetchMessages();
    }

    private void fetchMessages() {
        rootRef.child("Messages").child(messageSenderId).child(messageRceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    Message message = snapshot.getValue(Message.class);
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();

                    if(TextUtils.equals(message.getFrom(),messageRceiverID)){
                        message.setSeen(true);

                        rootRef.child("Messages").child(messageSenderId).child(messageRceiverID).child(snapshot.getKey()).child("isSeen").setValue(true);
                        rootRef.child("Messages").child(messageRceiverID).child(messageSenderId).child(snapshot.getKey()).child("isSeen").setValue(true);
                        rootRef.child("Chats").child(messageSenderId).child(messageRceiverID).child("lastMessageIsSeen").setValue("true");
                        rootRef.child("Chats").child(messageRceiverID).child(messageSenderId).child("lastMessageIsSeen").setValue("true");
                    }
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

        //update seen in chats
    }

    private void sendMessage() {

        String messageText = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please enter message!", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref = "Messages/"+messageSenderId+"/"+messageRceiverID;
            String message_receiver_ref = "Messages/"+messageRceiverID+"/"+messageSenderId;

            DatabaseReference userMessageKey = rootRef.child("Messages").child(messageSenderId).child(messageRceiverID).push();
            String messageKey = userMessageKey.getKey();

            //ServerValue.TIMESTAMP;

            //SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");

            //currentTime.format(new Date(21728));

            //SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");

            //currentTime.format(new Date(21728));

            Map messageTextBody = new HashMap<>();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("isSeen",false);

            Map requestBody = new HashMap<>();

            requestBody.put(message_sender_ref+"/"+messageKey,messageTextBody);
            requestBody.put(message_receiver_ref+"/"+messageKey,messageTextBody);

            rootRef.updateChildren(requestBody).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        userMessageInput.setText("");

                        //updating chats
                        String chat_sender_ref = "Chats/"+messageSenderId+"/"+messageRceiverID;
                        String chat_receiver_ref = "Chats/"+messageRceiverID+"/"+messageSenderId;

                        Map chatSenderBody = new HashMap<>();
                        chatSenderBody.put("id",messageRceiverID);
                        chatSenderBody.put("lastMessageFrom",messageSenderId);
                        chatSenderBody.put("lastMessage",messageText);
                        chatSenderBody.put("lastMessageIsSeen","false");

                        Map chatReceiverBody = new HashMap<>();
                        chatReceiverBody.put("id",messageSenderId);
                        chatReceiverBody.put("lastMessageFrom",messageSenderId);
                        chatReceiverBody.put("lastMessage",messageText);
                        chatReceiverBody.put("lastMessageIsSeen","false");

                        Map requestBody = new HashMap<>();

                        requestBody.put(chat_sender_ref,chatSenderBody);
                        requestBody.put(chat_receiver_ref,chatReceiverBody);

                        rootRef.updateChildren(requestBody);

                    }
                }
            });

        }
    }

    private void displayReceiverInfo() {

        rootRef.child("Users").child(messageRceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);

                    receiverName.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(receiverProfileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeViews(){
        chatToolBar = findViewById(R.id.app_bar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(actionBarView);

        SendImageButton = findViewById(R.id.send_image);
        SendMessageButton = findViewById(R.id.send_message);

        receiverName = findViewById(R.id.custom_profile_name);
        receiverProfileImage = findViewById(R.id.image_profile);

        userMessageInput = findViewById(R.id.input_message);

        userMessagesList = findViewById(R.id.messages_list_users);
        messageAdapter = new MessageAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setAdapter(messageAdapter);
        userMessagesList.setLayoutManager(linearLayoutManager);


    }
}