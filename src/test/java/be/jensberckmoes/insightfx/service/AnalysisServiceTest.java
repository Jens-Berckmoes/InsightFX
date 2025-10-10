package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.CategorySummary;
import be.jensberckmoes.insightfx.model.DataRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalysisServiceTest {
    private AnalysisService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisService();
    }

    private Map<String, CategorySummary> toMap(final List<CategorySummary> results) {
        return results.stream()
                .collect(Collectors.toMap(CategorySummary::getCategory, Function.identity(),
                        (a, _) -> a, LinkedHashMap::new));
    }

    private DataRecord rec(final String desc, final BigDecimal amount) {
        return new DataRecord(desc, LocalDate.now(), amount, "");
    }

    @Test
    void testAnalyseWithMixedIncomeAndExpenses() {
        final List<DataRecord> records = List.of(
                rec("Torfs schoenen", new BigDecimal("-50")),
                rec("Therapie sessie", new BigDecimal("-100")),
                rec("Salary October", new BigDecimal("2000"))
        );

        final Map<String, CategorySummary> map = toMap(service.analyse(records));

        assertEquals(new BigDecimal("-50"), map.get("Clothing").getTotal());
        assertEquals(new BigDecimal("-100"), map.get("Health").getTotal());
        assertEquals(new BigDecimal("2000"), map.get("Income").getTotal());

        assertEquals(new BigDecimal("2000"), map.get("Total Income").getTotal());
        assertEquals(new BigDecimal("-150"), map.get("Total Expenses").getTotal());
        assertEquals(new BigDecimal("1850"), map.get("Balance").getTotal());
    }

    @Test
    void testAnalyseWithOnlyExpenses() {
        final List<DataRecord> records = List.of(
                rec("AH boodschappen", new BigDecimal("-75")),
                rec("Torfs schoenen", new BigDecimal("-50"))
        );

        final Map<String, CategorySummary> map = toMap(service.analyse(records));

        assertEquals(new BigDecimal("-75"), map.get("Groceries").getTotal());
        assertEquals(new BigDecimal("-50"), map.get("Clothing").getTotal());
        assertEquals(BigDecimal.ZERO, map.get("Total Income").getTotal());
        assertEquals(new BigDecimal("-125"), map.get("Total Expenses").getTotal());
        assertEquals(new BigDecimal("-125"), map.get("Balance").getTotal());
    }

    @Test
    void testAnalyseWithOnlyIncome() {
        final List<DataRecord> records = List.of(
                rec("Salary October", new BigDecimal("2000")),
                rec("Matt gift", new BigDecimal("500"))
        );

        final Map<String, CategorySummary> map = toMap(service.analyse(records));

        assertEquals(new BigDecimal("2500"), map.get("Income").getTotal()); // both salary and Matt keyword go to Income
        assertEquals(BigDecimal.ZERO, map.get("Total Expenses").getTotal());
        assertEquals(new BigDecimal("2500"), map.get("Total Income").getTotal());
        assertEquals(new BigDecimal("2500"), map.get("Balance").getTotal());
    }

    @Test
    void testAnalyseWithEmptyInput() {
        final Map<String, CategorySummary> map = toMap(service.analyse(Collections.emptyList()));

        assertEquals(3, map.size());
        assertTrue(map.containsKey("Total Income"));
        assertTrue(map.containsKey("Total Expenses"));
        assertTrue(map.containsKey("Balance"));

        assertEquals(BigDecimal.ZERO, map.get("Total Income").getTotal());
        assertEquals(BigDecimal.ZERO, map.get("Total Expenses").getTotal());
        assertEquals(BigDecimal.ZERO, map.get("Balance").getTotal());
    }

    @Test
    void testAnalyseWithUnknownCategory() {
        final List<DataRecord> records = List.of(
                rec("Random description", new BigDecimal("-10"))
        );

        final Map<String, CategorySummary> map = toMap(service.analyse(records));

        assertEquals(new BigDecimal("-10"), map.get("Other").getTotal());
        assertEquals(new BigDecimal("0"), map.get("Total Income").getTotal());
        assertEquals(new BigDecimal("-10"), map.get("Total Expenses").getTotal());
        assertEquals(new BigDecimal("-10"), map.get("Balance").getTotal());
    }
}
