package fr.sekelenao.skcsv.csv;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkCsvRow implements Iterable<String> {

    private final List<String> cells = new ArrayList<>();

    public SkCsvRow() {}

    public SkCsvRow(int amount) {
        SkAssertions.positive(amount);
        fill(amount);
    }

    public SkCsvRow(String... array) {
        Objects.requireNonNull(array);
        for (var value : array) {
            Objects.requireNonNull(value);
            cells.add(value);
        }
    }

    public SkCsvRow(Iterable<String> iterable) {
        Objects.requireNonNull(iterable);
        for (var value : iterable) {
            Objects.requireNonNull(value);
            cells.add(value);
        }
    }

    public int size() {
        return cells.size();
    }

    public boolean isEmpty() {
        return cells.isEmpty();
    }

    public boolean isBlank(){
        return cells.stream().allMatch(String::isBlank);
    }

    public void add(String value) {
        Objects.requireNonNull(value);
        cells.add(value);
    }

    public void addAll(Iterable<String> values){
        Objects.requireNonNull(values);
        for (var value : values) {
            Objects.requireNonNull(value);
            cells.add(value);
        }
    }

    public void addAll(String... values){
        Objects.requireNonNull(values);
        for (var value : values) {
            Objects.requireNonNull(value);
            cells.add(value);
        }
    }

    public void insert(int position, String value) {
        Objects.requireNonNull(value);
        SkAssertions.validPosition(position, cells.size());
        cells.add(position, value);
    }

    public void insertAll(int position, String... values) {
        SkAssertions.validPosition(position, cells.size());
        Objects.requireNonNull(values);
        if (position == cells.size()) {
            for (var value : values) {
                Objects.requireNonNull(value);
                cells.add(value);
            }
        } else {
            cells.addAll(position, Arrays.asList(values));
        }
    }

    public void insertAll(int position, Iterable<String> values) {
        SkAssertions.validPosition(position, cells.size());
        Objects.requireNonNull(values);
        if (position == cells.size()) {
            values.forEach(value -> {
                Objects.requireNonNull(value);
                cells.add(value);
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

    public String set(int index, String value) {
        Objects.checkIndex(index, cells.size());
        Objects.requireNonNull(value);
        return cells.set(index, value);
    }

    public void fill(int amount) {
        SkAssertions.positive(amount);
        for (int i = 0; i < amount; i++) cells.add("");
    }

    public String get(int index) {
        Objects.checkIndex(index, cells.size());
        return cells.get(index);
    }

    public String getFirst() {
        if (cells.isEmpty()) throw new NoSuchElementException();
        return cells.get(0);
    }

    public String getLast() {
        if (cells.isEmpty()) throw new NoSuchElementException();
        return cells.get(cells.size() - 1);
    }

    public String remove(int index) {
        Objects.checkIndex(index, cells.size());
        return cells.remove(index);
    }

    public boolean removeIf(Predicate<? super String> filter) {
        Objects.requireNonNull(filter);
        return cells.removeIf(filter);
    }

    public boolean contains(Object value) {
        return value != null && cells.stream().anyMatch(value::equals);
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        Objects.requireNonNull(action);
        cells.forEach(action);
    }

    @Override
    public Iterator<String> iterator() {
        return cells.iterator();
    }

    public Stream<String> stream() {
        return cells.stream();
    }

    public void map(Function<? super String, String> mapper) {
        Objects.requireNonNull(mapper);
        var it = cells.listIterator();
        while (it.hasNext()) {
            var mappedValue = mapper.apply(it.next());
            Objects.requireNonNull(mappedValue);
            it.set(mappedValue);
        }
    }

    public SkCsvRow copy() {
        return new SkCsvRow(this.cells);
    }

    @Override
    public boolean equals(Object other) {
        Objects.requireNonNull(other);
        return other instanceof SkCsvRow otherRow
                && otherRow.cells.size() == cells.size()
                && otherRow.cells.equals(cells);
    }

    @Override
    public int hashCode() {
        int hash = cells.size();
        for (String cell : cells) {
            hash ^= cell.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        var formatter = new CsvFormatter(SkCsvConfig.SEMICOLON);
        return formatter.toCsvString(cells);
    }

    public String toString(SkCsvConfig configuration) {
        Objects.requireNonNull(configuration);
        var formatter = new CsvFormatter(configuration);
        return formatter.toCsvString(cells);
    }

}
