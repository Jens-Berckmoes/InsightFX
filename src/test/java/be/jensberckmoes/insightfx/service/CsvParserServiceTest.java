package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.DataRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals("INSTANTOVERSCHRIJVING NAAR           03-10 BE97 7360 7410 6549 BANKIER BEGUNSTIGDE: KREDBEBBXXX BERCKMOES J & DUMONT M TERUGSTORTEN OM 14.54 UUR MET KBC MOBILE", first.getDescription());
        assertEquals(new BigDecimal("-11.6"), first.getAmount());
        assertEquals("Terugstorten", first.getComments());

    }
}
