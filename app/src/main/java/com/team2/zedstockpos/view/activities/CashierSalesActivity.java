
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private final Map<Integer, CartItem> cart = new LinkedHashMap<>();

    private boolean isProcessingSale = false;

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

        bindViews();
        setupRecyclerViews();
        setupListeners();

        setupProductAdapter(databaseHelper.getAllProducts());
        updateCartSummary();
    }

    // ---------------------------------------------------------
    // INITIALIZATION
    // ---------------------------------------------------------

    private void bindViews() {
        btnCashierMenu = findViewById(R.id.btnCashierMenu);
        searchCashierProducts = findViewById(R.id.searchCashierProducts);

        recyclerCashierProducts =
                findViewById(R.id.recyclerCashierProducts);

        recyclerCashierCart =
                findViewById(R.id.recyclerCashierCart);

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
    }

    private void setupRecyclerViews() {
        recyclerCashierProducts.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        recyclerCashierProducts.setHasFixedSize(true);
        recyclerCashierProducts.setNestedScrollingEnabled(true);

        recyclerCashierCart.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerCashierCart.setNestedScrollingEnabled(true);

        cartAdapter = new CashierCartAdapter(
                this::onProductQuantityChanged
        );

        recyclerCashierCart.setAdapter(cartAdapter);

        recyclerCashierCart.post(() -> {
            ViewGroup.LayoutParams params =
                    recyclerCashierCart.getLayoutParams();

            if (params != null) {
                params.height = (int) (
                        getResources()
                                .getDisplayMetrics()
                                .heightPixels * 0.22f
                );

                recyclerCashierCart.setLayoutParams(params);
            }
        });
    }

    private void setupListeners() {
        btnCashierMenu.setOnClickListener(this::showCashierMenu);

        searchCashierProducts.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterProducts(query);
                        searchCashierProducts.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterProducts(newText);
                        return true;
                    }
                }
        );

        txtCartClear.setOnClickListener(view -> clearCart());

        btnCompleteSale.setOnClickListener(
                view -> confirmCompleteSale()
        );
    }

    // ---------------------------------------------------------
    // CASHIER MENU
    // ---------------------------------------------------------

    private void showCashierMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);

        popupMenu.getMenu().add(0, 1, 0, "My Account");
        popupMenu.getMenu().add(0, 2, 1, "Logout");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                String email = sessionManager.getUserEmail();

                if (email == null || email.trim().isEmpty()) {
                    email = "Unknown user";
                }

                Toast.makeText(
                        this,
                        "Logged in as: " + email,
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
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> logout()
                )
                .show();
    }

    private void logout() {
        sessionManager.logout();

        Intent intent = new Intent(
                CashierSalesActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    // ---------------------------------------------------------
    // PRODUCT DISPLAY AND SEARCH
    // ---------------------------------------------------------

    private void setupProductAdapter(List<Product> products) {
        productAdapter = new CashierProductAdapter(
                products,
                this::onProductQuantityChanged
        );

        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            productAdapter.setQuantity(
                    entry.getKey(),
                    entry.getValue().quantity
            );
        }

        recyclerCashierProducts.setAdapter(productAdapter);
    }

    private void refreshProductAdapter() {
        String query = "";

        if (searchCashierProducts != null
                && searchCashierProducts.getQuery() != null) {
            query = searchCashierProducts.getQuery().toString();
        }

        filterProducts(query);
    }

    private void filterProducts(String query) {
        List<Product> allProducts = databaseHelper.getAllProducts();
        List<Product> filteredProducts = new ArrayList<>();

        String searchText = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        for (Product product : allProducts) {
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

    private void refreshProductsAndCart() {
        List<Product> latestProducts = databaseHelper.getAllProducts();

        Map<Integer, Product> productsById = new LinkedHashMap<>();

        for (Product product : latestProducts) {
            productsById.put(product.getId(), product);
        }

        for (CartItem item : cart.values()) {
            Product latestProduct =
                    productsById.get(item.product.getId());

            if (latestProduct != null) {
                item.product = latestProduct;
            }
        }

        refreshProductAdapter();
        updateCartSummary();
    }

    // ---------------------------------------------------------
    // CART MANAGEMENT
    // ---------------------------------------------------------

    private void onProductQuantityChanged(
            Product product,
            int quantity
    ) {
        if (product == null || isProcessingSale) {
            return;
        }

        int productId = product.getId();

        if (quantity > product.getStockQuantity()) {
            Toast.makeText(
                    this,
                    "Not enough stock available.",
                    Toast.LENGTH_SHORT
            ).show();

            refreshProductAdapter();
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

    private void clearCart() {
        if (isProcessingSale) {
            return;
        }

        if (cart.isEmpty()) {
            Toast.makeText(
                    this,
                    "The cart is already empty.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear Cart")
                .setMessage(
                        "Are you sure you want to remove all items "
                                + "from the current sale?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    cart.clear();

                    refreshProductAdapter();
                    updateCartSummary();

                    Toast.makeText(
                            this,
                            "Cart cleared.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    private void updateCartSummary() {
        int totalQuantity = 0;
        double subtotal = 0.0;

        List<CashierCartAdapter.CartItem> displayItems =
                new ArrayList<>();

        for (CartItem item : cart.values()) {
            totalQuantity += item.quantity;

            subtotal +=
                    item.product.getPrice() * item.quantity;

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

        txtCashierSubtotal.setText(formatMoney(subtotal));
        txtCashierDiscount.setText(
                "- " + formatMoney(discount)
        );
        txtCashierTotal.setText(formatMoney(total));

        cartAdapter.setItems(displayItems);

        recyclerCashierCart.setVisibility(
                cart.isEmpty() ? View.GONE : View.VISIBLE
        );

        boolean hasItems = !cart.isEmpty() && !isProcessingSale;

        btnCompleteSale.setEnabled(hasItems);
        txtCartClear.setEnabled(hasItems);

        btnCompleteSale.setAlpha(hasItems ? 1.0f : 0.5f);
        txtCartClear.setAlpha(hasItems ? 1.0f : 0.5f);
    }

    // ---------------------------------------------------------
    // CHECKOUT CONFIRMATION
    // ---------------------------------------------------------

    private void confirmCompleteSale() {
        if (isProcessingSale) {
            return;
        }

        if (cart.isEmpty()) {
            Toast.makeText(
                    this,
                    "Add products to the cart first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int itemCount = 0;

        for (CartItem item : cart.values()) {
            itemCount += item.quantity;
        }

        new AlertDialog.Builder(this)
                .setTitle("Complete Sale")
                .setMessage(
                        "Confirm this sale?\n\n"
                                + "Items: " + itemCount + "\n"
                                + "Estimated total: "
                                + txtCashierTotal.getText()
                                + "\n\n"
                                + "Stock will be updated after "
                                + "the sale is saved."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Complete Sale",
                        (dialog, which) -> completeSale()
                )
                .show();
    }

    // ---------------------------------------------------------
    // SAVE SALE AND OPEN RECEIPT
    // ---------------------------------------------------------

    private void completeSale() {
        if (isProcessingSale || cart.isEmpty()) {
            return;
        }

        String cashierEmail = sessionManager.getUserEmail();

        if (cashierEmail == null
                || cashierEmail.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Your login session could not be verified. "
                            + "Please log in again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Build the product ID -> quantity map for DatabaseHelper.
        Map<Integer, Integer> requestedItems =
                new LinkedHashMap<>();

        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            requestedItems.put(
                    entry.getKey(),
                    entry.getValue().quantity
            );
        }

        isProcessingSale = true;
        updateCartSummary();

        DatabaseHelper.SaleResult result;

        try {
            result = databaseHelper.completeSale(
                    cashierEmail.trim(),
                    requestedItems
            );
        } catch (Exception exception) {
            isProcessingSale = false;

            refreshProductsAndCart();

            new AlertDialog.Builder(this)
                    .setTitle("Sale Error")
                    .setMessage(
                            "An unexpected error occurred while "
                                    + "saving the sale. Your cart "
                                    + "has been kept."
                    )
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        if (result == null || !result.success) {
            isProcessingSale = false;

            refreshProductsAndCart();

            String message = result != null
                    && result.message != null
                    && !result.message.trim().isEmpty()
                    ? result.message
                    : "The sale could not be completed.";

            new AlertDialog.Builder(this)
                    .setTitle("Sale Not Completed")
                    .setMessage(
                            message
                                    + "\n\nYour cart has been kept. "
                                    + "Check product availability "
                                    + "and try again."
                    )
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        // Snapshot the cart before clearing it.
        int size = cart.size();

        String[] itemNames = new String[size];
        int[] quantities = new int[size];
        double[] prices = new double[size];

        int index = 0;

        for (CartItem item : cart.values()) {
            itemNames[index] = item.product.getName();
            quantities[index] = item.quantity;
            prices[index] = item.product.getPrice();
            index++;
        }

        String receiptNumber = String.format(
                Locale.US,
                "RCP-%d-%04d",
                System.currentTimeMillis(),
                result.saleId % 10000
        );

        String dateTime = new SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        Intent receiptIntent = new Intent(
                CashierSalesActivity.this,
                ReceiptActivity.class
        );

        receiptIntent.putExtra(
                "receipt_number",
                receiptNumber
        );

        receiptIntent.putExtra(
                "cashier",
                cashierEmail.trim()
        );

        receiptIntent.putExtra("date_time", dateTime);
        receiptIntent.putExtra("item_names", itemNames);
        receiptIntent.putExtra("quantities", quantities);
        receiptIntent.putExtra("prices", prices);
        receiptIntent.putExtra("subtotal", result.subtotal);

        receiptIntent.putExtra(
                "discount",
                Math.max(0.0, result.subtotal - result.total)
        );

        receiptIntent.putExtra("total", result.total);
        receiptIntent.putExtra("payment_method", "Cash");

        // The sale has been saved successfully. Clear the cart
        // and navigate to the receipt screen.
        cart.clear();
        isProcessingSale = false;

        startActivity(receiptIntent);
    }

    private String formatMoney(double amount) {
        return String.format(
                Locale.getDefault(),
                "ZMW %.2f",
                amount
        );
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
