package com.visualizer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBookUpdate {

    @JsonProperty("e")
    private String e;  // Event type

    @JsonProperty("E")
    private long E;    // Event time

    @JsonProperty("s")
    private String s;  // Symbol

    @JsonProperty("U")
    private long U;    // First update ID

    @JsonProperty("u")
    private long u;    // Final update ID

    @JsonProperty("b")
    private List<List<String>> bids;  // 👈 map "b" to bids

    @JsonProperty("a")
    private List<List<String>> asks;  // 👈 map "a" to asks

    // Getters
    public List<List<String>> getBids() { return bids; }
    public List<List<String>> getAsks() { return asks; }

    public String getE() { return e; }
    public long getEtime() { return E; }
    public String getS() { return s; }
    public long getU() { return U; }
    public long getUfinal() { return u; }

    @Override
    public String toString() {
        return "OrderBookUpdate{" +
                "event='" + e + '\'' +
                ", symbol='" + s + '\'' +
                ", bids=" + bids +
                ", asks=" + asks +
                '}';
    }
}
