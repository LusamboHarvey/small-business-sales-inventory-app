package com.team2.zedstockpos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.team2.zedstockpos.model.Product;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "zedstockpos.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PRODUCTS = "products";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_CATEGORY = "category";
    private static final String COL_PRICE = "price";
    private static final String COL_STOCK = "stock_quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PRODUCTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_PRICE + " REAL NOT NULL CHECK (" + COL_PRICE + " > 0), "
                + COL_STOCK + " INTEGER NOT NULL CHECK (" + COL_STOCK + " >= 0)"
                + ")";

        db.execSQL(createTable);

        insertSampleProduct(db, "Sugar", "Groceries", 35.00, 20);
        insertSampleProduct(db, "Cooking Oil", "Household", 85.00, 12);
        insertSampleProduct(db, "Bathing Soap", "Personal Care", 18.50, 30);
    }

    private void insertSampleProduct(
            SQLiteDatabase db,
            String name,
            String category,
            double price,
            int stock) {

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_CATEGORY, category);
        values.put(COL_PRICE, price);
        values.put(COL_STOCK, stock);

        db.insert(TABLE_PRODUCTS, null, values);
    }

    public long insertProduct(Product product) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, product.getName());
        values.put(COL_CATEGORY, product.getCategory());
        values.put(COL_PRICE, product.getPrice());
        values.put(COL_STOCK, product.getStockQuantity());

        return db.insert(TABLE_PRODUCTS, null, values);
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " ASC")) {

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME));
                String category = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_CATEGORY));
                double price = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_PRICE));
                int stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_STOCK));

                products.add(new Product(
                        id, name, category, price, stock));
            }
        }

        return products;
    }


    public int updateProduct(Product product) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, product.getName());
        values.put(COL_CATEGORY, product.getCategory());
        values.put(COL_PRICE, product.getPrice());
        values.put(COL_STOCK, product.getStockQuantity());

        return db.update(
                TABLE_PRODUCTS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(product.getId())}
        );
    }

    public int deleteProduct(int productId) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_PRODUCTS,
                COL_ID + " = ?",
                new String[]{String.valueOf(productId)}
        );
    }


    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {
        // Add database migration logic here when the schema changes.
    }
}