package com.visualizer;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DepthChartView extends VBox {

    private final AreaChart<Number, Number> depthChart;
    private final XYChart.Series<Number, Number> bidSeries;
    private final XYChart.Series<Number, Number> askSeries;

    public DepthChartView() {
        // Initialize final fields first
        this.bidSeries = new XYChart.Series<>();
        this.askSeries = new XYChart.Series<>();
        this.depthChart = createChart();

        setupLayout();
    }

    private AreaChart<Number, Number> createChart() {
        // Create axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Price (USDT)");
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cumulative Size");
        yAxis.setForceZeroInRange(true);

        // Create chart
        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("Market Depth");
        chart.setLegendVisible(true);
        chart.setCreateSymbols(false);
        chart.setPrefWidth(600);
        chart.setPrefHeight(400);

        // Set up series names
        bidSeries.setName("Bids");
        askSeries.setName("Asks");

        chart.getData().addAll(bidSeries, askSeries);

        // Apply CSS styles
        chart.getStyleClass().add("depth-chart");

        return chart;
    }

    private void setupLayout() {
        Label headerLabel = new Label("📊 Market Depth");
        headerLabel.getStyleClass().add("depth-chart-header");

        setAlignment(Pos.CENTER);
        setSpacing(10);
        getChildren().addAll(headerLabel, depthChart);
    }

    public void bindToOrderBook(ObservableList<Order> bidOrders, ObservableList<Order> askOrders) {
        // Listen for changes in bid orders
        bidOrders.addListener((ListChangeListener<Order>) change -> {
            updateBidSeries(bidOrders);
        });

        // Listen for changes in ask orders
        askOrders.addListener((ListChangeListener<Order>) change -> {
            updateAskSeries(askOrders);
        });

        // Initial update
        updateBidSeries(bidOrders);
        updateAskSeries(askOrders);
    }

    private void updateBidSeries(ObservableList<Order> bidOrders) {
        List<XYChart.Data<Number, Number>> bidData = new ArrayList<>();

        if (!bidOrders.isEmpty()) {
            double cumulativeSize = 0.0;

            // Bids should be processed in descending price order (highest to lowest)
            List<Order> sortedBids = new ArrayList<>(bidOrders);
            // Since bidOrders are already sorted from highest to lowest, we keep that order

            for (Order order : sortedBids) {
                try {
                    double price = Double.parseDouble(order.getPrice());
                    double quantity = Double.parseDouble(order.getQuantity());
                    cumulativeSize += quantity;

                    bidData.add(new XYChart.Data<>(price, cumulativeSize));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing bid order: " + e.getMessage());
                }
            }

            // Reverse to show cumulative depth correctly (from left to right)
            Collections.reverse(bidData);
        }

        bidSeries.getData().clear();
        bidSeries.getData().addAll(bidData);
    }

    private void updateAskSeries(ObservableList<Order> askOrders) {
        List<XYChart.Data<Number, Number>> askData = new ArrayList<>();

        if (!askOrders.isEmpty()) {
            double cumulativeSize = 0.0;

            // Asks should be processed in ascending price order (lowest to highest)
            List<Order> sortedAsks = new ArrayList<>(askOrders);
            // Since askOrders are already sorted from lowest to highest, we keep that order

            for (Order order : sortedAsks) {
                try {
                    double price = Double.parseDouble(order.getPrice());
                    double quantity = Double.parseDouble(order.getQuantity());
                    cumulativeSize += quantity;

                    askData.add(new XYChart.Data<>(price, cumulativeSize));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing ask order: " + e.getMessage());
                }
            }
        }

        askSeries.getData().clear();
        askSeries.getData().addAll(askData);
    }

    public void clear() {
        bidSeries.getData().clear();
        askSeries.getData().clear();
    }
}