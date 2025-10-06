package be.jensberckmoes.insightfx.controller;

import be.jensberckmoes.insightfx.model.DataRecord;
import be.jensberckmoes.insightfx.service.CsvParserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MainController {

    @FXML
    private TableView<DataRecord> tableView;
    @FXML
    private TableColumn<DataRecord, String> dateColumn;
    @FXML
    private TableColumn<DataRecord, String> descriptionColumn;
    @FXML
    private TableColumn<DataRecord, BigDecimal> amountColumn;
    @FXML
    private TableColumn<DataRecord, String> commentsColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private Button analyzeButton, chartButton, exportButton;

    private final CsvParserService csvParserService = new CsvParserService();

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCurrencyDate().toString()
        ));
        descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescription()
        ));
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getAmount()
        ));
        commentsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getComments()
        ));
    }

    @FXML
    private void onLoadCsv() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a CSV-file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        final File file = fileChooser.showOpenDialog(null);
        if (Objects.isNull(file)) return;

        try (final InputStream stream = new FileInputStream(file)) {
            final List<DataRecord> records = csvParserService.parse(stream);
            tableView.setItems(FXCollections.observableArrayList(records));

            final boolean hasData = !records.isEmpty();
            analyzeButton.setDisable(!hasData);
            chartButton.setDisable(!hasData);
            exportButton.setDisable(!hasData);
            statusLabel.setText("CSV loaded: " + records.size() + " records");
        } catch (final Exception e) {
            statusLabel.setText("Problem loading CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onAnalyze() {
        statusLabel.setText("Analyse uitvoeren... (nog te implementeren)");
    }

    @FXML
    private void onChart() {
        statusLabel.setText("Grafiek genereren... (nog te implementeren)");
    }

    @FXML
    private void onExport() {
        statusLabel.setText("Exporteren... (nog te implementeren)");
    }
}
