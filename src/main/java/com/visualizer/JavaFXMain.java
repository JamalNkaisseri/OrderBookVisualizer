package com.visualizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class JavaFXMain extends Application {

    private static final Logger LOGGER = Logger.getLogger(JavaFXMain.class.getName());

    private OrderBookView orderBookView;
    private WebSocketClient currentWebSocketClient;

    @Override
    public void start(Stage stage) {
        // Create initial trading pair
        TradingPair initialPair = new TradingPair("BTC", "USDT");

        // Create the order book view
        orderBookView = new OrderBookView(initialPair);

        // Set up pair change listener
        orderBookView.setPairChangeListener(this::onPairChanged);

        // Create scene
        Scene scene = new Scene(orderBookView, 1200, 900);
        scene.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());

        // Setup stage
        stage.setTitle("Order Book Visualizer");
        stage.setScene(scene);

        // Handle window close event to cleanup connections
        stage.setOnCloseRequest(event -> {
            stopCurrentWebSocketConnection();
            Platform.exit();
            System.exit(0);
        });

        stage.show();

        // Start initial WebSocket connection
        startWebSocketConnection(initialPair);
    }

    private void onPairChanged(TradingPair newPair, ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        // Stop current WebSocket connection
        stopCurrentWebSocketConnection();

        // Small delay to ensure clean disconnection
        Platform.runLater(() -> {
            try {
                Thread.sleep(100); // Brief pause for clean disconnection
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Start new WebSocket connection for the selected pair
            startWebSocketConnection(newPair);

            // Log instead of printing to console
            LOGGER.info("🔄 Switched to trading pair: " + newPair.getDisplayName());
        });
    }

    private void startWebSocketConnection(TradingPair tradingPair) {
        Platform.runLater(() -> {
            currentWebSocketClient = new WebSocketClient(
                    tradingPair.getWebSocketUrl(),
                    new OrderBookHandler(
                            orderBookView.getBidOrders(),
                            orderBookView.getAskOrders()
                    )
            );

            // Set the client reference in the view
            orderBookView.setCurrentWebSocketClient(currentWebSocketClient);

            currentWebSocketClient.start();
        });
    }

    private void stopCurrentWebSocketConnection() {
        if (currentWebSocketClient != null && currentWebSocketClient.isConnected()) {
            currentWebSocketClient.stop();
            currentWebSocketClient = null;
        }
    }

    public static void main(String[] args) {
        // Disable logs for shipping
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        rootLogger.setLevel(java.util.logging.Level.OFF);

        launch();
    }
}
