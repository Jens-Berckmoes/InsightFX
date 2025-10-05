package be.jensberckmoes.insightfx.exception;

public class CsvParsingException extends RuntimeException{
    public CsvParsingException(final Throwable cause) {
        super(cause);
    }

    public CsvParsingException(final String message) {
        super(message);
    }
}
