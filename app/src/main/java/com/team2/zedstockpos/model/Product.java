
package com.team2.zedstockpos.model;

public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;
    private String imagePath;

    // Keep the existing constructor for compatibility
    public Product(int id, String name, String category,
                   double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imagePath = null;
    }

    // Constructor with an image path
    public Product(int id, String name, String category,
                   double price, int stockQuantity, String imagePath) {
        this(id, name, category, price, stockQuantity);
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
