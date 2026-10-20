package com.team2.zedstockpos.view.activities;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.team2.zedstockpos.R;
import com.team2.zedstockpos.model.Product;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Adapter that displays products in the cashier screen
public class CashierProductAdapter
        extends RecyclerView.Adapter<CashierProductAdapter.ProductViewHolder> {

    // Callback so the activity can react when a quantity changes
    public interface QuantityListener {
        void onQuantityChanged(Product product, int quantity);
    }

    private final List<Product> products;
    // Keeps track of selected quantity per product ID
    private final Map<Integer, Integer> quantities = new HashMap<>();
    private final QuantityListener listener;

    public CashierProductAdapter(
            List<Product> products,
            QuantityListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Inflate one product row layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cashier_product, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder, int position) {

        // Get the product for this row
        Product product = products.get(position);
        int productId = product.getId();
        int stock = product.getStockQuantity();

        // Show basic product info
        holder.txtName.setText(product.getName());

        holder.txtPrice.setText(
                String.format(
                        Locale.getDefault(),
                        "ZMW %.2f",
                        product.getPrice()
                )
        );

        // Load product image from file if it exists
        String imagePath = product.getImagePath();

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                holder.imgProduct.setImageBitmap(
                        BitmapFactory.decodeFile(imageFile.getAbsolutePath())
                );
            } else {
                // Fallback image
                holder.imgProduct.setImageResource(
                        R.drawable.ic_launcher_foreground
                );
            }
        } else {
            holder.imgProduct.setImageResource(
                    R.drawable.ic_launcher_foreground
            );
        }

        // Show current selected quantity
        int quantity = quantities.getOrDefault(productId, 0);
        holder.txtQuantity.setText(String.valueOf(quantity));

        // Dim the row if out of stock
        boolean outOfStock = stock <= 0;
        holder.itemView.setAlpha(outOfStock ? 0.55f : 1.0f);

        // Enable/disable buttons based on stock and current quantity
        holder.btnPlus.setEnabled(!outOfStock && quantity < stock);
        holder.btnMinus.setEnabled(quantity > 0);

        // Increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            int current = quantities.getOrDefault(productId, 0);

            if (current >= stock) {
                Toast.makeText(
                        v.getContext(),
                        "Not enough stock available.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int updated = current + 1;
            quantities.put(productId, updated);

            holder.txtQuantity.setText(String.valueOf(updated));
            holder.btnMinus.setEnabled(true);
            holder.btnPlus.setEnabled(updated < stock);

            listener.onQuantityChanged(product, updated);
        });

        // Decrease quantity
        holder.btnMinus.setOnClickListener(v -> {
            int current = quantities.getOrDefault(productId, 0);

            if (current <= 0) {
                return;
            }

            int updated = current - 1;

            if (updated == 0) {
                quantities.remove(productId);
            } else {
                quantities.put(productId, updated);
            }

            holder.txtQuantity.setText(String.valueOf(updated));
            holder.btnMinus.setEnabled(updated > 0);
            holder.btnPlus.setEnabled(stock > updated);

            listener.onQuantityChanged(product, updated);
        });
    }

    // Programmatically set a quantity for a product
    public void setQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            quantities.remove(productId);
        } else {
            quantities.put(productId, quantity);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Holds references to views in one row
    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName;
        TextView txtPrice;
        TextView txtQuantity;
        MaterialButton btnPlus;
        MaterialButton btnMinus;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views once per row
            imgProduct = itemView.findViewById(R.id.imgCashierProduct);
            txtName = itemView.findViewById(R.id.txtCashierProductName);
            txtPrice = itemView.findViewById(R.id.txtCashierProductPrice);
            txtQuantity = itemView.findViewById(
                    R.id.txtCashierProductQuantity
            );
            btnPlus = itemView.findViewById(
                    R.id.btnCashierProductPlus
            );
            btnMinus = itemView.findViewById(
                    R.id.btnCashierProductMinus
            );
        }
    }
}
