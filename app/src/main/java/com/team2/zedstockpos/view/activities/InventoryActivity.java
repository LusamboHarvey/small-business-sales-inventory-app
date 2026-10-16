package com.team2.zedstockpos;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerInventory;
    private TextView txtLowStockSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerInventory = findViewById(R.id.recyclerInventory);
        txtLowStockSummary = findViewById(R.id.txtLowStockSummary);

        recyclerInventory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        txtLowStockSummary.setText(
                "Inventory monitoring is ready."
        );
    }
}
