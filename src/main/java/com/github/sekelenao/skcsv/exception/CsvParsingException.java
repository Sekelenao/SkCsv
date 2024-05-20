package com.github.sekelenao.skcsv.exception;

/**
 * Exception thrown when a CSV parsing error occurs.
 * <p>
 * This exception is thrown when the CSV data does not conform to the expected CSV format.
 * <p>
 * The expected CSV format includes:
 * <ul>
 *   <li>Fields are separated by a delimiter character (e.g., comma or semicolon).</li>
 *   <li>Fields containing special characters (e.g., delimiter or newline) must be enclosed in quotes.</li>
 *   <li>Quotes within quoted fields must be escaped by doubling the quote character. For example: {@code "hello""world"}</li>
 * </ul>
 */
public class CsvParsingException extends RuntimeException {

    /**
     * Constructs a new CsvParsingException with the specified detail message.
     *
     * @param parsed the portion of the CSV data that could not be parsed
     */
    public CsvParsingException(String parsed) {
        super("Could not parse, <" + parsed + "> does not match CSV format.");
    }

}