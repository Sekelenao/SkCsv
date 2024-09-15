package io.github.sekelenao.skcsv;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a single row in a CSV (Comma-Separated Values) file.
 * Each row consists of a sequence of elements separated by a delimiter.
 * This class provides methods to manipulate and access elements in the row.
 *
 * <p> The size of the row is dynamic and can grow as elements are added.
 *
 * <p>This class implements the {@code Iterable<String>} and {@code RandomAccess} interfaces,
 * allowing iteration over elements in the row and random access to individual elements by index.
 *
 * <p>Null elements are not permitted in instances of this class, ensuring consistency in data processing.
 */
public class SkCsvRow implements Iterable<String>, RandomAccess {

    /**
     * Default capacity for the storage array of CSV row values.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Amount by which the storage array of CSV row values grows when it needs to be resized.
     */
    private static final int GROWING_AMOUNT = 15;

    /**
     * Current modification count of the CSV row storage class.
     * <p>
     * This indicates the current modification count of the CSV row storage class.
     * It is used for managing concurrent modifications and detecting concurrent modification exceptions.
     */
    private int version = 0;

    /**
     * Array storing the row values.
     * <p>
     * This array stores the individual values of the CSV row.
     * It dynamically resizes as needed when more values are added.
     */
    private String[] cells;

    /**
     * Current size of the CSV row storage array.
     * <p>
     * This represents the number of values currently stored in the CSV row storage array.
     * It may be less than or equal to the length of the array.
     */
    private int size;


    /**
     * Constructs an empty SkCsvRow with a default initial capacity.
     */
    public SkCsvRow() {
        this.cells = new String[DEFAULT_CAPACITY];
    }

    /**
     * Constructs an empty SkCsvRow with the specified initial capacity.
     *
     * @param amount the initial capacity of the row
     * @throws IllegalArgumentException if the specified amount is not positive
     */
    public SkCsvRow(int amount) {
        SkAssertions.positive(amount);
        this.cells = new String[amount];
        Arrays.fill(cells, "");
        size = amount;
    }

    /**
     * Constructs an SkCsvRow initialized with the specified array of strings.
     * The provided array is copied, so subsequent changes to the array do not affect the SkCsvRow.
     *
     * @param array the array of strings to initialize the row
     * @throws NullPointerException if the specified array or any of its elements is null
     */
    public SkCsvRow(String... array) {
        Objects.requireNonNull(array);
        this.cells = new String[array.length];
        for (var value : array) {
            this.cells[size++] = Objects.requireNonNull(value);
        }
    }

    /**
     * Constructs an SkCsvRow initialized with the specified collection of strings.
     * The provided collection is copied, so subsequent changes to the collection do not affect the SkCsvRow.
     *
     * @param collection the collection of strings to initialize the row
     * @throws NullPointerException if the specified collection or any of its elements is null
     */
    public SkCsvRow(Collection<String> collection) {
        Objects.requireNonNull(collection);
        this.cells = new String[collection.size()];
        for(var value : collection){
            this.cells[size++] = Objects.requireNonNull(value);
        }
    }

    /**
     * Ensures that the internal storage array has sufficient capacity to accommodate additional elements.
     * If the current capacity is insufficient, the internal array is resized.
     *
     * @param amount the number of additional elements that need to be accommodated
     * @throws OutOfMemoryError if the required new size exceeds the maximum array size
     */
    private void growIfNecessary(int amount) {
        if(size + amount > cells.length){
            var newLength = size + Math.max(GROWING_AMOUNT, amount);
            if(newLength <= 0){
                throw new OutOfMemoryError("Required row size is too large");
            }
            cells = Arrays.copyOf(cells, newLength);
        }
    }

    /**
     * Constructs an SkCsvRow initialized with the specified iterable of strings and an estimated size.
     * The provided iterable is copied, so subsequent changes to the iterable do not affect the SkCsvRow.
     * This constructor may optimize allocations if an estimated size is given.
     *
     * @param iterable the iterable of strings to initialize the row
     * @param estimatedSize the estimated size of the iterable
     * @throws NullPointerException if the specified iterable or any of its elements is null
     * @throws IllegalArgumentException if the specified estimated size is not positive
     */
    public SkCsvRow(Iterable<String> iterable, int estimatedSize) {
        Objects.requireNonNull(iterable);
        SkAssertions.positive(estimatedSize);
        this.cells = new String[estimatedSize];
        for (var value : iterable){
            Objects.requireNonNull(value);
            growIfNecessary(1);
            this.cells[size++] = value;
        }
    }

    /**
     * Constructs an SkCsvRow initialized with the specified iterable of strings.
     * The provided iterable is copied, so subsequent changes to the iterable do not affect the SkCsvRow.
     * If an estimated size is known, use {@link #SkCsvRow(Iterable, int)}, as it may optimize allocations.
     *
     * @param iterable the iterable of strings to initialize the row
     * @throws NullPointerException if the specified iterable or any of its elements is null
     */
    public SkCsvRow(Iterable<String> iterable) {
        this(iterable, 0);
    }

    /**
     * Returns the number of elements in this SkCsvRow.
     *
     * @return the number of elements in this row
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this SkCsvRow contains no elements.
     *
     * @return {@code true} if this row contains no elements, {@code false} otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Adds the specified value to the end of this SkCsvRow.
     *
     * @param value the value to be added to the row
     * @throws NullPointerException if the specified value is null
     */
    public void add(String value) {
        Objects.requireNonNull(value);
        version++;
        growIfNecessary(1);
        cells[size++] = value;
    }

    /**
     * Adds all the elements from the specified array to the end of this SkCsvRow.
     *
     * @param array the array of strings to be added to the row
     * @throws NullPointerException if the specified array or any of its elements is null
     */
    public void addAll(String... array){
        Objects.requireNonNull(array);
        version++;
        growIfNecessary(array.length);
        for (var value : array) {
            cells[size++] = Objects.requireNonNull(value);
        }
    }

    /**
     * Adds all the elements from the specified collection to the end of this SkCsvRow.
     *
     * @param collection the collection of strings to be added to the row
     * @throws NullPointerException if the specified collection or any of its elements is null
     */
    public void addAll(Collection<String> collection){
        Objects.requireNonNull(collection);
        version++;
        growIfNecessary(collection.size());
        for (var value : collection) {
            cells[size++] = Objects.requireNonNull(value);
        }
    }

    /**
     * Adds all the elements from the specified iterable of strings to the end of this SkCsvRow.
     * This method may optimize allocations if an estimated size is given.
     *
     * @param iterable the iterable of strings to be added to the row
     * @param estimatedSize the estimated size of the iterable
     * @throws NullPointerException if the specified iterable or any of its elements is null
     * @throws IllegalArgumentException if the specified estimated size is not positive
     */
    public void addAll(Iterable<String> iterable, int estimatedSize){
        Objects.requireNonNull(iterable);
        SkAssertions.positive(estimatedSize);
        version++;
        growIfNecessary(estimatedSize);
        for (var value : iterable) {
            Objects.requireNonNull(value);
            growIfNecessary(1);
            cells[size++] = value;
        }
    }

    /**
     * Adds all the elements from the specified iterable of strings to the end of this SkCsvRow.
     * If an estimated size is known, use {@link #addAll(Iterable, int)} as it may optimize allocations.
     *
     * @param iterable the iterable of strings to be added to the row
     * @throws NullPointerException if the specified iterable or any of its elements is null
     */
    public void addAll(Iterable<String> iterable){
        Objects.requireNonNull(iterable);
        addAll(iterable, 0);
    }

    /**
     * Sets the value at the specified index in this SkCsvRow.
     *
     * @param index the index of the element to set
     * @param value the new value to set at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws NullPointerException if the specified value is null
     */
    public void set(int index, String value) {
        Objects.checkIndex(index, size);
        Objects.requireNonNull(value);
        cells[index] = value;
    }

    /**
     * Fills the SkCsvRow with the specified amount of empty values.
     *
     * @param amount the number of empty values to fill
     * @throws IllegalArgumentException if the specified amount is not positive
     */
    public void fill(int amount) {
        SkAssertions.positive(amount);
        version++;
        growIfNecessary(amount);
        Arrays.fill(cells, size, size + amount, "");
        size += amount;
    }

    /**
     * Returns the value at the specified index in this SkCsvRow.
     *
     * @param index the index of the element to return
     * @return the value at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public String get(int index) {
        Objects.checkIndex(index, size);
        return cells[index];
    }

    /**
     * Returns the first value in this SkCsvRow.
     *
     * @return the first value in this row
     * @throws NoSuchElementException if this row is empty
     */
    public String getFirst() {
        if (size == 0) throw new NoSuchElementException();
        return cells[0];
    }

    /**
     * Returns the last value in this SkCsvRow.
     *
     * @return the last value in this row
     * @throws NoSuchElementException if this row is empty
     */
    public String getLast() {
        if (size == 0) throw new NoSuchElementException();
        return cells[size - 1];
    }

    /**
     * Returns {@code true} if this row contains the specified element.
     * More formally, returns {@code true} if and only if this row contains
     * at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param object the object to check for containment in this row
     * @return {@code true} if this row contains the specified object, {@code false} otherwise
     */
    public boolean contains(Object object) {
        return object != null && Arrays.stream(cells, 0, size).anyMatch(object::equals);
    }

    /**
     * Returns an iterator over the elements in this SkCsvRow.
     * The iterator traverses the elements of the row in the order they were added.
     *
     * @return an iterator over the elements in this row
     */
    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {

            private final int expectedVersion = version;

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public String next() {
                if(!hasNext()) throw new NoSuchElementException();
                SkAssertions.concurrentModification(version, expectedVersion);
                return cells[index++];
            }
        };
    }

    /**
     * Performs the given action for each element of this SkCsvRow until all elements have been processed or the action
     * throws an exception.
     *
     * @param action the action to be performed for each element
     * @throws NullPointerException if the specified action is null
     */
    @Override
    public void forEach(Consumer<? super String> action) {
        Objects.requireNonNull(action);
        for (int i = 0; i < size; i++) {
            action.accept(cells[i]);
        }
    }

    /**
     * Creates a custom Spliterator for the specified range of elements in the array.
     *
     * @param start the starting index of the range (inclusive)
     * @param end the ending index of the range (exclusive)
     * @param array the array containing the elements
     * @param v the version of the SkCsvRow to check for concurrent modifications
     * @return a Spliterator for the specified range of elements in the array
     */
    private Spliterator<String> customSpliterator(int start, int end, String[] array, int v) {
        return new Spliterator<>() {

            private final int expectedVersion = v;

            private int index = start;

            @Override
            public Spliterator<String> trySplit() {
                var middle = (index + end) >>> 1;
                if (middle == index) {
                    return null;
                }
                var spliterator = customSpliterator(index, middle, array, expectedVersion);
                index = middle;
                return spliterator;
            }

            @Override
            public boolean tryAdvance(Consumer<? super String> consumer) {
                Objects.requireNonNull(consumer);
                if (index < end) {
                    consumer.accept(array[index++]);
                    SkAssertions.concurrentModification(version, expectedVersion);
                    return true;
                }
                return false;

            }

            @Override
            public int characteristics() {
                return SIZED | SUBSIZED | ORDERED | NONNULL;
            }

            @Override
            public long estimateSize() {
                return end - index;
            }

            @Override
            public void forEachRemaining(Consumer<? super String> consumer) {
                Objects.requireNonNull(consumer);
                while(index < end){
                    consumer.accept(cells[index++]);
                }
                SkAssertions.concurrentModification(version, expectedVersion);
            }
        };
    }

    /**
     * Returns a {@code Spliterator} over the rows in this SkCsv instance.
     *
     * <p><strong>Note:</strong> The {@code Spliterator} provided by this method is {@link Spliterator#NONNULL},
     * {@link Spliterator#SIZED}, and {@link Spliterator#ORDERED}.
     *
     * @return a {@code Spliterator} over the rows in this SkCsv instance
     */
    @Override
    public Spliterator<String> spliterator() {
        return customSpliterator(0, size, cells, version);
    }

    /**
     * Applies the specified function to each element in this SkCsvRow, replacing each element with the result of the
     * function.
     *
     * @param mapper the function to apply to each element
     * @throws NullPointerException if the specified mapper is null, or if the mapper returns null for any element
     */
    public void map(Function<? super String, String> mapper) {
        Objects.requireNonNull(mapper);
        for (int i = 0; i < size; i++) {
            cells[i] = Objects.requireNonNull(mapper.apply(cells[i]));
        }
    }

    /**
     * Returns a sequential Stream of the elements in this SkCsvRow.
     * The Stream traverses the elements of the row in the order they were added.
     *
     * @return a sequential Stream of the elements in this row
     */
    public Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a Collector that accumulates the input elements into a new SkCsvRow.
     *
     * @return a Collector that accumulates elements into a new SkCsvRow
     */
    public static Collector<String, ?, SkCsvRow> collector(){
        return Collector.of(
                SkCsvRow::new, SkCsvRow::addAll,
                (csv1, csv2) -> { csv1.addAll(csv2); return csv1; },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    /**
     * Indicates whether some other object is "equal to" this SkCsvRow instance.
     *
     * <p>This method returns {@code true} if the specified object is also a SkCsvRow instance,
     * both instances have the same size, and all corresponding elements are equal.
     * In other words, two SkCsvRow instances are defined to be equal if they contain the same elements in the same order.
     *
     * @param other the reference object with which to compare
     * @return {@code true} if this SkCsvRow instance is equal to the specified object, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if(other instanceof SkCsvRow otherRow && otherRow.size == size) {
            for (int i = 0; i < size; i++) {
                if (!otherRow.cells[i].equals(cells[i])) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns a hash code value for this SkCsvRow.
     * The hash code is computed based on the size of the row and the hash codes of its elements.
     *
     * @return a hash code value for this row
     */
    @Override
    public int hashCode() {
        int hash = size;
        for (int i = 0; i < size; i++) {
            hash ^= cells[i].hashCode();
        }
        return hash;
    }

    /**
     * Returns a CSV string representation of this SkCsvRow using the default configuration.
     * The text is formatted according to the CSV format, where elements are separated by a delimiter and enclosed in
     * quotes if necessary.
     * This method uses the {@link SkCsvConfig#SEMICOLON SEMICOLON} configuration by default, which uses a semicolon as
     * delimiter and double quotes.
     *
     * @return a CSV string representation of this row
     */
    @Override
    public String toString() {
        var formatter = new CsvFormatter(SkCsvConfig.SEMICOLON);
        return formatter.toCsvString(Arrays.asList(cells).subList(0, size));
    }

    /**
     * Returns a CSV string representation of this SkCsvRow using the specified configuration.
     * The text is formatted according to the CSV format, where elements are separated by a delimiter and enclosed in
     * quotes if necessary.
     *
     * @param configuration the CSV configuration to use
     * @return a CSV string representation of this row
     * @throws NullPointerException if the specified configuration is null
     */
    public String toString(SkCsvConfig configuration) {
        Objects.requireNonNull(configuration);
        var formatter = new CsvFormatter(configuration);
        return formatter.toCsvString(Arrays.asList(cells).subList(0, size));
    }

}
