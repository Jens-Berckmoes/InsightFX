package be.jensberckmoes.insightfx.model;
import java.math.BigDecimal;
import java.util.Objects;

import com.opencsv.bean.AbstractBeanField;

public class BigDecimalConverter extends AbstractBeanField<BigDecimal, String> {
    @Override
    protected Object convert(final String value) {
        if (Objects.isNull(value) || value.isBlank()) return null;
        return new BigDecimal(value.replace(",", ".").trim());
    }
}
