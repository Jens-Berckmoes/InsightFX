package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.CsvDelimiter;
import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
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

/**
 * Implementation of {@link ExportService} providing CSV, European CSV, and PDF export capabilities.
 */

public class ExportServiceImpl implements ExportService {
    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);
    private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(final List<? extends ExportableRow> rows,
                       final Path targetFile,
                       final ExportType exportType) throws IOException {
        export(rows, targetFile, exportType, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(final List<? extends ExportableRow> rows,
                       final Path targetFile,
                       final ExportType exportType,
                       final BufferedImage chartImage) throws IOException {
        if (Objects.isNull(exportType)) {
            log.error("Export type is null for target file: {}", targetFile);
            throw new IllegalArgumentException("Unsupported export type");
        }
        log.info("Starting export: type={}, targetFile={}, rowCount={}",
                exportType, targetFile, rows.size());
        try {
            switch (exportType) {
                case CSV -> exportCsv(rows, targetFile, CsvDelimiter.COMMA);
                case PDF -> exportPdf(rows, targetFile, chartImage);
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

    /**
     * Returns the standard headers for CSV export.
     *
     * @return list of CSV column names
     */
    private static List<String> getCsvHeaders() {
        return List.of("Category", "Amount");
    }

    /**
     * Writes the list of rows to a CSV file using the given delimiter.
     *
     * @param rows       the data to export
     * @param targetFile the target CSV file path
     * @param delimiter  the CSV delimiter to use
     * @throws IOException if writing to file fails
     */
    private void exportCsv(final List<? extends ExportableRow> rows, final Path targetFile, final CsvDelimiter delimiter) throws IOException {
        final long start = System.currentTimeMillis();
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
                    .map(mapRowToCsvLine(delimiter, headers))
                    .forEach(writeCsvLine(writer));
        }
        log.info("CSV export finished: {}", targetFile);
        log.info("Exported {} rows to {} in {} ms for CSV", rows.size(), targetFile, System.currentTimeMillis() - start);
    }

    /**
     * Returns a function that maps a row's key-value pairs to a CSV line string.
     *
     * @param delimiter the CSV delimiter
     * @param headers   the CSV headers
     * @return function mapping a row map to CSV line
     */
    private static Function<Map<String, Object>, String> mapRowToCsvLine(final CsvDelimiter delimiter, final List<String> headers) {
        return rowMap -> headers.stream()
                .map(h -> Objects.toString(rowMap.get(h), ""))
                .collect(Collectors.joining(delimiter.getSymbol()));
    }

    /**
     * Returns a consumer that writes a CSV line to the provided BufferedWriter.
     *
     * @param writer BufferedWriter to write lines to
     * @return consumer that writes each line
     */
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

    /**
     * Writes the given rows to a PDF document, optionally including a chart image.
     *
     * @param rows           data to write
     * @param targetFile     target PDF file path
     * @param chartImage     optional chart image
     */
    private void exportPdf(final List<? extends ExportableRow> rows, final Path targetFile, final BufferedImage chartImage) {
        final long start = System.currentTimeMillis();
        if (rows.isEmpty()) {
            log.warn("PDF export called with empty row list: {}", targetFile);
            return;
        }
        log.debug("PDF export rows count: {}, chart included: {}", rows.size(), Objects.nonNull(chartImage));

        try (final PDDocument document = new PDDocument()) {
            final PDPage page = new PDPage();
            document.addPage(page);

            try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                float yPos = 750;
                yPos = writeTitleWithSpaceBelow(contentStream, yPos);
                yPos = drawChartIfExists(document, contentStream, chartImage, yPos);

                writeRows(rows, contentStream, yPos);
            }

            document.save(targetFile.toFile());
            log.info("PDF export finished: {}", targetFile);
            log.info("Exported {} rows to {} in {} ms for PDF", rows.size(), targetFile, System.currentTimeMillis() - start);
        } catch (final IOException e) {
            log.error("Error exporting PDF to {}: {}", targetFile, e.getMessage(), e);
            throw new RuntimeException("Error exporting PDF", e);
        }
    }

    /**
     * Writes the PDF title and returns the new Y position after spacing.
     *
     * @param contentStream the content stream of the PDF
     * @param yPos          current vertical position
     * @return new Y position after title
     * @throws IOException if writing to PDF fails
     */
    private static float writeTitleWithSpaceBelow(final PDPageContentStream contentStream,
                                                  final float yPos) throws IOException {
        contentStream.beginText();
        contentStream.setFont(ExportServiceImpl.FONT_BOLD, 18);
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("InsightFX Export Summary");
        contentStream.endText();
        return moveDown(yPos, 50);
    }

    /**
     * Draws a chart image on the PDF if the path exists.
     *
     * @param document       the PDF document
     * @param contentStream  the content stream
     * @param chartImage     the chart image
     * @param yPos           current vertical position
     * @return new Y position after drawing chart
     * @throws IOException if drawing fails
     */
    private static float drawChartIfExists(final PDDocument document,
                                           final PDPageContentStream contentStream,
                                           final BufferedImage chartImage,
                                           final float yPos) throws IOException {
        if (Objects.nonNull(chartImage)) {
            log.debug("Including chart in PDF from in-memory image");

            final PDImageXObject chart = LosslessFactory.createFromImage(document, chartImage);
            final PDRectangle pageSize = document.getPage(0).getMediaBox();

            final float margin = 50;
            final float maxWidth = pageSize.getWidth() - 2 * margin;
            final float maxHeight = pageSize.getHeight() - 2 * margin;

            final float xScale = maxWidth / chart.getWidth();
            final float yScale = maxHeight / chart.getHeight();
            final float scale = Math.min(Math.min(xScale, yScale), 0.4f);

            final float width = chart.getWidth() * scale;
            final float height = chart.getHeight() * scale;

            contentStream.drawImage(chart, margin, yPos - height, width, height);
            return moveDown(yPos, height + 30);
        }
        return yPos;
    }

    /**
     * Writes each row's data to the PDF content stream.
     *
     * @param rows        the data rows
     * @param contentStream the PDF content stream
     * @param startY      starting Y position
     * @throws IOException if writing fails
     */
    private static void writeRows(final List<? extends ExportableRow> rows,
                                  final PDPageContentStream contentStream,
                                  final float startY) throws IOException {
        float xCategory = 50;
        float xAmount = 200;
        float fontSize = 12;
        float y = startY;

        contentStream.setFont(ExportServiceImpl.FONT_REGULAR, fontSize);

        for (final ExportableRow row : rows) {
            final Map<String, Object> data = row.toRow();
            final String category = Objects.toString(data.get("Category"), "");
            final String amount = Objects.toString(data.get("Amount"), "");

            contentStream.beginText();
            contentStream.newLineAtOffset(xCategory, y);
            contentStream.showText(category);
            contentStream.endText();

            float textWidthForAligningRight = ExportServiceImpl.FONT_REGULAR.getStringWidth(amount) / 1000 * fontSize;
            contentStream.beginText();
            contentStream.newLineAtOffset(xAmount - textWidthForAligningRight, y);
            contentStream.showText(amount);
            contentStream.endText();

            y = moveDown(y, 20);
        }
    }

    /**
     * Moves down the Y-coordinate by the given amount.
     *
     * @param y      current Y position
     * @param amount amount to move down
     * @return new Y position
     */
    private static float moveDown(float y, float amount) {
        return y - amount;
    }

}
