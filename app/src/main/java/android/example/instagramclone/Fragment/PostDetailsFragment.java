package android.example.instagramclone.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.example.instagramclone.Adapter.PostAdapter;
import android.example.instagramclone.Model.Post2;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PostDetailsFragment extends Fragment {

    String postid;
    private TextView nrlikes, descriptionPost, servings;
    private String[] prepartion, quantity, ingredient;
    LinearLayout ll,ll2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_post_details, container, false);

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postid = preferences.getString("postid", "none");
        descriptionPost = view.findViewById(R.id.postDescription);
        servings = view.findViewById(R.id.servings);
        ll = view.findViewById(R.id.IngredientItems);
        ll2=view.findViewById(R.id.Preparation);

        readPost();



        return view;
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                };
                Map<String, Object> map = snapshot.getValue(genericTypeIndicator);
                Log.w("post", map.toString());
                Log.w("num", map.get("numberOfServings").getClass().getName());

                String description;
                Map ingredients;
                Map steps;
                String serving;
                Post2 post = new Post2((String) map.get("postid"), (ArrayList) map.get("postimages"), description = (String) map.get("description").toString(), (String) map.get("name"), (String) map.get("publisher"), (Map<String, Object>) map.get("tags"), ingredients = (Map<String, Object>) map.get("ingredients"), steps = (Map<String, Object>) map.get("steps"), serving = (String) map.get("numberOfServings"));
                Object[] ing = ingredients.keySet().toArray();
                Object[] quantities = ingredients.values().toArray();
                Object[] step = steps.keySet().toArray();
                Object[] step2 = steps.values().toArray();
                ingredient = Arrays.copyOf(ing, ing.length, String[].class);
                quantity = Arrays.copyOf(quantities, quantities.length, String[].class);
                prepartion = Arrays.copyOf(step2, step2.length, String[].class);
                servings.setText(serving+" Serving(s)");
                descriptionPost.setText(description);
                LayoutInflater li = LayoutInflater.from(getActivity());
                for(int i=0;i<ingredient.length;i++){
                    View view1 = li.inflate(R.layout.ing_display,null,false);
                    TextView quan = view1.findViewById(R.id.ingquan);
                    TextView ingr = view1.findViewById(R.id.ingredientdisplay);
                    quan.setText(quantity[i]);
                    ingr.setText(ingredient[i]);
                    ll.addView(view1);
                }
                for(int i=0;i<prepartion.length;i++){
                    View view2 = li.inflate(R.layout.prepartion,null,false);
                    TextView stepnum = view2.findViewById(R.id.stepDisplay);
                    TextView stepdesc = view2.findViewById(R.id.stepDescription);
                    stepnum.setText("Step "+(i+1));
                    stepdesc.setText(prepartion[i]);
                    ll2.addView(view2);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}