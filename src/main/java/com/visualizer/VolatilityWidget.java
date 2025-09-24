package com.visualizer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class VolatilityWidget extends VBox {

    private final VolatilityIndicators volatilityCalculator;
    private final AtomicReference<BigDecimal> currentPrice = new AtomicReference<>(BigDecimal.ZERO);

    // UI Components
    private Label atrLabel;
    private Label atrValue;
    private Label volatilityPercentileLabel;
    private Label volatilityPercentileValue;
    private Label priceVelocityLabel;
    private Label priceVelocityValue;
    private Label bollingerWidthLabel;
    private Label bollingerWidthValue;
    private Label volatilitySpikeLabel;
    private Label volatilitySpikeValue;
    private Label historicalVolLabel;
    private Label historicalVolValue;

    // Update timeline
    private Timeline updateTimeline;

    public VolatilityWidget() {
        this.volatilityCalculator = new VolatilityIndicators(1000); // Keep 1000 data points
        initializeWidget();
        startUpdateTimer();
    }

    private void initializeWidget() {
        // Widget header
        Label headerLabel = new Label("📊 Volatility Analysis");
        headerLabel.getStyleClass().add("volatility-widget-header");

        // Create metric boxes
        VBox atrBox = createMetricBox("ATR (14)", "0.00");
        atrLabel = (Label) ((VBox) atrBox.getChildren().get(0)).getChildren().get(0);
        atrValue = (Label) ((VBox) atrBox.getChildren().get(0)).getChildren().get(1);

        VBox percentileBox = createMetricBox("Vol Percentile", "0%");
        volatilityPercentileLabel = (Label) ((VBox) percentileBox.getChildren().get(0)).getChildren().get(0);
        volatilityPercentileValue = (Label) ((VBox) percentileBox.getChildren().get(0)).getChildren().get(1);

        VBox velocityBox = createMetricBox("Price Velocity", "0.00/s");
        priceVelocityLabel = (Label) ((VBox) velocityBox.getChildren().get(0)).getChildren().get(0);
        priceVelocityValue = (Label) ((VBox) velocityBox.getChildren().get(0)).getChildren().get(1);

        VBox bollingerBox = createMetricBox("BB Width", "0.00");
        bollingerWidthLabel = (Label) ((VBox) bollingerBox.getChildren().get(0)).getChildren().get(0);
        bollingerWidthValue = (Label) ((VBox) bollingerBox.getChildren().get(0)).getChildren().get(1);

        VBox spikeBox = createMetricBox("Vol Spike", "Normal");
        volatilitySpikeLabel = (Label) ((VBox) spikeBox.getChildren().get(0)).getChildren().get(0);
        volatilitySpikeValue = (Label) ((VBox) spikeBox.getChildren().get(0)).getChildren().get(1);

        VBox histVolBox = createMetricBox("Historical Vol", "0.00%");
        historicalVolLabel = (Label) ((VBox) histVolBox.getChildren().get(0)).getChildren().get(0);
        historicalVolValue = (Label) ((VBox) histVolBox.getChildren().get(0)).getChildren().get(1);

        // Arrange metrics in a grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(10);
        metricsGrid.setVgap(10);
        metricsGrid.setPadding(new Insets(10));
        metricsGrid.setAlignment(Pos.CENTER);

        // First row
        metricsGrid.add(atrBox, 0, 0);
        metricsGrid.add(percentileBox, 1, 0);
        metricsGrid.add(velocityBox, 2, 0);

        // Second row
        metricsGrid.add(bollingerBox, 0, 1);
        metricsGrid.add(spikeBox, 1, 1);
        metricsGrid.add(histVolBox, 2, 1);

        // Main container
        VBox container = new VBox(15);
        container.getChildren().addAll(headerLabel, metricsGrid);
        container.getStyleClass().add("volatility-widget-container");
        container.setAlignment(Pos.CENTER);

        getChildren().add(container);
        getStyleClass().add("volatility-widget");
        setPadding(new Insets(15));
    }

    private VBox createMetricBox(String labelText, String defaultValue) {
        Label titleLabel = new Label(labelText);
        titleLabel.getStyleClass().add("volatility-metric-title");

        Label valueLabel = new Label(defaultValue);
        valueLabel.getStyleClass().add("volatility-metric-value");

        VBox content = new VBox(5);
        content.getChildren().addAll(titleLabel, valueLabel);
        content.setAlignment(Pos.CENTER);

        VBox box = new VBox();
        box.getChildren().add(content);
        box.getStyleClass().add("volatility-metric-box");
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(120);
        box.setPrefHeight(80);

        return box;
    }

    public void bindToOrderBook(ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        // Listen for order book changes to extract price data
        bidOrders.addListener((javafx.collections.ListChangeListener<? super Order>) change -> {
            updateFromOrderBook(bidOrders, askOrders);
        });

        askOrders.addListener((javafx.collections.ListChangeListener<? super Order>) change -> {
            updateFromOrderBook(bidOrders, askOrders);
        });
    }

    private void updateFromOrderBook(ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        if (!bidOrders.isEmpty() && !askOrders.isEmpty()) {
            try {
                BigDecimal bestBid = new BigDecimal(bidOrders.get(0).getPrice());
                BigDecimal bestAsk = new BigDecimal(askOrders.get(0).getPrice());
                BigDecimal midPrice = bestBid.add(bestAsk).divide(BigDecimal.valueOf(2));

                // Add price data point
                long timestamp = System.currentTimeMillis();
                BigDecimal volume = BigDecimal.ONE; // We don't have volume data from order book
                volatilityCalculator.addPriceData(midPrice, timestamp, volume);

                currentPrice.set(midPrice);

            } catch (Exception e) {
                System.err.println("Error updating volatility from order book: " + e.getMessage());
            }
        }
    }

    private void startUpdateTimer() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateVolatilityMetrics()));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }

    private void updateVolatilityMetrics() {
        Platform.runLater(() -> {
            try {
                // Calculate ATR (14 periods)
                BigDecimal atr = volatilityCalculator.calculateATR(14);
                atrValue.setText(String.format("%.4f", atr.doubleValue()));
                updateATRColor(atr);

                // Calculate Volatility Percentile
                BigDecimal percentile = volatilityCalculator.calculateVolatilityPercentile(100, 14);
                volatilityPercentileValue.setText(String.format("%.0f%%", percentile.doubleValue()));
                updatePercentileColor(percentile);

                // Calculate Price Velocity
                BigDecimal velocity = volatilityCalculator.calculatePriceVelocity(5);
                priceVelocityValue.setText(String.format("%.2f/s", velocity.doubleValue()));
                updateVelocityColor(velocity);

                // Calculate Bollinger Band Width
                BigDecimal bbWidth = volatilityCalculator.calculateBollingerBandWidth(20, 2.0);
                bollingerWidthValue.setText(String.format("%.4f", bbWidth.doubleValue()));
                updateBollingerColor(bbWidth);

                // Detect Volatility Spike
                VolatilityIndicators.VolatilitySpike spike =
                        volatilityCalculator.detectVolatilitySpike(5, 20, 1.5);
                volatilitySpikeValue.setText(spike.isSpike ? "SPIKE!" : "Normal");
                updateSpikeColor(spike.isSpike);

                // Calculate Historical Volatility
                BigDecimal histVol = volatilityCalculator.calculateHistoricalVolatility(30);
                historicalVolValue.setText(String.format("%.2f%%", histVol.doubleValue() * 100));
                updateHistVolColor(histVol);

            } catch (Exception e) {
                System.err.println("Error updating volatility metrics: " + e.getMessage());
            }
        });
    }

    // Color coding methods
    private void updateATRColor(BigDecimal atr) {
        double value = atr.doubleValue();
        if (value > 100) {
            atrValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            atrValue.getStyleClass().add("volatility-high");
        } else if (value > 50) {
            atrValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            atrValue.getStyleClass().add("volatility-medium");
        } else {
            atrValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            atrValue.getStyleClass().add("volatility-low");
        }
    }

    private void updatePercentileColor(BigDecimal percentile) {
        double value = percentile.doubleValue();
        if (value > 80) {
            volatilityPercentileValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            volatilityPercentileValue.getStyleClass().add("volatility-high");
        } else if (value > 50) {
            volatilityPercentileValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            volatilityPercentileValue.getStyleClass().add("volatility-medium");
        } else {
            volatilityPercentileValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            volatilityPercentileValue.getStyleClass().add("volatility-low");
        }
    }

    private void updateVelocityColor(BigDecimal velocity) {
        double absValue = velocity.abs().doubleValue();
        if (absValue > 10) {
            priceVelocityValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            priceVelocityValue.getStyleClass().add("volatility-high");
        } else if (absValue > 5) {
            priceVelocityValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            priceVelocityValue.getStyleClass().add("volatility-medium");
        } else {
            priceVelocityValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            priceVelocityValue.getStyleClass().add("volatility-low");
        }
    }

    private void updateBollingerColor(BigDecimal bbWidth) {
        double value = bbWidth.doubleValue();
        if (value > 0.1) {
            bollingerWidthValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            bollingerWidthValue.getStyleClass().add("volatility-high");
        } else if (value > 0.05) {
            bollingerWidthValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            bollingerWidthValue.getStyleClass().add("volatility-medium");
        } else {
            bollingerWidthValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            bollingerWidthValue.getStyleClass().add("volatility-low");
        }
    }

    private void updateSpikeColor(boolean isSpike) {
        if (isSpike) {
            volatilitySpikeValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            volatilitySpikeValue.getStyleClass().add("volatility-spike");
        } else {
            volatilitySpikeValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high", "volatility-spike");
            volatilitySpikeValue.getStyleClass().add("volatility-low");
        }
    }

    private void updateHistVolColor(BigDecimal histVol) {
        double value = histVol.doubleValue();
        if (value > 0.5) {
            historicalVolValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            historicalVolValue.getStyleClass().add("volatility-high");
        } else if (value > 0.2) {
            historicalVolValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            historicalVolValue.getStyleClass().add("volatility-medium");
        } else {
            historicalVolValue.getStyleClass().removeAll("volatility-low", "volatility-medium", "volatility-high");
            historicalVolValue.getStyleClass().add("volatility-low");
        }
    }

    public void clear() {
        Platform.runLater(() -> {
            atrValue.setText("0.00");
            volatilityPercentileValue.setText("0%");
            priceVelocityValue.setText("0.00/s");
            bollingerWidthValue.setText("0.00");
            volatilitySpikeValue.setText("Normal");
            historicalVolValue.setText("0.00%");
        });
    }

    public void stop() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
    }
}