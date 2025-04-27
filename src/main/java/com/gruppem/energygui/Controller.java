package com.gruppem.energygui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class Controller {

    private TextArea textArea = new TextArea();

    public VBox createUI() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Button btnCurrent = new Button("Get Current Usage");
        Button btnHistorical = new Button("Get Historical Usage");

        btnCurrent.setOnAction(e -> fetchCurrentUsage());
        btnHistorical.setOnAction(e -> fetchHistoricalUsage());

        layout.getChildren().addAll(new Label("Energy Community Dashboard:"), btnCurrent, btnHistorical, textArea);

        return layout;
    }

    private void fetchCurrentUsage() {
        try {
            String response = RestClient.getCurrentEnergyData();
            textArea.setText(response);
        } catch (Exception e) {
            textArea.setText("Error fetching current usage:\n" + e.getMessage());
        }
    }

    private void fetchHistoricalUsage() {
        try {
            String response = RestClient.getHistoricalEnergyData("2025-04-26T00:00:00", "2025-04-27T00:00:00");
            textArea.setText(response);
        } catch (Exception e) {
            textArea.setText("Error fetching historical usage:\n" + e.getMessage());
        }
    }
}
