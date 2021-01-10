package android.example.instagramclone.Adapter;

import android.content.Context;
import android.example.instagramclone.Model.ShoppingCart;
import android.example.instagramclone.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;



public class CartAdapter extends RecyclerView.Adapter<CartAdapter.cartViewHolder> {
    Context context;
    List<ShoppingCart> cartlist;
    public CartAdapter(Context context, List<ShoppingCart> cartlist) {
        this.context = context;
        this.cartlist = cartlist;
    }

    @NonNull
    @Override
    public cartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.cart_layout,parent,false);

        return new cartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull cartViewHolder holder, int position) {
        holder.product.setText(cartlist.get(position).ingredient);
        holder.amount.setText(cartlist.get(position).amount);

    }

    @Override
    public int getItemCount() {
        return cartlist.size();
    }

    class cartViewHolder extends RecyclerView.ViewHolder{
        TextView product;
        EditText amount;
        Button inc,dec;


        public cartViewHolder(@NonNull View itemView) {
            super(itemView);

            product = itemView.findViewById(R.id.product_name);
            amount = itemView.findViewById(R.id.amount);
            inc = itemView.findViewById(R.id.inc);
            dec = itemView.findViewById(R.id.dec);
            inc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(amount.getText().toString().trim());
                    amount.setText(value+1);

                }
            });
            dec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(amount.getText().toString().trim());
                    amount.setText(value-1);
                }
            });

        }
    }
}
