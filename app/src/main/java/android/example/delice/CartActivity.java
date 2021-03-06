package android.example.delice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.example.delice.Adapter.CartAdapter;
import android.example.delice.Model.ShoppingCart;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    TextView clear;

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
        clear = findViewById(R.id.clearall);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Cart")
                        .child(firebaseUser.getUid())
                        .removeValue();
            }
        });
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