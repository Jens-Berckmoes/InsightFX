package be.jensberckmoes.insightfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Data
public class AnalysisResult {
    private final List<CategorySummary> summaries;

    private Path chartTempPath;

    public Optional<Path> getChartTempPath() {
        return Optional.ofNullable(chartTempPath);
    }
}

