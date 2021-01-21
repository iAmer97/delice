package android.example.instagramclone.Fragment;

import android.example.instagramclone.Adapter.UserAdapter;
import android.example.instagramclone.Model.User;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.example.instagramclone.R;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<String> tags;
    ChipGroup cg;
    FirebaseUser firebaseUser;


    EditText search_bar;

    Button search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search,container,false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_bar = view.findViewById(R.id.search_bar);
        search = view.findViewById(R.id.search);
        cg = view.findViewById(R.id.cg);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        LayoutInflater li = LayoutInflater.from(getActivity());

        tags = new ArrayList<>();
        final int[] id = {0};
        FirebaseDatabase.getInstance().getReference("Tags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int temp = id[0];
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(!tags.contains(dataSnapshot.getKey())){
                        Chip chip = (Chip) li.inflate(R.layout.tags,null,false);
                        cg.addView(chip);
                        chip.setText(dataSnapshot.getKey());
                        chip.setId(temp);
                        temp++;
                        tags.add(dataSnapshot.getKey());
                    }
                }
                id[0] = temp;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> ids = cg.getCheckedChipIds();
                if(ids.size() == 0){
                    Toast.makeText(getContext(), "Please select at least one tag", Toast.LENGTH_SHORT).show();
                }
                else {
                    ArrayList<String> checkedTags = new ArrayList<>();
                    for (Integer id : ids) {
                        Chip chip = cg.findViewById(id);
                        checkedTags.add(chip.getText().toString());

                    }

                    Bundle bundle = new Bundle();

                    bundle.putStringArrayList("checkedTags", checkedTags);

                    Fragment fragment = new HomeFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(),mUsers,true);
        recyclerView.setAdapter(userAdapter);

        readUsers();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void searchUsers(String s){
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    Log.d("USER: ",""+user.getUsername());
                    mUsers.add(user);
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("error",error.getMessage());
            }
        });
    }

    private void readUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("readUsers","hi");
                if(search_bar.getText().toString().equals("")){
                    mUsers.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        Log.i("user",user.getUsername());
                        mUsers.add(user);
                    }

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}