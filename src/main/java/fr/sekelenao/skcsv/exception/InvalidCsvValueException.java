package fr.sekelenao.skcsv.exception;

public class InvalidCsvValueException extends RuntimeException {

    public InvalidCsvValueException(String wrongValue) {
        super("Wrong value, '" + wrongValue + "' not permitted for CSV format.");
    }

}