package fr.sekelenao.skcsv.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class SkCsv implements Iterable<SkCsvRow> {

    private final List<SkCsvRow> skCsvRows = new ArrayList<>();

    private SkCsvConfig config = SkCsvConfig.SEMICOLON;

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

    public SkCsv configure(SkCsvConfig config) {
        this.config = Objects.requireNonNull(config);
        return this;
    }

    public SkCsvConfig configuration() {
        return config;
    }

    public int size(){
        return skCsvRows.size();
    }

    public boolean isEmpty(){
        return skCsvRows.isEmpty();
    }

    public void add(SkCsvRow row) {
        Objects.requireNonNull(row);
        skCsvRows.add(row);
    }

    public void addAll(Iterable<SkCsvRow> rows){
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            skCsvRows.add(row);
        }
    }

    public void addAll(SkCsvRow... rows){
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            skCsvRows.add(row);
        }
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
            addAll(rows);
        } else {
            skCsvRows.addAll(position, Arrays.asList(rows));
        }
    }

    public void insertAll(int position, Iterable<SkCsvRow> rows) {
        SkAssertions.validPosition(position, skCsvRows.size());
        Objects.requireNonNull(rows);
        if (position == skCsvRows.size()) {
            addAll(rows);
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
        return skCsvRows.get(0);
    }

    public SkCsvRow getLast() {
        if (skCsvRows.isEmpty()) throw new NoSuchElementException();
        return skCsvRows.get(skCsvRows.size() - 1);
    }

    public SkCsvRow remove(int index) {
        Objects.checkIndex(index, skCsvRows.size());
        return skCsvRows.remove(index);
    }

    public SkCsvRow removeFirst(){
        if(skCsvRows.isEmpty()) throw new NoSuchElementException();
        return skCsvRows.remove(0);
    }

    public SkCsvRow removeLast(){
        if(skCsvRows.isEmpty()) throw new NoSuchElementException();
        return skCsvRows.remove(skCsvRows.size() - 1);
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

    public static Collector<SkCsvRow, SkCsv, SkCsv> collector(){
        return Collector.of(
                SkCsv::new, SkCsv::addAll,
                (csv1, csv2) -> {csv1.addAll(csv2); return csv1;},
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    public static SkCsv from(Path path, SkCsvConfig config) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(config);
        var formatter = new CsvFormatter(config);
        return formatter.split(Files.readAllLines(path));
    }

    public static SkCsv from(Path path) throws IOException {
        Objects.requireNonNull(path);
        return from(path, SkCsvConfig.SEMICOLON);
    }

    public static SkCsv from(Iterable<String> text, SkCsvConfig config) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(config);
        var formatter = new CsvFormatter(config);
        return formatter.split(text);
    }

    public static SkCsv from(Iterable<String> text) {
        Objects.requireNonNull(text);
        return from(text, SkCsvConfig.SEMICOLON);
    }

    public void export(Path path, OpenOption... openOptions) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(openOptions);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, openOptions)) {
            for (var row : skCsvRows) {
                writer.write(formatter.toCsvString(row));
                writer.newLine();
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        Objects.requireNonNull(other);
        return other instanceof SkCsv otherCsv
                && otherCsv.skCsvRows.size() == skCsvRows.size()
                && otherCsv.skCsvRows.equals(skCsvRows);
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

    public static void main(String[] args) {
        var row = new SkCsvRow("x", "y", "z", "x", "z", "y");
        var row2 = row.stream().filter(s -> !s.equals("z")).collect(SkCsvRow.collector());
        System.out.println(row2);
    }

}
