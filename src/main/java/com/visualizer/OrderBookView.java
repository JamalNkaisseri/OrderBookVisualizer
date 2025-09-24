package com.visualizer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class OrderBookView extends VBox {

    private final ObservableList<Order> bidOrders;
    private final ObservableList<Order> askOrders;
    private TradingPair currentTradingPair;
    private Label headerLabel;
    private ComboBox<TradingPair> pairSelector;
    private WebSocketClient currentWebSocketClient;
    private DepthChartView depthChartView;
    private MarketInfoWidget marketInfoWidget;
    private VolatilityWidget volatilityWidget;

    // Callback interface for notifying about pair changes
    public interface PairChangeListener {
        void onPairChanged(TradingPair newPair, ObservableList<Order> bidOrders, ObservableList<Order> askOrders);
    }

    private PairChangeListener pairChangeListener;

    public OrderBookView(TradingPair initialTradingPair) {
        this.currentTradingPair = initialTradingPair;
        this.bidOrders = FXCollections.observableArrayList();
        this.askOrders = FXCollections.observableArrayList();
        this.depthChartView = new DepthChartView();
        this.marketInfoWidget = new MarketInfoWidget();
        this.volatilityWidget = new VolatilityWidget();

        initializeView();
        bindDepthChart();
        bindMarketInfo();
        bindVolatilityWidget();
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

        // Create order book tables
        OrderBookTableView bidsTable = new OrderBookTableView(bidOrders, true);
        OrderBookTableView asksTable = new OrderBookTableView(askOrders, false);

        // Create order book section (left side)
        VBox orderBookSection = createOrderBookSection(bidsTable, asksTable);

        // Create widgets section (top right) - compact layout
        HBox widgetsSection = new HBox(15);
        widgetsSection.setAlignment(Pos.TOP_CENTER);
        widgetsSection.getChildren().addAll(marketInfoWidget, volatilityWidget);

        // Create right side with widgets on top and large depth chart below
        VBox rightSide = new VBox(20);
        rightSide.setAlignment(Pos.TOP_CENTER);
        rightSide.getChildren().addAll(widgetsSection, depthChartView);

        // Create main content area
        HBox mainContent = new HBox(30);
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.getChildren().addAll(orderBookSection, rightSide);

        // Make the right side expand to fill available space
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        // Make the depth chart expand within the right side
        VBox.setVgrow(depthChartView, Priority.ALWAYS);

        // Main layout
        setPadding(new Insets(25));
        setAlignment(Pos.TOP_CENTER);
        setSpacing(25);
        getChildren().addAll(headerSection, mainContent);
    }

    private VBox createOrderBookSection(OrderBookTableView bidsTable, OrderBookTableView asksTable) {
        // Order book header
        Label orderBookHeader = new Label("📈 Order Book");
        orderBookHeader.getStyleClass().add("section-header");

        // Create sections for bids and asks
        VBox bidSection = createTableSection("BIDS", "bid-label", bidsTable);
        VBox askSection = createTableSection("ASKS", "ask-label", asksTable);

        // Layout bids and asks vertically (bids on top, asks below)
        VBox tablesContainer = new VBox(20);
        tablesContainer.getChildren().addAll(bidSection, askSection);
        tablesContainer.setAlignment(Pos.CENTER);

        // Complete order book section
        VBox orderBookSection = new VBox(15);
        orderBookSection.getChildren().addAll(orderBookHeader, tablesContainer);
        orderBookSection.setAlignment(Pos.TOP_CENTER);
        orderBookSection.setPrefWidth(400); // Fixed width for order book

        return orderBookSection;
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

        // Clear depth chart, market info, and volatility widget
        depthChartView.clear();
        marketInfoWidget.clear();
        volatilityWidget.clear();

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

        VBox section = new VBox(10);
        section.getChildren().addAll(label, table);
        section.setAlignment(Pos.CENTER);

        return section;
    }

    private void bindDepthChart() {
        // Bind the depth chart to order book data
        depthChartView.bindToOrderBook(bidOrders, askOrders);
    }

    private void bindMarketInfo() {
        // Bind the market info widget to order book data
        marketInfoWidget.bindToOrderBook(bidOrders, askOrders);
    }

    private void bindVolatilityWidget() {
        // Bind the volatility widget to order book data
        volatilityWidget.bindToOrderBook(bidOrders, askOrders);
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

    // Cleanup method to stop all widgets when view is destroyed
    public void cleanup() {
        if (volatilityWidget != null) {
            volatilityWidget.stop();
        }
        if (currentWebSocketClient != null) {
            // Add proper WebSocket cleanup here when available
        }
    }
}