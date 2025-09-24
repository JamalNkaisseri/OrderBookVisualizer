package com.visualizer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single trade event from Binance's WebSocket stream.
 *
 * Binance sends trade data as JSON like:
 * {
 *   "e": "trade",        // event type
 *   "E": 1672515782136,  // event time
 *   "s": "BTCUSDT",      // symbol
 *   "t": 12345,          // trade ID
 *   "p": "0.001",        // price
 *   "q": "100",          // quantity
 *   "T": 1672515782136,  // trade time
 *   "m": true,           // is the buyer the market maker?
 *   ...
 * }
 *
 * This class maps the important fields using Jackson annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 Ignore extra fields Binance may add
public class Trade {

    // ✅ "e": event type (e.g. "trade")
    @JsonProperty("e")
    private String eventType;

    // ✅ "E": event time (epoch ms)
    @JsonProperty("E")
    private long eventTime;

    // ✅ "s": symbol (e.g. "BTCUSDT")
    @JsonProperty("s")
    private String symbol;

    // ✅ "t": trade ID (unique identifier for this trade)
    @JsonProperty("t")
    private long tradeId;

    // ✅ "p": price (as a string to preserve precision)
    @JsonProperty("p")
    private String price;

    // ✅ "q": quantity (also a string to preserve precision)
    @JsonProperty("q")
    private String quantity;

    // ✅ "T": trade execution time (epoch ms)
    @JsonProperty("T")
    private long tradeTime;

    // ✅ "m": was the buyer the market maker? (true/false)
    @JsonProperty("m")
    private boolean isMarketMaker;

    // ---------- Getters ----------
    public String getEventType() { return eventType; }
    public long getEventTime() { return eventTime; }
    public String getSymbol() { return symbol; }
    public long getTradeId() { return tradeId; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public long getTradeTime() { return tradeTime; }
    public boolean isMarketMaker() { return isMarketMaker; }

    // ✅ A nice formatted string for logging/debugging
    @Override
    public String toString() {
        return String.format(
                "Trade [symbol=%s, price=%s, qty=%s, time=%d, maker=%s]",
                symbol, price, quantity, tradeTime, isMarketMaker
        );
    }
}
