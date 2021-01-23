package android.example.instagramclone.Fragment;

import android.content.Intent;
import android.example.instagramclone.Adapter.PostAdapter;
import android.example.instagramclone.Adapter.StoryAdapter;
import android.example.instagramclone.CartActivity;
import android.example.instagramclone.DmActivite;
import android.example.instagramclone.Model.Post;
import android.example.instagramclone.Model.Story;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.example.instagramclone.R;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView,story;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<Story> storiesList;
    private StoryAdapter storyAdapter;
    private List<String> followingList;
    ImageView not,close,navDM;

    ProgressBar progressBar;
    TextView searchResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home,container,false);
        searchResults = view.findViewById(R.id.search_results);
        close = view.findViewById(R.id.close);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(postAdapter);
        story = view.findViewById(R.id.recycler_view_story);
        story.setHasFixedSize(true);
        LinearLayoutManager ll = new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false);
        story.setLayoutManager(ll);
        storiesList = new ArrayList<>();
        storyAdapter=new StoryAdapter(getContext(),storiesList);
        story.setAdapter(storyAdapter);
        not = view.findViewById(R.id.nav_cart);
        not.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CartActivity.class);
               startActivity(intent);
            }
        });

        navDM = view.findViewById(R.id.nav_dm);

        navDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DmActivite.class));
            }
        });

        progressBar = view.findViewById(R.id.progress_circular);


        progressBar.setVisibility(View.VISIBLE);

        if(getArguments()!=null){

            searchResults.setVisibility(View.VISIBLE);
            close.setVisibility(View.VISIBLE);

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    searchResults.setVisibility(View.INVISIBLE);
                    close.setVisibility(View.INVISIBLE);
                    checkFollowing();
                }
            });

            getSearchResults();

        }
        else {
            checkFollowing();
        }
        return view;
    }

    private void checkFollowing(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following");
        followingList = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    followingList.add(dataSnapshot.getKey());
                }

                Log.i("following",""+followingList.size());

                readPosts();
                readStory();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                    Log.w("post", map.toString());
                    Log.w("num", map.get("numberOfServings").getClass().getName());

                    Post post = new Post((String)map.get("postid"),(ArrayList) map.get("postimages"), (String) map.get("description"), (String) map.get("name"),(String) map.get("publisher"),(Map<String, Object>)map.get("tags"),(Map<String, Object>) map.get("ingredients"),(Map<String, Object>) map.get("steps"),(String) map.get("numberOfServings"));

                    Log.w("ingredients", post.getIngredients().toString());
                    if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getPublisher())){
                        postList.add(post);
                    }
                    else {
                        for (String id : followingList) {
                            if (id.equals(post.getPublisher())) {
                                postList.add(post);
                            }
                        }
                    }

                }

                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSearchResults(){
        ArrayList<String> checkedTags = (ArrayList<String>)getArguments().getSerializable("checkedTags");
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("tags/"+checkedTags.get(0)).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);

                    Post post = new Post((String)map.get("postid"),(ArrayList) map.get("postimages"), (String) map.get("description"), (String) map.get("name"),(String) map.get("publisher"),(Map<String, Object>)map.get("tags"),(Map<String, Object>) map.get("ingredients"),(Map<String, Object>) map.get("steps"),(String) map.get("numberOfServings"));

                    List<String> postTags = new ArrayList<>(post.getTags().keySet());

                    if(postTags.containsAll(checkedTags)){
                        postList.add(post);
                    }
                }

                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");

        Log.i("story","h1");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("story","h2");
                long timecurrent = System.currentTimeMillis();
                storiesList.clear();
                storiesList.add(new Story("","",FirebaseAuth.getInstance().getCurrentUser().getUid(),0
                        ,0));
                Log.i("story","h13");
                for(String id : followingList){
                    int countStory =0;
                    Story story = null;
                    for(DataSnapshot dataSnapshot : snapshot.child(id).getChildren() ){
                        story = dataSnapshot.getValue(Story.class);
                        if(timecurrent>story.getTimestart() && timecurrent<story.getTimeend()){
                            countStory++;
                        }
                    }
                    if(countStory>0){
                        storiesList.add(story);
                    }
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}