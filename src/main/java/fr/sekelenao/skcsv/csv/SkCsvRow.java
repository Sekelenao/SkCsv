package fr.sekelenao.skcsv.csv;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents a single row in a CSV (Comma-Separated Values) file.
 * It provides various methods for manipulating and accessing the contents of the row.
 *
 * @author Sekelenao
 * @version 1.0
 * @since 21.0.2
 */
public class SkCsvRow implements Iterable<String> {

    private final List<String> cells = new ArrayList<>();

    /**
     * Constructs an empty CsvRow.
     */
    public SkCsvRow() {}

    /**
     * Constructs a CsvRow with a specified size and fills it with empty strings.
     *
     * @param size The size of the CsvRow to be created.
     */
    public SkCsvRow(int size) {
        SkAssertions.positive(size);
        fill(size);
    }

    /**
     * Constructs a CsvRow from an array of strings.
     *
     * @param array The array of strings to initialize the CsvRow with.
     * @throws NullPointerException if the provided array or any of its elements is null.
     */
    public SkCsvRow(String... array) {
        Objects.requireNonNull(array);
        for (var value : array) {
            Objects.requireNonNull(value);
            cells.addLast(value);
        }
    }

    /**
     * Constructs a CsvRow from an iterable collection of strings.
     *
     * @param iterable The iterable collection of strings to initialize the CsvRow with.
     * @throws NullPointerException if the provided iterable or any of its elements is null.
     */
    public SkCsvRow(Iterable<String> iterable) {
        Objects.requireNonNull(iterable);
        for (var value : iterable) {
            Objects.requireNonNull(value);
            cells.addLast(value);
        }
    }

    /**
     * Returns the number of cells in this CsvRow.
     *
     * @return The number of cells in this CsvRow.
     */
    public int size() {
        return cells.size();
    }

    /**
     * Checks if this CsvRow is empty.
     *
     * @return true if this CsvRow contains no cells, false otherwise.
     */
    public boolean isEmpty() {
        return cells.isEmpty();
    }

    /**
     * Appends a cell to the end of this CsvRow.
     *
     * @param value The value of the cell to be appended.
     * @throws NullPointerException if the provided value is null.
     */
    public void add(String value) {
        Objects.requireNonNull(value);
        cells.addLast(value);
    }

    /**
     * Inserts a cell at the specified position in this CsvRow.
     *
     * @param position The index at which the cell is to be inserted.
     * @param value    The value of the cell to be inserted.
     * @throws NullPointerException      if the provided value is null.
     * @throws IndexOutOfBoundsException if the specified position is out of range.
     */
    public void insert(int position, String value) {
        Objects.requireNonNull(value);
        SkAssertions.validPosition(position, cells.size());
        cells.add(position, value);
    }

    /**
     * Inserts multiple cells at the specified position in this CsvRow.
     * If the position is equal to the size of the CsvRow, cells are appended to the end.
     *
     * @param position The index at which the cells are to be inserted.
     * @param values   The values of the cells to be inserted.
     * @throws NullPointerException      if the provided values or any of its elements is null.
     * @throws IndexOutOfBoundsException if the specified position is out of range.
     */
    public void insertAll(int position, String... values) {
        SkAssertions.validPosition(position, cells.size());
        Objects.requireNonNull(values);
        if (position == cells.size()) {
            for (var value : values) {
                Objects.requireNonNull(value);
                this.add(value);
            }
        } else {
            var lst = new ArrayList<String>();
            for (var value : values) {
                Objects.requireNonNull(value);
                lst.add(value);
            }
            cells.addAll(position, lst);
        }
    }

    /**
     * Inserts multiple cells at the specified position in this CsvRow.
     * If the position is equal to the size of the CsvRow, cells are appended to the end.
     *
     * @param position The index at which the cells are to be inserted.
     * @param values   The values of the cells to be inserted.
     * @throws NullPointerException      if the provided values or any of its elements is null.
     * @throws IndexOutOfBoundsException if the specified position is out of range.
     */
    public void insertAll(int position, Iterable<String> values) {
        SkAssertions.validPosition(position, cells.size());
        Objects.requireNonNull(values);
        if (position == cells.size()) {
            values.forEach(value -> {
                Objects.requireNonNull(value);
                this.add(value);
            });
        } else {
            var lst = new ArrayList<String>();
            for (var value : values) {
                Objects.requireNonNull(value);
                lst.add(value);
            }
            cells.addAll(position, lst);
        }
    }

    /**
     * Sets the value of the cell at the specified index in this CsvRow.
     *
     * @param index The index of the cell to be set.
     * @param value The new value of the cell.
     * @return The previous value of the cell at the specified index.
     * @throws NullPointerException      if the provided value is null.
     * @throws IndexOutOfBoundsException if the specified index is out of range.
     */
    public String set(int index, String value) {
        Objects.checkIndex(index, cells.size());
        Objects.requireNonNull(value);
        return cells.set(index, value);
    }

    /**
     * Fills this CsvRow with a specified amount of empty cells.
     *
     * @param amount The number of empty cells to fill this CsvRow with.
     * @throws IllegalArgumentException if the specified amount is negative.
     */
    public void fill(int amount) {
        SkAssertions.positive(amount);
        for (int i = 0; i < amount; i++) add("");
    }

    /**
     * Returns the value of the cell at the specified index in this CsvRow.
     *
     * @param index The index of the cell to retrieve.
     * @return The value of the cell at the specified index.
     * @throws IndexOutOfBoundsException if the specified index is out of range.
     */
    public String get(int index) {
        Objects.checkIndex(index, cells.size());
        return cells.get(index);
    }

    /**
     * Returns the value of the first cell in this CsvRow.
     *
     * @return The value of the first cell in this CsvRow.
     * @throws NoSuchElementException if this CsvRow is empty.
     */
    public String getFirst() {
        if (cells.isEmpty()) throw new NoSuchElementException();
        return cells.getFirst();
    }

    /**
     * Returns the value of the last cell in this CsvRow.
     *
     * @return The value of the last cell in this CsvRow.
     * @throws NoSuchElementException if this CsvRow is empty.
     */
    public String getLast() {
        if (cells.isEmpty()) throw new NoSuchElementException();
        return cells.getLast();
    }

    /**
     * Removes the cell at the specified index in this CsvRow.
     *
     * @param index The index of the cell to be removed.
     * @return The value of the cell that was removed.
     * @throws IndexOutOfBoundsException if the specified index is out of range.
     */
    public String remove(int index) {
        Objects.checkIndex(index, cells.size());
        return cells.remove(index);
    }

    /**
     * Removes all cells from this CsvRow that satisfy the given predicate.
     *
     * @param filter The predicate used to determine which cells to remove.
     * @return true if any cells were removed, false otherwise.
     * @throws NullPointerException if the provided filter is null.
     */
    public boolean removeIf(Predicate<? super String> filter) {
        return cells.removeIf(Objects.requireNonNull(filter));
    }

    /**
     * Checks if this CsvRow contains the specified value.
     *
     * @param value The value to be checked for containment in this CsvRow.
     * @return true if this CsvRow contains the specified value, false otherwise.
     */
    public boolean contains(Object value) {
        return value instanceof String s && cells.contains(s);
    }

    /**
     * Performs the given action for each cell in this CsvRow until all cells have been processed
     * or the action throws an exception.
     *
     * @param action The action to be performed for each cell.
     * @throws NullPointerException if the provided action is null.
     */
    @Override
    public void forEach(Consumer<? super String> action) {
        cells.forEach(Objects.requireNonNull(action));
    }

    /**
     * Returns an {@code Iterator} over the cells in this CsvRow in proper sequence.
     *
     * @return An {@code Iterator} over the cells in this CsvRow.
     */
    @Override
    public Iterator<String> iterator() {
        return cells.iterator();
    }

    /**
     * Returns a sequential {@code Stream} with the cells of this CsvRow as its source.
     *
     * @return A sequential {@code Stream} over the cells in this CsvRow.
     */
    public Stream<String> stream() {
        return cells.stream();
    }

    /**
     * Applies the given function to each cell in this CsvRow and replaces the cell with the result.
     *
     * @param mapper The function to apply to each cell.
     * @throws NullPointerException if the provided mapper is null.
     */
    public void map(Function<? super String, String> mapper) {
        Objects.requireNonNull(mapper);
        var it = cells.listIterator();
        while (it.hasNext()) {
            var mappedValue = mapper.apply(it.next());
            Objects.requireNonNull(mappedValue);
            it.set(mappedValue);
        }
    }

    /**
     * Creates a copy of this CsvRow.
     *
     * @return A new CsvRow containing the same cells as this CsvRow.
     */
    public SkCsvRow copy() {
        return new SkCsvRow(this.cells);
    }

    /**
     * Indicates whether some other object is "equal to" this CsvRow.
     *
     * @param o The object to compare for equality.
     * @return true if the specified object is equal to this CsvRow, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        Objects.requireNonNull(o);
        return o instanceof SkCsvRow other
                && other.cells.size() == cells.size()
                && other.cells.equals(cells);
    }

    /**
     * Returns the hash code value for this CsvRow.
     *
     * @return The hash code value for this CsvRow.
     */
    @Override
    public int hashCode() {
        int hash = cells.size();
        for (String cell : cells) {
            hash ^= cell.hashCode();
        }
        return hash;
    }

    /**
     * Returns a string representation of this CsvRow.
     *
     * @return A string representation of this CsvRow.
     */
    @Override
    public String toString() {
        var formatter = new CsvFormatter(CsvConfiguration.SEMICOLON);
        return formatter.toCsvString(cells);
    }

    /**
     * Returns a string representation of this CsvRow using the specified CSV configuration.
     *
     * @param configuration The CSV configuration defining the format of the CSV row.
     * @return A string representation of this CsvRow according to the specified configuration.
     * @throws NullPointerException if the provided configuration is null.
     */
    public String toString(CsvConfiguration configuration) {
        Objects.requireNonNull(configuration);
        var formatter = new CsvFormatter(configuration);
        return formatter.toCsvString(cells);
    }

}
