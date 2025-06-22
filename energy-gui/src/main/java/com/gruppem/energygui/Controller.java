package com.gruppem.energygui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class Controller {

    @FXML private Button getCurrentButton;
    @FXML private Button getHistoricalButton;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea outputArea;
    @FXML private Label statusLabel;
    @FXML private TableView<EnergyDataFX> historyTable;
    @FXML private TableColumn<EnergyDataFX, String> hourColumn;
    @FXML private TableColumn<EnergyDataFX, Number> communityDepletedColumn;
    @FXML private TableColumn<EnergyDataFX, Number> gridPortionColumn;

    private final RestClient restClient = new RestClient();
    private static final DateTimeFormatter ISO_OFFSET_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // Эти границы мы подхватим из /available-range
    private Instant availableFrom, availableTo;

    @FXML
    private void initialize() {
        // 1) Сначала запросим диапазон
        new Thread(() -> {
            try {
                String resp = restClient.getAvailableRange();   // новый метод в RestClient
                JSONObject json = new JSONObject(resp);
                availableFrom = Instant.parse(json.getString("from"));
                availableTo   = Instant.parse(json.getString("to"));
                // Установим datepickers на эти значения в FX-потоке
                Platform.runLater(() -> {
                    LocalDate fromDate = LocalDateTime.ofInstant(availableFrom, ZoneOffset.UTC).toLocalDate();
                    LocalDate toDate   = LocalDateTime.ofInstant(availableTo,   ZoneOffset.UTC).toLocalDate();
                    startDatePicker.setValue(fromDate);
                    startTimeField  .setText(LocalDateTime.ofInstant(availableFrom, ZoneOffset.UTC)
                            .toLocalTime().toString());
                    endDatePicker  .setValue(toDate);
                    endTimeField   .setText(LocalDateTime.ofInstant(availableTo, ZoneOffset.UTC)
                            .toLocalTime().toString());
                    statusLabel.setText(String.format("Доступно: %s … %s",
                            availableFrom, availableTo));
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Не удалось получить диапазон данных");
                });
                ex.printStackTrace();
            }
        }).start();

        // 2) Настройка таблицы и кнопок
        hourColumn             .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHour()));
        communityDepletedColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getCommunityDepleted()));
        gridPortionColumn      .setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getGridPortion()));

        getCurrentButton    .setOnAction(e -> handleGetCurrentData());
        getHistoricalButton .setOnAction(e -> handleGetHistoricalData());
    }

    private void handleGetCurrentData() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Статус: Загружаем текущее…"));
                String resp = restClient.getCurrentEnergyData();
                JSONObject json = new JSONObject(resp);
                String out = String.format(
                        "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%",
                        json.getString("hour"),
                        json.getDouble("communityDepleted"),
                        json.getDouble("gridPortion")
                );
                Platform.runLater(() -> {
                    outputArea.setText(out);
                    statusLabel.setText("Статус: Текущее загружено");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    outputArea.setText("Ошибка: " + ex.getMessage());
                    statusLabel.setText("Статус: Ошибка при загрузке");
                });
            }
        }).start();
    }

    @FXML
    private void handleGetHistoricalData() {
        try {
            // 1) Собираем Instant из UI
            LocalDate startD = startDatePicker.getValue();
            LocalTime startT = LocalTime.parse(startTimeField.getText());
            Instant startI = startD.atTime(startT).toInstant(ZoneOffset.UTC);

            LocalDate endD = endDatePicker.getValue();
            LocalTime endT = LocalTime.parse(endTimeField.getText());
            Instant endI = endD.atTime(endT).toInstant(ZoneOffset.UTC);

            // 2) Проверяем, чтобы было внутри availableFrom…availableTo
            if (availableFrom == null || availableTo == null || startI.isBefore(availableFrom) || endI.isAfter(availableTo)) {
                historyTable.getItems().clear();
                outputArea.setText("Запрос за пределами доступных данных:\n" +
                        availableFrom + " … " + availableTo);
                statusLabel.setText("Статус: Невозможно загрузить");
                return;
            }

            // 3) Всё ок — запускаем в фоне
            new Thread(() -> {
                try {
                    Platform.runLater(() -> statusLabel.setText("Статус: Загружаем историю…"));
                    String resp = restClient.getHistoricalEnergyData(
                            startI.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT),
                            endI  .atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT)
                    );
                    JSONArray arr = new JSONArray(resp);
                    ObservableList<EnergyDataFX> list = FXCollections.observableArrayList();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String hour = o.getString("hour");
                        double cd   = o.getDouble("communityDepleted");
                        double gp   = o.getDouble("gridPortion");
                        sb.append(String.format(
                                "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%\n\n",
                                hour, cd, gp
                        ));
                        list.add(new EnergyDataFX(hour, cd, gp));
                    }
                    Platform.runLater(() -> {
                        historyTable.setItems(list);
                        outputArea.setText(sb.toString());
                        statusLabel.setText("Статус: История загружена");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        historyTable.getItems().clear();
                        outputArea.setText("Ошибка: " + ex.getMessage());
                        statusLabel.setText("Статус: Ошибка загрузки истории");
                    });
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
            historyTable.getItems().clear();
            outputArea.setText("Неверный формат даты/времени");
            statusLabel.setText("Статус: Ошибка ввода");
        }
    }
}
