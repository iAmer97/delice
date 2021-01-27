package android.example.delice.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.delice.CommentsActivity;
import android.example.delice.FollowingActivity;
import android.example.delice.Model.Post;
import android.example.delice.Model.User;
import android.example.delice.PostingActivity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.example.delice.R;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostDetailsFragment extends Fragment {

    String postid;
    private TextView  descriptionPost, servings, username;
    private String[] prepartion, quantity, ingredient;
    ImageView like, addCart, save, more;
    TextView likes, addCartText,comments,name;
    LinearLayout ll,ll2;
    FirebaseUser firebaseUser;
    ImageView imageUser,postImage;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_post_details, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postid = preferences.getString("postid", "none");
        descriptionPost = view.findViewById(R.id.postDescription);
        servings = view.findViewById(R.id.servings);
        ll = view.findViewById(R.id.IngredientItems);
        ll2=view.findViewById(R.id.Preparation);
        imageUser = view.findViewById(R.id.image_profile);
        postImage = view.findViewById(R.id.post_image);
        username = view.findViewById(R.id.username);
        more = view.findViewById(R.id.more);



        readPost();



        like = view.findViewById(R.id.like);
        name = view.findViewById(R.id.name);
        addCart = view.findViewById(R.id.add_cart);
        save = view.findViewById(R.id.save);
        likes = view.findViewById(R.id.likes);
        addCartText = view.findViewById(R.id.add_cart_text);
        comments = view.findViewById(R.id.comments);



        return view;
    }




    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);

        final Post[] post = new Post[1];
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                };
                Map<String, Object> map = snapshot.getValue(genericTypeIndicator);

                post[0] = new Post((String)map.get("postid"),(ArrayList) map.get("postimages"), (String) map.get("description"), (String) map.get("name"),(String) map.get("publisher"),(Map<String, Object>)map.get("tags"),(Map<String, Object>) map.get("ingredients"),(Map<String, Object>) map.get("steps"),(String) map.get("numberOfServings"));


                String description;
                Map ingredients;
                Map steps;
                String serving;
                description = post[0].getDescription();
                ingredients = post[0].getIngredients();
                steps = post[0].getSteps();
                serving = post[0].getNumberOfServings();
                name.setText(post[0].getName().toString());
                String publisherID = (String)map.get("publisher");
                if(!publisherID.equals(firebaseUser.getUid())){
                    more.setVisibility(View.GONE);
                }

                publisherInfo(imageUser,username,(String)map.get("publisher")); //use method publisherInfo() to get the publisher info and put them in the layout

                Object[] ing = ingredients.keySet().toArray();
                Object[] quantities = ingredients.values().toArray();
                Object[] step = steps.keySet().toArray();
                Object[] step2 = steps.values().toArray();
                ingredient = Arrays.copyOf(ing, ing.length, String[].class);
                quantity = Arrays.copyOf(quantities, quantities.length, String[].class);
                prepartion = Arrays.copyOf(step2, step2.length, String[].class);
                servings.setText(serving+" Serving(s)");
                Glide.with(getContext())
                        .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder)).load(post[0].getPostimages().get(0)).into(postImage);

                if(post[0].getDescription().equals("")){
                    descriptionPost.setVisibility(View.GONE);
                }
                else {
                    descriptionPost.setVisibility(View.VISIBLE);
                    descriptionPost.setText(post[0].getDescription());
                }
                LayoutInflater li = LayoutInflater.from(getActivity());
                for(int i=0;i<ingredient.length;i++){
                    View view1 = li.inflate(R.layout.ing_display,null,false);
                    TextView quan = view1.findViewById(R.id.ingquan);
                    TextView ingr = view1.findViewById(R.id.ingredientdisplay);
                    quan.setText(quantity[i]);
                    ingr.setText(ingredient[i]);
                    ll.addView(view1);
                }
                Log.w("steps", post[0].getSteps().toString());
                for(int i=0;i<prepartion.length;i++){
                    View view2 = li.inflate(R.layout.prepartion,null,false);
                    TextView stepnum = view2.findViewById(R.id.stepDisplay);
                    TextView stepdesc = view2.findViewById(R.id.stepDescription);
                    stepnum.setText("Step "+(i+1));
                    Log.w("steps", post[0].getSteps().get("step Step "+(i+1)).toString());
                    stepdesc.setText(post[0].getSteps().get("step Step "+(i+1)).toString());
                    ll2.addView(view2);
                }


                isLiked(post[0].getPostid(),like);
                nrlikes(likes,post[0].getPostid());
                getComments(post[0].getPostid(),comments);
                isSaved(post[0].getPostid(),save);

                addCart.setOnClickListener(new View.OnClickListener() {

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


                                for (String ingredient : post[0].getIngredients().keySet()){
                                    Log.i("hi",map.toString()+ ingredient);
                                    if(map != null && map.containsKey(ingredient)){
                                        Log.i("hi","ingerdient");
                                        int sum = Integer.parseInt(map.get(ingredient))+ Integer.parseInt((String)post[0].getIngredients().get(ingredient));
                                        map.put(ingredient,sum+"");
                                    }

                                    else{
                                        Log.i("hi","ingerdient2");
                                        map.put(ingredient,(String)post[0].getIngredients().get(ingredient));
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

                addCartText.setOnClickListener(new View.OnClickListener() {

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


                                for (String ingredient : post[0].getIngredients().keySet()){
                                    Log.i("hi",map.toString()+ ingredient);
                                    if(map != null && map.containsKey(ingredient)){
                                        Log.i("hi","ingerdient");
                                        int sum = Integer.parseInt(map.get(ingredient))+ Integer.parseInt((String)post[0].getIngredients().get(ingredient));
                                        map.put(ingredient,sum+"");
                                    }

                                    else{
                                        Log.i("hi","ingerdient2");
                                        map.put(ingredient,(String)post[0].getIngredients().get(ingredient));
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

                likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), FollowingActivity.class);

                        intent.putExtra("id",post[0].getPostid());
                        intent.putExtra("title","likes");

                        getContext().startActivity(intent);
                    }
                });

                like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(like.getTag().equals("like")){
                            FirebaseDatabase.getInstance().getReference().child("Likes")
                                    .child(post[0].getPostid())
                                    .child(firebaseUser.getUid())
                                    .setValue(true);

                            addNotification(post[0].getPublisher(),post[0].getPostid());
                        } else {
                            FirebaseDatabase.getInstance().getReference().child("Likes")
                                    .child(post[0].getPostid())
                                    .child(firebaseUser.getUid())
                                    .removeValue();
                        }
                    }
                });

                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), CommentsActivity.class);
                        intent.putExtra("postid",post[0].getPostid());
                        intent.putExtra("publisherid",post[0].getPublisher());
                        getContext().startActivity(intent);
                    }
                });


                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(save.getTag().equals("save")){
                            FirebaseDatabase.getInstance().getReference().child("Saves")
                                    .child(firebaseUser.getUid())
                                    .child(post[0].getPostid()).setValue(true);
                        }
                        else {
                            FirebaseDatabase.getInstance().getReference().child("Saves")
                                    .child(firebaseUser.getUid()).child(post[0].getPostid())
                                    .removeValue();
                        }
                    }
                });

                imageUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();

                        editor.putString("profileid",post[0].getPublisher());
                        editor.apply();

                        ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

                    }
                });

                username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();

                        editor.putString("profileid",post[0].getPublisher());
                        editor.apply();

                        ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(getContext(), v);

                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit:
                                Intent intent = new Intent(getContext(), PostingActivity.class);
                                intent.putExtra("key","edit");
                                intent.putExtra("postid",post[0].getPostid());
                                getContext().startActivity(intent);
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts").child(post[0].getPostid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("postid").equalTo(post[0].getPostid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            if(snapshot.exists()) {
                                                                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                                                };
                                                                Map<String, Object> map = snapshot.getValue(genericTypeIndicator);

                                                                for (String key : map.keySet()) {
                                                                    map.put(key, null);
                                                                }

                                                                FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(map);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                    FirebaseDatabase.getInstance().getReference("Comments").child(post[0].getPostid()).removeValue();
                                                    FirebaseDatabase.getInstance().getReference("Likes").child(post[0].getPostid()).removeValue();

                                                    Toast.makeText(getContext(), "Deleted!", Toast.LENGTH_SHORT).show();
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
                if(!post[0].getPublisher().equals(firebaseUser.getUid())){
                    pop.getMenu().findItem(R.id.edit).setVisible(false);
                    pop.getMenu().findItem(R.id.delete).setVisible(false);
                }
                pop.show();
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
                    imageView.setImageResource(R.drawable.heart);
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

    private void isSaved(String id, ImageView imageView){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(id).exists()){
                    imageView.setImageResource(R.drawable.saved2);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.saved3);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void publisherInfo(ImageView image_profile, TextView username, String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
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

}