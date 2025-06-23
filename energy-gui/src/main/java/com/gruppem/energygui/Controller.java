package com.gruppem.energygui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import java.time.*;

import java.time.format.DateTimeParseException;

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

    static RestClient restClient = new RestClient();
    private static final DateTimeFormatter ISO_OFFSET_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter UI_HOUR_FMT   = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Границы данных для UI
    private Instant availableFrom, availableTo;

    @FXML
    private void initialize() {
        // 1) Запрос диапазона доступных данных
        new Thread(() -> {
            try {
                String resp = restClient.getAvailableRange();
                JSONObject json = new JSONObject(resp);
                availableFrom = Instant.parse(json.getString("from"));
                availableTo   = Instant.parse(json.getString("to"));

                Platform.runLater(() -> {
                    LocalDate fromDate = LocalDateTime
                            .ofInstant(availableFrom, ZoneOffset.UTC)
                            .toLocalDate();
                    LocalDate toDate = LocalDateTime
                            .ofInstant(availableTo, ZoneOffset.UTC)
                            .toLocalDate();

                    startDatePicker.setValue(fromDate);
                    endDatePicker.setValue(toDate);
                    startTimeField.setText(
                            LocalDateTime.ofInstant(availableFrom, ZoneOffset.UTC)
                                    .toLocalTime().toString()
                    );
                    endTimeField.setText(
                            LocalDateTime.ofInstant(availableTo, ZoneOffset.UTC)
                                    .toLocalTime().toString()
                    );

                    statusLabel.setText(String.format(
                            "Доступно: %s … %s",
                            UI_HOUR_FMT.format(LocalDateTime.ofInstant(availableFrom, ZoneOffset.UTC)),
                            UI_HOUR_FMT.format(LocalDateTime.ofInstant(availableTo,   ZoneOffset.UTC))
                    ));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        statusLabel.setText("Не удалось получить диапазон данных")
                );
            }
        }).start();

        // 2) Настройка колонок

        // Час — просто строка
        hourColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getHour())
        );
        // Убираем любой cellFactory для hourColumn — он не нужен

        // Проценты как числа
        communityDepletedColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getCommunityDepleted())
        );
        gridPortionColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getGridPortion())
        );

        // Форматируем проценты с двумя знаками и знаком '%'
        communityDepletedColumn.setCellFactory(col -> new TableCell<EnergyDataFX, Number>() {
            @Override protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null
                        ? null
                        : String.format("%.2f%%", val.doubleValue()));
            }
        });
        gridPortionColumn.setCellFactory(col -> new TableCell<EnergyDataFX, Number>() {
            @Override protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null
                        ? null
                        : String.format("%.2f%%", val.doubleValue()));
            }
        });

        // 3) Обработчики кнопок
        getCurrentButton   .setOnAction(e -> handleGetCurrentData());
        getHistoricalButton.setOnAction(e -> handleGetHistoricalData());
    }
    public static void setRestClient(RestClient client) {
        restClient = client;
    }



    /**
     * Универсальный парсер ISO-строк:
     *  - если есть суффикс Z или смещение → Instant.parse
     *  - иначе → LocalDateTime.parse
     */
    private LocalDateTime parseToLocalDateTime(String iso) {
        try {
            Instant inst = Instant.parse(iso);
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(iso);
        }
    }

    @FXML
    private void handleGetCurrentData() {
        getCurrentButton.setDisable(true);
        new Thread(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Статус: Загружаем текущее…"));
                JSONObject json = new JSONObject(restClient.getCurrentEnergyData());

                String isoHour = json.getString("hour");
                double cd      = json.getDouble("communityDepleted");
                double gp      = json.getDouble("gridPortion");

                // Новый код: парсим в Instant и форматируем в UTC
                Instant inst = Instant.parse(isoHour);
                String displayHour = LocalDateTime
                        .ofInstant(inst, ZoneOffset.UTC)
                        .format(UI_HOUR_FMT);

                String out = String.format(Locale.US,
                        "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%",
                        displayHour, cd, gp
                );

                Platform.runLater(() -> {
                    outputArea.setText(out);
                    statusLabel.setText("Статус: Текущее загружено");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR,
                            "Не удалось получить текущее значение:\n" + ex.getMessage(),
                            ButtonType.OK).showAndWait();
                    statusLabel.setText("Статус: Ошибка при загрузке");
                });
            } finally {
                Platform.runLater(() -> getCurrentButton.setDisable(false));
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

            getHistoricalButton.setDisable(true);
            new Thread(() -> {
                try {
                    Platform.runLater(() ->
                            statusLabel.setText("Статус: Загружаем историю…")
                    );

                    String resp = restClient.getHistoricalEnergyData(
                            startI.atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT),
                            endI  .atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FMT)
                    );
                    JSONArray arr = new JSONArray(resp);
                    ObservableList<EnergyDataFX> list = FXCollections.observableArrayList();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String isoHour = o.getString("hour");

                        // Парсим через Instant и форматируем в UTC
                        Instant inst = Instant.parse(isoHour);
                        LocalDateTime dtUtc = LocalDateTime.ofInstant(inst, ZoneOffset.UTC);
                        String displayHour = dtUtc.format(UI_HOUR_FMT);

                        double cd = o.getDouble("communityDepleted");
                        double gp = o.getDouble("gridPortion");

                        sb.append(String.format(Locale.US,
                                "Hour: %s\nCommunity Depleted: %.2f%%\nGrid Portion: %.2f%%\n\n",
                                displayHour, cd, gp
                        ));

                        list.add(new EnergyDataFX(displayHour, cd, gp));
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
                        new Alert(Alert.AlertType.ERROR,
                                "Не удалось загрузить историю:\n" + ex.getMessage(),
                                ButtonType.OK).showAndWait();
                        statusLabel.setText("Статус: Ошибка загрузки истории");
                    });
                } finally {
                    Platform.runLater(() ->
                            getHistoricalButton.setDisable(false)
                    );
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
