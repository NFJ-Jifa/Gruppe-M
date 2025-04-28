package com.gruppem.energygui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONObject;  // Добавим библиотеку org.json

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

    private final RestClient restClient = new RestClient();

    @FXML
    private void initialize() {
        getCurrentButton.setOnAction(event -> handleGetCurrentData());
        getHistoricalButton.setOnAction(event -> handleGetHistoricalData());
    }

    private void handleGetCurrentData() {
        try {
            statusLabel.setText("Status: Fetching current data...");
            String response = restClient.getCurrentEnergyData();

            // Парсим JSON
            JSONObject json = new JSONObject(response);
            String formatted = String.format(
                    "Hour: %s\nCommunity Depleted: %.2f\nGrid Portion: %.2f",
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
            String start = startTimeField.getText();
            String end = endTimeField.getText();
            String response = restClient.getHistoricalEnergyData(start, end);

            // Пока просто выводим как есть (если хочешь, тоже можем оформить красиво)
            outputArea.setText(response);
            statusLabel.setText("Status: Historical data loaded");
        } catch (Exception e) {
            outputArea.setText("");
            statusLabel.setText("Status: Error fetching historical energy data.");
            e.printStackTrace();
        }
    }
}
