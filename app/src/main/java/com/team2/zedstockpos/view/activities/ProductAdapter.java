package com.team2.zedstockpos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team2.zedstockpos.model.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setOnProductActionListener(
            OnProductActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder, int position) {

        Product product = productList.get(position);

        holder.txtProductName.setText(product.getName());
        holder.txtProductCategory.setText(product.getCategory());
        holder.txtProductPrice.setText(
                String.format(
                        Locale.getDefault(),
                        "Price: K%.2f",
                        product.getPrice()
                )
        );
        holder.txtProductStock.setText(
                "Stock: " + product.getStockQuantity()
        );

        holder.btnEditProduct.setOnClickListener(view -> {
            if (listener != null) {
                listener.onEdit(product);
            }
        });

        holder.btnDeleteProduct.setOnClickListener(view -> {
            if (listener != null) {
                listener.onDelete(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView txtProductName;
        TextView txtProductCategory;
        TextView txtProductPrice;
        TextView txtProductStock;
        Button btnEditProduct;
        Button btnDeleteProduct;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName =
                    itemView.findViewById(R.id.txtProductName);
            txtProductCategory =
                    itemView.findViewById(R.id.txtProductCategory);
            txtProductPrice =
                    itemView.findViewById(R.id.txtProductPrice);
            txtProductStock =
                    itemView.findViewById(R.id.txtProductStock);
            btnEditProduct =
                    itemView.findViewById(R.id.btnEditProduct);
            btnDeleteProduct =
                    itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}