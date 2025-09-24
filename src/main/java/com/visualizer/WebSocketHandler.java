package com.visualizer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for handling messages from Binance WebSocket.
 */
public interface WebSocketHandler {
    void handleMessage(String json, ObjectMapper mapper);
}
