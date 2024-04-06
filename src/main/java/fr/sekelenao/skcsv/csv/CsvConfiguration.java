package fr.sekelenao.skcsv.csv;

public record CsvConfiguration(char delimiter, char quote) {

    public static final CsvConfiguration SEMICOLON = new CsvConfiguration(';', '"');

    public static final CsvConfiguration COMMA = new CsvConfiguration(',', '"');

    public CsvConfiguration {
        SkAssertions.validChar(quote);
        SkAssertions.validChar(delimiter);
        if(delimiter == quote)
            throw new IllegalArgumentException("Delimiter should be different than quotes");
    }

    public String delimiterAsString(){
        return Character.toString(delimiter);
    }

    public String quoteAsString(){
        return Character.toString(quote);
    }

}
