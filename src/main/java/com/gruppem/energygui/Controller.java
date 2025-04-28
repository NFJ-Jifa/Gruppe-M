package com.gruppem.energygui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

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
    private TextArea outputArea;

    @FXML
    private Label statusLabel;

    private final RestClient restClient = new RestClient();  // клиент для отправки HTTP запросов

    @FXML
    private void initialize() {
        getCurrentButton.setOnAction(event -> fetchCurrentEnergyData());
        getHistoricalButton.setOnAction(event -> fetchHistoricalEnergyData());
    }

    private void fetchCurrentEnergyData() {
        new Thread(() -> {
            try {
                updateStatus("Fetching current energy data...");
                String response = restClient.getCurrentEnergyData();
                updateOutput(response);
                updateStatus("Current energy data fetched successfully.");
            } catch (Exception e) {
                updateStatus("Error fetching current energy data.");
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchHistoricalEnergyData() {
        new Thread(() -> {
            try {
                String startTime = startTimeField.getText();
                String endTime = endTimeField.getText();
                updateStatus("Fetching historical energy data...");
                String response = restClient.getHistoricalEnergyData(startTime, endTime);
                updateOutput(response);
                updateStatus("Historical energy data fetched successfully.");
            } catch (Exception e) {
                updateStatus("Error fetching historical energy data.");
                e.printStackTrace();
            }
        }).start();
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText("Status: " + message));
    }

    private void updateOutput(String message) {
        Platform.runLater(() -> outputArea.setText(message));
    }
}
