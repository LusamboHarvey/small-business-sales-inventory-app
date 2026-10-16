
package com.team2.zedstockpos;

import android.util.Base64;

import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_PRODUCTS = "products";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_CATEGORY = "category";
    private static final String COL_PRICE = "price";
    private static final String COL_STOCK = "stock_quantity";
    private static final String COL_IMAGE_PATH = "image_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProductsTable =
                "CREATE TABLE products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "price REAL NOT NULL CHECK(price > 0), " +
                        "stock_quantity INTEGER NOT NULL CHECK(stock_quantity >= 0), " +
                        "image_path TEXT" +
                        ")";

        db.execSQL(createProductsTable);

        insertSampleProduct(db, "Sugar", "Groceries", 35.00, 20);
        insertSampleProduct(db, "Cooking Oil", "Household", 85.00, 12);
        insertSampleProduct(db, "Bathing Soap", "Personal Care", 18.50, 30);

        createUsersTable(db);
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "email TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        "password_hash TEXT NOT NULL, " +
                        "password_salt TEXT NOT NULL, " +
                        "role TEXT NOT NULL CHECK(role IN ('ADMIN', 'CASHIER')), " +
                        "created_at INTEGER NOT NULL" +
                        ")"
        );
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
        values.put(COL_IMAGE_PATH, product.getImagePath());
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
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_ID));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME));

                String category = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_CATEGORY));

                double price = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_PRICE));

                int stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_STOCK));

                String imagePath = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_IMAGE_PATH));

                products.add(new Product(
                        id, name, category, price, stock, imagePath));
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
        values.put(COL_IMAGE_PATH, product.getImagePath());

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
            SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE products ADD COLUMN image_path TEXT"
            );
        }

        if (oldVersion < 3) {
            createUsersTable(db);
        }
    }

    public boolean updateStockQuantity(int productId, int newQuantity) {
        if (newQuantity < 0) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_STOCK, newQuantity);

        int rowsUpdated = db.update(
                TABLE_PRODUCTS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(productId)}
        );

        return rowsUpdated > 0;
    }

    // Generate a secure password salt.
    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    // Hash a password using PBKDF2.
    private String hashPassword(String password, String saltString) {
        try {
            byte[] salt = Base64.decode(saltString, Base64.NO_WRAP);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    120000,
                    256
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();

            return Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to secure the password.", e);
        }
    }

    // Create a cashier account.
    public boolean createCashier(
            String name,
            String email,
            String password) {

        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.length() < 6) {
            return false;
        }

        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);

        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("email", email.trim());
        values.put("password_hash", passwordHash);
        values.put("password_salt", salt);
        values.put("role", "CASHIER");
        values.put("created_at", System.currentTimeMillis());

        long result = getWritableDatabase().insert("users", null, values);

        return result != -1;
    }

    // Authenticate a registered user.
    public boolean authenticateUser(
            String email,
            String password,
            String requiredRole) {

        if (email == null || password == null || requiredRole == null) {
            return false;
        }

        try (Cursor cursor = getReadableDatabase().query(
                "users",
                new String[]{"password_hash", "password_salt", "role"},
                "email = ? COLLATE NOCASE",
                new String[]{email.trim()},
                null,
                null,
                null)) {

            if (!cursor.moveToFirst()) {
                return false;
            }

            String storedHash = cursor.getString(0);
            String storedSalt = cursor.getString(1);
            String storedRole = cursor.getString(2);

            if (!requiredRole.equalsIgnoreCase(storedRole)) {
                return false;
            }

            String enteredHash = hashPassword(password, storedSalt);

            return java.security.MessageDigest.isEqual(
                    Base64.decode(storedHash, Base64.NO_WRAP),
                    Base64.decode(enteredHash, Base64.NO_WRAP)
            );

        } catch (Exception e) {
            return false;
        }
    }


    public List<String[]> getAllCashiers() {
        List<String[]> cashiers = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                "users",
                new String[]{"name", "email"},
                "role = ?",
                new String[]{"CASHIER"},
                null,
                null,
                "name COLLATE NOCASE ASC")) {

            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String email = cursor.getString(1);

                cashiers.add(new String[]{name, email});
            }
        }

        return cashiers;
    }

}
