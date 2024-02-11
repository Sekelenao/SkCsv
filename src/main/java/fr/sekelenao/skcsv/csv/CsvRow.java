package fr.sekelenao.skcsv.csv;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Sekelenao
 * @version 1.0
 * @since 1.19.2
 */
public class CsvRow implements Iterable<String> {

    private final List<String> cells;

    private RowConfiguration config = RowConfiguration.SEMICOLON;

    public CsvRow() {
        this.cells = new LinkedList<>();
    }

    public CsvRow(int size){
        SkAssertions.positive(size);
        this.cells = new LinkedList<>();
        fill(size);
    }

    public CsvRow(String... array){
        Objects.requireNonNull(array);
        this.cells = new LinkedList<>();
        for(var value : array){
            Objects.requireNonNull(value);
            SkAssertions.conformValue(value);
            cells.addLast(value);
        }
    }

    public CsvRow(Iterable<String> iterable) {
        Objects.requireNonNull(iterable);
        this.cells = new LinkedList<>();
        for(var value : iterable){
            Objects.requireNonNull(value);
            SkAssertions.conformValue(value);
            cells.addLast(value);
        }
    }

    public CsvRow configure(RowConfiguration config) {
        this.config = Objects.requireNonNull(config);
        return this;
    }

    public RowConfiguration configuration(){
        return config;
    }

    public static CsvRow from(String text, RowConfiguration config) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(config);
        var splitter = new CsvSplitter(config);
        return splitter.split(text);
    }

    public static CsvRow from(String text) {
        Objects.requireNonNull(text);
        return from(text, RowConfiguration.SEMICOLON);
    }

    public int size() {
        return cells.size();
    }

    public boolean isEmpty(){
        return cells.isEmpty();
    }

    public void addFirst(String value) {
        Objects.requireNonNull(value);
        SkAssertions.conformValue(value);
        cells.addFirst(value);
    }

    public void addLast(String value) {
        Objects.requireNonNull(value);
        SkAssertions.conformValue(value);
        cells.addLast(value);
    }

    public void add(int position, String value){
        SkAssertions.validPosition(position, cells.size());
        SkAssertions.conformValue(value);
        cells.add(position, value);
    }

    public void addAll(int position, String... values){
        SkAssertions.validPosition(position, cells.size());
        Objects.requireNonNull(values);
        if(position == cells.size()){
            for(var value: values){
                Objects.requireNonNull(value);
                SkAssertions.conformValue(value);
                this.addLast(value);
            }
        } else {
            var it = cells.listIterator(position);
            for(var value: values){
                Objects.requireNonNull(value);
                SkAssertions.conformValue(value);
                it.add(value);
            }
        }
    }

    public void addAll(int position, Iterable<String> values){
        SkAssertions.validPosition(position, cells.size());
        Objects.requireNonNull(values);
        if(position == cells.size()){
            values.forEach(value -> {
                Objects.requireNonNull(value);
                SkAssertions.conformValue(value);
                this.addLast(value);
            });
        } else {
            var it = cells.listIterator(position);
            values.forEach(value -> {
                Objects.requireNonNull(value);
                SkAssertions.conformValue(value);
                it.add(value);
            });
        }
    }

    public String set(int index, String value) {
        Objects.checkIndex(index, cells.size());
        Objects.requireNonNull(value);
        SkAssertions.conformValue(value);
        return cells.set(index, value);
    }

    public void fill(int amount){
        SkAssertions.positive(amount);
        for(int i = 0; i < amount; i++) addLast("");
    }

    public String get(int index) {
        Objects.checkIndex(index, cells.size());
        return cells.get(index);
    }

    public String getFirst(){
        if(cells.isEmpty()) throw new NoSuchElementException();
        return cells.getFirst();
    }

    public String getLast(){
        if(cells.isEmpty()) throw new NoSuchElementException();
        return cells.getLast();
    }

    public String remove(int index) {
        Objects.checkIndex(index, cells.size());
        return cells.remove(index);
    }

    public boolean removeIf(Predicate<? super String> filter) {
        return cells.removeIf(Objects.requireNonNull(filter));
    }

    public boolean contains(Object value) {
        return value instanceof String s && cells.contains(s);
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        cells.forEach(Objects.requireNonNull(action));
    }

    @Override
    public Iterator<String> iterator() {
        return cells.iterator();
    }

    public ListIterator<String> listIterator(){
        return new ListIterator<>() {

            private final ListIterator<String> it = cells.listIterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public String next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return it.next();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public String previous() {
                if(!hasPrevious())
                    throw new NoSuchElementException();
                return it.previous();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                it.remove();
            }

            @Override
            public void set(String string) {
                Objects.requireNonNull(string);
                SkAssertions.conformValue(string);
                it.set(string);
            }

            @Override
            public void add(String string) {
                Objects.requireNonNull(string);
                SkAssertions.conformValue(string);
                it.add(string);
            }

        };
    }

    public Stream<String> stream() {
        return cells.stream();
    }

    public void map(Function<? super String, String> mapper){
        Objects.requireNonNull(mapper);
        var it = cells.listIterator();
        while (it.hasNext()){
            var mappedValue = mapper.apply(it.next());
            SkAssertions.conformValue(mappedValue);
            it.set(mappedValue);
        }
    }

    public CsvRow copy() {
        return new CsvRow(this.cells);
    }

    @Override
    public boolean equals(Object o) {
        Objects.requireNonNull(o);
        return o instanceof CsvRow other
                && other.cells.size() == cells.size()
                && other.cells.equals(cells);
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
        var quote = config.quoteAsString();
        var delimiter = config.delimiterAsString();
        var result = new StringBuilder();
        var pattern = Pattern.compile("[" + config.delimiter() + config.quote() + "]");
        for (var v : cells) {
            if (pattern.matcher(v).find()) {
                result.append(quote).append(v.replace(quote, quote.repeat(2))).append(quote);
            } else {
                result.append(v);
            }
            result.append(delimiter);
        }
        if (!result.isEmpty()) {
            result.setLength(result.length() - delimiter.length());
        }
        return result.toString();
    }

}
