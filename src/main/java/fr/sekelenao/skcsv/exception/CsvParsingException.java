package fr.sekelenao.skcsv.exception;

public class CsvParsingException extends RuntimeException {

    public CsvParsingException(String parsed) {
        super("Could not parse, <" + parsed + "> does not match CSV format.");
    }

}