package com.gruppem.energygui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
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
    private TextArea outputArea;

    @FXML
    private Label statusLabel;

    private final RestClient restClient = new RestClient();

    @FXML
    private void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        startTimeField.setText(now.format(formatter));
        endTimeField.setText(oneHourLater.format(formatter));

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
            String start = startTimeField.getText();
            String end = endTimeField.getText();
            String response = restClient.getHistoricalEnergyData(start, end);

            JSONArray jsonArray = new JSONArray(response);
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                formatted.append(String.format(
                        "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%\n\n",
                        entry.getString("hour"),
                        entry.getDouble("communityDepleted"),
                        entry.getDouble("gridPortion")
                ));
            }

            outputArea.setText(formatted.toString());
            statusLabel.setText("Status: Historical data loaded");
        } catch (Exception e) {
            outputArea.setText("");
            statusLabel.setText("Status: Error fetching historical energy data.");
            e.printStackTrace();
        }
    }
}
