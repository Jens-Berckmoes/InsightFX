package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class ExportServiceTest {

    private ExportService exportService;
    private Path tempFile;

    @BeforeEach
    void testSetup() throws IOException {
        exportService = new ExportServiceImpl();
        tempFile = Files.createTempFile("test-export", ".csv");
    }

    @AfterEach
    void testCleanup() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    private record DummyRow(String category, String amount) implements ExportableRow {

        @Override
        public Map<String, Object> toRow() {
            return Map.of("Category", category, "Amount", amount);
        }
    }

    private static List<ExportableRow> sampleRows() {
        return List.of(
                new DummyRow("Food", "123.45"),
                new DummyRow("Travel", "-42.00")
        );
    }

    @Test
    void testExportCsvSuccessfully(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("data.csv");
        final List<ExportableRow> rows = sampleRows();

        exportService.export(rows, target, ExportType.CSV);

        assertThat(Files.exists(target)).isTrue();
        final String content = Files.readString(target);

        assertThat(content)
                .contains("Category,Amount")
                .contains("Food,123.45")
                .contains("Travel,-42.00");
    }

    @Test
    void testExportEuropeanCsvSuccessfully(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("data_eu.csv");
        final List<ExportableRow> rows = sampleRows();

        exportService.export(rows, target, ExportType.EUROPEAN_CSV);

        final String content = Files.readString(target);
        assertThat(content)
                .contains("Category;Amount")
                .contains("Food;123.45");
    }

    @Test
    void testEmptyRowsCsvDoNotFail(@TempDir final Path tempDir) {
        final Path target = tempDir.resolve("empty.csv");

        assertThatCode(() -> exportService.export(Collections.emptyList(), target, ExportType.CSV))
                .doesNotThrowAnyException();

        assertThat(Files.exists(target)).isFalse();
    }

    @Test
    void tesCsvHeaderOrderConsistent(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("header.csv");
        final List<ExportableRow> rows = List.of(new DummyRow("A", "1"));

        exportService.export(rows, target, ExportType.CSV);

        final List<String> lines = Files.readAllLines(target);
        assertThat(lines.getFirst()).isEqualTo("Category,Amount");
    }

    @Test
    void testCsvSpecialCharacters(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("special.csv");
        final List<ExportableRow> rows = List.of(
                new DummyRow("Foo,Bar", "1"),
                new DummyRow("Baz;Qux", "2")
        );

        exportService.export(rows, target, ExportType.CSV);

        final List<String> lines = Files.readAllLines(target);
        assertThat(lines).contains("Foo,Bar,1", "Baz;Qux,2");
    }

    @Test
    void testCsvCannotBeWritten(@TempDir final Path tempDir) throws IOException {
        final Path invalidTarget = tempDir.resolve("cannotWrite.csv");
        final List<ExportableRow> rows = sampleRows();

        Files.createDirectory(invalidTarget);

        assertThatThrownBy(() -> exportService.export(rows, invalidTarget, ExportType.CSV))
                .isInstanceOf(IOException.class);
    }

    @Test
    void testExportPdfSuccessfully(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("report.pdf");
        final List<ExportableRow> rows = sampleRows();

        exportService.export(rows, target, ExportType.PDF);

        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.size(target)).isGreaterThan(0);

        try (final PDDocument doc = Loader.loadPDF(target.toFile())) {
            final String text = new PDFTextStripper().getText(doc);
            assertThat(text)
                    .contains("InsightFX Export Summary")
                    .contains("Food")
                    .contains("Travel")
                    .contains("123.45");
        }
    }

    @Test
    void testExportPdfWithoutChart(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("report_no_chart.pdf");
        final List<ExportableRow> rows = sampleRows();

        exportService.export(rows, target, ExportType.PDF, tempDir.resolve("non_existing.png"));

        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.size(target)).isGreaterThan(0);
    }

    @Test
    void testEmptyRowsPdfDoNotFail(@TempDir final Path tempDir) {
        final Path target = tempDir.resolve("empty.pdf");

        assertThatCode(() -> exportService.export(Collections.emptyList(), target, ExportType.PDF))
                .doesNotThrowAnyException();
    }

    @Test
    void testPdfInvalidPathThrows() {
        final Path invalidPath = Path.of("/this/path/does/not/exist/report.pdf");
        final List<ExportableRow> rows = sampleRows();

        assertThatThrownBy(() -> exportService.export(rows, invalidPath, ExportType.PDF))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error exporting PDF");
    }

    @Test
    void testNullExportTypeThrows(@TempDir final Path tempDir) {
        final Path target = tempDir.resolve("invalid.pdf");
        final List<ExportableRow> rows = sampleRows();

        assertThatThrownBy(() -> exportService.export(rows, target, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported export type");
    }

    @Test
    void testUnsupportedExportTypeThrows(@TempDir final Path tempDir) {
        final Path target = tempDir.resolve("unknown.pdf");
        final List<ExportableRow> rows = sampleRows();

        assertThatThrownBy(() -> exportService.export(rows, target, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCsvEdgeCases(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("edge_cases.csv");
        final List<ExportableRow> rows = List.of(
                new DummyRow("Normal", "100"),
                new DummyRow("Comma,Value", "200"),
                new DummyRow("Semicolon;Value", "300"),
                new DummyRow("Quotes\"Test", "400"),
                new DummyRow("Negative", "-500"),
                new DummyRow("Zero", "0"),
                new DummyRow("", "999999999999999999")
        );

        exportService.export(rows, target, ExportType.CSV);

        assertThat(Files.exists(target)).isTrue();
        final List<String> lines = Files.readAllLines(target);

        assertThat(lines).contains(
                "Category,Amount",
                "Normal,100",
                "Comma,Value,200",
                "Semicolon;Value,300",
                "Quotes\"Test,400",
                "Negative,-500",
                "Zero,0",
                ",999999999999999999"
        );
    }

    @Test
    void testPdfEdgeCases(@TempDir final Path tempDir) throws IOException {
        final Path target = tempDir.resolve("edge_cases.pdf");
        final List<ExportableRow> rows = List.of(
                new DummyRow("Normal", "100"),
                new DummyRow("Comma,Value", "200"),
                new DummyRow("Semicolon;Value", "300"),
                new DummyRow("Quotes\"Test", "400"),
                new DummyRow("Negative", "-500"),
                new DummyRow("Zero", "0"),
                new DummyRow("", "999999999999999999")
        );

        exportService.export(rows, target, ExportType.PDF);

        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.size(target)).isGreaterThan(0);

        try (final PDDocument doc = Loader.loadPDF(target.toFile())) {
            final String text = new PDFTextStripper().getText(doc);

            assertThat(text).contains("InsightFX Export Summary");

            assertThat(text)
                    .contains("Normal", "100")
                    .contains("Comma,Value", "200")
                    .contains("Semicolon;Value", "300")
                    .contains("Quotes\"Test", "400")
                    .contains("Negative", "-500")
                    .contains("Zero", "0")
                    .contains("999999999999999999");
        }
    }

}

