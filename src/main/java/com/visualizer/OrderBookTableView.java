package com.visualizer;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OrderBookTableView extends VBox {

    private TableView<Order> tableView;

    public OrderBookTableView(ObservableList<Order> orders, boolean isBidTable) {
        createCustomHeader(isBidTable);
        createTable(orders, isBidTable);
        setupLayout();
    }

    private void createCustomHeader(boolean isBidTable) {
        String priceHeaderText = isBidTable ? "BID PRICE" : "ASK PRICE";
        String priceStyleClass = isBidTable ? "bid-price-header" : "ask-price-header";

        Label priceHeader = new Label(priceHeaderText);
        priceHeader.getStyleClass().addAll("table-header-label", priceStyleClass);
        priceHeader.setPrefWidth(190);
        priceHeader.setAlignment(Pos.CENTER);

        Label quantityHeader = new Label("SIZE");
        quantityHeader.getStyleClass().addAll("table-header-label", "quantity-header");
        quantityHeader.setPrefWidth(190);
        quantityHeader.setAlignment(Pos.CENTER);

        HBox headerRow = new HBox();
        headerRow.getChildren().addAll(priceHeader, quantityHeader);
        headerRow.getStyleClass().add("custom-table-header");
        headerRow.setPrefWidth(380);

        getChildren().add(headerRow);
    }

    private void createTable(ObservableList<Order> orders, boolean isBidTable) {
        tableView = new TableView<>(orders);
        setupTable(isBidTable);
        createColumns(isBidTable);
    }

    private void setupTable(boolean isBidTable) {
        tableView.setPrefWidth(380);
        tableView.setPrefHeight(250); // Reduced since we have custom header
        tableView.setMaxHeight(250);
        tableView.getStyleClass().add(isBidTable ? "bid-table" : "ask-table");

        // Hide the built-in header since we're using custom header
        tableView.getStyleClass().add("headerless-table");

        tableView.setFixedCellSize(25);
        tableView.setTableMenuButtonVisible(false);
        tableView.setPlaceholder(new Label(""));

        // Disable column sorting and reordering
        tableView.setSortPolicy(null);
    }

    private void createColumns(boolean isBidTable) {
        String priceColumnStyle = isBidTable ? "bid-price-column" : "ask-price-column";

        TableColumn<Order, String> priceCol = new TableColumn<>(); // Empty header
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(190);
        priceCol.getStyleClass().add(priceColumnStyle);
        priceCol.setResizable(false);
        priceCol.setSortable(false);
        priceCol.setReorderable(false);

        TableColumn<Order, String> qtyCol = new TableColumn<>(); // Empty header
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(190);
        qtyCol.getStyleClass().add("quantity-column");
        qtyCol.setResizable(false);
        qtyCol.setSortable(false);
        qtyCol.setReorderable(false);

        tableView.getColumns().addAll(priceCol, qtyCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupLayout() {
        getChildren().add(tableView);
        setSpacing(0); // No gap between header and table
        setPrefWidth(380);
        getStyleClass().add("orderbook-table-container");
    }

    // Expose TableView methods that might be needed
    public TableView<Order> getTableView() {
        return tableView;
    }

    public void setItems(ObservableList<Order> items) {
        tableView.setItems(items);
    }

    public ObservableList<Order> getItems() {
        return tableView.getItems();
    }
}