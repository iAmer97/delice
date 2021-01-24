package android.example.delice.Adapter;

import android.content.Context;
import android.example.delice.Model.ShoppingCart;
import android.example.delice.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;



public class CartAdapter extends RecyclerView.Adapter<CartAdapter.cartViewHolder> {
    Context context;
    List<ShoppingCart> cartlist;
    FirebaseUser firebaseUser;

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
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ShoppingCart item = cartlist.get(position);
        holder.product.setText(item.getIngredient());
        holder.amount.setText(item.getAmount());
        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.parseInt(holder.amount.getText().toString().trim());
                holder.amount.setText(String.valueOf(value+1));
                String a = Integer.toString(value+1);
                FirebaseDatabase.getInstance().getReference().child("Cart")
                        .child(firebaseUser.getUid())
                        .child(holder.product.getText().toString())
                        .setValue(a);

            }
        });
        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.parseInt(holder.amount.getText().toString().trim());
                holder.amount.setText(String.valueOf(value-1));
                String a = Integer.toString(value-1);
                FirebaseDatabase.getInstance().getReference().child("Cart")
                        .child(firebaseUser.getUid())
                        .child(holder.product.getText().toString())
                        .setValue(a);
                int k = Integer.parseInt(a);
                if(k < 1){
                    FirebaseDatabase.getInstance().getReference().child("Cart")
                            .child(firebaseUser.getUid())
                            .child(holder.product.getText().toString())
                            .removeValue();
                }
            }
        });
        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Cart")
                        .child(firebaseUser.getUid())
                        .child(holder.product.getText().toString())
                        .removeValue();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartlist == null ? 0 : cartlist.size();
    }

    class cartViewHolder extends RecyclerView.ViewHolder{
        TextView product;
        EditText amount;
        ImageView inc,dec,close;


        public cartViewHolder(@NonNull View itemView) {
            super(itemView);

            product = itemView.findViewById(R.id.product_name);
            amount = itemView.findViewById(R.id.amount);
            inc = itemView.findViewById(R.id.inc);
            dec = itemView.findViewById(R.id.dec);
            close = itemView.findViewById(R.id.closeIng);
            inc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(amount.getText().toString().trim());
                    amount.setText(String.valueOf(value+1));

                }
            });
            dec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(amount.getText().toString().trim());
                    amount.setText(String.valueOf(value-1));
                }
            });

        }
    }
}
