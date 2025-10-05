package be.jensberckmoes.insightfx.model;

import be.jensberckmoes.insightfx.exception.CsvParsingException;
import com.opencsv.bean.AbstractBeanField;

public class RequiredValueConverter extends AbstractBeanField<String, String> {
    @Override
    protected Object convert(final String value) {
        if (value == null || value.isBlank()) {
            throw new CsvParsingException("Field is required!");
        }
        return value.trim();
    }
}
