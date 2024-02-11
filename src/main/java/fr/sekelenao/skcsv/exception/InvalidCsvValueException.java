package fr.sekelenao.skcsv.exception;

public class InvalidCsvValueException extends RuntimeException {

    public InvalidCsvValueException(String escapedChar) {
        super("Wrong value, '" + escapedChar + "' not permitted for CSV format.");
    }

}