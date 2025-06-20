package com.gruppem.energygui;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class EnergyHistoryController {

    @FXML
    private TableView<EnergyDataFX> energyTable;

    @FXML
    private TableColumn<EnergyDataFX, String> meterIdColumn;

    @FXML
    private TableColumn<EnergyDataFX, java.time.LocalDateTime> timestampColumn;

    @FXML
    private TableColumn<EnergyDataFX, Double> valueColumn;

    @FXML
    public void initialize() {
        meterIdColumn.setCellValueFactory(new PropertyValueFactory<>("meterId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }


    public void setData(java.util.List<EnergyDataFX> data) {
        energyTable.getItems().setAll(data);
    }
}
