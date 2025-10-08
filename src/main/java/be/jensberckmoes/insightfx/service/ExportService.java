package be.jensberckmoes.insightfx.service;

import be.jensberckmoes.insightfx.model.ExportType;
import be.jensberckmoes.insightfx.model.ExportableRow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ExportService {
    void export(final List<? extends ExportableRow> rows,
                final Path targetFile,
                final ExportType exportType) throws IOException;

    void export(final List<? extends ExportableRow> rows,
                final Path target,
                final ExportType type,
                final Path chartImagePath) throws IOException;

}
