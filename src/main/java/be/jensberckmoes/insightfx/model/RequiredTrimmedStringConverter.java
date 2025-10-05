package be.jensberckmoes.insightfx.model;

import be.jensberckmoes.insightfx.exception.CsvParsingException;
import com.opencsv.bean.AbstractBeanField;

import java.util.Objects;

public class RequiredTrimmedStringConverter extends AbstractBeanField<String, String> {
    @Override
    protected Object convert(final String value) {
        if (Objects.isNull(value)) {
            throw new CsvParsingException("Field is mandatory");
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new CsvParsingException("Field is mandatory and shouldn't be empty");
        }
        return trimmed;
    }
}
