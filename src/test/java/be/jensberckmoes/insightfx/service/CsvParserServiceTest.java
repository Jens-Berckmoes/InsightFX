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
        assertEquals("INSTANTOVERSCHRIJVING NAAR", first.getDescription());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());

    }

    @Test
    public void shouldReturnEmptyListForEmptyFile() {
        final InputStream csvStream = getClass().getResourceAsStream("/empty.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);

        assertEquals(0, records.size());
    }

    @Test
    public void shouldThrowRunTimeExceptionForMalformedDateCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-date.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForMalformedAmountCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-amount.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForNullAmountCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-null-amount.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForEmptyDescriptionCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-empty-description.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForTooFewHeadersColumnsCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-headers.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldThrowRunTimeExceptionForZeroAmountCsv() {
        final InputStream csvStream = getClass().getResourceAsStream("/malformed-amount-zero.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        assertThrows(RuntimeException.class, () -> parser.parse(csvStream));
    }

    @Test
    public void shouldHandleQuotes() {
        final InputStream csvStream = getClass().getResourceAsStream("/quotes-test-data.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);
        assertEquals(1, records.size());

        final DataRecord first = records.getFirst();
        assertEquals("2025-10-03", first.getCurrencyDate().toString());
        assertEquals("Omschrijving met ; en komma, en zelfs \"quotes\"", first.getDescription());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());
    }

    @Test
    public void shouldHandleMultipleLines() {
        final InputStream csvStream = getClass().getResourceAsStream("/multiple-lines.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);
        assertEquals(1, records.size());

        final DataRecord first = records.getFirst();
        assertEquals("2025-10-03", first.getCurrencyDate().toString());
        assertEquals("Omschrijving met ;\nen komma, en zelfs \"quotes\"", first.getDescription());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());
    }

    @Test
    public void shouldTrimMultipleLines() {
        final InputStream csvStream = getClass().getResourceAsStream("/multiple-whitespace.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);
        assertEquals(1, records.size());

        final DataRecord first = records.getFirst();
        assertEquals("2025-10-03", first.getCurrencyDate().toString());
        assertEquals("Omschrijving met ; en komma, en zelfs \"quotes\"", first.getDescription());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());
    }

    @Test
    public void shouldWorkWithUtf8() {
        final InputStream csvStream = getClass().getResourceAsStream("/utf8.csv");
        assertNotNull(csvStream, "Test CSV moet bestaan");

        final List<DataRecord> records = parser.parse(csvStream);
        assertEquals(1, records.size());

        final DataRecord first = records.getFirst();
        assertEquals("2025-10-03", first.getCurrencyDate().toString());
        assertEquals("Café avec €, é, ü, ç", first.getDescription());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());
    }
}
