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
        // Подменяем REST-клиент ДО того, как FXML вызовет initialize()
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
                      {"hour":"2025-06-22T21:00:00Z","communityDepleted":29.05,"gridPortion":13.20},
                      {"hour":"2025-06-22T22:00:00Z","communityDepleted":33.68,"gridPortion":0.08}
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
    void testGetCurrentPopulatesOutput(FxRobot robot) {
        // Нажимаем кнопку «Get Current»
        robot.clickOn("#getCurrentButton");
        WaitForAsyncUtils.waitForFxEvents();

        // Проверяем статус
        Label status = robot.lookup("#statusLabel").queryAs(Label.class);
        assertEquals("Статус: Текущее загружено", status.getText());

        // Проверяем содержимое TextArea
        TextArea out = robot.lookup("#outputArea").queryAs(TextArea.class);
        String txt = out.getText();
        assertTrue(txt.contains("Community Depleted: 29.05%"),
                "Ожидали «Community Depleted: 29.05%», получили:\n" + txt);
        assertTrue(txt.contains("Grid Portion: 13.20%"),
                "Ожидали «Grid Portion: 13.20%», получили:\n" + txt);
    }

    @Test
    void testGetHistoricalPopulatesTableOnly(FxRobot robot) {
        // Нажимаем кнопку «Get Historical»
        robot.clickOn("#getHistoricalButton");
        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        TableView<EnergyDataFX> table = robot.lookup("#historyTable")
                .queryTableView();

        // Должны увидеть ровно 2 строки
        assertEquals(2, table.getItems().size(), "Неверное число строк в таблице");

        EnergyDataFX first  = table.getItems().get(0);
        EnergyDataFX second = table.getItems().get(1);

        assertEquals("22.06.2025 21:00", first.getHour());
        assertEquals(29.05, first.getCommunityDepleted(), 1e-6);
        assertEquals(13.20, first.getGridPortion(),       1e-6);

        assertEquals("22.06.2025 22:00", second.getHour());
        assertEquals(33.68, second.getCommunityDepleted(), 1e-6);
        assertEquals( 0.08, second.getGridPortion(),       1e-6);
    }
}
