package com.visualizer;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Enhanced WebSocket client with connection management capabilities.
 */
public class WebSocketClient extends WebSocketListener {
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
            System.out.println("⚠️ WebSocket already connected to " + url);
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
            System.out.println("🔌 Disconnected from " + url);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        isConnected = true;
        System.out.println("✅ Connected to " + url);
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
        System.out.println("⌛ Closed: " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        isConnected = false;
        System.err.println("⚠️ Error: " + t.getMessage());
    }

    // Getters
    public boolean isConnected() {
        return isConnected;
    }

    public String getUrl() {
        return url;
    }
}