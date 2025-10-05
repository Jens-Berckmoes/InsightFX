package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.exception.CsvParsingException;
import be.jensberckmoes.insightfx.model.DataRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class CsvParserService {
    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);

    public List<DataRecord> parse(final InputStream csvStream) {
        log.info("Start parsing CSV stream...");
        try (final Reader reader = new InputStreamReader(csvStream, StandardCharsets.UTF_8)) {
            final HeaderColumnNameMappingStrategy<DataRecord> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(DataRecord.class);

            final CsvToBean<DataRecord> csvToBean = new CsvToBeanBuilder<DataRecord>(reader)
                    .withType(DataRecord.class)
                    .withMappingStrategy(strategy)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            final List<DataRecord> records = csvToBean.parse();
            log.debug("Amount of rows loaded: {}", records.size());
            return records;
        } catch (final Exception e) {
            final Throwable rootCause = Objects.nonNull(e.getCause())? e.getCause() : e;
            log.error("CSV parsing failure: {}", rootCause.getMessage(), e);
            throw new CsvParsingException("Error parsing: " + rootCause.getMessage(), e);
        }
    }
}
