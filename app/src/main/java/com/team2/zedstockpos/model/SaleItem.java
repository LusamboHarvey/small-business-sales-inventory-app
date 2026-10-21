
package com.team2.zedstockpos.model;

import java.math.BigDecimal;

public class SaleItem {

    private long id;
    private long saleId;
    private Integer productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public SaleItem(
            long id,
            long saleId,
            Integer productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public long getId() {
        return id;
    }

    public long getSaleId() {
        return saleId;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}

