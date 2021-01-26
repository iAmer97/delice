package android.example.delice.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.delice.CommentsActivity;
import android.example.delice.FollowingActivity;
import android.example.delice.Fragment.PostDetailsFragment;
import android.example.delice.Fragment.ProfileFragment;
import android.example.delice.Model.Post;
import android.example.delice.Model.User;
import android.example.delice.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(position);

        Glide.with(mContext)
                .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder)).load(post.getPostimages().get(0)).into(holder.post_image);

        if(post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        }
        else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        holder.name.setText(post.getName());

        publisherInfo(holder.image_profile,holder.publisher,post.getPublisher());

        isLiked(post.getPostid(),holder.like);
        nrlikes(holder.likes,post.getPostid());
        getComments(post.getPostid(),holder.comments);
        isSaved(post.getPostid(),holder.save);

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();

                editor.putString("profileid",post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });

        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();

                editor.putString("profileid",post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();

                editor.putString("postid",post.getPostid());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailsFragment()).commit();

            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(holder.save.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves")
                            .child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                }
                else {
                    FirebaseDatabase.getInstance().getReference().child("Saves")
                            .child(firebaseUser.getUid()).child(post.getPostid())
                            .removeValue();
                }
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(post.getPostid())
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    addNotification(post.getPublisher(),post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(post.getPostid())
                            .child(firebaseUser.getUid())
                            .removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisherid",post.getPublisher());
                mContext.startActivity(intent);
            }
        });


        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisherid",post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FollowingActivity.class);

                intent.putExtra("id",post.getPostid());
                intent.putExtra("title","likes");

                mContext.startActivity(intent);
            }
        });

        holder.add_cart.setOnClickListener(new View.OnClickListener() {

            GenericTypeIndicator<Map<String,String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>(){};
            Map<String, String> map= new HashMap<String,String>();
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Cart").child(firebaseUser.getUid());

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.exists()) {
                                map = snapshot.getValue(genericTypeIndicator);
                                Log.d("snapshot", snapshot.toString());
                            }


                        for (String ingredient : post.getIngredients().keySet()){
                            Log.i("hi",map.toString()+ ingredient);
                            if(map != null && map.containsKey(ingredient)){
                                Log.i("hi","ingerdient");
                                int sum = Integer.parseInt(map.get(ingredient))+ Integer.parseInt((String)post.getIngredients().get(ingredient));
                                map.put(ingredient,sum+"");
                            }

                            else{
                                Log.i("hi","ingerdient2");
                                map.put(ingredient,(String)post.getIngredients().get(ingredient));
                            }
                        }

                        databaseReference.setValue(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
        holder.edit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(mContext, v);

                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit:
                                //TODO EDIT POST
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                pop.inflate(R.menu.post_menu);
                if(!post.getPublisher().equals(firebaseUser.getUid())){
                    pop.getMenu().findItem(R.id.edit).setVisible(false);
                    pop.getMenu().findItem(R.id.delete).setVisible(false);
                }
                pop.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, post_image, like, comment, save,add_cart,edit_post;
        public TextView username, likes,publisher,name, description, comments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            publisher = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
            add_cart = itemView.findViewById(R.id.add_cart);
            edit_post = itemView.findViewById(R.id.edit_post);
        }
    }

    private void getComments(String postid, TextView comments){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText("View All "+snapshot.getChildrenCount()+" Comments.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void isLiked(String postid, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.like2);
                    imageView.setTag("liked");
                } else{
                    imageView.setImageResource(R.drawable.like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrlikes(TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount()+" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void publisherInfo(ImageView image_profile, TextView publisher, String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Glide.with(mContext).load(user.getImageurl()).into(image_profile);
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void isSaved(String id, ImageView imageView){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(id).exists()){
                    imageView.setImageResource(R.drawable.saved2);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotification(String userid, String postid){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("postid",postid);
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("ispost",true);
        hashMap.put("text","liked your post.");

        reference.push().setValue(hashMap);


    }
}
