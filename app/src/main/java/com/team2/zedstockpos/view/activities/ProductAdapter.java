
package com.team2.zedstockpos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team2.zedstockpos.model.Product;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private OnProductActionListener listener;

    // Decode images away from the main UI thread.
    private final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(2);

    // Keep recently used images in memory.
    private final LruCache<String, Bitmap> imageCache;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;

        int cacheSizeKb = (int) (
                Runtime.getRuntime().maxMemory() / 1024 / 8
        );

        imageCache = new LruCache<String, Bitmap>(cacheSizeKb) {
            @Override
            protected int sizeOf(
                    @NonNull String key,
                    @NonNull Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
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

        loadProductImage(holder.imgProduct, product.getImagePath());

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

    private void loadProductImage(
            ImageView imageView, String imagePath) {

        // Reset recycled rows so old images never flash.
        imageView.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        imageView.setTag(imagePath);

        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        File imageFile = new File(imagePath);

        if (!imageFile.exists() || !imageFile.isFile()) {
            return;
        }

        // Use a cached bitmap immediately when available.
        Bitmap cachedBitmap = imageCache.get(imagePath);

        if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        // Decode the image in the background.
        imageExecutor.execute(() -> {
            Bitmap bitmap = decodeSampledBitmap(imagePath);

            if (bitmap == null) {
                return;
            }

            imageCache.put(imagePath, bitmap);

            // Update the UI only if this row still represents
            // the same product image.
            imageView.post(() -> {
                Object currentTag = imageView.getTag();

                if (imagePath.equals(currentTag)
                        && !bitmap.isRecycled()) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private Bitmap decodeSampledBitmap(String imagePath) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, bounds);

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        // Match the approximately 100dp image view in item_product.xml.
        int targetSize = (int) (
                100 * android.content.res.Resources.getSystem()
                        .getDisplayMetrics().density
        );

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(
                bounds, targetSize, targetSize
        );
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        try {
            return BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError error) {
            return null;
        }
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options,
            int requestedWidth,
            int requestedHeight) {

        int height = options.outHeight;
        int width = options.outWidth;
        int sampleSize = 1;

        while ((height / sampleSize) > requestedHeight * 2
                || (width / sampleSize) > requestedWidth * 2) {
            sampleSize *= 2;
        }

        return sampleSize;
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtProductName;
        TextView txtProductCategory;
        TextView txtProductPrice;
        TextView txtProductStock;
        Button btnEditProduct;
        Button btnDeleteProduct;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
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
