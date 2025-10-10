package be.jensberckmoes.insightfx.converter;
import java.math.BigDecimal;
import java.util.Objects;

import be.jensberckmoes.insightfx.exception.CsvParsingException;
import com.opencsv.bean.AbstractBeanField;
/**
 * Converter for BigDecimal velden in CSV.
 * throws CsvParsingException when invalid or empty values received or when amount received is zero.
 */
public class BigDecimalConverter extends AbstractBeanField<BigDecimal, String> {
    @Override
    protected Object convert(final String value) {
        if (Objects.isNull(value) || value.isBlank()) throw new CsvParsingException("Amount can't be empty");
        final BigDecimal decimal = new BigDecimal(value.replace(",", ".").trim());
        if(decimal.equals(BigDecimal.ZERO)) throw new CsvParsingException("Amount can't be zero");
        return decimal;
    }
}
