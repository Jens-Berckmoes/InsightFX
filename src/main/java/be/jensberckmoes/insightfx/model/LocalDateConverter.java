package be.jensberckmoes.insightfx.model;

import com.opencsv.bean.AbstractBeanField;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateConverter extends AbstractBeanField<LocalDate, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected Object convert(final String value) {
        if (Objects.isNull(value) || value.isBlank()) return null;
        return LocalDate.parse(value.trim(), FORMATTER);
    }
}
