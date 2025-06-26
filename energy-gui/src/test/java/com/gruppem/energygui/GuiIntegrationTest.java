package com.gruppem.energygui;

import static org.junit.jupiter.api.Assertions.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Integration tests for the JavaFX GUI using TestFX.
 * Tests interaction with a mocked backend via a fake RestClient.
 */
@ExtendWith(ApplicationExtension.class)
class GuiIntegrationTest {

    /**
     * Starts the GUI with a mocked RestClient to simulate backend responses.
     */
    @Start
    public void start(Stage stage) throws Exception {
        // Override RestClient with stubbed responses
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

        // Load FXML and show GUI
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/gruppem/energygui/MainView.fxml")
        );
        Parent root = loader.load();
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    /**
     * Tests that clicking the Refresh button updates the current energy labels.
     */
    @Test
    void testGetCurrentUpdatesLabels(FxRobot robot) {
        // Simulate button click
        robot.clickOn("#refreshButton");
        WaitForAsyncUtils.waitForFxEvents();

        // Check updated status label
        Label status = robot.lookup("#statusLabel").queryAs(Label.class);
        assertEquals("Статус: Текущее загружено", status.getText());

        // Check if labels show expected values
        Label community = robot.lookup("#communityLabel").queryAs(Label.class);
        Label grid      = robot.lookup("#gridLabel").queryAs(Label.class);

        assertEquals("29.05% used", community.getText());
        assertEquals("13.20%",     grid.getText());
    }

    /**
     * Tests that clicking the Show Data button populates the table
     * and displays the correct aggregated energy values.
     */
    @Test
    void testGetHistoricalPopulatesTableAndSummaries(FxRobot robot) {
        // Simulate button click
        robot.clickOn("#getHistoricalButton");
        WaitForAsyncUtils.waitForFxEvents();

        // Get reference to the TableView
        @SuppressWarnings("unchecked")
        TableView<EnergyDataFX> table = robot.lookup("#historyTable").queryTableView();

        // Verify table has 2 rows
        assertEquals(2, table.getItems().size(), "Unexpected number of table rows");

        // Verify total energy labels
        Label prodLabel     = robot.lookup("#prodLabel").queryAs(Label.class);
        Label usedLabel     = robot.lookup("#usedLabel").queryAs(Label.class);
        Label gridUsedLabel = robot.lookup("#gridUsedLabel").queryAs(Label.class);

        // Produced = 143.024 + 150.000 = 293.024
        assertTrue(prodLabel.getText().contains("293.024"), "Incorrect total produced energy");

        // Used = 130.101 + 140.000 = 270.101
        assertTrue(usedLabel.getText().contains("270.101"), "Incorrect total used energy");

        // Grid = 14.923 + 10.000 = 24.923
        assertTrue(gridUsedLabel.getText().contains("24.923"), "Incorrect total grid energy");
    }
}
