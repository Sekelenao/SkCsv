package fr.sekelenao.skcsv.csv;

public record RowConfiguration(char delimiter, char quote) {

    public static final RowConfiguration SEMICOLON = new RowConfiguration(';', '"');

    public static final RowConfiguration COMMA = new RowConfiguration(',', '"');

    public RowConfiguration {
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
