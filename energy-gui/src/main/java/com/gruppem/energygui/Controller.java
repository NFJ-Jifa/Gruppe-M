package com.gruppem.energygui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Controller {

    @FXML
    private Button getCurrentButton;

    @FXML
    private Button getHistoricalButton;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextArea outputArea;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<EnergyDataFX> historyTable;

    @FXML
    private TableColumn<EnergyDataFX, String> hourColumn;

    @FXML
    private TableColumn<EnergyDataFX, Number> communityDepletedColumn;

    @FXML
    private TableColumn<EnergyDataFX, Number> gridPortionColumn;

    private final RestClient restClient = new RestClient();

    @FXML
    private void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        startDatePicker.setValue(now.toLocalDate());
        endDatePicker.setValue(oneHourLater.toLocalDate());
        startTimeField.setText(now.toLocalTime().withSecond(0).withNano(0).toString());
        endTimeField.setText(oneHourLater.toLocalTime().withSecond(0).withNano(0).toString());

        // Настройка TableView
        hourColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHour()));
        communityDepletedColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCommunityDepleted()));
        gridPortionColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGridPortion()));

        getCurrentButton.setOnAction(event -> handleGetCurrentData());
        getHistoricalButton.setOnAction(event -> handleGetHistoricalData());
    }

    private void handleGetCurrentData() {
        try {
            statusLabel.setText("Status: Fetching current data...");
            String response = restClient.getCurrentEnergyData();

            JSONObject json = new JSONObject(response);
            String formatted = String.format(
                    "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%",
                    json.getString("hour"),
                    json.getDouble("communityDepleted"),
                    json.getDouble("gridPortion")
            );

            outputArea.setText(formatted);
            statusLabel.setText("Status: Current data loaded");
        } catch (Exception e) {
            outputArea.setText("");
            statusLabel.setText("Status: Error fetching current energy data.");
            e.printStackTrace();
        }
    }

    private void handleGetHistoricalData() {
        try {
            statusLabel.setText("Status: Fetching historical data...");

            // Собираем дату и время
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            LocalTime startTime = LocalTime.parse(startTimeField.getText());
            LocalTime endTime = LocalTime.parse(endTimeField.getText());

            String start = startDate.atTime(startTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String end = endDate.atTime(endTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String response = restClient.getHistoricalEnergyData(start, end);
            JSONArray jsonArray = new JSONArray(response);

            ObservableList<EnergyDataFX> data = FXCollections.observableArrayList();
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                String hour = entry.getString("hour");
                double depleted = entry.getDouble("communityDepleted");
                double grid = entry.getDouble("gridPortion");

                formatted.append(String.format(
                        "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%\n\n",
                        hour, depleted, grid
                ));

                data.add(new EnergyDataFX(hour, depleted, grid));
            }

            historyTable.setItems(data);
            outputArea.setText(formatted.toString());
            statusLabel.setText("Status: Historical data loaded");
        } catch (Exception e) {
            outputArea.setText("");
            statusLabel.setText("Status: Error fetching historical energy data.");
            e.printStackTrace();
        }
    }
}
