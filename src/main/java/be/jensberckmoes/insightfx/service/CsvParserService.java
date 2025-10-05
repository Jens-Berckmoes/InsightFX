package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.exception.CsvParsingException;
import be.jensberckmoes.insightfx.model.DataRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class CsvParserService {
    public List<DataRecord> parse(final InputStream csvStream) {
        try (Reader reader = new InputStreamReader(csvStream)) {
            final HeaderColumnNameMappingStrategy<DataRecord> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(DataRecord.class);

            final CsvToBean<DataRecord> csvToBean = new CsvToBeanBuilder<DataRecord>(reader)
                    .withType(DataRecord.class)
                    .withMappingStrategy(strategy)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        } catch (Exception e) {
            throw new CsvParsingException(e);
        }
    }
}
