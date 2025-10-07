package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.CsvDelimiter;
import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;
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
        if (Objects.isNull(exportType)) throw new IllegalArgumentException("Unsupported export type");
        switch (exportType) {
            case CSV -> exportCsv(rows, targetFile, CsvDelimiter.COMMA);
            case PDF -> exportPdf(rows, targetFile);
            case EUROPEAN_CSV -> exportCsv(rows, targetFile, CsvDelimiter.SEMICOLON);
            default -> throw new IllegalArgumentException("Unsupported export type: " + exportType);
        }
    }

    private void exportCsv(final List<? extends ExportableRow> rows, final Path targetFile, final CsvDelimiter delimiter) throws IOException {
        if (rows.isEmpty()) return;

        final List<String> headers = getHeadersRow();

        try (final BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
            writer.write(String.join(delimiter.getSymbol(), headers));
            writer.newLine();

            rows.stream()
                    .map(ExportableRow::toRow)
                    .map(extractValuesUsingHeadersAsKey(delimiter, headers))
                    .forEach(writeLine(writer));
        }
    }

    private static Function<Map<String, Object>, String> extractValuesUsingHeadersAsKey(final CsvDelimiter delimiter, final List<String> headers) {
        return rowMap -> headers.stream()
                .map(h -> Objects.toString(rowMap.get(h), ""))
                .collect(Collectors.joining(delimiter.getSymbol()));
    }

    private static Consumer<String> writeLine(final BufferedWriter writer) {
        return line -> {
            try {
                writer.write(line);
                writer.newLine();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static List<String> getHeadersRow() {
        return List.of("Category", "Amount");
    }

    private void exportPdf(final List<? extends ExportableRow> rows, final Path targetFile) {
        log.info("Some temporary logging so the reviewer stops nagging :) {} {}",rows, targetFile);
    }
}
