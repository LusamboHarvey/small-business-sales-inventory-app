
package com.team2.zedstockpos;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team2.zedstockpos.model.Product;

import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private InventoryAdapter inventoryAdapter;
    private RecyclerView recyclerInventory;
    private TextView txtLowStockSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        databaseHelper = new DatabaseHelper(this);

        recyclerInventory = findViewById(R.id.recyclerInventory);
        txtLowStockSummary = findViewById(R.id.txtLowStockSummary);

        recyclerInventory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadInventory();
    }

    private void loadInventory() {
        List<Product> products = databaseHelper.getAllProducts();

        inventoryAdapter = new InventoryAdapter(
                products,
                databaseHelper,
                this::loadInventory
        );

        recyclerInventory.setAdapter(inventoryAdapter);

        int lowStockCount = 0;
        int outOfStockCount = 0;

        for (Product product : products) {
            int stock = product.getStockQuantity();

            if (stock == 0) {
                outOfStockCount++;
            } else if (stock <= 10) {
                lowStockCount++;
            }
        }

        txtLowStockSummary.setText(
                "Low stock: " + lowStockCount
                        + "  |  Out of stock: " + outOfStockCount
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && recyclerInventory != null) {
            loadInventory();
        }
    }
}
