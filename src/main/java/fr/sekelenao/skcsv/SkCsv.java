package fr.sekelenao.skcsv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SkCsv implements Iterable<SkCsvRow> {

    private final LinkedList<SkCsvRow> internalRows = new LinkedList<>();

    private SkCsvConfig config = SkCsvConfig.SEMICOLON;

    public SkCsv() {}

    public SkCsv(SkCsvRow... rows) {
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            internalRows.add(row);
        }
    }

    public SkCsv(Iterable<SkCsvRow> rows) {
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            internalRows.add(row);
        }
    }

    public SkCsv configure(SkCsvConfig config) {
        this.config = Objects.requireNonNull(config);
        return this;
    }

    public SkCsvConfig configuration() {
        return config;
    }

    public int size(){
        return internalRows.size();
    }

    public boolean isEmpty(){
        return internalRows.isEmpty();
    }

    public void add(SkCsvRow row) {
        Objects.requireNonNull(row);
        internalRows.addLast(row);
    }

    public void addAll(SkCsvRow... rows){
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            internalRows.add(row);
        }
    }

    public void addAll(Iterable<SkCsvRow> rows){
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            internalRows.add(row);
        }
    }

    public void insert(int position, SkCsvRow row) {
        SkAssertions.validPosition(position, internalRows.size());
        Objects.requireNonNull(row);
        internalRows.add(position, row);
    }

    public void insertAll(int position, SkCsvRow... rows) {
        SkAssertions.validPosition(position, this.internalRows.size());
        Objects.requireNonNull(rows);
        var lstItr = internalRows.listIterator(position);
        for (var row : rows) {
            Objects.requireNonNull(row);
            lstItr.add(row);
        }
    }

    public void insertAll(int position, Iterable<SkCsvRow> rows) {
        SkAssertions.validPosition(position, this.internalRows.size());
        Objects.requireNonNull(rows);
        var lstItr = internalRows.listIterator(position);
        for (var row : rows) {
            Objects.requireNonNull(row);
            lstItr.add(row);
        }
    }

    public void set(int index, SkCsvRow row) {
        Objects.checkIndex(index, internalRows.size());
        Objects.requireNonNull(row);
        internalRows.set(index, row);
    }

    public SkCsvRow get(int index) {
        Objects.checkIndex(index, internalRows.size());
        return internalRows.get(index);
    }

    public SkCsvRow getFirst() {
        if (internalRows.isEmpty()) throw new NoSuchElementException();
        return internalRows.getFirst();
    }

    public SkCsvRow getLast() {
        if (internalRows.isEmpty()) throw new NoSuchElementException();
        return internalRows.getLast();
    }

    public void remove(int index) {
        Objects.checkIndex(index, internalRows.size());
        internalRows.remove(index);
    }

    public void removeFirst(){
        if(internalRows.isEmpty()) throw new NoSuchElementException();
        internalRows.removeFirst();
    }

    public void removeLast(){
        if(internalRows.isEmpty()) throw new NoSuchElementException();
        internalRows.removeLast();
    }

    public boolean removeIf(Predicate<? super SkCsvRow> filter) {
        return internalRows.removeIf(Objects.requireNonNull(filter));
    }

    public boolean contains(Object object) {
        return object != null && internalRows.stream().anyMatch(object::equals);
    }

    public ListIterator<SkCsvRow> listIterator(int index){
        Objects.checkIndex(index, internalRows.size());
        return new ListIterator<>() {

            private final ListIterator<SkCsvRow> lstItr = internalRows.listIterator(index);

            @Override
            public boolean hasNext() {
                return lstItr.hasNext();
            }

            @Override
            public SkCsvRow next() {
                return lstItr.next();
            }

            @Override
            public boolean hasPrevious() {
                return lstItr.hasPrevious();
            }

            @Override
            public SkCsvRow previous() {
                return lstItr.previous();
            }

            @Override
            public int nextIndex() {
                return lstItr.nextIndex();
            }

            @Override
            public int previousIndex() {
                return lstItr.previousIndex();
            }

            @Override
            public void remove() {
                lstItr.remove();
            }

            @Override
            public void set(SkCsvRow row) {
                Objects.requireNonNull(row);
                lstItr.set(row);
            }

            @Override
            public void add(SkCsvRow row) {
                Objects.requireNonNull(row);
                lstItr.add(row);
            }

        };
    }

    @Override
    public Iterator<SkCsvRow> iterator() {
        return listIterator(0);
    }

    @Override
    public void forEach(Consumer<? super SkCsvRow> action) {
        Objects.requireNonNull(action);
        internalRows.forEach(action);
    }

    @Override
    public Spliterator<SkCsvRow> spliterator() {
        return Spliterators.spliterator(internalRows, Spliterator.NONNULL | Spliterator.SIZED | Spliterator.ORDERED);
    }

    public Stream<SkCsvRow> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public void map(Function<? super SkCsvRow, SkCsvRow> mapper) {
        Objects.requireNonNull(mapper);
        var lstItr = internalRows.listIterator();
        while (lstItr.hasNext()) {
            var mappedValue = mapper.apply(lstItr.next());
            Objects.requireNonNull(mappedValue);
            lstItr.set(mappedValue);
        }
    }

    public static Collector<SkCsvRow, ?, SkCsv> collector(){
        return Collector.of(
                SkCsv::new, SkCsv::addAll,
                (csv1, csv2) -> { csv1.addAll(csv2); return csv1; },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    public static SkCsv from(Path path, SkCsvConfig config, Charset charset) throws IOException {
        SkAssertions.requireNonNulls(path, config, charset);
        var formatter = new CsvFormatter(config);
        return formatter.split(Files.readAllLines(path, charset));
    }

    public static SkCsv from(Path path, SkCsvConfig config) throws IOException {
        SkAssertions.requireNonNulls(path, config);
        return from(path, config, Charset.defaultCharset());
    }

    public static SkCsv from(Path path, Charset charset) throws IOException {
        SkAssertions.requireNonNulls(path, charset);
        return from(path, SkCsvConfig.SEMICOLON, charset);
    }

    public static SkCsv from(Path path) throws IOException {
        return from(Objects.requireNonNull(path), SkCsvConfig.SEMICOLON);
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

    public void export(Path path, Charset charset, OpenOption... openOptions) throws IOException {
        SkAssertions.requireNonNulls(path, charset, openOptions);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, charset, openOptions)) {
            for (var row : internalRows) {
                writer.write(formatter.toCsvString(row));
                writer.newLine();
            }
        }
    }

    public void export(Path path, OpenOption... openOptions) throws IOException {
        SkAssertions.requireNonNulls(path, openOptions);
        export(path, Charset.defaultCharset(), openOptions);
    }

    @Override
    public boolean equals(Object other) {
        Objects.requireNonNull(other);
        return other instanceof SkCsv otherCsv
                && otherCsv.internalRows.size() == internalRows.size()
                && otherCsv.internalRows.equals(internalRows);
    }

    @Override
    public int hashCode() {
        int hash = internalRows.size();
        for (var row : internalRows) {
            hash ^= row.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        var formatter = new CsvFormatter(config);
        var builder = new StringBuilder();
        for(var row : internalRows){
            builder.append(formatter.toCsvString(row)).append("\n");
        }
        return builder.toString();
    }

}
