package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.CategorySummary;
import be.jensberckmoes.insightfx.model.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Map.entry;

public class AnalysisService {
    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);

    private final Map<String, List<String>> keywordMap = Map.ofEntries(
            entry("Groceries", List.of("AH", "Delhaize", "Lidl", "Colruyt", "Aldi","Carrefour","TOO GOOD TO GO")),
            entry("Subscriptions", List.of("Netflix", "Spotify", "YouTube", "Apple","Disney")),
            entry("Health", List.of("Loes Koolen")),
            entry("Transport", List.of("NMBS", "Uber", "Shell", "Q8","Van raak","dats","Gabriels", "Lukoil")),
            entry("Takeout", List.of("McDonalds","Burger King","The Nile","Hasselt-food")),
            entry("Leasure time", List.of("Bol.com", "Coolblue", "Steam", "MediaMarkt")),
            entry("Bank Costs",List.of("Bijdrage")),
            entry("Clothing", List.of("Torfs")),
            entry("Services", List.of("DIENSTENCHEQUE")),
            entry("Household", List.of("Hasselt-store")),
            entry("Income", List.of("Matt")),
            entry("Pet", List.of("Invivo"))
    );

    public List<CategorySummary> analyse(final List<DataRecord> records) {
        log.info("Start analysing records...");
        final Map<String, CategorySummary> categoryMap = new LinkedHashMap<>();

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (final DataRecord record : records) {
            final BigDecimal amount = record.getAmount();
            final String category = determineCategory(record.getDescription());
            log.debug("record category found: {} and amount: {}", category, amount);
            categoryMap.putIfAbsent(category, new CategorySummary(category));

            final CategorySummary summary = categoryMap.get(category);
            summary.addTransaction(amount);
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(amount);
            } else {
                totalExpenses = totalExpenses.add(amount);
            }
        }

        categoryMap.put("Total Income", new CategorySummary("Total Income", totalIncome));
        categoryMap.put("Total Expenses", new CategorySummary("Total Expenses", totalExpenses));
        categoryMap.put("Balance", new CategorySummary("Balance", totalIncome.add(totalExpenses)));

        return new ArrayList<>(categoryMap.values());
    }

    private String determineCategory(final String description) {
        log.debug("Determining record: {}", description);
        if (Objects.isNull(description)) return "Unknown";
        for (final var entry : keywordMap.entrySet()) {
            for (final String keyword : entry.getValue()) {
                if (description.toLowerCase().contains(keyword.toLowerCase())) {
                    log.debug("record category found: {}", entry.getKey());
                    return entry.getKey();
                }
            }
        }
        log.debug("no record category found, returning: {}", "Other");
        return "Other";
    }
}
