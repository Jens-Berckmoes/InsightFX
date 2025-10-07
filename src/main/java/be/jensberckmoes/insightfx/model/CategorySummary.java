package be.jensberckmoes.insightfx.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class CategorySummary implements ExportableRow {
    private final String category;
    private int count = 0;
    private BigDecimal total = BigDecimal.ZERO;

    public CategorySummary(final String category) {
        this.category = category;
    }

    public CategorySummary(final String category, final BigDecimal total) {
        this.category = category;
        this.total = total;
    }

    public void addTransaction(final BigDecimal amount) {
        total = total.add(amount);
        count++;
    }

    @Override
    public Map<String, Object> toRow() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("Category", category);
        map.put("Amount", total);
        return map;
    }
}