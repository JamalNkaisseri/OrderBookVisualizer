package com.visualizer;

public class Order {
    private final String price;
    private final String quantity;

    public Order(String price, String quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
}
