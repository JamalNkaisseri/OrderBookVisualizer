package com.visualizer;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrderBookTableView extends TableView<Order> {

    public OrderBookTableView(ObservableList<Order> orders, boolean isBidTable) {
        super(orders);
        setupTable(isBidTable);
        createColumns(isBidTable);
    }

    private void setupTable(boolean isBidTable) {
        setPrefWidth(380);
        setPrefHeight(250); // Reduced height since we're stacking them vertically
        setMaxHeight(250);  // Prevent tables from growing too large
        getStyleClass().add(isBidTable ? "bid-table" : "ask-table");

        // Show only a limited number of rows for cleaner layout
        setFixedCellSize(25);

        // Hide table header for cleaner look (optional)
        // setTableMenuButtonVisible(false);
    }

    private void createColumns(boolean isBidTable) {
        String priceColumnTitle = isBidTable ? "BID PRICE" : "ASK PRICE";
        String priceColumnStyle = isBidTable ? "bid-price-column" : "ask-price-column";

        TableColumn<Order, String> priceCol = new TableColumn<>(priceColumnTitle);
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(190);
        priceCol.getStyleClass().add(priceColumnStyle);
        priceCol.setResizable(false);

        TableColumn<Order, String> qtyCol = new TableColumn<>("SIZE");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(190);
        qtyCol.getStyleClass().add("quantity-column");
        qtyCol.setResizable(false);

        getColumns().addAll(priceCol, qtyCol);

        // Make columns non-reorderable
        getColumns().forEach(col -> col.setReorderable(false));
    }
}