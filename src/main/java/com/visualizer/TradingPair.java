package com.visualizer;

public class TradingPair {
    private final String baseAsset;
    private final String quoteAsset;
    private final String symbol;
    private final String displayName;

    public TradingPair(String baseAsset, String quoteAsset) {
        this.baseAsset = baseAsset.toUpperCase();
        this.quoteAsset = quoteAsset.toUpperCase();
        this.symbol = (baseAsset + quoteAsset).toLowerCase();
        this.displayName = baseAsset.toUpperCase() + "/" + quoteAsset.toUpperCase();
    }

    public String getBaseAsset() { return baseAsset; }
    public String getQuoteAsset() { return quoteAsset; }
    public String getSymbol() { return symbol; }
    public String getDisplayName() { return displayName; }

    public String getWebSocketUrl() {
        return "wss://stream.binance.com:9443/ws/" + symbol + "@depth";
    }

    @Override
    public String toString() {
        return displayName;
    }
}