package com.gruppem.energygui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Controller {

    // === UI ELEMENTS ===

    // Top section: displays current status
    @FXML private Label communityLabel;
    @FXML private Label gridLabel;
    @FXML private Button refreshButton;

    // Input fields for historical range
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private Button getHistoricalButton;

    @FXML private Label statusLabel;

    // Table for historical results
    @FXML private TableView<EnergyDataFX> historyTable;
    @FXML private TableColumn<EnergyDataFX, String> hourColumn;
    @FXML private TableColumn<EnergyDataFX, Number> communityDepletedColumn;
    @FXML private TableColumn<EnergyDataFX, Number> gridPortionColumn;

    // Labels for kWh totals
    @FXML private Label prodLabel;
    @FXML private Label usedLabel;
    @FXML private Label gridUsedLabel;

    // === BACKEND CONNECTION ===

    private static RestClient restClient = new RestClient(); // Handles HTTP communication

    private static final DateTimeFormatter ISO_OFFSET_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter UI_HOUR_FMT   = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Instant availableFrom, availableTo; // Available range fetched from backend

    // === INIT METHOD ===

    @FXML
    private void initialize() {
        // 1) Load available data range (executed in background)
        new Thread(() -> {
            try {
                JSONObject range = new JSONObject(restClient.getAvailableRange());
                availableFrom = Instant.parse(range.getString("from"));
                availableTo   = Instant.parse(range.getString("to"));

                // Update UI with fetched range
                Platform.runLater(() -> {
                    LocalDateTime fromLdt = LocalDateTime.ofInstant(availableFrom, ZoneOffset.UTC);
                    LocalDateTime toLdt   = LocalDateTime.ofInstant(availableTo,   ZoneOffset.UTC);

                    startDatePicker.setValue(fromLdt.toLocalDate());
                    startTimeField.setText(fromLdt.toLocalTime().toString());
                    endDatePicker.setValue(toLdt.toLocalDate());
                    endTimeField.setText(toLdt.toLocalTime().toString());

                    statusLabel.setText(String.format(
                            "Available: %s … %s",
                            UI_HOUR_FMT.format(fromLdt),
                            UI_HOUR_FMT.format(toLdt)
                    ));
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        statusLabel.setText("Failed to load data range")
                );
            }
        }).start();

        // 2) Configure historical data table columns
        hourColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getHour())
        );
        communityDepletedColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getCommunityDepleted())
        );
        gridPortionColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getGridPortion())
        );

        // Custom formatting: show percentage in cell
        communityDepletedColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null
                        : String.format(Locale.US, "%.2f%% used", val.doubleValue()));
            }
        });
        gridPortionColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null
                        : String.format(Locale.US, "%.2f%%", val.doubleValue()));
            }
        });

        // 3) Connect buttons to handlers
        refreshButton.setOnAction(e -> handleGetCurrentData());
        getHistoricalButton.setOnAction(e -> handleGetHistoricalData());
    }

    // === HANDLER: GET CURRENT DATA ===

    @FXML
    private void handleGetCurrentData() {
        refreshButton.setDisable(true);
        statusLabel.setText("Status: Loading current…");

        new Thread(() -> {
            try {
                JSONObject json = new JSONObject(restClient.getCurrentEnergyData());
                Instant inst = Instant.parse(json.getString("hour"));
                double cd = json.getDouble("communityDepleted");
                double gp = json.getDouble("gridPortion");

                Platform.runLater(() -> {
                    communityLabel.setText(String.format(Locale.US, "%.2f%% used", cd));
                    gridLabel.setText(String.format(Locale.US, "%.2f%%", gp));
                    statusLabel.setText("Status: Current loaded");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Status: Error loading current");
                    new Alert(Alert.AlertType.ERROR,
                            "Failed to get current value:\n" + ex.getMessage(),
                            ButtonType.OK).showAndWait();
                });
            } finally {
                Platform.runLater(() -> refreshButton.setDisable(false));
            }
        }).start();
    }

    // === HANDLER: GET HISTORICAL DATA ===

    @FXML
    private void handleGetHistoricalData() {
        try {
            // Read input values
            LocalDate startD = startDatePicker.getValue();
            LocalTime startT = LocalTime.parse(startTimeField.getText());
            Instant startI = startD.atTime(startT).toInstant(ZoneOffset.UTC);

            LocalDate endD = endDatePicker.getValue();
            LocalTime endT = LocalTime.parse(endTimeField.getText());
            Instant endI = endD.atTime(endT).toInstant(ZoneOffset.UTC);

            getHistoricalButton.setDisable(true);
            statusLabel.setText("Status: Loading history…");

            new Thread(() -> {
                try {
                    String resp = restClient.getHistoricalEnergyData(
                            startI.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT),
                            endI.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT)
                    );
                    JSONArray arr = new JSONArray(resp);
                    ObservableList<EnergyDataFX> list = FXCollections.observableArrayList();

                    double sumProduced = 0, sumUsed = 0, sumGrid = 0;

                    // Loop through results and fill table + calculate totals
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        Instant inst = Instant.parse(o.getString("hour"));
                        String disp = LocalDateTime.ofInstant(inst, ZoneOffset.UTC).format(UI_HOUR_FMT);

                        double prod = o.getDouble("communityProduced");
                        double used = o.getDouble("communityUsed");
                        double grid = o.getDouble("gridUsed");
                        double cd = o.getDouble("communityDepleted");
                        double gp = o.getDouble("gridPortion");

                        sumProduced += prod;
                        sumUsed     += used;
                        sumGrid     += grid;

                        list.add(new EnergyDataFX(disp, cd, gp));
                    }

                    // Update UI with table data and totals
                    double pSum = sumProduced, uSum = sumUsed, gSum = sumGrid;
                    Platform.runLater(() -> {
                        historyTable.setItems(list);
                        prodLabel.setText(String.format(Locale.US, "Community produced %.3f kWh", pSum));
                        usedLabel.setText(String.format(Locale.US, "Community used     %.3f kWh", uSum));
                        gridUsedLabel.setText(String.format(Locale.US, "Grid used          %.3f kWh", gSum));
                        statusLabel.setText("Status: History loaded");
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        historyTable.getItems().clear();
                        statusLabel.setText("Status: Error loading history");
                        new Alert(Alert.AlertType.ERROR,
                                "Failed to load history:\n" + ex.getMessage(),
                                ButtonType.OK).showAndWait();
                    });
                } finally {
                    Platform.runLater(() -> getHistoricalButton.setDisable(false));
                }
            }).start();

        } catch (Exception ex) {
            historyTable.getItems().clear();
            statusLabel.setText("Status: Invalid input (date/time)");
        }
    }

    /**
     * Allows unit tests to inject a mock RestClient
     */
    public static void setRestClient(RestClient client) {
        restClient = client;
    }
}
