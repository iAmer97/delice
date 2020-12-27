package android.example.instagramclone.Model;

import androidx.appcompat.app.AppCompatActivity;

import android.example.instagramclone.R;
import android.os.Bundle;

public class ShoppingCart {
    public String product;
    public String amount;

    public ShoppingCart(String product, String amount) {
        this.product = product;
        this.amount = amount;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}