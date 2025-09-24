package com.visualizer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OrderBookView extends VBox {

    private final ObservableList<Order> bidOrders;
    private final ObservableList<Order> askOrders;
    private TradingPair currentTradingPair;
    private Label headerLabel;
    private ComboBox<TradingPair> pairSelector;
    private WebSocketClient currentWebSocketClient;

    // Callback interface for notifying about pair changes
    public interface PairChangeListener {
        void onPairChanged(TradingPair newPair, ObservableList<Order> bidOrders, ObservableList<Order> askOrders);
    }

    private PairChangeListener pairChangeListener;

    public OrderBookView(TradingPair initialTradingPair) {
        this.currentTradingPair = initialTradingPair;
        this.bidOrders = FXCollections.observableArrayList();
        this.askOrders = FXCollections.observableArrayList();

        initializeView();
    }

    private void initializeView() {
        // Pair selector dropdown
        pairSelector = createPairSelector();

        // Main header
        headerLabel = new Label("₿ " + currentTradingPair.getDisplayName() + " Live Order Book");
        headerLabel.getStyleClass().add("main-header");
        headerLabel.setAlignment(Pos.CENTER);

        // Header section with selector and title
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.getChildren().addAll(pairSelector, headerLabel);

        // Create tables
        OrderBookTableView bidsTable = new OrderBookTableView(bidOrders, true);
        OrderBookTableView asksTable = new OrderBookTableView(askOrders, false);

        // Create sections
        VBox bidSection = createTableSection("BIDS", "bid-label", bidsTable);
        VBox askSection = createTableSection("ASKS", "ask-label", asksTable);

        // Layout tables horizontally
        HBox tablesContainer = new HBox(30);
        tablesContainer.getChildren().addAll(bidSection, askSection);
        tablesContainer.setAlignment(Pos.CENTER);

        // Main layout
        setPadding(new Insets(25));
        setAlignment(Pos.TOP_CENTER);
        setSpacing(25);
        getChildren().addAll(headerSection, tablesContainer);
    }

    private ComboBox<TradingPair> createPairSelector() {
        // Popular trading pairs
        ObservableList<TradingPair> tradingPairs = FXCollections.observableArrayList(
                new TradingPair("BTC", "USDT"),
                new TradingPair("ETH", "USDT"),
                new TradingPair("BNB", "USDT"),
                new TradingPair("ADA", "USDT"),
                new TradingPair("SOL", "USDT"),
                new TradingPair("XRP", "USDT"),
                new TradingPair("DOT", "USDT"),
                new TradingPair("AVAX", "USDT"),
                new TradingPair("MATIC", "USDT"),
                new TradingPair("LINK", "USDT")
        );

        ComboBox<TradingPair> selector = new ComboBox<>(tradingPairs);
        selector.setValue(currentTradingPair);
        selector.setPromptText("Select Trading Pair");
        selector.setPrefWidth(200);
        selector.getStyleClass().add("pair-selector");

        // Handle pair selection changes
        selector.setOnAction(event -> {
            TradingPair selectedPair = selector.getValue();
            if (selectedPair != null && !selectedPair.equals(currentTradingPair)) {
                changeTradingPair(selectedPair);
            }
        });

        return selector;
    }

    private void changeTradingPair(TradingPair newPair) {
        // Stop current WebSocket connection
        if (currentWebSocketClient != null) {
            // Note: WebSocketClient doesn't have a stop method in your current implementation
            // You might want to add one or handle this differently
        }

        // Clear existing data
        bidOrders.clear();
        askOrders.clear();

        // Update current pair
        currentTradingPair = newPair;

        // Update header
        headerLabel.setText("₿ " + newPair.getDisplayName() + " Live Order Book");

        // Notify listener about the change
        if (pairChangeListener != null) {
            pairChangeListener.onPairChanged(newPair, bidOrders, askOrders);
        }
    }

    private VBox createTableSection(String labelText, String labelStyleClass, OrderBookTableView table) {
        Label label = new Label(labelText);
        label.getStyleClass().add(labelStyleClass);

        VBox section = new VBox(15);
        section.getChildren().addAll(label, table);
        section.setAlignment(Pos.CENTER);

        return section;
    }

    // Getters
    public ObservableList<Order> getBidOrders() { return bidOrders; }
    public ObservableList<Order> getAskOrders() { return askOrders; }
    public TradingPair getTradingPair() { return currentTradingPair; }

    // Setter for pair change listener
    public void setPairChangeListener(PairChangeListener listener) {
        this.pairChangeListener = listener;
    }

    // Method to set current WebSocket client (for cleanup)
    public void setCurrentWebSocketClient(WebSocketClient client) {
        this.currentWebSocketClient = client;
    }
}