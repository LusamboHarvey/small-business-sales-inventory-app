
package com.team2.zedstockpos.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.team2.zedstockpos.DatabaseHelper;
import com.team2.zedstockpos.R;
import com.team2.zedstockpos.SessionManager;
import com.team2.zedstockpos.model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CashierSalesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private SearchView searchCashierProducts;
    private RecyclerView recyclerCashierProducts;
    private RecyclerView recyclerCashierCart;

    private CashierProductAdapter productAdapter;
    private CashierCartAdapter cartAdapter;

    private TextView txtCashierCartCount;
    private TextView txtCashierSubtotal;
    private TextView txtCashierDiscount;
    private TextView txtCashierTotal;
    private TextView txtCartClear;

    private MaterialButton btnCompleteSale;
    private ImageButton btnCashierMenu;

    private final Map<Integer, CartItem> cart =
            new LinkedHashMap<>();

    private static class CartItem {
        Product product;
        int quantity;

        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier_sales);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Header menu.
        btnCashierMenu = findViewById(R.id.btnCashierMenu);

        // Product search and lists.
        searchCashierProducts =
                findViewById(R.id.searchCashierProducts);

        recyclerCashierProducts =
                findViewById(R.id.recyclerCashierProducts);

        recyclerCashierCart =
                findViewById(R.id.recyclerCashierCart);

        // Cart summary.
        txtCashierCartCount =
                findViewById(R.id.txtCashierCartCount);

        txtCashierSubtotal =
                findViewById(R.id.txtCashierSubtotal);

        txtCashierDiscount =
                findViewById(R.id.txtCashierDiscount);

        txtCashierTotal =
                findViewById(R.id.txtCashierTotal);

        txtCartClear =
                findViewById(R.id.btnClearCashierCart);

        btnCompleteSale =
                findViewById(R.id.btnCompleteSale);

        // Set up the available-products grid.
        recyclerCashierProducts.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        recyclerCashierProducts.setHasFixedSize(true);
        recyclerCashierProducts.setNestedScrollingEnabled(true);

        // Set up the independently scrollable cart.
        recyclerCashierCart.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerCashierCart.setNestedScrollingEnabled(true);

        cartAdapter = new CashierCartAdapter(
                this::onProductQuantityChanged
        );

        recyclerCashierCart.setAdapter(cartAdapter);

        // Limit cart height so it cannot consume the entire screen.
        recyclerCashierCart.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            int screenHeight = getResources()
                    .getDisplayMetrics().heightPixels;

            int maxCartHeight = (int) (screenHeight * 0.22f);

            ViewGroup.LayoutParams params =
                    recyclerCashierCart.getLayoutParams();

            if (params != null) {
                params.height = maxCartHeight;
                recyclerCashierCart.setLayoutParams(params);
            }
        });

        // Open the hamburger menu.
        btnCashierMenu.setOnClickListener(
                this::showCashierMenu
        );

        // Load available products.
        setupProductAdapter(databaseHelper.getAllProducts());

        // Configure product search.
        searchCashierProducts.setQueryHint("Search products...");

        searchCashierProducts.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterProducts(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterProducts(newText);
                        return true;
                    }
                }
        );

        // Clear the cart.
        txtCartClear.setOnClickListener(view -> {
            cart.clear();
            refreshProductAdapter();
            updateCartSummary();
        });

        // Checkout is still a placeholder.
        btnCompleteSale.setOnClickListener(view ->
                Toast.makeText(
                        CashierSalesActivity.this,
                        "Checkout will be implemented next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        updateCartSummary();
    }

    private void showCashierMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);

        popupMenu.getMenu().add(
                0,
                1,
                0,
                "My Account"
        );

        popupMenu.getMenu().add(
                0,
                2,
                1,
                "Logout"
        );

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Toast.makeText(
                        this,
                        "Logged in as: "
                                + sessionManager.getUserEmail(),
                        Toast.LENGTH_SHORT
                ).show();

                return true;
            }

            if (item.getItemId() == 2) {
                confirmLogout();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage(
                        "Are you sure you want to log out?"
                )
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()
                )
                .setPositiveButton("Logout", (dialog, which) ->
                        logout()
                )
                .show();
    }

    private void logout() {
        // Clear the saved login session only after confirmation.
        sessionManager.logout();

        Intent intent = new Intent(
                CashierSalesActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void setupProductAdapter(List<Product> products) {
        productAdapter = new CashierProductAdapter(
                products,
                this::onProductQuantityChanged
        );

        for (Product product : products) {
            CartItem item = cart.get(product.getId());

            int quantity = item == null ? 0 : item.quantity;

            productAdapter.setQuantity(
                    product.getId(),
                    quantity
            );
        }

        recyclerCashierProducts.setAdapter(productAdapter);
    }

    private void refreshProductAdapter() {
        String query = searchCashierProducts.getQuery() == null
                ? ""
                : searchCashierProducts.getQuery().toString();

        filterProducts(query);
    }

    private void filterProducts(String query) {
        List<Product> filteredProducts = new ArrayList<>();

        String searchText = query == null
                ? ""
                : query.toLowerCase(Locale.ROOT).trim();

        for (Product product : databaseHelper.getAllProducts()) {
            String name = product.getName() == null
                    ? ""
                    : product.getName().toLowerCase(Locale.ROOT);

            String category = product.getCategory() == null
                    ? ""
                    : product.getCategory().toLowerCase(Locale.ROOT);

            if (name.contains(searchText)
                    || category.contains(searchText)) {
                filteredProducts.add(product);
            }
        }

        setupProductAdapter(filteredProducts);
    }

    private void onProductQuantityChanged(
            Product product,
            int quantity) {

        int productId = product.getId();

        if (quantity > product.getStockQuantity()) {
            Toast.makeText(
                    this,
                    "Not enough stock available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            CartItem existingItem = cart.get(productId);

            if (existingItem != null) {
                existingItem.product = product;
                existingItem.quantity = quantity;
            } else {
                cart.put(
                        productId,
                        new CartItem(product, quantity)
                );
            }
        }

        updateCartSummary();
    }

    private void updateCartSummary() {
        int totalQuantity = 0;
        double subtotal = 0.0;

        List<CashierCartAdapter.CartItem> displayItems =
                new ArrayList<>();

        for (CartItem item : cart.values()) {
            totalQuantity += item.quantity;

            subtotal += item.product.getPrice() * item.quantity;

            displayItems.add(
                    new CashierCartAdapter.CartItem(
                            item.product,
                            item.quantity
                    )
            );
        }

        double discount = 0.0;
        double total = subtotal - discount;

        txtCashierCartCount.setText(
                "Current Sale (" + totalQuantity + " items)"
        );

        txtCashierSubtotal.setText(
                String.format(
                        Locale.getDefault(),
                        "ZMW %.2f",
                        subtotal
                )
        );

        txtCashierDiscount.setText(
                String.format(
                        Locale.getDefault(),
                        "- ZMW %.2f",
                        discount
                )
        );

        txtCashierTotal.setText(
                String.format(
                        Locale.getDefault(),
                        "ZMW %.2f",
                        total
                )
        );

        cartAdapter.setItems(displayItems);

        recyclerCashierCart.setVisibility(
                cart.isEmpty() ? View.GONE : View.VISIBLE
        );

        btnCompleteSale.setEnabled(!cart.isEmpty());
        txtCartClear.setEnabled(!cart.isEmpty());
    }
}
