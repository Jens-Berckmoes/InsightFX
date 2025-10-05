package be.jensberckmoes.insightfx.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataRecord {
    @CsvCustomBindByName(column = "Omschrijving", converter = RequiredTrimmedStringConverter.class)
    private String description;

    @CsvCustomBindByName(column = "Valuta", converter = LocalDateConverter.class)
    private LocalDate currencyDate;

    @CsvCustomBindByName(column = "Bedrag", converter = BigDecimalConverter.class)
    private BigDecimal amount;

    @CsvBindByName(column = "Vrije mededeling")
    private String comments;

}
