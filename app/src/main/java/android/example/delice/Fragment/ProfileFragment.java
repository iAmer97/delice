package android.example.delice.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.delice.Adapter.MyPhotoAdapter;
import android.example.delice.ChatActivity;
import android.example.delice.EditProfileActivity;
import android.example.delice.FollowingActivity;
import android.example.delice.Model.Post;
import android.example.delice.Model.User;
import android.example.delice.OptionsActivity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.example.delice.R;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ImageView image_profile, options;
    TextView posts, followers, following, fullname, bio, username;
    Button edit_profile,message;

    private List<String> mySaves;

    RecyclerView recyclerView_saved;
    MyPhotoAdapter myPhotoAdapter_saved;
    List<Post> postList_saved;

    RecyclerView recyclerView;
    MyPhotoAdapter myPhotoAdapter;
    List<Post> postList;

    FirebaseUser firebaseUser;
    String profileid,usernameText;

    ImageButton my_photos, saved_photos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid","none");

        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        my_photos = view.findViewById(R.id.my_photos);
        saved_photos = view.findViewById(R.id.saved_photos);
        edit_profile = view.findViewById(R.id.edit_profile);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        message = view.findViewById(R.id.message);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);

        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getContext(),postList);
        recyclerView.setAdapter(myPhotoAdapter);

        recyclerView_saved = view.findViewById(R.id.recycler_view_saved);
        recyclerView_saved.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_saved = new GridLayoutManager(getContext(),3);
        recyclerView_saved.setLayoutManager(linearLayoutManager_saved);
        postList_saved = new ArrayList<>();
        myPhotoAdapter_saved = new MyPhotoAdapter(getContext(),postList_saved);
        recyclerView_saved.setAdapter(myPhotoAdapter_saved);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saved.setVisibility(View.GONE);


        userInfo();
        getFollowers();
        getSaves();
        getPosts();
        getFollowing();
        getSaves();

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("userId",profileid);
                intent.putExtra("source","profile");

                startActivity(intent);
            }
        });

        if(profileid.equals(firebaseUser.getUid())){
            edit_profile.setText("Edit Profile");
            message.setVisibility(View.GONE);
        }
        else {
            checkFollow();
            saved_photos.setVisibility(View.GONE);
        }


        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();

                if(btn.equals("Edit Profile")){
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }
                else if(btn.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid).child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotification();
                }
                else if(btn.equals("following")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid).child("followers").child(firebaseUser.getUid()).removeValue();

                }
            }
        });

        my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saved.setVisibility(View.GONE);
            }
        });

        saved_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView_saved.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowingActivity.class);

                intent.putExtra("id",profileid);
                intent.putExtra("title","followers");

                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowingActivity.class);

                intent.putExtra("id",profileid);
                intent.putExtra("title","following");

                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OptionsActivity.class));
            }
        });
        return view;
    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(getContext()==null){
                    return;
                }
                User user = snapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
                usernameText = user.getUsername();
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(profileid).exists()){
                    edit_profile.setText("following");
                }

                else{
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowing(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                postList.clear();
                for( DataSnapshot dataSnapshot : snapshot.getChildren()){
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    Log.w("post", map.toString());
                    Log.w("num", map.get("numberOfServings").getClass().getName());

                    Post post = new Post((String)map.get("postid"),(ArrayList) map.get("postimages"), (String) map.get("description"), (String) map.get("name"),(String) map.get("publisher"),(Map<String, Object>)map.get("tags"),(Map<String, Object>) map.get("ingredients"),(Map<String, Object>) map.get("steps"),(String) map.get("numberOfServings"));

                    if(post.getPublisher().equals(profileid)){
                        postList.add(post);
                        i++;
                    }
                }
                Collections.reverse(postList);
                myPhotoAdapter.notifyDataSetChanged();
                posts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSaves() {
        mySaves = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    mySaves.add(dataSnapshot.getKey());
                }

                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void readSaves(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saved.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    Log.w("post", map.toString());
                    Log.w("num", map.get("numberOfServings").getClass().getName());

                    Post post = new Post((String)map.get("postid"),(ArrayList) map.get("postimages"), (String) map.get("description"), (String) map.get("name"),(String) map.get("publisher"),(Map<String, Object>)map.get("tags"),(Map<String, Object>) map.get("ingredients"),(Map<String, Object>) map.get("steps"),(String) map.get("numberOfServings"));

                    if(mySaves.contains(post.getPostid())){
                        postList_saved.add(post);
                    }
                }

                myPhotoAdapter_saved.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotification(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("postid","");
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("ispost",false);
        hashMap.put("text","started following you");

        reference.push().setValue(hashMap);


    }
}