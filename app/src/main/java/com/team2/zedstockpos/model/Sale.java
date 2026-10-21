
package com.team2.zedstockpos.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Sale {

    private long id;
    private String clientSaleId;
    private String cashierEmail;
    private long saleDate;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private List<SaleItem> items = new ArrayList<>();

    public Sale(
            long id,
            String clientSaleId,
            String cashierEmail,
            long saleDate,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal total,
            List<SaleItem> items
    ) {
        this.id = id;
        this.clientSaleId = clientSaleId;
        this.cashierEmail = cashierEmail;
        this.saleDate = saleDate;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;

        if (items != null) {
            this.items.addAll(items);
        }
    }

    public long getId() {
        return id;
    }

    public String getClientSaleId() {
        return clientSaleId;
    }

    public String getCashierEmail() {
        return cashierEmail;
    }

    public long getSaleDate() {
        return saleDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<SaleItem> getItems() {
        return new ArrayList<>(items);
    }
}
