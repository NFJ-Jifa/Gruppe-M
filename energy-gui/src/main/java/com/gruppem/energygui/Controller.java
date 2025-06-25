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

    // Верхние лейблы и кнопка Refresh
    @FXML private Label communityLabel;
    @FXML private Label gridLabel;
    @FXML private Button refreshButton;

    // Поля для диапазона и кнопка показа истории
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private Button getHistoricalButton;

    @FXML private Label statusLabel;

    // Таблица истории
    @FXML private TableView<EnergyDataFX> historyTable;
    @FXML private TableColumn<EnergyDataFX, String> hourColumn;
    @FXML private TableColumn<EnergyDataFX, Number> communityDepletedColumn;
    @FXML private TableColumn<EnergyDataFX, Number> gridPortionColumn;

    // Новые лейблы для суммарных kWh
    @FXML private Label prodLabel;
    @FXML private Label usedLabel;
    @FXML private Label gridUsedLabel;

    private static RestClient restClient = new RestClient();
    private static final DateTimeFormatter ISO_OFFSET_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter UI_HOUR_FMT   = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Доступный диапазон
    private Instant availableFrom, availableTo;

    @FXML
    private void initialize() {
        // 1) Получаем available-range
        new Thread(() -> {
            try {
                JSONObject range = new JSONObject(restClient.getAvailableRange());
                availableFrom = Instant.parse(range.getString("from"));
                availableTo   = Instant.parse(range.getString("to"));

                Platform.runLater(() -> {
                    LocalDateTime fromLdt = LocalDateTime.ofInstant(availableFrom, ZoneOffset.UTC);
                    LocalDateTime toLdt   = LocalDateTime.ofInstant(availableTo,   ZoneOffset.UTC);

                    startDatePicker.setValue(fromLdt.toLocalDate());
                    startTimeField .setText(fromLdt.toLocalTime().toString());
                    endDatePicker  .setValue(toLdt.toLocalDate());
                    endTimeField   .setText(toLdt.toLocalTime().toString());

                    statusLabel.setText(String.format(
                            "Доступно: %s … %s",
                            UI_HOUR_FMT.format(fromLdt),
                            UI_HOUR_FMT.format(toLdt)
                    ));
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        statusLabel.setText("Не удалось получить диапазон данных")
                );
            }
        }).start();

        // 2) Настраиваем таблицу истории
        hourColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getHour())
        );
        communityDepletedColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getCommunityDepleted())
        );
        gridPortionColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getGridPortion())
        );

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

        // 3) Обработчики кнопок
        refreshButton.setOnAction(e -> handleGetCurrentData());
        getHistoricalButton.setOnAction(e -> handleGetHistoricalData());
    }

    @FXML
    private void handleGetCurrentData() {
        refreshButton.setDisable(true);
        statusLabel.setText("Статус: Загружаем текущее…");
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject(restClient.getCurrentEnergyData());
                Instant inst = Instant.parse(json.getString("hour"));
                double cd = json.getDouble("communityDepleted");
                double gp = json.getDouble("gridPortion");

                Platform.runLater(() -> {
                    communityLabel.setText(String.format(Locale.US, "%.2f%% used", cd));
                    gridLabel     .setText(String.format(Locale.US, "%.2f%%",    gp));
                    statusLabel   .setText("Статус: Текущее загружено");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Статус: Ошибка при загрузке");
                    new Alert(Alert.AlertType.ERROR,
                            "Не удалось получить текущее значение:\n" + ex.getMessage(),
                            ButtonType.OK).showAndWait();
                });
            } finally {
                Platform.runLater(() -> refreshButton.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void handleGetHistoricalData() {
        try {
            LocalDate startD = startDatePicker.getValue();
            LocalTime startT = LocalTime.parse(startTimeField.getText());
            Instant startI = startD.atTime(startT).toInstant(ZoneOffset.UTC);

            LocalDate endD = endDatePicker.getValue();
            LocalTime endT = LocalTime.parse(endTimeField.getText());
            Instant endI = endD.atTime(endT).toInstant(ZoneOffset.UTC);

            getHistoricalButton.setDisable(true);
            statusLabel.setText("Статус: Загружаем историю…");

            new Thread(() -> {
                try {
                    String resp = restClient.getHistoricalEnergyData(
                            startI.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT),
                            endI  .atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT)
                    );
                    JSONArray arr = new JSONArray(resp);
                    ObservableList<EnergyDataFX> list = FXCollections.observableArrayList();

                    double sumProduced = 0, sumUsed = 0, sumGrid = 0;
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        Instant inst = Instant.parse(o.getString("hour"));
                        String disp = LocalDateTime.ofInstant(inst, ZoneOffset.UTC)
                                .format(UI_HOUR_FMT);
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

                    double pSum = sumProduced, uSum = sumUsed, gSum = sumGrid;
                    Platform.runLater(() -> {
                        historyTable.setItems(list);
                        prodLabel   .setText(String.format(Locale.US, "Community produced %.3f kWh", pSum));
                        usedLabel   .setText(String.format(Locale.US, "Community used     %.3f kWh", uSum));
                        gridUsedLabel.setText(String.format(Locale.US, "Grid used          %.3f kWh", gSum));
                        statusLabel .setText("Статус: История загружена");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        historyTable.getItems().clear();
                        statusLabel.setText("Статус: Ошибка загрузки истории");
                        new Alert(Alert.AlertType.ERROR,
                                "Не удалось загрузить историю:\n" + ex.getMessage(),
                                ButtonType.OK).showAndWait();
                    });
                } finally {
                    Platform.runLater(() -> getHistoricalButton.setDisable(false));
                }
            }).start();

        } catch (Exception ex) {
            historyTable.getItems().clear();
            statusLabel.setText("Статус: Ошибка ввода даты/времени");
        }
    }

    /** Для тестирования можно подменить RestClient */
    public static void setRestClient(RestClient client) {
        restClient = client;
    }
}
