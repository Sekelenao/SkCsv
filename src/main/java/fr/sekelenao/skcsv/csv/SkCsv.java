package fr.sekelenao.skcsv.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkCsv implements Iterable<SkCsvRow> {

    private final List<SkCsvRow> skCsvRows = new ArrayList<>();

    private CsvConfiguration config = CsvConfiguration.SEMICOLON;

    public SkCsv() {}

    public SkCsv(SkCsvRow... skCsvRows) {
        Objects.requireNonNull(skCsvRows);
        for (var row : skCsvRows) {
            Objects.requireNonNull(row);
            this.skCsvRows.add(row);
        }
    }

    public SkCsv(Iterable<SkCsvRow> csvRows) {
        Objects.requireNonNull(csvRows);
        csvRows.forEach(row -> {
            Objects.requireNonNull(row);
            this.skCsvRows.add(row);
        });
    }

    public SkCsv configure(CsvConfiguration config) {
        this.config = Objects.requireNonNull(config);
        return this;
    }

    public CsvConfiguration configuration() {
        return config;
    }

    public int size(){
        return skCsvRows.size();
    }

    public boolean isEmpty(){
        return skCsvRows.isEmpty();
    }

    public void append(SkCsvRow row) {
        Objects.requireNonNull(row);
        skCsvRows.add(row);
    }

    public void insert(int position, SkCsvRow row) {
        SkAssertions.validPosition(position, skCsvRows.size());
        Objects.requireNonNull(row);
        skCsvRows.add(position, row);
    }

    public void insertAll(int position, SkCsvRow... rows) {
        SkAssertions.validPosition(position, skCsvRows.size());
        Objects.requireNonNull(rows);
        if (position == skCsvRows.size()) {
            for (var row : rows) {
                Objects.requireNonNull(row);
                skCsvRows.add(row);
            }
        } else {
            var lst = new ArrayList<SkCsvRow>();
            for (var row : rows) {
                Objects.requireNonNull(row);
                lst.add(row);
            }
            skCsvRows.addAll(position, lst);
        }
    }

    public void insertAll(int position, Iterable<SkCsvRow> rows) {
        SkAssertions.validPosition(position, skCsvRows.size());
        Objects.requireNonNull(rows);
        if (position == skCsvRows.size()) {
            rows.forEach(row -> {
                Objects.requireNonNull(row);
                skCsvRows.add(row);
            });
        } else {
            var lst = new ArrayList<SkCsvRow>();
            for (var row : rows) {
                Objects.requireNonNull(row);
                lst.add(row);
            }
            skCsvRows.addAll(position, lst);
        }
    }

    public SkCsvRow set(int index, SkCsvRow row) {
        Objects.checkIndex(index, skCsvRows.size());
        Objects.requireNonNull(row);
        return skCsvRows.set(index, row);
    }

    public SkCsvRow get(int index) {
        Objects.checkIndex(index, skCsvRows.size());
        return skCsvRows.get(index);
    }

    public SkCsvRow getFirst() {
        if (skCsvRows.isEmpty()) throw new NoSuchElementException();
        return skCsvRows.getFirst();
    }

    public SkCsvRow getLast() {
        if (skCsvRows.isEmpty()) throw new NoSuchElementException();
        return skCsvRows.getLast();
    }

    public SkCsvRow remove(int index) {
        Objects.checkIndex(index, skCsvRows.size());
        return skCsvRows.remove(index);
    }

    public boolean removeIf(Predicate<? super SkCsvRow> filter) {
        return skCsvRows.removeIf(Objects.requireNonNull(filter));
    }

    public boolean contains(Object row) {
        return row instanceof SkCsvRow skCsvRow && skCsvRows.contains(skCsvRow);
    }

    @Override
    public Iterator<SkCsvRow> iterator() {
        return skCsvRows.iterator();
    }

    @Override
    public void forEach(Consumer<? super SkCsvRow> action) {
        skCsvRows.forEach(action);
    }

    public Stream<SkCsvRow> stream() {
        return skCsvRows.stream();
    }

    public void map(Function<? super SkCsvRow, SkCsvRow> mapper) {
        Objects.requireNonNull(mapper);
        var it = skCsvRows.listIterator();
        while (it.hasNext()) {
            var mappedValue = mapper.apply(it.next());
            Objects.requireNonNull(mappedValue);
            it.set(mappedValue);
        }
    }

    public SkCsv copy() {
        var newCsv = new SkCsv();
        skCsvRows.stream()
                .map(SkCsvRow::copy)
                .forEach(newCsv::append);
        return newCsv;
    }

    public static SkCsv from(Path file, CsvConfiguration config) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(config);
        var formatter = new CsvFormatter(config);
        var csv = new SkCsv();
        try (var reader = Files.newBufferedReader(file)) {
            csv.append(formatter.split(reader.readLine()));
        }
        return csv;
    }

    public void export(Path path) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(config);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (var row : this) {
                writer.write(formatter.toCsvString(row));
                writer.newLine();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        Objects.requireNonNull(o);
        return o instanceof SkCsv other
                && other.skCsvRows.size() == skCsvRows.size()
                && other.skCsvRows.equals(skCsvRows);
    }

    @Override
    public int hashCode() {
        int hash = skCsvRows.size();
        for (var row : skCsvRows) {
            hash ^= row.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        var formatter = new CsvFormatter(config);
        var builder = new StringBuilder();
        for(var row : skCsvRows){
            builder.append(formatter.toCsvString(row)).append("\n");
        }
        return builder.toString();
    }

}
