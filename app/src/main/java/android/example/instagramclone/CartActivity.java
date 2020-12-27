package android.example.instagramclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.example.instagramclone.Adapter.CartAdapter;
import android.example.instagramclone.Model.ShoppingCart;
import android.os.Bundle;

import java.util.List;

public class CartActivity extends AppCompatActivity {
    RecyclerView cart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        cart = findViewById(R.id.cart);
        cart.setLayoutManager(new LinearLayoutManager(this));
        cart.setHasFixedSize(true);

    }

    public void displayCart(List<ShoppingCart> carts){
        CartAdapter cartAdapter = new CartAdapter(this, carts);
        cart.setAdapter(cartAdapter);

    }
}