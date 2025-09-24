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
        setPrefHeight(550);
        getStyleClass().add(isBidTable ? "bid-table" : "ask-table");
    }

    private void createColumns(boolean isBidTable) {
        String priceColumnTitle = isBidTable ? "BID PRICE" : "ASK PRICE";
        String priceColumnStyle = isBidTable ? "bid-price-column" : "ask-price-column";

        TableColumn<Order, String> priceCol = new TableColumn<>(priceColumnTitle);
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(190);
        priceCol.getStyleClass().add(priceColumnStyle);

        TableColumn<Order, String> qtyCol = new TableColumn<>("SIZE");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(190);
        qtyCol.getStyleClass().add("quantity-column");

        getColumns().addAll(priceCol, qtyCol);
    }
}
