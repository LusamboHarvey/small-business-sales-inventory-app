package com.team2.zedstockpos.view.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.team2.zedstockpos.R;
import com.team2.zedstockpos.model.Product;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adapter that shows the items currently in the cashier's cart
public class CashierCartAdapter
        extends RecyclerView.Adapter<CashierCartAdapter.CartViewHolder> {

    // Callback so the activity can update quantity when +/- is pressed
    public interface CartListener {
        void onQuantityChanged(Product product, int quantity);
    }

    // Simple wrapper that pairs a product with its selected quantity
    public static class CartItem {
        public final Product product;
        public final int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    private final List<CartItem> items = new ArrayList<>(); // current cart rows
    private final CartListener listener; // notifies activity of changes

    public CashierCartAdapter(CartListener listener) {
        this.listener = listener;
    }

    // Replace the cart list and refresh the UI
    public void setItems(List<CartItem> newItems) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        // Inflate one cart row layout
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_cashier_cart,
                parent,
                false
        );

        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder holder,
            int position
    ) {
        CartItem item = items.get(position);
        Product product = item.product;

        // Populate the row views
        holder.name.setText(product.getName());

        // Show price per unit
        holder.price.setText(String.format(
                Locale.getDefault(),
                "ZMW %.2f each",
                product.getPrice()
        ));

        // Show selected quantity
        holder.quantity.setText(String.valueOf(item.quantity));

        // Show total for this row = price * quantity
        holder.lineTotal.setText(String.format(
                Locale.getDefault(),
                "ZMW %.2f",
                product.getPrice() * item.quantity
        ));

        // Load the product image or show a placeholder.
        loadProductImage(holder.productImage, product);

        // Decrease quantity.
        holder.minus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuantityChanged(
                        product,
                        item.quantity - 1
                );
            }
        });

        // Increase quantity only when stock is available.
        holder.plus.setOnClickListener(v -> {
            if (item.quantity < product.getStockQuantity()
                    && listener != null) {
                listener.onQuantityChanged(
                        product,
                        item.quantity + 1
                );
            }
        });
    }

    private void loadProductImage(
            ImageView imageView,
            Product product
    ) {
        // Default image shown when no product image is available.
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);

        // This requires Product.java to contain getImagePath().
        String imagePath = product.getImagePath();

        if (TextUtils.isEmpty(imagePath)) {
            return;
        }

        try {
            // Handle content:// or file:// URIs (e.g., picked via photo picker)
            if (imagePath.startsWith("content://")
                    || imagePath.startsWith("file://")) {

                Uri imageUri = Uri.parse(imagePath);

                try (InputStream inputStream =
                             imageView.getContext()
                                     .getContentResolver()
                                     .openInputStream(imageUri)) {

                    if (inputStream != null) {
                        Bitmap bitmap =
                                BitmapFactory.decodeStream(inputStream);

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }

            } else {
                // Handle a regular filesystem path.
                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            imageFile.getAbsolutePath()
                    );

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }

        } catch (Exception exception) {
            // Keep the placeholder if the image cannot be loaded.
            imageView.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }

    @Override
    public int getItemCount() {
        // Number of cart rows
        return items.size();
    }

    // Holds references to views in one cart row
    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView name, price, quantity, lineTotal;
        MaterialButton minus, plus;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views once per row (avoids repeated findViewById calls)
            productImage = itemView.findViewById(
                    R.id.imgCartProduct
            );

            name = itemView.findViewById(
                    R.id.txtCartProductName
            );

            price = itemView.findViewById(
                    R.id.txtCartProductPrice
            );

            quantity = itemView.findViewById(
                    R.id.txtCartQuantity
            );

            lineTotal = itemView.findViewById(
                    R.id.txtCartLineTotal
            );

            minus = itemView.findViewById(
                    R.id.btnCartMinus
            );

            plus = itemView.findViewById(
                    R.id.btnCartPlus
            );
        }
    }
}
