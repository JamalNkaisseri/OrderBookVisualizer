package com.visualizer;

import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MarketInfoWidget extends VBox {

    private final Label bestBidLabel;
    private final Label bestAskLabel;
    private final Label spreadLabel;
    private final Label spreadPercentLabel;
    private final Label midPriceLabel;

    private final DecimalFormat priceFormat;
    private final DecimalFormat percentFormat;

    private BigDecimal currentBestBid = BigDecimal.ZERO;
    private BigDecimal currentBestAsk = BigDecimal.ZERO;

    public MarketInfoWidget() {
        // Initialize formatters
        priceFormat = new DecimalFormat("#,##0.00");
        percentFormat = new DecimalFormat("0.000%");

        // Initialize labels
        bestBidLabel = createValueLabel("--", "best-bid-value");
        bestAskLabel = createValueLabel("--", "best-ask-value");
        spreadLabel = createValueLabel("--", "spread-value");
        spreadPercentLabel = createValueLabel("--", "spread-percent-value");
        midPriceLabel = createValueLabel("--", "mid-price-value");

        setupLayout();
    }

    private void setupLayout() {
        // Main header
        Label headerLabel = new Label("📊 Market Info");
        headerLabel.getStyleClass().add("market-info-header");

        // Create info grid
        GridPane infoGrid = createInfoGrid();

        // Container for the grid with background
        VBox gridContainer = new VBox();
        gridContainer.getChildren().add(infoGrid);
        gridContainer.getStyleClass().add("market-info-container");
        gridContainer.setPadding(new Insets(15));

        // Main layout
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPrefWidth(350);
        getChildren().addAll(headerLabel, gridContainer);
        getStyleClass().add("market-info-widget");
    }

    private GridPane createInfoGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(12);

        // Row 0: Best Bid and Best Ask
        grid.add(createInfoBox("🟢 BEST BID", bestBidLabel), 0, 0);
        grid.add(createInfoBox("🔴 BEST ASK", bestAskLabel), 1, 0);

        // Row 1: Mid Price (spans both columns)
        VBox midPriceBox = createInfoBox("⚖️ MID PRICE", midPriceLabel);
        grid.add(midPriceBox, 0, 1, 2, 1); // spans 2 columns

        // Row 2: Spread info
        HBox spreadBox = new HBox(10);
        spreadBox.setAlignment(Pos.CENTER);

        VBox absoluteSpreadBox = createInfoBox("📏 SPREAD", spreadLabel);
        VBox percentSpreadBox = createInfoBox("📊 SPREAD %", spreadPercentLabel);

        spreadBox.getChildren().addAll(absoluteSpreadBox, percentSpreadBox);
        grid.add(spreadBox, 0, 2, 2, 1); // spans 2 columns

        return grid;
    }

    private VBox createInfoBox(String titleText, Label valueLabel) {
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("market-info-title");

        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(titleLabel, valueLabel);
        box.getStyleClass().add("market-info-box");

        return box;
    }

    private Label createValueLabel(String initialText, String styleClass) {
        Label label = new Label(initialText);
        label.getStyleClass().add("market-info-value");
        label.getStyleClass().add(styleClass);
        return label;
    }

    public void bindToOrderBook(ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        // Listen for changes in bid orders
        bidOrders.addListener((ListChangeListener<Order>) change -> {
            updateMarketInfo(bidOrders, askOrders);
        });

        // Listen for changes in ask orders
        askOrders.addListener((ListChangeListener<Order>) change -> {
            updateMarketInfo(bidOrders, askOrders);
        });

        // Initial update
        updateMarketInfo(bidOrders, askOrders);
    }

    private void updateMarketInfo(ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        try {
            BigDecimal newBestBid = BigDecimal.ZERO;
            BigDecimal newBestAsk = BigDecimal.ZERO;

            // Get best bid (highest bid price)
            if (!bidOrders.isEmpty()) {
                newBestBid = new BigDecimal(bidOrders.get(0).getPrice());
            }

            // Get best ask (lowest ask price)
            if (!askOrders.isEmpty()) {
                newBestAsk = new BigDecimal(askOrders.get(0).getPrice());
            }

            // Update labels with animation if values changed
            updateBidLabel(newBestBid);
            updateAskLabel(newBestAsk);

            // Calculate and update derived values
            updateDerivedValues(newBestBid, newBestAsk);

            // Store current values
            currentBestBid = newBestBid;
            currentBestAsk = newBestAsk;

        } catch (Exception e) {
            System.err.println("Error updating market info: " + e.getMessage());
        }
    }

    private void updateBidLabel(BigDecimal newBid) {
        if (newBid.compareTo(BigDecimal.ZERO) > 0) {
            bestBidLabel.setText("$" + priceFormat.format(newBid));

            // Add flash animation if value changed
            if (!newBid.equals(currentBestBid)) {
                flashLabel(bestBidLabel, "flash-green");
            }
        } else {
            bestBidLabel.setText("--");
        }
    }

    private void updateAskLabel(BigDecimal newAsk) {
        if (newAsk.compareTo(BigDecimal.ZERO) > 0) {
            bestAskLabel.setText("$" + priceFormat.format(newAsk));

            // Add flash animation if value changed
            if (!newAsk.equals(currentBestAsk)) {
                flashLabel(bestAskLabel, "flash-red");
            }
        } else {
            bestAskLabel.setText("--");
        }
    }

    private void updateDerivedValues(BigDecimal bestBid, BigDecimal bestAsk) {
        if (bestBid.compareTo(BigDecimal.ZERO) > 0 && bestAsk.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate mid price
            BigDecimal midPrice = bestBid.add(bestAsk).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            midPriceLabel.setText("$" + priceFormat.format(midPrice));

            // Calculate spread
            BigDecimal spread = bestAsk.subtract(bestBid);
            spreadLabel.setText("$" + priceFormat.format(spread));

            // Calculate spread percentage
            BigDecimal spreadPercent = spread.divide(midPrice, 6, RoundingMode.HALF_UP);
            spreadPercentLabel.setText(percentFormat.format(spreadPercent));

            // Color code spread percentage
            double percentValue = spreadPercent.doubleValue();
            if (percentValue < 0.001) { // < 0.1%
                spreadPercentLabel.getStyleClass().removeAll("spread-wide", "spread-normal");
                spreadPercentLabel.getStyleClass().add("spread-tight");
            } else if (percentValue < 0.005) { // < 0.5%
                spreadPercentLabel.getStyleClass().removeAll("spread-tight", "spread-wide");
                spreadPercentLabel.getStyleClass().add("spread-normal");
            } else { // >= 0.5%
                spreadPercentLabel.getStyleClass().removeAll("spread-tight", "spread-normal");
                spreadPercentLabel.getStyleClass().add("spread-wide");
            }

        } else {
            midPriceLabel.setText("--");
            spreadLabel.setText("--");
            spreadPercentLabel.setText("--");
        }
    }

    private void flashLabel(Label label, String flashStyleClass) {
        // Add flash style
        label.getStyleClass().add(flashStyleClass);

        // Create fade animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(100), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.7);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), label);
        fadeIn.setFromValue(0.7);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> {
            fadeIn.play();
            fadeIn.setOnFinished(e2 -> {
                // Remove flash style after animation
                label.getStyleClass().remove(flashStyleClass);
            });
        });

        fadeOut.play();
    }

    public void clear() {
        bestBidLabel.setText("--");
        bestAskLabel.setText("--");
        midPriceLabel.setText("--");
        spreadLabel.setText("--");
        spreadPercentLabel.setText("--");

        currentBestBid = BigDecimal.ZERO;
        currentBestAsk = BigDecimal.ZERO;
    }
}