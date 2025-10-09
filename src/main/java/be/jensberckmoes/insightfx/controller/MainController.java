package be.jensberckmoes.insightfx.controller;

import be.jensberckmoes.insightfx.model.*;
import be.jensberckmoes.insightfx.service.AnalysisService;
import be.jensberckmoes.insightfx.service.CsvParserService;
import be.jensberckmoes.insightfx.service.ExportService;
import be.jensberckmoes.insightfx.service.ExportServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
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

    private final List<CategorySummary> results = new ArrayList<>();
    private final List<DataRecord> records = new ArrayList<>();
    private AnalysisResult analysisResult;

    @FXML
    public void initialize() {
        setupTableColumns();
        rightAlignColumn(amountColumn);
        rightAlignColumn(totalColumn);
        exportTypeComboBox.setDisable(true);
    }

    /**
     * Configures the TableView columns to display the correct properties of DataRecord and CategorySummary.
     */
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCurrencyDate().toString()));
        descriptionColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescription()));
        amountColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getAmount()));
        commentsColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getComments()));
        categoryColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategory()));
        countColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCount()));
        totalColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTotal()));
    }

    /**
     * Aligns the given TableColumn of BigDecimal values to the right.
     *
     * @param column the column to align
     * @param <S>    the type of the TableView items
     */
    private <S> void rightAlignColumn(final TableColumn<S, BigDecimal> column) {
        column.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(final BigDecimal item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });
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
            records.clear();
            records.addAll(csvParserService.parse(stream));
            tableView.setItems(FXCollections.observableArrayList(records));
            analyzeButton.setDisable(records.isEmpty());
            log.info("CSV loaded successfully: {} records", records.size());
            statusLabel.setText("CSV loaded: " + records.size() + " records");
        } catch (final Exception e) {
            log.error("Failed to load CSV: {}", e.getMessage(), e);
            new Alert(Alert.AlertType.ERROR, "Error loading CSV:\n" + e.getMessage(), ButtonType.OK).showAndWait();
            statusLabel.setText("Problem loading CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onAnalyze() {
        if (records.isEmpty()) {
            statusLabel.setText("No data to analyse");
            return;
        }

        results.clear();
        results.addAll(analysisService.analyse(records));
        analysisTable.setItems(FXCollections.observableArrayList(results));

        analysisTab.setDisable(false);
        tabPane.getSelectionModel().select(analysisTab);
        chartButton.setDisable(results.isEmpty());

        log.info("Analysis completed: {} categories", results.size());
        statusLabel.setText("Analysis completed: " + results.size() + " categories");
    }

    @FXML
    private void onChart() {
        if (results.isEmpty()) return;

        final List<CategorySummary> chartData = filterChartCategories(results);

        final double totalAmount = chartData.stream()
                .mapToDouble(CategorySummary::getCount)
                .sum();

        final ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                chartData.stream()
                        .map(r -> new PieChart.Data(getNameWithPercent(r, totalAmount), r.getCount()))
                        .toList()
        );

        final PieChart chart = new PieChart(pieData);
        chart.setTitle("Expenses");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.LEFT);

        chartPane.getChildren().clear();
        chartPane.getChildren().setAll(chart);
        chartTab.setDisable(false);
        tabPane.getSelectionModel().select(chartTab);
        exportButton.setDisable(chartData.isEmpty());
        loadExportComboBox();

        analysisResult = prepareChartTempFile(chart);
        log.info("Chart generated with {} categories", chartData.size());
        statusLabel.setText("Chart generated: " + results.size() + " categories");
    }

    /**
     * Filters out summary categories (Income, Expenses, Balance) from chart data.
     *
     * @param categories list of CategorySummary objects
     * @return filtered list suitable for chart display
     */
    private List<CategorySummary> filterChartCategories(final List<CategorySummary> categories) {
        return categories.stream()
                .filter(r -> !r.getCategory().equals("Total Income") &&
                        !r.getCategory().equals("Total Expenses") &&
                        !r.getCategory().equals("Balance"))
                .toList();
    }

    /**
     * Generates a temporary PNG image of the chart for export and returns it wrapped in an AnalysisResult.
     *
     * @param chart the JavaFX PieChart to snapshot
     * @return AnalysisResult containing current results and chart image path
     */
    private AnalysisResult prepareChartTempFile(final PieChart chart) {
        final WritableImage fxImage = chart.snapshot(new SnapshotParameters(), null);
        final BufferedImage buffered = SwingFXUtils.fromFXImage(fxImage, null);
        try {
            final File tmp = File.createTempFile("insightfx_chart_", ".png", new File(System.getProperty("java.io.tmpdir")));
            ImageIO.write(buffered, "png", tmp);
            return new AnalysisResult(results, tmp.toPath());
        } catch (final IOException e) {
            log.error("Failed to create chart image: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates and enables the export type ComboBox with all available ExportType values.
     */
    private void loadExportComboBox() {
        exportTypeComboBox.setDisable(false);
        exportTypeComboBox.getItems().setAll(ExportType.values());
        exportTypeComboBox.getSelectionModel().select(ExportType.CSV);
    }

    @FXML
    private void onExport() {
        if (results.isEmpty() || Objects.isNull(analysisResult)) return;

        final ExportType selectedType = exportTypeComboBox.getSelectionModel().getSelectedItem();
        if (Objects.isNull(selectedType)) return;

        final File file = chooseExportFile(selectedType);
        if (Objects.isNull(file)) {
            log.info("Export cancelled by user for type {}", selectedType);
            return;
        }

        exportResults(selectedType, file.toPath(), results);

    }

    /**
     * Opens a FileChooser for selecting the target file path for export.
     *
     * @param type the selected export type
     * @return the chosen file or null if cancelled
     */
    private File chooseExportFile(final ExportType type) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Dialog");
        fileChooser.setInitialFileName("InsightFX_export");

        switch (type) {
            case CSV, EUROPEAN_CSV -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV file", "*.csv")
            );
            case PDF -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF file", "*.pdf")
            );
        }

        return fileChooser.showSaveDialog(null);
    }

    /**
     * Handles the actual export process, including optional chart image deletion.
     *
     * @param type the export type
     * @param path the target file path
     * @param rows the data to export
     */
    private void exportResults(final ExportType type, final Path path, final List<? extends ExportableRow> rows) {
        log.info("Starting export: type={}, target={}", type, path.toAbsolutePath());
        try {
            final Path imagePath = analysisResult.getChartTempPath().orElse(null);
            exportService.export(rows, path, type, imagePath);
            log.info("Export completed successfully: {}", path.toAbsolutePath());
            statusLabel.setText("Export completed: " + path.toAbsolutePath());
            statusLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            analysisResult.getChartTempPath().ifPresent(p -> {
                try {
                    Files.deleteIfExists(p);
                    log.debug("Deleting of image succeeded");
                } catch (Exception e) {
                    log.error("Deleting of image failed: {}", e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        } catch (final IOException e) {
            log.error("Export failed: {}", e.getMessage(), e);
            statusLabel.setText("Export failed: " + e.getMessage());
            statusLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        }
    }

    /**
     * Resets UI state and clears data when a new CSV file is loaded.
     */
    private void resetForNewCSV() {
        analyzeButton.setDisable(true);
        chartButton.setDisable(true);
        exportButton.setDisable(true);
        exportTypeComboBox.setDisable(true);

        analysisTab.setDisable(true);
        chartTab.setDisable(true);

        results.clear();
        records.clear();
        chartPane.getChildren().clear();
        analysisTable.getItems().clear();

        statusLabel.setText("CSV loaded: none");
        tabPane.getSelectionModel().select(dataTab);
    }

    /**
     * Returns the category name with percentage of total for chart display labels.
     *
     * @param categorySummary the category data
     * @param totalAmount     total sum of all chart categories
     * @return string like "Groceries (25.0%)"
     */
    private String getNameWithPercent(final CategorySummary categorySummary, final double totalAmount) {
        final double percent = categorySummary.getCount() / totalAmount * 100;
        return categorySummary.getCategory() + " (" + String.format("%.1f", percent) + "%)";
    }

}
