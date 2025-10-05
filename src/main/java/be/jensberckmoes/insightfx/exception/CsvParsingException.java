package be.jensberckmoes.insightfx.exception;

public class CsvParsingException extends RuntimeException{

    public CsvParsingException(final String message) {
        super(message);
    }

    public CsvParsingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
