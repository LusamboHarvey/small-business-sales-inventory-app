
package com.team2.zedstockpos.view.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.team2.zedstockpos.R;
import com.team2.zedstockpos.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CashierCartAdapter
        extends RecyclerView.Adapter<CashierCartAdapter.CartViewHolder> {

    public interface CartListener {
        void onQuantityChanged(Product product, int quantity);
    }

    public static class CartItem {
        public final Product product;
        public final int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    private final List<CartItem> items = new ArrayList<>();
    private final CartListener listener;

    public CashierCartAdapter(CartListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_cashier_cart,
                parent,
                false
        );

        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder holder, int position) {

        CartItem item = items.get(position);
        Product product = item.product;

        holder.name.setText(product.getName());
        holder.price.setText(String.format(
                Locale.getDefault(),
                "ZMW %.2f each",
                product.getPrice()
        ));
        holder.quantity.setText(String.valueOf(item.quantity));
        holder.lineTotal.setText(String.format(
                Locale.getDefault(),
                "ZMW %.2f",
                product.getPrice() * item.quantity
        ));

        holder.minus.setOnClickListener(v ->
                listener.onQuantityChanged(
                        product,
                        item.quantity - 1
                )
        );

        holder.plus.setOnClickListener(v -> {
            if (item.quantity < product.getStockQuantity()) {
                listener.onQuantityChanged(
                        product,
                        item.quantity + 1
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, quantity, lineTotal;
        MaterialButton minus, plus;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txtCartProductName);
            price = itemView.findViewById(R.id.txtCartProductPrice);
            quantity = itemView.findViewById(R.id.txtCartQuantity);
            lineTotal = itemView.findViewById(R.id.txtCartLineTotal);
            minus = itemView.findViewById(R.id.btnCartMinus);
            plus = itemView.findViewById(R.id.btnCartPlus);
        }
    }
}
