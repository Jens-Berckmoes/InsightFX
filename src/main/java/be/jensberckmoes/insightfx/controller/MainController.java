package be.jensberckmoes.insightfx.controller;

import be.jensberckmoes.insightfx.model.CategorySummary;
import be.jensberckmoes.insightfx.model.DataRecord;
import be.jensberckmoes.insightfx.service.AnalysisService;
import be.jensberckmoes.insightfx.service.CsvParserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MainController {

    @FXML
    public TabPane tabPane;
    @FXML
    public TableView<CategorySummary> analysisTable;
    @FXML
    public TableColumn<CategorySummary, String> categoryColumn;
    @FXML
    public TableColumn<CategorySummary, Integer> countColumn;
    @FXML
    public TableColumn<CategorySummary, BigDecimal> totalColumn;
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
    private Button analyzeButton, chartButton, exportButton, loadButton;
    @FXML
    private Tab analysisTab;

    private final CsvParserService csvParserService = new CsvParserService();
    private final AnalysisService analysisService = new AnalysisService();

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
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategory()
        ));
        countColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getCount()
        ));
        totalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getTotal()
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
            new Alert(Alert.AlertType.ERROR, "Error loading CSV:\n" + e.getMessage(), ButtonType.OK).showAndWait();
            statusLabel.setText("Problem loading CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onAnalyze() {
        final List<DataRecord> records = tableView.getItems();
        if (Objects.isNull(records) || records.isEmpty()) {
            statusLabel.setText("No data to analyse");
            return;
        }

        final List<CategorySummary> results = analysisService.analyse(records);
        analysisTable.setItems(FXCollections.observableArrayList(results));

        analysisTab.setDisable(false);
        tabPane.getSelectionModel().select(analysisTab);

        statusLabel.setText("Analysis completed: " + results.size() + " categories");
    }

    @FXML
    private void onChart() {
        statusLabel.setText("Generate Graphics... (to implement)");
    }

    @FXML
    private void onExport() {
        statusLabel.setText("Exporting... (to implement)");
    }
}
