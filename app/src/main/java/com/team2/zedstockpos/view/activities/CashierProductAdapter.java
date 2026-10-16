
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

public class CashierProductAdapter
        extends RecyclerView.Adapter<CashierProductAdapter.ProductViewHolder> {

    public interface QuantityListener {
        void onQuantityChanged(Product product, int quantity);
    }

    private final List<Product> products;
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

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cashier_product, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder, int position) {

        Product product = products.get(position);
        int productId = product.getId();
        int stock = product.getStockQuantity();

        holder.txtName.setText(product.getName());

        holder.txtPrice.setText(
                String.format(
                        Locale.getDefault(),
                        "ZMW %.2f",
                        product.getPrice()
                )
        );

        String imagePath = product.getImagePath();

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                holder.imgProduct.setImageBitmap(
                        BitmapFactory.decodeFile(imageFile.getAbsolutePath())
                );
            } else {
                holder.imgProduct.setImageResource(
                        R.drawable.ic_launcher_foreground
                );
            }
        } else {
            holder.imgProduct.setImageResource(
                    R.drawable.ic_launcher_foreground
            );
        }

        int quantity = quantities.getOrDefault(productId, 0);
        holder.txtQuantity.setText(String.valueOf(quantity));

        boolean outOfStock = stock <= 0;
        holder.itemView.setAlpha(outOfStock ? 0.55f : 1.0f);

        holder.btnPlus.setEnabled(!outOfStock && quantity < stock);
        holder.btnMinus.setEnabled(quantity > 0);

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

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName;
        TextView txtPrice;
        TextView txtQuantity;
        MaterialButton btnPlus;
        MaterialButton btnMinus;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);

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
