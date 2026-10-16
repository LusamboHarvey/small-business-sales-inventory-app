package com.team2.zedstockpos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team2.zedstockpos.model.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private Button btnAddProduct;
    private ProductAdapter productAdapter;

    private final List<Product> productList = new ArrayList<>();
    private DatabaseHelper databaseHelper;

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

        productList.addAll(databaseHelper.getAllProducts());


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

        btnAddProduct.setOnClickListener(
                view -> showAddProductDialog()
        );
    }

    private void loadSampleProducts() {
        productList.add(
                new Product(1, "Sugar", "Groceries", 35.00, 20)
        );
        productList.add(
                new Product(2, "Cooking Oil", "Household", 85.00, 12)
        );
        productList.add(
                new Product(3, "Bathing Soap", "Personal Care", 18.50, 30)
        );
    }

    private void showAddProductDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (16 * getResources()
                .getDisplayMetrics().density);
        form.setPadding(padding, padding, padding, 0);

        EditText inputName = createInput("Product name");
        EditText inputCategory = createInput("Category");
        EditText inputPrice = createInput("Price (K)");
        EditText inputStock = createInput("Stock quantity");

        inputPrice.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        inputStock.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

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

                            if (name.isEmpty()
                                    || category.isEmpty()
                                    || priceText.isEmpty()
                                    || stockText.isEmpty()) {
                                Toast.makeText(this,
                                        "Please fill in all fields.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                double price = Double.parseDouble(priceText);
                                int stock = Integer.parseInt(stockText);

                                if (!Double.isFinite(price)
                                        || price <= 0 || stock < 0) {
                                    Toast.makeText(this,
                                            "Enter a valid price and stock quantity.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Product product = new Product(
                                        0,
                                        name,
                                        category,
                                        price,
                                        stock
                                );

                                long result = databaseHelper.insertProduct(product);

                                if (result != -1) {
                                    productList.clear();
                                    productList.addAll(databaseHelper.getAllProducts());

                                    productAdapter.notifyDataSetChanged();

                                    recyclerProducts.scrollToPosition(
                                            productList.size() - 1
                                    );

                                    Toast.makeText(this,
                                            "Product saved successfully.",
                                            Toast.LENGTH_SHORT).show();

                                    dialog.dismiss();

                                } else {
                                    Toast.makeText(this,
                                            "Failed to save product. Please try again.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(this,
                                        "Enter a valid price and whole stock quantity.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
        );

        dialog.show();
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

    private void showEditProductDialog(Product product) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (16 * getResources()
                .getDisplayMetrics().density);
        form.setPadding(padding, padding, padding, 0);

        EditText inputName = createInput("Product name");
        EditText inputCategory = createInput("Category");
        EditText inputPrice = createInput("Price (K)");
        EditText inputStock = createInput("Stock quantity");

        inputName.setText(product.getName());
        inputCategory.setText(product.getCategory());
        inputPrice.setText(String.valueOf(product.getPrice()));
        inputStock.setText(String.valueOf(product.getStockQuantity()));

        inputPrice.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        inputStock.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        form.addView(inputName);
        form.addView(inputCategory);
        form.addView(inputPrice);
        form.addView(inputStock);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Changes", null)
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
                                Toast.makeText(this,
                                        "Please fill in all fields.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                double price =
                                        Double.parseDouble(priceText);
                                int stock =
                                        Integer.parseInt(stockText);

                                if (!Double.isFinite(price)
                                        || price <= 0 || stock < 0) {
                                    Toast.makeText(this,
                                            "Enter a valid price and stock quantity.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                product.setName(name);
                                product.setCategory(category);
                                product.setPrice(price);
                                product.setStockQuantity(stock);

                                int rows = databaseHelper
                                        .updateProduct(product);

                                if (rows > 0) {
                                    productList.clear();
                                    productList.addAll(
                                            databaseHelper.getAllProducts()
                                    );
                                    productAdapter.notifyDataSetChanged();

                                    Toast.makeText(this,
                                            "Product updated successfully.",
                                            Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(this,
                                            "Could not update product.",
                                            Toast.LENGTH_SHORT).show();
                                }

                            } catch (NumberFormatException e) {
                                Toast.makeText(this,
                                        "Enter a valid price and whole stock quantity.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
        );

        dialog.show();
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
                        int position = productList.indexOf(product);

                        productList.clear();
                        productList.addAll(
                                databaseHelper.getAllProducts()
                        );
                        productAdapter.notifyDataSetChanged();

                        Toast.makeText(this,
                                "Product deleted successfully.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Could not delete product.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}