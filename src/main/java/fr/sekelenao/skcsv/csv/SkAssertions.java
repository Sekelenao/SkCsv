package fr.sekelenao.skcsv.csv;

import fr.sekelenao.skcsv.exception.InvalidCsvValueException;

final class SkAssertions {

    private SkAssertions() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    static void positive(int integer) {
        if (integer < 0) throw new IllegalArgumentException("Must be positive.");
    }

    static void validChar(int character) {
        switch (character) {
            case '\n' -> throw new InvalidCsvValueException("\\n");
            case '\r' -> throw new InvalidCsvValueException("\\r");
            case '\b' -> throw new InvalidCsvValueException("\\b");
            case '\f' -> throw new InvalidCsvValueException("\\f");
            case '\0' -> throw new InvalidCsvValueException("\\0");
            default -> {/*pass*/}
        }
    }

    static void validPosition(int position, int size) {
        if (position > size || position < 0)
            throw new IndexOutOfBoundsException("Position " + position + " out of bounds for length " + size);
    }



}
