package com.visualizer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TradeHandler implements WebSocketHandler {
    @Override
    public void handleMessage(String json, ObjectMapper mapper) {
        try {
            Trade trade = mapper.readValue(json, Trade.class);

            // Format trade info (right side of terminal)
            String output = String.format(
                    "TRADE     | Price: %-10s Qty: %-10s Maker: %s",
                    trade.getPrice(), trade.getQuantity(), trade.isMarketMaker()
            );

            // Print aligned
            System.out.printf("%100s%n", output);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to parse trade JSON: " + e.getMessage());
        }
    }
}
