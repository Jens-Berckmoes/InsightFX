package be.jensberckmoes.insightfx.model;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public record AnalysisResult(List<CategorySummary> results, BufferedImage chartImage) {
    public Optional<BufferedImage> getChartImage() {
        return Optional.ofNullable(chartImage);
    }

}

