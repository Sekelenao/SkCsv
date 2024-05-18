package fr.sekelenao.skcsv;

/**
 * Configuration class for CSV files.
 * This class defines the delimiter and quote characters used in CSV syntax.
 */
public record SkCsvConfig(char delimiter, char quote) {

    /**
     * Predefined configuration using a semicolon as delimiter and double quotes.
     * This is the default configuration.
     */
    public static final SkCsvConfig SEMICOLON = new SkCsvConfig(';', '"');

    /**
     * Predefined configuration using a comma as delimiter and double quotes.
     */
    public static final SkCsvConfig COMMA = new SkCsvConfig(',', '"');

    /**
     * Constructs a new CSV configuration with the specified delimiter and quote characters.
     *
     * @param delimiter the character used to separate values
     * @param quote the character used to quote values
     *
     * @throws IllegalArgumentException if the delimiter and quote characters are the same
     */
    public SkCsvConfig {
        SkAssertions.validChar(quote);
        SkAssertions.validChar(delimiter);
        if (delimiter == quote) {
            throw new IllegalArgumentException("Delimiter should be different than quotes");
        }
    }

}
