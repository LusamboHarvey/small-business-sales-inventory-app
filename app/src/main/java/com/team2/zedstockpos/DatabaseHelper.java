
package com.team2.zedstockpos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.team2.zedstockpos.model.Product;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "zedstockpos.db";
    private static final int DATABASE_VERSION = 4;

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

    // Enable SQLite foreign-key constraints.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Create all database tables.
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createProductsTable =
                "CREATE TABLE products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "price REAL NOT NULL CHECK(price > 0), " +
                        "stock_quantity INTEGER NOT NULL " +
                        "CHECK(stock_quantity >= 0), " +
                        "image_path TEXT" +
                        ")";

        db.execSQL(createProductsTable);

        insertSampleProduct(
                db, "Sugar", "Groceries", 35.00, 20
        );

        insertSampleProduct(
                db, "Cooking Oil", "Household", 85.00, 12
        );

        insertSampleProduct(
                db, "Bathing Soap", "Personal Care", 18.50, 30
        );

        createUsersTable(db);
        createSalesTables(db);
    }

    // Create the users table.
    private void createUsersTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "email TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        "password_hash TEXT NOT NULL, " +
                        "password_salt TEXT NOT NULL, " +
                        "role TEXT NOT NULL " +
                        "CHECK(role IN ('ADMIN', 'CASHIER')), " +
                        "created_at INTEGER NOT NULL" +
                        ")"
        );
    }

    // Create the sales and sale-items tables.
    private void createSalesTables(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS sales (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "cashier_email TEXT NOT NULL, " +
                        "sale_date INTEGER NOT NULL, " +
                        "subtotal REAL NOT NULL CHECK(subtotal >= 0), " +
                        "discount REAL NOT NULL DEFAULT 0 " +
                        "CHECK(discount >= 0), " +
                        "total REAL NOT NULL CHECK(total >= 0)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS sale_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "sale_id INTEGER NOT NULL, " +
                        "product_id INTEGER, " +
                        "product_name TEXT NOT NULL, " +
                        "unit_price REAL NOT NULL " +
                        "CHECK(unit_price >= 0), " +
                        "quantity INTEGER NOT NULL CHECK(quantity > 0), " +
                        "line_total REAL NOT NULL " +
                        "CHECK(line_total >= 0), " +
                        "FOREIGN KEY(sale_id) REFERENCES sales(id) " +
                        "ON DELETE CASCADE, " +
                        "FOREIGN KEY(product_id) REFERENCES products(id) " +
                        "ON DELETE SET NULL" +
                        ")"
        );
    }

    // Insert sample products into a new database.
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

    // Insert a product.
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

    // Retrieve all products.
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
                        cursor.getColumnIndexOrThrow(COL_ID)
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME)
                );

                String category = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_CATEGORY)
                );

                double price = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_PRICE)
                );

                int stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_STOCK)
                );

                String imagePath = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)
                );

                products.add(
                        new Product(
                                id,
                                name,
                                category,
                                price,
                                stock,
                                imagePath
                        )
                );
            }
        }

        return products;
    }

    // Update a product.
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
                new String[]{
                        String.valueOf(product.getId())
                }
        );
    }

    // Delete a product.
    public int deleteProduct(int productId) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_PRODUCTS,
                COL_ID + " = ?",
                new String[]{
                        String.valueOf(productId)
                }
        );
    }

    // Update stock quantity.
    public boolean updateStockQuantity(
            int productId,
            int newQuantity) {

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
                new String[]{
                        String.valueOf(productId)
                }
        );

        return rowsUpdated > 0;
    }

    // Migrate existing databases without deleting products or users.
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE products ADD COLUMN image_path TEXT"
            );
        }

        if (oldVersion < 3) {
            createUsersTable(db);
        }

        if (oldVersion < 4) {
            createSalesTables(db);
        }
    }

    // Generate a secure password salt.
    private String generateSalt() {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        return Base64.encodeToString(
                salt,
                Base64.NO_WRAP
        );
    }

    // Hash a password using PBKDF2.
    private String hashPassword(
            String password,
            String saltString) {

        try {
            byte[] salt = Base64.decode(
                    saltString,
                    Base64.NO_WRAP
            );

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    120000,
                    256
            );

            try {
                SecretKeyFactory factory =
                        SecretKeyFactory.getInstance(
                                "PBKDF2WithHmacSHA256"
                        );

                byte[] hash = factory
                        .generateSecret(spec)
                        .getEncoded();

                return Base64.encodeToString(
                        hash,
                        Base64.NO_WRAP
                );

            } finally {
                spec.clearPassword();
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to secure the password.",
                    e
            );
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

        long result = getWritableDatabase().insert(
                "users",
                null,
                values
        );

        return result != -1;
    }

    // Authenticate a registered user.
    public boolean authenticateUser(
            String email,
            String password,
            String requiredRole) {

        if (email == null
                || password == null
                || requiredRole == null) {

            return false;
        }

        try (Cursor cursor = getReadableDatabase().query(
                "users",
                new String[]{
                        "password_hash",
                        "password_salt",
                        "role"
                },
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

            String enteredHash = hashPassword(
                    password,
                    storedSalt
            );

            return MessageDigest.isEqual(
                    Base64.decode(
                            storedHash,
                            Base64.NO_WRAP
                    ),
                    Base64.decode(
                            enteredHash,
                            Base64.NO_WRAP
                    )
            );

        } catch (Exception e) {
            return false;
        }
    }

    // Retrieve all cashier names and email addresses.
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

                cashiers.add(new String[]{
                        name,
                        email
                });
            }
        }

        return cashiers;
    }

    // Result returned after a checkout attempt.
    public static class SaleResult {

        public final boolean success;
        public final long saleId;
        public final double subtotal;
        public final double total;
        public final String message;

        public SaleResult(
                boolean success,
                long saleId,
                double subtotal,
                double total,
                String message) {

            this.success = success;
            this.saleId = saleId;
            this.subtotal = subtotal;
            this.total = total;
            this.message = message;
        }
    }

    // Internal representation of a product being sold.
    private static class SaleLine {

        int productId;
        String productName;
        double unitPrice;
        int quantity;
        int stockBefore;

        SaleLine(
                int productId,
                String productName,
                double unitPrice,
                int quantity,
                int stockBefore) {

            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.stockBefore = stockBefore;
        }
    }

    // Complete a sale and deduct inventory atomically.
    public SaleResult completeSale(
            String cashierEmail,
            Map<Integer, Integer> requestedItems) {

        if (cashierEmail == null
                || cashierEmail.trim().isEmpty()
                || requestedItems == null
                || requestedItems.isEmpty()) {

            return new SaleResult(
                    false,
                    -1,
                    0,
                    0,
                    "The cart is empty or the cashier is not identified."
            );
        }

        SQLiteDatabase db = getWritableDatabase();

        double subtotal = 0.0;
        double discount = 0.0;

        db.beginTransaction();

        try {
            List<SaleLine> lines = new ArrayList<>();

            // Validate the requested items against current stock.
            for (Map.Entry<Integer, Integer> entry
                    : requestedItems.entrySet()) {

                int productId = entry.getKey();
                int quantity = entry.getValue();

                if (quantity <= 0) {
                    return new SaleResult(
                            false,
                            -1,
                            0,
                            0,
                            "A product has an invalid quantity."
                    );
                }

                try (Cursor cursor = db.query(
                        TABLE_PRODUCTS,
                        new String[]{
                                COL_ID,
                                COL_NAME,
                                COL_PRICE,
                                COL_STOCK
                        },
                        COL_ID + " = ?",
                        new String[]{
                                String.valueOf(productId)
                        },
                        null,
                        null,
                        null)) {

                    if (!cursor.moveToFirst()) {
                        return new SaleResult(
                                false,
                                -1,
                                0,
                                0,
                                "A product in the cart no longer exists."
                        );
                    }

                    String productName = cursor.getString(1);
                    double price = cursor.getDouble(2);
                    int stock = cursor.getInt(3);

                    if (quantity > stock) {
                        return new SaleResult(
                                false,
                                -1,
                                0,
                                0,
                                "Insufficient stock for " + productName
                                        + ". Available: " + stock
                        );
                    }

                    lines.add(
                            new SaleLine(
                                    productId,
                                    productName,
                                    price,
                                    quantity,
                                    stock
                            )
                    );

                    subtotal += price * quantity;
                }
            }

            // Discounts are not enabled yet.
            double total = subtotal - discount;

            // Save the sale record.
            ContentValues saleValues = new ContentValues();
            saleValues.put(
                    "cashier_email",
                    cashierEmail.trim()
            );
            saleValues.put(
                    "sale_date",
                    System.currentTimeMillis()
            );
            saleValues.put("subtotal", subtotal);
            saleValues.put("discount", discount);
            saleValues.put("total", total);

            long saleId = db.insert(
                    "sales",
                    null,
                    saleValues
            );

            if (saleId == -1) {
                return new SaleResult(
                        false,
                        -1,
                        0,
                        0,
                        "The sale could not be saved."
                );
            }

            // Save each sold item and deduct its stock.
            for (SaleLine line : lines) {

                ContentValues itemValues = new ContentValues();
                itemValues.put("sale_id", saleId);
                itemValues.put("product_id", line.productId);
                itemValues.put(
                        "product_name",
                        line.productName
                );
                itemValues.put("unit_price", line.unitPrice);
                itemValues.put("quantity", line.quantity);
                itemValues.put(
                        "line_total",
                        line.unitPrice * line.quantity
                );

                long itemId = db.insert(
                        "sale_items",
                        null,
                        itemValues
                );

                if (itemId == -1) {
                    return new SaleResult(
                            false,
                            -1,
                            0,
                            0,
                            "A sale item could not be saved."
                    );
                }

                ContentValues stockValues = new ContentValues();
                stockValues.put(
                        COL_STOCK,
                        line.stockBefore - line.quantity
                );

                int rowsUpdated = db.update(
                        TABLE_PRODUCTS,
                        stockValues,
                        COL_ID + " = ? AND " + COL_STOCK + " = ?",
                        new String[]{
                                String.valueOf(line.productId),
                                String.valueOf(line.stockBefore)
                        }
                );

                if (rowsUpdated != 1) {
                    return new SaleResult(
                            false,
                            -1,
                            0,
                            0,
                            "Stock changed during checkout. Please try again."
                    );
                }
            }

            // Commit only when every operation has succeeded.
            db.setTransactionSuccessful();

            return new SaleResult(
                    true,
                    saleId,
                    subtotal,
                    total,
                    "Sale completed successfully."
            );

        } catch (Exception e) {

            return new SaleResult(
                    false,
                    -1,
                    0,
                    0,
                    "The sale could not be completed. Please try again."
            );

        } finally {
            // Unsuccessful transactions are rolled back here.
            db.endTransaction();
        }
    }
}
