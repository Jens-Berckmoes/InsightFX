package be.jensberckmoes.insightfx.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategorySummary {
    private final String category;
    private int count = 0;
    private BigDecimal total = BigDecimal.ZERO;

    public CategorySummary(String category) {
        this.category = category;
    }

    public CategorySummary(String category, BigDecimal total) {
        this.category = category;
        this.total = total;
    }

    public void addTransaction(BigDecimal amount) {
        total = total.add(amount);
        count++;
    }

}