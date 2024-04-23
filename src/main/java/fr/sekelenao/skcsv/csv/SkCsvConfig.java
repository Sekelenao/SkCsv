package fr.sekelenao.skcsv.csv;

public record SkCsvConfig(char delimiter, char quote) {

    public static final SkCsvConfig SEMICOLON = new SkCsvConfig(';', '"');

    public static final SkCsvConfig COMMA = new SkCsvConfig(',', '"');

    public SkCsvConfig {
        SkAssertions.validChar(quote);
        SkAssertions.validChar(delimiter);
        if(delimiter == quote)
            throw new IllegalArgumentException("Delimiter should be different than quotes");
    }

}
