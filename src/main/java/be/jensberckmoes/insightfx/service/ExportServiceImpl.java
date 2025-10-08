package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.CsvDelimiter;
import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExportServiceImpl implements ExportService {
    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);

    @Override
    public void export(final List<? extends ExportableRow> rows,
                       final Path targetFile,
                       final ExportType exportType) throws IOException {
        export(rows, targetFile, exportType, null);
    }

    @Override
    public void export(final List<? extends ExportableRow> rows,
                       final Path targetFile,
                       final ExportType exportType,
                       final Path chartImagePath) throws IOException {
        if (Objects.isNull(exportType)) {
            log.error("Export type is null for target file: {}", targetFile);
            throw new IllegalArgumentException("Unsupported export type");
        }
        log.info("Starting export: type={}, targetFile={}, rowCount={}",
                exportType, targetFile, rows.size());
        try {
            switch (exportType) {
                case CSV -> exportCsv(rows, targetFile, CsvDelimiter.COMMA);
                case PDF -> exportPdf(rows, targetFile, chartImagePath);
                case EUROPEAN_CSV -> exportCsv(rows, targetFile, CsvDelimiter.SEMICOLON);
                default -> {
                    log.error("Unsupported export type: {}", exportType);
                    throw new IllegalArgumentException("Unsupported export type: " + exportType);
                }
            }
            log.info("Export completed successfully: {}", targetFile);
        } catch (final IOException | RuntimeException e) {
            log.error("Failed to export file {}: {}", targetFile, e.getMessage(), e);
            throw e;
        }

    }

    private static List<String> getCsvHeaders() {
        return List.of("Category", "Amount");
    }

    private void exportCsv(final List<? extends ExportableRow> rows, final Path targetFile, final CsvDelimiter delimiter) throws IOException {
        if (rows.isEmpty()) {
            log.warn("CSV export called with empty row list: {}", targetFile);
            return;
        }
        final List<String> headers = getCsvHeaders();
        if (log.isDebugEnabled()) {
            log.debug("CSV headers: {}", headers);
            rows.stream().limit(3).forEach(row -> log.debug("Sample row: {}", row.toRow()));
        }

        try (final BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
            writer.write(String.join(delimiter.getSymbol(), headers));
            writer.newLine();

            rows.stream()
                    .map(ExportableRow::toRow)
                    .map(rowToCsvLine(delimiter, headers))
                    .forEach(writeCsvLine(writer));
        }
        log.info("CSV export finished: {}", targetFile);
    }

    private static Function<Map<String, Object>, String> rowToCsvLine(final CsvDelimiter delimiter, final List<String> headers) {
        return rowMap -> headers.stream()
                .map(h -> Objects.toString(rowMap.get(h), ""))
                .collect(Collectors.joining(delimiter.getSymbol()));
    }

    private static Consumer<String> writeCsvLine(final BufferedWriter writer) {
        return line -> {
            try {
                writer.write(line);
                writer.newLine();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private void exportPdf(final List<? extends ExportableRow> rows, final Path targetFile, final Path chartImagePath) {
        if (rows.isEmpty()) {
            log.warn("PDF export called with empty row list: {}", targetFile);
            return;
        }
        log.debug("PDF export rows count: {}, chart included: {}", rows.size(), Objects.nonNull(chartImagePath));

        try (final PDDocument document = new PDDocument()) {
            final PDPage page = new PDPage();
            document.addPage(page);

            try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                final PDFont regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                final PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                float yPos = 750;
                yPos = writeTitleWithSpaceBelow(contentStream, boldFont, yPos);
                yPos = drawChartIfExists(document, contentStream, chartImagePath, yPos);

                writeRows(rows, contentStream, regularFont, yPos);
            }

            document.save(targetFile.toFile());
            log.info("PDF export finished: {}", targetFile);
        } catch (final IOException e) {
            log.error("Error exporting PDF to {}: {}", targetFile, e.getMessage(), e);
            throw new RuntimeException("Error exporting PDF", e);
        }
    }

    private static float writeTitleWithSpaceBelow(final PDPageContentStream contentStream,
                                                  final PDFont font,
                                                  final float yPos) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, 18);
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("InsightFX Export Summary");
        contentStream.endText();
        return yPos - 50;
    }

    private static float drawChartIfExists(final PDDocument document,
                                           final PDPageContentStream contentStream,
                                           final Path chartImagePath,
                                           final float yPos) throws IOException {
        if (isAnExistingFile(chartImagePath)) {
            log.debug("Including chart in PDF: {}", chartImagePath);
            final PDImageXObject chart = PDImageXObject.createFromFile(chartImagePath.toString(), document);
            final float scale = 0.4f;
            final float width = chart.getWidth() * scale;
            final float height = chart.getHeight() * scale;
            contentStream.drawImage(chart, 50, yPos - height, width, height);
            return yPos - height - 30;
        }
        return yPos;
    }

    private static boolean isAnExistingFile(final Path chartImagePath) {
        return Objects.nonNull(chartImagePath) && Files.exists(chartImagePath);
    }

    private static void writeRows(final List<? extends ExportableRow> rows,
                                  final PDPageContentStream contentStream,
                                  final PDFont font,
                                  final float startY) throws IOException {
        float xCategory = 50;
        float xAmount = 200;
        float fontSize = 12;
        float y = startY;

        contentStream.setFont(font, fontSize);

        for (final ExportableRow row : rows) {
            final Map<String, Object> data = row.toRow();
            final String category = Objects.toString(data.get("Category"), "");
            final String amount = Objects.toString(data.get("Amount"), "");

            contentStream.beginText();
            contentStream.newLineAtOffset(xCategory, y);
            contentStream.showText(category);
            contentStream.endText();

            float textWidthForAligningRight = font.getStringWidth(amount) / 1000 * fontSize;
            contentStream.beginText();
            contentStream.newLineAtOffset(xAmount - textWidthForAligningRight, y);
            contentStream.showText(amount);
            contentStream.endText();

            y -= 20;
        }
    }

}
