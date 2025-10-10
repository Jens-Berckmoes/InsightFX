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
            entry("Groceries", List.of("AH", "Delhaize", "Lidl", "Colruyt", "Aldi", "Carrefour", "TOO GOOD TO")),
            entry("Subscriptions", List.of("Netflix", "Spotify", "YouTube", "Apple", "Disney")),
            entry("Health", List.of("Loes Koolen", "Therapie", "Therapy", "Ricardo", "A-cura")),
            entry("Transport", List.of("NMBS", "Uber", "Shell", "Q8", "Van raak", "dats", "Gabriels", "Lukoil")),
            entry("Takeout", List.of("McDonalds", "Burger King", "The Nile", "Hasselt-food")),
            entry("Leasure time", List.of("Bol.com", "Coolblue", "Steam", "MediaMarkt")),
            entry("Bank Costs", List.of("Bijdrage")),
            entry("Clothing", List.of("Torfs")),
            entry("Services", List.of("DIENSTENCHEQUE")),
            entry("Household", List.of("Hasselt-store")),
            entry("Income", List.of("Matt", "Salaris", "Uitkering", "Salary")),
            entry("Pet", List.of("Invivo")),
            entry("Insurance", List.of("Kliniplan", "AXA", "Dela")),
            entry("Charitable contribution", List.of("Rode kruis"))
    );

    /**
     * Analyzes a list of {@link DataRecord} and summarizes them into categories.
     * <p>
     * Each record is assigned a category based on keywords defined in {@link #keywordMap}.
     * The method calculates the total amount per category, as well as overall
     * totals for income, expenses, and balance.
     * </p>
     *
     * @param records the list of {@link DataRecord} to analyze; must not be null
     * @return a {@link List} of {@link CategorySummary} objects including:
     * <ul>
     *     <li>All matched categories with total amounts and transaction counts</li>
     *     <li>"Total Income" if there are any positive amounts</li>
     *     <li>"Total Expenses" if there are any negative amounts</li>
     *     <li>"Balance" representing the sum of income and expenses</li>
     * </ul>
     *
     * @throws NullPointerException if the records list itself is null
     * @see #determineCategory(String)
     */
    public List<CategorySummary> analyse(final List<DataRecord> records) {
        log.info("Starting analysis of {} records...", records.size());
        final Map<String, CategorySummary> categoryMap = new LinkedHashMap<>();

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (final DataRecord record : records) {
            final BigDecimal amount = record.getAmount();
            final String category = determineCategory(record.getDescription());

            final CategorySummary summary = categoryMap.computeIfAbsent(category, CategorySummary::new);
            summary.addTransaction(amount);

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(amount);
            } else {
                totalExpenses = totalExpenses.add(amount);
            }
            log.debug("Processed record: '{}' | category: {} | amount: {}", record.getDescription(), category, amount);
        }

        addSummaryCategories(totalIncome, categoryMap, totalExpenses);
        log.info("Analysis completed: {} categories found.", categoryMap.size());
        return new ArrayList<>(categoryMap.values());
    }

    private static void addSummaryCategories(BigDecimal totalIncome, Map<String, CategorySummary> categoryMap, BigDecimal totalExpenses) {
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            categoryMap.put("Total Income", new CategorySummary("Total Income", totalIncome));
        }
        if (totalExpenses.compareTo(BigDecimal.ZERO) < 0) {
            categoryMap.put("Total Expenses", new CategorySummary("Total Expenses", totalExpenses));
        }
        categoryMap.put("Balance", new CategorySummary("Balance", totalIncome.add(totalExpenses)));
    }

    private String determineCategory(final String description) {
        if (Objects.isNull(description) || description.isBlank()) return "Unknown";

        for (final var entry : keywordMap.entrySet()) {
            for (final String keyword : entry.getValue()) {
                if (description.toLowerCase().contains(keyword.toLowerCase())) {
                    log.debug("Matched keyword '{}' for category '{}'", keyword, entry.getKey());
                    return entry.getKey();
                }
            }
        }
        log.debug("No category matched for description '{}', assigning 'Other'", description);
        return "Other";
    }
}
