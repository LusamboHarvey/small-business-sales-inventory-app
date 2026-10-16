
package com.team2.zedstockpos;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team2.zedstockpos.model.Product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private Button btnAddProduct;
    private ProductAdapter productAdapter;
    private DatabaseHelper databaseHelper;

    private final List<Product> productList = new ArrayList<>();

    private Uri selectedImageUri = null;
    private ImageView imgProductPreview;

    private EditText inputEditName;
    private EditText inputEditCategory;
    private EditText inputEditPrice;
    private EditText inputEditStock;
    private ImageView imgEditPreview;
    private Uri editImageUri = null;
    private String editImagePath = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;

                            if (imgProductPreview != null) {
                                imgProductPreview.setImageURI(uri);
                            }

                            if (imgEditPreview != null) {
                                imgEditPreview.setImageURI(uri);
                                editImageUri = uri;
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        recyclerProducts = findViewById(R.id.recyclerProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(this)
        );

        databaseHelper = new DatabaseHelper(this);

        productAdapter = new ProductAdapter(productList);

        productAdapter.setOnProductActionListener(
                new ProductAdapter.OnProductActionListener() {
                    @Override
                    public void onEdit(Product product) {
                        showEditProductDialog(product);
                    }

                    @Override
                    public void onDelete(Product product) {
                        confirmDeleteProduct(product);
                    }
                }
        );

        recyclerProducts.setAdapter(productAdapter);

        loadProducts();

        btnAddProduct.setOnClickListener(
                view -> showAddProductDialog()
        );
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(databaseHelper.getAllProducts());
        productAdapter.notifyDataSetChanged();
    }

    private void showAddProductDialog() {
        selectedImageUri = null;
        imgProductPreview = null;
        imgEditPreview = null;

        LinearLayout form = createForm();

        ImageView preview = createImagePreview();
        imgProductPreview = preview;

        Button btnChooseImage = new Button(this);
        btnChooseImage.setText("Choose Product Image");
        btnChooseImage.setOnClickListener(
                view -> imagePickerLauncher.launch("image/*")
        );

        EditText inputName = createInput("Product name");
        EditText inputCategory = createInput("Category");
        EditText inputPrice = createInput("Price (K)");
        EditText inputStock = createInput("Stock quantity");

        configureNumberInputs(inputPrice, inputStock);

        form.addView(preview);
        form.addView(btnChooseImage);
        form.addView(inputName);
        form.addView(inputCategory);
        form.addView(inputPrice);
        form.addView(inputStock);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            String name = inputName.getText()
                                    .toString().trim();
                            String category = inputCategory.getText()
                                    .toString().trim();
                            String priceText = inputPrice.getText()
                                    .toString().trim();
                            String stockText = inputStock.getText()
                                    .toString().trim();

                            if (name.isEmpty() || category.isEmpty()
                                    || priceText.isEmpty()
                                    || stockText.isEmpty()) {
                                showToast("Please fill in all fields.");
                                return;
                            }

                            try {
                                double price =
                                        Double.parseDouble(priceText);
                                int stock =
                                        Integer.parseInt(stockText);

                                if (!Double.isFinite(price)
                                        || price <= 0 || stock < 0) {
                                    showToast(
                                            "Enter a valid price and stock quantity."
                                    );
                                    return;
                                }

                                String imagePath = null;

                                if (selectedImageUri != null) {
                                    imagePath = saveImageToInternalStorage(
                                            selectedImageUri
                                    );

                                    if (imagePath == null) {
                                        showToast(
                                                "Could not save the selected image."
                                        );
                                        return;
                                    }
                                }

                                Product product = new Product(
                                        0,
                                        name,
                                        category,
                                        price,
                                        stock,
                                        imagePath
                                );

                                long result =
                                        databaseHelper.insertProduct(product);

                                if (result != -1) {
                                    loadProducts();

                                    if (!productList.isEmpty()) {
                                        recyclerProducts.scrollToPosition(
                                                productList.size() - 1
                                        );
                                    }

                                    showToast(
                                            "Product saved successfully."
                                    );
                                    dialog.dismiss();
                                } else {
                                    deleteImageFile(imagePath);
                                    showToast(
                                            "Failed to save product."
                                    );
                                }

                            } catch (NumberFormatException e) {
                                showToast(
                                        "Enter a valid price and whole stock quantity."
                                );
                            }
                        })
        );

        dialog.setOnDismissListener(ignored -> {
            imgProductPreview = null;
            selectedImageUri = null;
        });

        dialog.show();
    }

    private void showEditProductDialog(Product product) {
        editImageUri = null;
        editImagePath = product.getImagePath();

        imgProductPreview = null;
        imgEditPreview = null;

        LinearLayout form = createForm();

        ImageView preview = createImagePreview();
        imgEditPreview = preview;

        displayProductImage(preview, product.getImagePath());

        Button btnChooseImage = new Button(this);
        btnChooseImage.setText("Choose Product Image");
        btnChooseImage.setOnClickListener(
                view -> imagePickerLauncher.launch("image/*")
        );

        inputEditName = createInput("Product name");
        inputEditCategory = createInput("Category");
        inputEditPrice = createInput("Price (K)");
        inputEditStock = createInput("Stock quantity");

        inputEditName.setText(product.getName());
        inputEditCategory.setText(product.getCategory());
        inputEditPrice.setText(String.valueOf(product.getPrice()));
        inputEditStock.setText(
                String.valueOf(product.getStockQuantity())
        );

        configureNumberInputs(inputEditPrice, inputEditStock);

        form.addView(preview);
        form.addView(btnChooseImage);
        form.addView(inputEditName);
        form.addView(inputEditCategory);
        form.addView(inputEditPrice);
        form.addView(inputEditStock);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Changes", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            String name = inputEditName.getText()
                                    .toString().trim();
                            String category = inputEditCategory.getText()
                                    .toString().trim();
                            String priceText = inputEditPrice.getText()
                                    .toString().trim();
                            String stockText = inputEditStock.getText()
                                    .toString().trim();

                            if (name.isEmpty() || category.isEmpty()
                                    || priceText.isEmpty()
                                    || stockText.isEmpty()) {
                                showToast("Please fill in all fields.");
                                return;
                            }

                            try {
                                double price =
                                        Double.parseDouble(priceText);
                                int stock =
                                        Integer.parseInt(stockText);

                                if (!Double.isFinite(price)
                                        || price <= 0 || stock < 0) {
                                    showToast(
                                            "Enter a valid price and stock quantity."
                                    );
                                    return;
                                }

                                String oldImagePath = product.getImagePath();
                                String newImagePath = oldImagePath;

                                if (editImageUri != null) {
                                    newImagePath =
                                            saveImageToInternalStorage(
                                                    editImageUri
                                            );

                                    if (newImagePath == null) {
                                        showToast(
                                                "Could not save the selected image."
                                        );
                                        return;
                                    }
                                }

                                product.setName(name);
                                product.setCategory(category);
                                product.setPrice(price);
                                product.setStockQuantity(stock);
                                product.setImagePath(newImagePath);

                                int rows =
                                        databaseHelper.updateProduct(product);

                                if (rows > 0) {
                                    loadProducts();

                                    if (editImageUri != null
                                            && oldImagePath != null
                                            && !oldImagePath.equals(
                                            newImagePath)) {
                                        deleteImageFile(oldImagePath);
                                    }

                                    showToast(
                                            "Product updated successfully."
                                    );
                                    dialog.dismiss();
                                } else {
                                    if (editImageUri != null) {
                                        deleteImageFile(newImagePath);
                                    }

                                    showToast(
                                            "Could not update product."
                                    );
                                }

                            } catch (NumberFormatException e) {
                                showToast(
                                        "Enter a valid price and whole stock quantity."
                                );
                            }
                        })
        );

        dialog.setOnDismissListener(ignored -> {
            imgEditPreview = null;
            editImageUri = null;
        });

        dialog.show();
    }

    private LinearLayout createForm() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (16 * getResources()
                .getDisplayMetrics().density);

        form.setPadding(padding, padding, padding, 0);
        return form;
    }

    private ImageView createImagePreview() {
        ImageView imageView = new ImageView(this);

        int imageSize = (int) (120 * getResources()
                .getDisplayMetrics().density);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(imageSize, imageSize);

        params.bottomMargin = (int) (8 * getResources()
                .getDisplayMetrics().density);

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        return imageView;
    }

    private EditText createInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);

        input.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return input;
    }

    private void configureNumberInputs(
            EditText priceInput, EditText stockInput) {

        priceInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        stockInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );
    }

    private String saveImageToInternalStorage(Uri uri) {
        File imageFile = null;

        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                return null;
            }

            File imageDirectory = new File(
                    getFilesDir(), "product_images"
            );

            if (!imageDirectory.exists()
                    && !imageDirectory.mkdirs()) {
                return null;
            }

            imageFile = File.createTempFile(
                    "product_", ".jpg", imageDirectory
            );

            try (FileOutputStream outputStream =
                         new FileOutputStream(imageFile)) {

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap == null) {
                    imageFile.delete();
                    return null;
                }

                boolean saved = bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        85,
                        outputStream
                );

                bitmap.recycle();

                if (!saved) {
                    imageFile.delete();
                    return null;
                }

                outputStream.flush();
            }

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            if (imageFile != null) {
                imageFile.delete();
            }
            return null;
        }
    }

    private void displayProductImage(
            ImageView imageView, String imagePath) {

        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
            return;
        }

        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            imageView.setImageURI(Uri.fromFile(imageFile));
        } else {
            imageView.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }

    private void deleteImageFile(String imagePath) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File file = new File(imagePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception ignored) {
                // A failed cleanup should not crash the app.
            }
        }
    }

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage(
                        "Are you sure you want to delete "
                                + product.getName() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    int rows = databaseHelper.deleteProduct(
                            product.getId()
                    );

                    if (rows > 0) {
                        deleteImageFile(product.getImagePath());
                        loadProducts();

                        showToast("Product deleted successfully.");
                    } else {
                        showToast("Could not delete product.");
                    }
                })
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
