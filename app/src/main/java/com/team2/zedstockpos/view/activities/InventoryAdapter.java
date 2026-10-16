
package com.team2.zedstockpos;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team2.zedstockpos.model.Product;

import java.io.File;
import java.util.List;

public class InventoryAdapter
        extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface StockActionListener {
        void onStockChanged();
    }

    private final List<Product> productList;
    private final DatabaseHelper databaseHelper;
    private final StockActionListener listener;

    public InventoryAdapter(
            List<Product> productList,
            DatabaseHelper databaseHelper,
            StockActionListener listener) {

        this.productList = productList;
        this.databaseHelper = databaseHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);

        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull InventoryViewHolder holder, int position) {

        Product product = productList.get(position);
        int stock = product.getStockQuantity();

        holder.txtName.setText(product.getName());
        holder.txtSku.setText("SKU: PRD-" + product.getId());
        holder.txtCategory.setText(product.getCategory());
        holder.txtStock.setText("Available quantity: " + stock);

        // Load the saved product image.
        String imagePath = product.getImagePath();

        holder.imgProduct.setTag(imagePath);
        holder.imgProduct.setImageResource(
                android.R.drawable.ic_menu_gallery);

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File imageFile = new File(imagePath);

            if (imageFile.exists() && imageFile.isFile()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                if (bitmap != null
                        && imagePath.equals(holder.imgProduct.getTag())) {
                    holder.imgProduct.setImageBitmap(bitmap);
                }
            }
        }

        if (stock == 0) {
            holder.txtStatus.setText("Out of Stock");
            holder.txtStatus.setTextColor(0xFFC62828);
            holder.txtStatus.setBackgroundColor(0xFFFFEBEE);
        } else if (stock <= 10) {
            holder.txtStatus.setText("Low Stock");
            holder.txtStatus.setTextColor(0xFFE65100);
            holder.txtStatus.setBackgroundColor(0xFFFFF3E0);
        } else {
            holder.txtStatus.setText("In Stock");
            holder.txtStatus.setTextColor(0xFF1B5E20);
            holder.txtStatus.setBackgroundColor(0xFFE8F5E9);
        }

        holder.btnStockIn.setOnClickListener(
                view -> showStockDialog(
                        view.getContext(), product, true));

        holder.btnStockOut.setOnClickListener(
                view -> showStockDialog(
                        view.getContext(), product, false));
    }

    private void showStockDialog(
            Context context, Product product, boolean stockIn) {

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity");
        input.setPadding(40, 20, 40, 20);

        String action = stockIn ? "Stock In" : "Stock Out";

        new AlertDialog.Builder(context)
                .setTitle(action + " - " + product.getName())
                .setMessage("Current stock: " + product.getStockQuantity())
                .setView(input)
                .setNegativeButton("Cancel",
                        (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", (dialog, which) -> {
                    String entered = input.getText().toString().trim();

                    if (entered.isEmpty()) {
                        Toast.makeText(context,
                                "Enter a quantity.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int quantity = Integer.parseInt(entered);

                        if (quantity <= 0) {
                            Toast.makeText(context,
                                    "Quantity must be greater than zero.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int currentStock = product.getStockQuantity();
                        int newStock;

                        if (stockIn) {
                            if (quantity > Integer.MAX_VALUE - currentStock) {
                                Toast.makeText(context,
                                        "Quantity is too large.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            newStock = currentStock + quantity;
                        } else {
                            if (quantity > currentStock) {
                                Toast.makeText(context,
                                        "Not enough stock available.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            newStock = currentStock - quantity;
                        }

                        boolean updated = databaseHelper.updateStockQuantity(
                                product.getId(), newStock);

                        if (updated) {
                            Toast.makeText(context,
                                    action + " successful.",
                                    Toast.LENGTH_SHORT).show();

                            if (listener != null) {
                                listener.onStockChanged();
                            }
                        } else {
                            Toast.makeText(context,
                                    "Could not update stock.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(context,
                                "Enter a valid whole number.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtSku, txtCategory, txtStock, txtStatus;
        Button btnStockIn, btnStockOut;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgInventoryProduct);
            txtName = itemView.findViewById(R.id.txtInventoryName);
            txtSku = itemView.findViewById(R.id.txtInventorySku);
            txtCategory = itemView.findViewById(R.id.txtInventoryCategory);
            txtStock = itemView.findViewById(R.id.txtInventoryStock);
            txtStatus = itemView.findViewById(R.id.txtInventoryStatus);
            btnStockIn = itemView.findViewById(R.id.btnStockIn);
            btnStockOut = itemView.findViewById(R.id.btnStockOut);
        }
    }
}
