package fr.sekelenao.skcsv;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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


    public SkCsvRow() {
        this.cells = new String[DEFAULT_CAPACITY];
    }

    public SkCsvRow(int amount) {
        SkAssertions.positive(amount);
        this.cells = new String[amount];
        Arrays.fill(cells, "");
        size = amount;
    }

    public SkCsvRow(String... array) {
        Objects.requireNonNull(array);
        this.cells = new String[array.length];
        for (int i = 0; i < array.length; i++)
            this.cells[i] = Objects.requireNonNull(array[i]);
        size = array.length;
    }

    public SkCsvRow(Collection<String> collection) {
        Objects.requireNonNull(collection);
        this.cells = new String[collection.size()];
        for(var value : collection){
            this.cells[size++] = Objects.requireNonNull(value);
        }
    }

    private void growIfNecessary(int amount) {
        if(size + amount > cells.length){
            var newLength = size + Math.max(GROWING_AMOUNT, amount);
            if(newLength <= 0){
                throw new OutOfMemoryError("Required row size is too large");
            }
            cells = Arrays.copyOf(cells, newLength);
        }
    }

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

    public SkCsvRow(Iterable<String> iterable) {
        this(iterable, 0);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(String value) {
        Objects.requireNonNull(value);
        version++;
        growIfNecessary(1);
        cells[size++] = value;
    }

    public void addAll(String... array){
        Objects.requireNonNull(array);
        version++;
        growIfNecessary(array.length);
        for (var value : array) {
            cells[size++] = Objects.requireNonNull(value);
        }
    }

    public void addAll(Collection<String> collection){
        Objects.requireNonNull(collection);
        version++;
        growIfNecessary(collection.size());
        for (var value : collection) {
            cells[size++] = Objects.requireNonNull(value);
        }
    }

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

    public void addAll(Iterable<String> iterable){
        Objects.requireNonNull(iterable);
        addAll(iterable, 0);
    }

    public void set(int index, String value) {
        Objects.checkIndex(index, size);
        Objects.requireNonNull(value);
        cells[index] = value;
    }

    public void fill(int amount) {
        SkAssertions.positive(amount);
        version++;
        growIfNecessary(amount);
        Arrays.fill(cells, size, size + amount, "");
        size += amount;
    }

    public String get(int index) {
        Objects.checkIndex(index, size);
        return cells[index];
    }

    public String getFirst() {
        if (size == 0) throw new NoSuchElementException();
        return cells[0];
    }

    public String getLast() {
        if (size == 0) throw new NoSuchElementException();
        return cells[size - 1];
    }

    public boolean contains(Object object) {
        return object != null && Arrays.stream(cells, 0, size).anyMatch(object::equals);
    }

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

    @Override
    public void forEach(Consumer<? super String> action) {
        Objects.requireNonNull(action);
        for (int i = 0; i < size; i++) {
            action.accept(cells[i]);
        }
    }

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

    @Override
    public Spliterator<String> spliterator() {
        return customSpliterator(0, size, cells, version);
    }

    public void map(Function<? super String, String> mapper) {
        Objects.requireNonNull(mapper);
        for (int i = 0; i < size; i++) {
            cells[i] = Objects.requireNonNull(mapper.apply(cells[i]));
        }
    }

    public Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public static Collector<String, ?, SkCsvRow> collector(){
        return Collector.of(
                SkCsvRow::new, SkCsvRow::addAll,
                (csv1, csv2) -> { csv1.addAll(csv2); return csv1; },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    @Override
    public boolean equals(Object other) {
        Objects.requireNonNull(other);
        if(other instanceof SkCsvRow otherRow && otherRow.size == size) {
            for (int i = 0; i < size; i++) {
                if (!otherRow.cells[i].equals(cells[i])) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = size;
        for (int i = 0; i < size; i++) {
            hash ^= cells[i].hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        var formatter = new CsvFormatter(SkCsvConfig.SEMICOLON);
        return formatter.toCsvString(Arrays.asList(cells).subList(0, size));
    }

    public String toString(SkCsvConfig configuration) {
        Objects.requireNonNull(configuration);
        var formatter = new CsvFormatter(configuration);
        return formatter.toCsvString(Arrays.asList(cells).subList(0, size));
    }

}
