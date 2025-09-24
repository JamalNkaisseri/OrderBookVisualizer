package com.visualizer;

public class Main {
    public static void main(String[] args) {
        // Trade stream (executed orders)
        String tradeStream = "wss://stream.binance.com:9443/ws/btcusdt@trade";
        WebSocketClient tradeClient = new WebSocketClient(tradeStream, new TradeHandler());

        // Order book stream (bids/asks)
        String orderBookStream = "wss://stream.binance.com:9443/ws/btcusdt@depth";
        WebSocketClient orderBookClient = new WebSocketClient(orderBookStream, new OrderBookHandler());

        // Start both
        tradeClient.start();
        orderBookClient.start();

        // Header
        System.out.println("🚀 BTC/USDT LIVE FEED");
        System.out.println("-------------------------------------------------------------");
        System.out.println("ORDERBOOK (Best Bid / Best Ask)                  ||                         TRADES");
        System.out.println("-------------------------------------------------------------");
    }
}
