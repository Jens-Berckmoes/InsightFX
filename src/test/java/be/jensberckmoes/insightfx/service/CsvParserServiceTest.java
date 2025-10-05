package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.DataRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvParserServiceTest {

    private CsvParserService parser;

    @BeforeEach
    void setup() {
        parser = new CsvParserService();
    }

    @Test
    public void shouldParseValidCsvIntoRecords() {
        final InputStream csvStream = getClass().getResourceAsStream("/test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);

        assertEquals(7, records.size());

        final DataRecord first = records.getFirst();
        assertEquals("2025-10-03", first.getCurrencyDate().toString());
        assertEquals("INSTANTOVERSCHRIJVING NAAR", first.getDescription().trim());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());

    }

    @Test
    public void shouldReturnEmptyListForEmptyFile() {
        final InputStream csvStream = getClass().getResourceAsStream("/empty-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);

        assertEquals(0, records.size());
    }

    @Test
    public void shouldThrowRunTimeExceptionForMalformedDateCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-date-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForMalformedAmountCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-amount-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForNullAmountCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-null-amount-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForEmptyDescriptionCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-empty-description-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForTooFewHeadersColumnsCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-headers-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }
}
