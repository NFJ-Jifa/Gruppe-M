package com.gruppem.energygui;

import static org.junit.jupiter.api.Assertions.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


@ExtendWith(ApplicationExtension.class)
class GuiIntegrationTest {

    @Start
    public void start(Stage stage) throws Exception {
        Controller.setRestClient(new RestClient() {
            @Override
            public String getAvailableRange() {
                return "{\"from\":\"2025-06-22T21:00:00Z\",\"to\":\"2025-06-22T22:00:00Z\"}";
            }
            @Override
            public String getCurrentEnergyData() {
                return "{\"hour\":\"2025-06-22T21:00:00Z\",\"communityDepleted\":29.05,\"gridPortion\":13.20}";
            }
            @Override
            public String getHistoricalEnergyData(String start, String end) {
                return """
                    [
                      {"hour":"2025-06-22T21:00:00Z","communityProduced":143.024,"communityUsed":130.101,"gridUsed":14.923,"communityDepleted":29.05,"gridPortion":13.20},
                      {"hour":"2025-06-22T22:00:00Z","communityProduced":150.000,"communityUsed":140.000,"gridUsed":10.000,"communityDepleted":93.33,"gridPortion":6.67}
                    ]
                """;
            }
        });

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/gruppem/energygui/MainView.fxml")
        );
        Parent root = loader.load();
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @Test
    void testGetCurrentUpdatesLabels(FxRobot robot) {
        // Кликаем по кнопке Refresh (fx:id="refreshButton")
        robot.clickOn("#refreshButton");
        WaitForAsyncUtils.waitForFxEvents();

        // Проверяем статус
        Label status = robot.lookup("#statusLabel").queryAs(Label.class);
        assertEquals("Статус: Текущее загружено", status.getText());

        // Проверяем значения лейблов community / grid
        Label community = robot.lookup("#communityLabel").queryAs(Label.class);
        Label grid      = robot.lookup("#gridLabel").queryAs(Label.class);

        assertEquals("29.05% used", community.getText());
        assertEquals("13.20%",     grid.getText());
    }

    @Test
    void testGetHistoricalPopulatesTableAndSummaries(FxRobot robot) {
        // Кликаем по кнопке Show Data (fx:id="getHistoricalButton")
        robot.clickOn("#getHistoricalButton");
        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        TableView<EnergyDataFX> table = robot.lookup("#historyTable")
                .queryTableView();

        // Должны увидеть ровно 2 строки
        assertEquals(2, table.getItems().size(), "Неверное число строк в таблице");

        // Проверяем, что суммарные метки обновились
        Label prodLabel     = robot.lookup("#prodLabel").queryAs(Label.class);
        Label usedLabel     = robot.lookup("#usedLabel").queryAs(Label.class);
        Label gridUsedLabel = robot.lookup("#gridUsedLabel").queryAs(Label.class);

        // Сумма communityProduced = 143.024 + 150.000 = 293.024
        assertTrue(prodLabel.getText().contains("293.024"), "Неверная сумма произведённой энергии");
        // Сумма communityUsed = 130.101 + 140.000 = 270.101
        assertTrue(usedLabel.getText().contains("270.101"), "Неверная сумма потреблённой энергии");
        // Сумма gridUsed = 14.923 + 10.000 = 24.923
        assertTrue(gridUsedLabel.getText().contains("24.923"), "Неверная сумма энергии из сети");
    }
}
