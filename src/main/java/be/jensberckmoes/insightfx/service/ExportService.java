package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service responsible for exporting data in various formats such as CSV, European-style CSV, and PDF.
 * Implementations should handle formatting, file creation, and optional inclusion of charts.
 */
public interface ExportService {
    /**
     * Exports the given rows to a file of the specified type.
     *
     * @param rows       The list of {@link ExportableRow} to export. Must not be null.
     * @param targetFile The target file path where the exported file will be written. Must not be null.
     * @param exportType The type of export to perform ({@link ExportType}). Must not be null.
     * @throws IOException              If there is an error writing to the target file.
     * @throws IllegalArgumentException If the export type is null or unsupported.
     */
    void export(final List<? extends ExportableRow> rows,
                final Path targetFile,
                final ExportType exportType) throws IOException;

    /**
     * Exports the given rows to a file of the specified type, optionally including a chart image.
     *
     * @param rows           The list of {@link ExportableRow} to export. Must not be null.
     * @param target         The target file path where the exported file will be written. Must not be null.
     * @param type           The type of export to perform ({@link ExportType}). Must not be null.
     * @param chartImagePath Optional path to a chart image to include in the export (PDF only). Can be null.
     * @throws IOException              If there is an error writing to the target file.
     * @throws IllegalArgumentException If the export type is null or unsupported.
     */
    void export(final List<? extends ExportableRow> rows,
                final Path target,
                final ExportType type,
                final Path chartImagePath) throws IOException;

}
