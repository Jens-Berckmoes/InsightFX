package be.jensberckmoes.insightfx.controller;

import be.jensberckmoes.insightfx.model.CategorySummary;
import be.jensberckmoes.insightfx.model.DataRecord;
import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;
import be.jensberckmoes.insightfx.service.AnalysisService;
import be.jensberckmoes.insightfx.service.CsvParserService;
import be.jensberckmoes.insightfx.service.ExportService;
import be.jensberckmoes.insightfx.service.ExportServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    public ComboBox<ExportType> exportTypeComboBox;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab analysisTab;
    @FXML
    private Tab chartTab;
    @FXML
    public Tab dataTab;
    @FXML
    private TableView<CategorySummary> analysisTable;
    @FXML
    private TableColumn<CategorySummary, String> categoryColumn;
    @FXML
    private TableColumn<CategorySummary, Integer> countColumn;
    @FXML
    private TableColumn<CategorySummary, BigDecimal> totalColumn;
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
    private StackPane chartPane;
    @FXML
    private Button analyzeButton, chartButton, exportButton;

    private final CsvParserService csvParserService = new CsvParserService();
    private final AnalysisService analysisService = new AnalysisService();
    private final ExportService exportService = new ExportServiceImpl();
    private List<CategorySummary> results = new ArrayList<>();
    private List<DataRecord> records = new ArrayList<>();

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
        exportTypeComboBox.getItems().setAll(ExportType.values());
        exportTypeComboBox.getSelectionModel().select(ExportType.CSV);
    }

    @FXML
    private void onLoadCsv() {
        resetForNewCSV();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a CSV-file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        final File file = fileChooser.showOpenDialog(null);
        if (Objects.isNull(file)) return;

        try (final InputStream stream = new FileInputStream(file)) {
            records = csvParserService.parse(stream);
            tableView.setItems(FXCollections.observableArrayList(records));

            final boolean hasData = !records.isEmpty();
            analyzeButton.setDisable(!hasData);
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

        results = analysisService.analyse(records);
        analysisTable.setItems(FXCollections.observableArrayList(results));

        analysisTab.setDisable(false);
        tabPane.getSelectionModel().select(analysisTab);
        final boolean hasData = !results.isEmpty();
        chartButton.setDisable(!hasData);

        statusLabel.setText("Analysis completed: " + results.size() + " categories");
    }

    @FXML
    private void onChart() {
        if (results.isEmpty()) return;

        final List<CategorySummary> categoryResults = results.stream()
                .filter(r -> !r.getCategory().equals("Total Income") &&
                        !r.getCategory().equals("Total Expenses") &&
                        !r.getCategory().equals("Balance"))
                .toList();

        final double totalAmount = categoryResults.stream()
                .mapToDouble(CategorySummary::getCount)
                .sum();

        final ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                categoryResults.stream()
                        .map(r -> new PieChart.Data(getNameWithPercent(r, totalAmount), r.getCount()))
                        .toList()
        );

        final PieChart chart = new PieChart(pieData);
        chart.setTitle("Expenses");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.LEFT);

        chartPane.getChildren().clear();
        chartPane.getChildren().add(chart);

        chartTab.setDisable(false);
        tabPane.getSelectionModel().select(chartTab);
        final boolean hasData = !categoryResults.isEmpty();
        exportButton.setDisable(!hasData);

        statusLabel.setText("Chart generated: " + results.size() + " categories");

    }

    @FXML
    private void onExport() {
        if (results.isEmpty()) return;

        final ExportType selectedType = exportTypeComboBox.getSelectionModel().getSelectedItem();
        if (selectedType == null) return;

        final File file = chooseExportFile(selectedType);
        if (file == null){
            log.info("Export cancelled by user for type {}", selectedType);
            return;
        }

        exportResults(selectedType, file.toPath(), results);
    }

    private File chooseExportFile(final ExportType type) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Dialog");
        fileChooser.setInitialFileName("InsightFX_export");

        switch (type) {
            case CSV,EUROPEAN_CSV -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV file", "*.csv")
            );
            case PDF -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF file", "*.pdf")
            );
        }

        return fileChooser.showSaveDialog(null);
    }

    private void exportResults(final ExportType type, final Path path, final List<? extends ExportableRow> rows) {
        log.info("Starting export: type={}, target={}", type, path.toAbsolutePath());
        try {
            exportService.export(rows, path, type);
            log.info("Export completed successfully: {}", path.toAbsolutePath());
            statusLabel.setText("Export completed: " + path.toAbsolutePath());
            statusLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        } catch (final IOException e) {
            log.error("Export failed: {}", e.getMessage(), e);
            statusLabel.setText("Export failed: " + e.getMessage());
            statusLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        }
    }

    private void resetForNewCSV() {
        analyzeButton.setDisable(true);
        chartButton.setDisable(true);
        exportButton.setDisable(true);

        analysisTab.setDisable(true);
        chartTab.setDisable(true);

        results.clear();
        records.clear();
        chartPane.getChildren().clear();
        analysisTable.getItems().clear();

        statusLabel.setText("CSV loaded: none");
        tabPane.getSelectionModel().select(dataTab);
    }

    private String getNameWithPercent(final CategorySummary categorySummary, final double totalAmount) {
        final double percent = categorySummary.getCount() / totalAmount * 100;
        return categorySummary.getCategory() + " (" + String.format("%.1f", percent) + "%)";
    }

}
