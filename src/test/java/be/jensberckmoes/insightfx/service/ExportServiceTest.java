package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExportServiceTest {

    private ExportService exportService;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        exportService = new ExportServiceImpl();
        tempFile = Files.createTempFile("test-export", ".csv");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    private ExportableRow createRow(String category, BigDecimal amount) {
        return () -> Map.of(
                "Category", category,
                "Amount", amount
        );
    }

    @Test
    void testCsvExportBasic() throws IOException {
        final List<ExportableRow> rows = List.of(
                createRow("Food", BigDecimal.valueOf(123)),
                createRow("Transport", BigDecimal.valueOf(456))
        );

        exportService.export(rows, tempFile, ExportType.CSV);

        final List<String> lines = Files.readAllLines(tempFile);
        assertEquals(3, lines.size());
        assertEquals("Category,Amount", lines.getFirst());
        assertTrue(lines.contains("Food,123"));
        assertTrue(lines.contains("Transport,456"));
    }

    @Test
    void testEuropeanCsvExport() throws IOException {
        final List<ExportableRow> rows = List.of(
                createRow("Food", BigDecimal.valueOf(123))
        );

        exportService.export(rows, tempFile, ExportType.EUROPEAN_CSV);

        final List<String> lines = Files.readAllLines(tempFile);
        assertEquals(2, lines.size());
        assertEquals("Category;Amount", lines.get(0));
        assertEquals("Food;123", lines.get(1));
    }

    @Test
    void testEmptyRowsDoesNotFail() throws IOException {
        final List<ExportableRow> rows = Collections.emptyList();

        exportService.export(rows, tempFile, ExportType.CSV);

        final List<String> lines = Files.readAllLines(tempFile);
        assertTrue(lines.isEmpty() || Files.size(tempFile) == 0);
    }

    @Test
    void testUnsupportedExportType() {
        final List<ExportableRow> rows = List.of(createRow("Foo", BigDecimal.ONE));

        final Exception exception = assertThrows(IllegalArgumentException.class,
                () -> exportService.export(rows, tempFile, null));

        assertTrue(exception.getMessage().contains("Unsupported export type"));
    }

    @Test
    void testCsvHeaderOrderConsistent() throws IOException {
        final List<ExportableRow> rows = List.of(createRow("A", BigDecimal.ONE));

        exportService.export(rows, tempFile, ExportType.CSV);

        final List<String> lines = Files.readAllLines(tempFile);
        assertEquals("Category,Amount", lines.getFirst());
    }

    @Test
    void testCsvSpecialCharacters() throws IOException {
        final List<ExportableRow> rows = List.of(
                createRow("Foo,Bar", BigDecimal.valueOf(1)),
                createRow("Baz;Qux", BigDecimal.valueOf(2))
        );

        exportService.export(rows, tempFile, ExportType.CSV);

        final List<String> lines = Files.readAllLines(tempFile);
        assertTrue(lines.contains("Foo,Bar,1"));
        assertTrue(lines.contains("Baz;Qux,2"));
    }
}
