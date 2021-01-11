package android.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.example.instagramclone.Adapter.CartAdapter;
import android.example.instagramclone.Model.Comment;
import android.example.instagramclone.Model.ShoppingCart;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    RecyclerView cart;
    List<ShoppingCart> carts;
    FirebaseUser firebaseUser;
    CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        carts = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        cart = findViewById(R.id.cart);
        cart.setLayoutManager(new LinearLayoutManager(this));
        cart.setHasFixedSize(true);
        cartAdapter = new CartAdapter(this, carts);

        cart.setAdapter(cartAdapter);
        getItems();
    }

    public void getItems(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                carts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    carts.add(new ShoppingCart(dataSnapshot.getKey(),dataSnapshot.getValue(String.class)));
                }
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}