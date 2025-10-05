package be.jensberckmoes.insightfx.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class DataRecord {
    @CsvBindByName(column = "Omschrijving")
    private String description;

    @CsvCustomBindByName(column = "Valuta", converter = LocalDateConverter.class)
    private LocalDate currencyDate;

    @CsvCustomBindByName(column = "Bedrag", converter = BigDecimalConverter.class)
    private BigDecimal amount;

    @CsvBindByName(column = "Vrije mededeling")
    private String comments;

    public DataRecord() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCurrencyDate() {
        return currencyDate;
    }

    public void setCurrencyDate(LocalDate currencyDate) {
        this.currencyDate = currencyDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRecord that = (DataRecord) o;
        return Objects.equals(description, that.description) && Objects.equals(currencyDate, that.currencyDate) && Objects.equals(amount, that.amount) && Objects.equals(comments, that.comments);
    }

}
