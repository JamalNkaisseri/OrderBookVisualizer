package com.visualizer;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced WebSocket client with connection management capabilities.
 */
public class WebSocketClient extends WebSocketListener {
    private static final Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final String url;
    private final WebSocketHandler handler;
    private WebSocket webSocket;
    private boolean isConnected = false;

    public WebSocketClient(String url, WebSocketHandler handler) {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
        this.url = url;
        this.handler = handler;
    }

    /**
     * Starts the WebSocket connection.
     */
    public void start() {
        if (isConnected) {
            LOGGER.fine("⚠️ WebSocket already connected to " + url);
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    /**
     * Stops the WebSocket connection.
     */
    public void stop() {
        if (webSocket != null && isConnected) {
            webSocket.close(1000, "Client closing connection");
            isConnected = false;
            LOGGER.info("🔌 Disconnected from " + url);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        isConnected = true;
        LOGGER.info("✅ Connected to " + url);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (isConnected) {
            handler.handleMessage(text, mapper);
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        isConnected = false;
        LOGGER.warning("Connection closed: " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        isConnected = false;
        LOGGER.log(Level.SEVERE, "WebSocket failure", t);
    }

    // Getters
    public boolean isConnected() {
        return isConnected;
    }

    public String getUrl() {
        return url;
    }
}
