package fr.sekelenao.skcsv.exception;

/**
 * Exception thrown when an invalid value is encountered in a CSV field.
 * <p>
 * This exception is thrown in the following situations:
 * <ul>
 *   <li>If a value contains characters that are not permitted outside quotes in the CSV format.</li>
 *   <li>If the CSV configuration is modified with characters that are not permitted outside quotes.</li>
 * </ul>
 * <p>
 * In the CSV format:
 * <ul>
 *   <li>Characters not permitted outside quotes typically include newline characters.</li>
 *   <li>If these characters are encountered outside quotes, it indicates a violation of the CSV format rules.</li>
 * </ul>
 */
public class InvalidCsvValueException extends RuntimeException {

    /**
     * Constructs a new InvalidCsvValueException with the specified detail message.
     *
     * @param wrongValue the value that is not permitted for CSV format outside quotes
     */
    public InvalidCsvValueException(String wrongValue) {
        super("Wrong value, '" + wrongValue + "' not permitted for CSV format outside quotes.");
    }
}