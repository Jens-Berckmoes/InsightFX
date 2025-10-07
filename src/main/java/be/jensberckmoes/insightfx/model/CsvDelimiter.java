package be.jensberckmoes.insightfx.model;

import lombok.Getter;

@Getter
public enum CsvDelimiter {
    COMMA(","), SEMICOLON(";"), TAB("\t");

    private final String symbol;
    CsvDelimiter(String symbol) { this.symbol = symbol; }
}