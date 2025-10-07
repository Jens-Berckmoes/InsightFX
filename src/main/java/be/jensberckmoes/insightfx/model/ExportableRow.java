package be.jensberckmoes.insightfx.model;

import java.util.Map;

public interface ExportableRow {
    Map<String, Object> toRow();
}
