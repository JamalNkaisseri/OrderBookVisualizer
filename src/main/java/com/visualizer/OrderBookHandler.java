package com.visualizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class OrderBookHandler implements WebSocketHandler {

    private final ObservableList<Order> bidOrders;
    private final ObservableList<Order> askOrders;

    // Maintain sorted order books
    // TreeMap keeps entries sorted by key
    private final TreeMap<BigDecimal, String> bids = new TreeMap<>(Collections.reverseOrder()); // Descending (highest first)
    private final TreeMap<BigDecimal, String> asks = new TreeMap<>(); // Ascending (lowest first)

    // Console-only constructor
    public OrderBookHandler() {
        this(null, null);
    }

    // JavaFX constructor
    public OrderBookHandler(ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;
    }

    @Override
    public void handleMessage(String json, ObjectMapper mapper) {
        try {
            OrderBookUpdate update = mapper.readValue(json, OrderBookUpdate.class);

            // Process bid updates
            for (List<String> entry : update.getBids()) {
                BigDecimal price = new BigDecimal(entry.get(0));
                String quantity = entry.get(1);

                if ("0.00000000".equals(quantity) || "0".equals(quantity)) {
                    // Remove this price level
                    bids.remove(price);
                } else {
                    // Add or update this price level
                    bids.put(price, quantity);
                }
            }

            // Process ask updates
            for (List<String> entry : update.getAsks()) {
                BigDecimal price = new BigDecimal(entry.get(0));
                String quantity = entry.get(1);

                if ("0.00000000".equals(quantity) || "0".equals(quantity)) {
                    // Remove this price level
                    asks.remove(price);
                } else {
                    // Add or update this price level
                    asks.put(price, quantity);
                }
            }

            if (bidOrders == null || askOrders == null) {
                // 👉 Console mode - show ACTUAL best bid/ask
                String bestBid = bids.isEmpty() ? "-" :
                        String.format("Bid: %s @ %s", bids.firstEntry().getValue(), bids.firstKey());

                String bestAsk = asks.isEmpty() ? "-" :
                        String.format("Ask: %s @ %s", asks.firstEntry().getValue(), asks.firstKey());

                System.out.printf("ORDERBOOK | %-50s | %-50s%n", bestBid, bestAsk);

            } else {
                // 👉 JavaFX mode - update tables with properly sorted data
                Platform.runLater(() -> {
                    // Convert to Order objects (top 10)
                    List<Order> topBids = bids.entrySet().stream()
                            .limit(10)
                            .map(entry -> new Order(entry.getKey().toString(), entry.getValue()))
                            .collect(Collectors.toList());

                    List<Order> topAsks = asks.entrySet().stream()
                            .limit(10)
                            .map(entry -> new Order(entry.getKey().toString(), entry.getValue()))
                            .collect(Collectors.toList());

                    bidOrders.setAll(topBids);
                    askOrders.setAll(topAsks);
                });
            }

        } catch (Exception e) {
            System.err.println("⚠️ Failed to parse order book JSON: " + e.getMessage());
        }
    }
}