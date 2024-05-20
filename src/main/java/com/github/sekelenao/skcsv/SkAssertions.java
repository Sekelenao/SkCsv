package com.github.sekelenao.skcsv;

import com.github.sekelenao.skcsv.exception.InvalidCsvValueException;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Objects;

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

    static void requireNonNulls(Object... objects) {
        Arrays.stream(objects).forEach(Objects::requireNonNull);
    }

    static void concurrentModification(int version, int expectedVersion){
        if(version != expectedVersion){
            throw new ConcurrentModificationException();
        }
    }

}
