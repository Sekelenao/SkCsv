package io.github.sekelenao.skcsv;

import io.github.sekelenao.skcsv.exception.CsvParsingException;
import io.github.sekelenao.skcsv.exception.InvalidCsvValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

final class SkCsvTest {

    private static SkCsv csvTemplate() {
        return new SkCsv(
                new SkCsvRow("\"Hello", "world", "!;"),
                new SkCsvRow("'Hello,", "\"second,\"", "world", "!'", "")
        );
    }

    private static SkCsv csvTemplate(int lineNumber) {
        return new SkCsv(
                IntStream.range(0, lineNumber)
                        .mapToObj(i -> new SkCsvRow(String.valueOf(i)))
                        .toList()
        );
    }

    @Nested
    final class Constructors {

        @Test
        @DisplayName("Empty after default constructor")
        void byEmpty() {
            assertAll("Empty after default constructor",
                    () -> assertEquals(0, new SkCsv().size()),
                    () -> assertEquals("", new SkCsv().toString()),
                    () -> assertTrue(new SkCsv().isEmpty())
            );
        }

        @Test
        @DisplayName("VarArgs constructor")
        void byVarArgs() {
            var array = new SkCsvRow[]{
                    new SkCsvRow("\"Hello", "world", "!;"),
                    new SkCsvRow("'Hello,", "\"second\"", "world", "!'", "")
            };
            var csv = new SkCsv(array);
            assertAll("Simple operations",
                    () -> assertEquals(2, csv.size()),
                    () -> assertEquals("""
                                    \"""Hello";world;"!;"
                                    'Hello,;\"""second\""";world;!';
                                    """
                            , csv.toString()),
                    () -> assertEquals(0, new SkCsv(new SkCsvRow[]{}).size()),
                    () -> assertEquals("", new SkCsv(new SkCsvRow[]{}).toString()),
                    () -> assertEquals(1, new SkCsv(new SkCsvRow()).size()),
                    () -> assertEquals("\n", new SkCsv(new SkCsvRow()).toString())
            );
        }

        @Test
        @DisplayName("VarArgs constructor null assertions")
        void byVarArgsAssertions() {
            var emptyRow = new SkCsvRow();
            var array1 = new SkCsvRow[]{new SkCsvRow(), null};
            assertAll("VarArgs constructor null assertions",
                    () -> assertThrows(NullPointerException.class, () -> new SkCsv((SkCsvRow) null)),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsv(new SkCsvRow[]{null})),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsv(array1)),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsv(emptyRow, null))
            );
        }

        @Test
        @DisplayName("Iterable constructor")
        void byIterable() {
            var rowList = new ArrayList<>(
                    List.of(
                            new SkCsvRow("One", "!"),
                            new SkCsvRow("Two", "!"),
                            new SkCsvRow("Three", "!")
                    )
            );
            var csv = new SkCsv(rowList);
            assertAll("Simple operations",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals("""
                            One;!
                            Two;!
                            Three;!
                            """, csv.toString()),
                    () -> assertFalse(csv.isEmpty()),
                    () -> assertEquals(0, new SkCsv(Collections.emptyList()).size()),
                    () -> assertEquals(1, new SkCsv(new SkCsvRow()).size()),
                    () -> assertEquals("", new SkCsv().toString()),
                    () -> assertEquals("\n", new SkCsv(new SkCsvRow()).toString()),
                    () -> assertEquals("\n\n", new SkCsv(List.of(new SkCsvRow(), new SkCsvRow())).toString())
            );
        }

        @Test
        @DisplayName("Iterable constructor null assertions")
        void byIterableAssertions() {
            var wrongList = new ArrayList<SkCsvRow>();
            wrongList.add(new SkCsvRow());
            wrongList.add(null);
            assertAll("NullPointer assertions",
                    () -> assertThrows(NullPointerException.class, () -> new SkCsv((List<SkCsvRow>) null)),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsv(wrongList))
            );
        }

    }

    @Nested
    final class Configuration {

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

        @Test
        @DisplayName("Configuration is updated")
        void configUpdated() {
            var csv = csvTemplate();
            assertAll("Configuration is updated",
                    () -> assertEquals("""
                            \"""Hello";world;"!;"
                            'Hello,;\"""second,\""";world;!';
                            """, csv.toString()),
                    () -> assertEquals(SkCsvConfig.SEMICOLON, csv.configuration()),
                    () -> {
                        csv.configure(new SkCsvConfig(',', '\''));
                        assertEquals("""
                                "Hello,world,!;
                                '''Hello,','"second,"',world,'!''',
                                """, csv.toString());
                    },
                    () -> assertEquals(new SkCsvConfig(',', '\''), csv.configuration())
            );
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Configuration assertions")
        void configAssertions(char wrongChar) {
            assertAll("Configuration assertions",
                    () -> assertThrows(InvalidCsvValueException.class, () -> new SkCsvConfig(wrongChar, '"')),
                    () -> assertThrows(InvalidCsvValueException.class, () -> new SkCsvConfig(';', wrongChar)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new SkCsvConfig(';', ';'))
            );
        }
    }

    @Nested
    @DisplayName("Inserts and add")
    final class InsertAndAdd {

        @Test
        @DisplayName("add")
        void add() {
            var csv = new SkCsv();
            csv.add(new SkCsvRow("Hello", "world", "!"));
            assertAll("add",
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("Hello;world;!\n", csv.toString()),
                    () -> assertEquals(new SkCsvRow("Hello", "world", "!"), csv.get(0)),
                    () -> {
                        csv.add(new SkCsvRow("I", "love", "Java", ""));
                        assertEquals("""
                                        Hello;world;!
                                        I;love;Java;
                                        """
                                , csv.toString());
                    },
                    () -> assertEquals(new SkCsvRow("I", "love", "Java", ""), csv.get(1))
            );
        }

        @Test
        @DisplayName("add null assertions")
        void addNullAssertions() {
            var emptyCsv = new SkCsv();
            assertThrows(NullPointerException.class, () -> emptyCsv.add(null));
        }

        @Test
        @DisplayName("add first")
        void addFirst() {
            var csv = new SkCsv();
            csv.addFirst(new SkCsvRow("Hello", "world", "!"));
            assertAll("addFirst",
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("Hello;world;!\n", csv.toString()),
                    () -> assertEquals(new SkCsvRow("Hello", "world", "!"), csv.get(0)),
                    () -> {
                        csv.addFirst(new SkCsvRow("I", "love", "Java", ""));
                        assertEquals("""
                                        I;love;Java;
                                        Hello;world;!
                                        """
                                , csv.toString());
                    },
                    () -> assertEquals(new SkCsvRow("Hello", "world", "!"), csv.get(1))
            );
        }

        @Test
        @DisplayName("addFirst null assertions")
        void addFirstNullAssertions() {
            var emptyCsv = new SkCsv();
            assertThrows(NullPointerException.class, () -> emptyCsv.addFirst(null));
        }

        @Test
        @DisplayName("Insert")
        void insert() {
            var csv = new SkCsv();
            csv.insert(0, new SkCsvRow("Hello"));
            csv.insert(0, new SkCsvRow("world"));
            csv.insert(1, new SkCsvRow("!"));
            assertAll("Insert",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals("""
                                    world
                                    !
                                    Hello
                                    """
                            , csv.toString()),
                    () -> assertEquals(new SkCsvRow("world"), csv.get(0)),
                    () -> assertEquals(new SkCsvRow("!"), csv.get(1)),
                    () -> assertEquals(new SkCsvRow("Hello"), csv.get(2))
            );
        }

        @Test
        @DisplayName("Insert null assertions")
        void insertNullAssertions() {
            var emptyCsv = new SkCsv();
            assertThrows(NullPointerException.class, () -> emptyCsv.insert(0, null));
        }

        @Test
        @DisplayName("Insert indices assertions")
        void insertIndicesAssertions() {
            var csv = new SkCsv();
            var row = new SkCsvRow("out");
            assertAll("Insert indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(-1, row)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(1, row))
            );
            csv.insertAll(csv.size(), new SkCsvRow("Hello", "world", "!"));
            assertAll("Insert indices assertions 2",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(-1, row)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(4, row))
            );
        }

        @Test
        @DisplayName("Insert all at end")
        void insertAllAtEnd() {
            var csv = csvTemplate();
            csv.insertAll(csv.size(), new SkCsvRow("(", "and Meta-verse", ")"), new SkCsvRow("Java"));
            csv.insertAll(csv.size(), List.of(new SkCsvRow("yes"), new SkCsvRow("ok")));
            assertAll("Insert all at end",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    ""\"Hello";world;"!;"
                                    'Hello,;""\"second,""\";world;!';
                                    (;and Meta-verse;)
                                    Java
                                    yes
                                    ok
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all at start")
        void insertAllAtStart() {
            var csv = csvTemplate();
            csv.insertAll(0, new SkCsvRow("(", "and Meta-verse", ")"), new SkCsvRow());
            csv.insertAll(0, List.of(new SkCsvRow(), new SkCsvRow()));
            assertAll("Insert all at start",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    
                                    
                                    (;and Meta-verse;)
                                    
                                    ""\"Hello";world;"!;"
                                    'Hello,;""\"second,""\";world;!';
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all")
        void insertAll() {
            var csv = csvTemplate();
            csv.insertAll(1, List.of(new SkCsvRow("(", "and Meta-verse", ")"), new SkCsvRow()));
            csv.insertAll(1, new SkCsvRow("1"), new SkCsvRow("2"));
            assertAll("Insert all",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    ""\"Hello";world;"!;"
                                    1
                                    2
                                    (;and Meta-verse;)
                                    
                                    'Hello,;""\"second,""\";world;!';
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all null assertions")
        void insertAllNullAssertions() {
            var emptyCsv = new SkCsv();
            var helloCsv = csvTemplate();
            var lst = new ArrayList<SkCsvRow>();
            lst.add(new SkCsvRow());
            lst.add(null);
            assertAll("Insert all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, (Iterable<SkCsvRow>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, (SkCsvRow[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, lst)),
                    () -> assertThrows(NullPointerException.class, () -> helloCsv.insertAll(1, lst)),
                    () -> assertThrows(NullPointerException.class, () -> helloCsv.insertAll(1, new SkCsvRow[]{null}))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 3, 5, 100})
        @DisplayName("Insert all position assertions")
        void insertAllPositionAssertions(int index) {
            var csv = csvTemplate();
            var lst = new ArrayList<SkCsvRow>();
            var row = new SkCsvRow();
            assertAll("Insert all position assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insertAll(index, lst)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insertAll(index, row))
            );
        }

        @Test
        @DisplayName("Add all at end")
        void addAll() {
            var csv = csvTemplate();
            csv.addAll(new SkCsvRow("(", "and Meta-verse", ")"), new SkCsvRow("Java"));
            csv.addAll(List.of(new SkCsvRow("yes"), new SkCsvRow("ok")));
            assertAll("Add all",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    ""\"Hello";world;"!;"
                                    'Hello,;""\"second,""\";world;!';
                                    (;and Meta-verse;)
                                    Java
                                    yes
                                    ok
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all null assertions")
        void addAllNullAssertions() {
            var emptyCsv = new SkCsv();
            var lst = Collections.singleton((SkCsvRow) null);
            assertAll("Add all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll((Iterable<SkCsvRow>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll((SkCsvRow[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll(lst))
            );
        }

    }

    @Nested
    @DisplayName("Set")
    final class SetTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Set all indices working")
        void setAllIndices(int index) {
            var csv = csvTemplate(8);
            assertAll("Set all indices",
                    () -> {
                        csv.set(index, new SkCsvRow("replaced"));
                        assertEquals("replaced", csv.get(index).getFirst());
                    }
            );
        }

        @Test
        @DisplayName("Set indices assertions")
        void setAllIndicesAssertions() {
            var csv = csvTemplate(8);
            var emptyCsv = new SkCsv();
            var emptyRow = new SkCsvRow();
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.set(-1, emptyRow)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.set(8, emptyRow)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyCsv.set(0, emptyRow))
            );
        }

        @Test
        @DisplayName("Set null assertions")
        void setNullAssertions() {
            var csv = new SkCsv(new SkCsvRow("Alone"));
            assertThrows(NullPointerException.class, () -> csv.set(0, null));
        }

    }

    @Nested
    final class Get {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Get all indices working")
        void getAllIndices(int index) {
            var csv = csvTemplate(8);
            assertEquals(String.valueOf(index), csv.get(index).getFirst());
        }

        @Test
        @DisplayName("Get indices assertions")
        void getAllIndicesAssertions() {
            var emptyCsv = new SkCsv();
            var csv = csvTemplate(8);
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.get(8)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyCsv.get(0))
            );
        }

        @Test
        @DisplayName("Get first and last")
        void getFirstAndLast() {
            var row = csvTemplate(8);
            assertAll("Get first and last",
                    () -> assertEquals("0", row.getFirst().getFirst()),
                    () -> assertEquals("7", row.getLast().getLast())
            );
        }

        @Test
        @DisplayName("Get indices assertions")
        void getFirstAndLastAssertions() {
            var emptyCsv = new SkCsv();
            assertAll("Get first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::getFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::getLast)
            );
        }

    }

    @Nested
    final class Remove {

        @ParameterizedTest
        @ValueSource(ints = {0, 1})
        @DisplayName("Remove basic tests")
        void remove(int index) {
            var csv = csvTemplate(2);
            csv.remove(index);
            assertAll("Remove basic tests",
                    () -> assertEquals(1, csv.size()),
                    () -> assertDoesNotThrow(() -> csv.remove(0)),
                    () -> assertEquals(0, csv.size())
            );
        }

        @Test
        @DisplayName("Remove all values")
        void removeAll() {
            var csv = csvTemplate();
            var initialSize = csv.size();
            for (int i = 0; i < initialSize; i++) csv.remove(0);
            assertAll("Remove all",
                    () -> assertTrue(csv.isEmpty()),
                    () -> assertEquals(0, csv.size()),
                    () -> assertEquals("", csv.toString())
            );
        }

        @Test
        @DisplayName("Remove assertions")
        void removeAssertions() {
            var csv = csvTemplate();
            csv.remove(0);
            assertAll("Remove assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.remove(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.remove(2)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.remove(1))
            );
        }

        @Test
        @DisplayName("Remove first and last")
        void removeFirstAndLast() {
            int count = 0;
            var csv = csvTemplate(3);
            while (!csv.isEmpty()) {
                csv.removeFirst();
                count++;
                assertEquals(3 - count, csv.size());
            }
            assertEquals(3, count);
            count = 0;
            var csv2 = csvTemplate(3);
            while (!csv2.isEmpty()) {
                csv2.removeLast();
                count++;
                assertEquals(3 - count, csv2.size());
            }
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Remove first and last assertions")
        void removeFirstAndLastAssertions() {
            var csv = csvTemplate(3);
            var emptyCsv = new SkCsv();
            csv.removeFirst();
            csv.removeLast();
            csv.removeFirst();
            assertAll("Remove first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, csv::removeFirst),
                    () -> assertThrows(NoSuchElementException.class, csv::removeLast),
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::removeFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::removeLast)
            );
        }

        @Test
        @DisplayName("RemoveIf basic tests")
        void removeIf() {
            var csv = csvTemplate(8);
            int expectedSize = csv.size() / 2;
            assertAll("RemoveIf basic tests",
                    () -> assertTrue(csv.removeIf(r -> (Integer.parseInt(r.getFirst()) & 1) == 0)),
                    () -> assertTrue(csv.stream().allMatch(r -> (Integer.parseInt(r.getFirst()) & 1) == 1)),
                    () -> assertEquals(expectedSize, csv.size()),
                    () -> assertFalse(() -> csv.removeIf((Object value) -> false)),
                    () -> assertEquals(expectedSize, csv.size()),
                    () -> {
                        csv.removeIf(value -> true);
                        assertEquals(0, csv.size());
                    }
            );
        }

        @Test
        @DisplayName("RemoveIf null assertions")
        void removeIfNullAssertions() {
            var emptyCsv = new SkCsv();
            assertThrows(NullPointerException.class, () -> emptyCsv.removeIf(null));
        }

    }

    @Nested
    final class Contains {

        @Test
        @DisplayName("Contains basic tests")
        void contains() {
            var csv = csvTemplate(8);
            assertAll("Contains basic tests",
                    () -> assertTrue(csv.contains(new SkCsvRow("5"))),
                    () -> assertTrue(csv.contains(new SkCsvRow("0"))),
                    () -> assertFalse(csv.contains(new SkCsvRow("8"))),
                    () -> assertFalse(csv.contains(new SkCsvRow("-1"))),
                    () -> assertFalse(csv.contains(new Object())),
                    () -> assertFalse(csv.contains(null))
            );
        }

    }

    @Nested
    final class ForEach {

        @Test
        @DisplayName("ForEach basic test")
        void forEach() {
            var csv = csvTemplate();
            var lst = new ArrayList<SkCsvRow>();
            csv.forEach((Object value) -> lst.add((SkCsvRow) value));
            assertTrue(IntStream.range(0, csv.size()).allMatch(i -> csv.get(i).equals(lst.get(i))));
        }

        @Test
        @DisplayName("ForEach null assertions")
        void forEachNullAssertions() {
            var emptyCsv = new SkCsv();
            assertThrows(NullPointerException.class, () -> emptyCsv.forEach(null));
        }

    }

    @Nested
    @DisplayName("Iterable")
    final class IterableTest {

        @Test
        @DisplayName("For each loop is working")
        void iterableFor() {
            var lst = new ArrayList<SkCsvRow>();
            var csv = csvTemplate();
            for (var row : csv) lst.add(row);
            assertAll("For each working",
                    () -> assertEquals(csv.size(), lst.size()),
                    () -> assertTrue(IntStream.range(0, csv.size()).allMatch(i -> csv.get(i).equals(lst.get(i))))
            );
        }

        @Test
        @DisplayName("Iterator is working")
        void iterator() {
            var csv = csvTemplate();
            var it = csv.iterator();
            var emptyIt = new SkCsvRow().iterator();
            assertAll("Iterator is working",
                    () -> assertTrue(it.hasNext()),
                    () -> assertTrue(it.hasNext()),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            assertEquals(csv.get(i), it.next());
                        }
                    },
                    () -> assertFalse(it::hasNext),
                    () -> assertThrows(NoSuchElementException.class, it::next),
                    () -> assertFalse(emptyIt.hasNext())
            );
        }

        @Test
        @DisplayName("Iterator remove is working")
        void iteratorRemove() {
            var csv = csvTemplate();
            var it = csv.iterator();
            assertAll("Iterator remove",
                    () -> assertThrows(IllegalStateException.class, it::remove),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            if (i == 1) {
                                it.remove();
                                assertThrows(IllegalStateException.class, it::remove);
                            }
                            it.next();
                        }
                    },
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("'Hello,;\"\"\"second,\"\"\";world;!';\n", csv.toString())
            );
        }

        @Test
        @DisplayName("Iterator for each remaining is working")
        void iterableForEachRemaining() {
            var lst = new ArrayList<SkCsvRow>();
            var csv = csvTemplate();
            var it = csv.iterator();
            it.next();
            it.forEachRemaining(lst::add);
            assertAll("For each remaining working",
                    () -> assertEquals(1, lst.size()),
                    () -> assertEquals(new SkCsvRow("'Hello,", "\"second,\"", "world", "!'", ""), lst.get(0))
            );
        }

        @Test
        @DisplayName("Iterator concurrent modifications")
        void iteratorConcurrentModifications() {
            var csv = csvTemplate();
            var it = csv.iterator();
            csv.remove(0);
            assertThrows(ConcurrentModificationException.class, it::next);
        }

        @Test
        @DisplayName("ListIterator is working")
        void listIterator(){
            var csv = csvTemplate();
            var expected = 0;
            var lstItr = csv.listIterator(0);
            while (lstItr.hasNext()){
                assertEquals(expected++, lstItr.nextIndex());
                lstItr.next();
            }
            expected--;
            while (lstItr.hasPrevious()){
                assertEquals(expected--, lstItr.previousIndex());
                lstItr.previous();
            }
            lstItr.add(new SkCsvRow("add"));
            assertEquals(3, csv.size());
        }

        @Test
        @DisplayName("ListIterator assertions")
        void listIteratorAssertions(){
            var csv = csvTemplate();
            var lstItr = csv.listIterator();
            var emptyRow = new SkCsvRow();
            assertAll(
                    () -> assertThrows(IllegalStateException.class, lstItr::remove),
                    () -> assertThrows(IllegalStateException.class, () -> lstItr.set(emptyRow)),
                    () -> assertThrows(NullPointerException.class, () -> lstItr.set(null)),
                    () -> assertThrows(NullPointerException.class, () -> lstItr.forEachRemaining(null)),
                    () -> assertThrows(NullPointerException.class, () -> lstItr.add(null))
            );
        }

    }

    @Nested
    @DisplayName("Stream")
    final class StreamTest {

        @Test
        @DisplayName("Stream basic tests")
        void stream() {
            var lst = new ArrayList<SkCsvRow>();
            var emptyCsv = new SkCsv();
            var csv = csvTemplate();
            csv.add(new SkCsvRow());
            csv.stream().filter(SkCsvRow::isEmpty).forEach(lst::add);
            csv.remove(csv.size() - 1);
            assertAll("Stream basics",
                    () -> assertEquals(2, csv.size()),
                    () -> assertEquals(1, lst.size()),
                    () -> assertDoesNotThrow(emptyCsv::stream)
            );
        }

    }

    @Nested
    @DisplayName("Map")
    final class MapTest {

        @Test
        @DisplayName("Map basic tests")
        void map() {
            var csv = csvTemplate();
            csv.map(r -> {
                r.map(String::toUpperCase);
                return r;
            });
            assertAll("Map basics",
                    () -> assertEquals("""
                                    ""\"HELLO";WORLD;"!;"
                                    'HELLO,;""\"SECOND,""\";WORLD;!';
                                    """
                            , csv.toString()),
                    () -> assertEquals(2, csv.size()),
                    () -> assertDoesNotThrow(() -> csv.map((Object s) -> new SkCsvRow())),
                    () -> assertEquals(0, csv.stream().mapToInt(SkCsvRow::size).sum())
            );
        }

        @Test
        @DisplayName("Map null assertions")
        void mapAssertions() {
            var csv = csvTemplate();
            assertAll("Map null assertions",
                    () -> assertThrows(NullPointerException.class, () -> csv.map(null)),
                    () -> assertThrows(NullPointerException.class, () -> csv.map(r -> null))
            );
        }

    }

    @Nested
    @DisplayName("Collector")
    final class CollectorTest {

        @Test
        @DisplayName("Collector is working")
        void collector() {
            var csv = new SkCsv(
                    new SkCsvRow("x"),
                    new SkCsvRow("y"),
                    new SkCsvRow("z"),
                    new SkCsvRow("x"),
                    new SkCsvRow("z"),
                    new SkCsvRow("y")
            );
            var csv2 = csv.stream()
                    .filter(s -> !s.contains("z"))
                    .collect(SkCsv.collector());
            var csv3 = csv.stream().collect(SkCsv.collector());
            assertAll("Collector",
                    () -> assertEquals(new SkCsv(
                            new SkCsvRow("x"),
                            new SkCsvRow("y"),
                            new SkCsvRow("x"),
                            new SkCsvRow("y")
                    ), csv2),
                    () -> assertEquals(csv, csv3)
            );
        }

        @Test
        @DisplayName("Parallel collector")
        void ParallelCollector() {
            var result = IntStream.range(0, 1000).parallel()
                    .mapToObj(i -> new SkCsvRow(String.valueOf(i)))
                    .collect(SkCsv.collector());
            assertTrue(IntStream.range(0, 1000).allMatch(i -> result.get(i).equals(new SkCsvRow(String.valueOf(i)))));
        }

    }

    @Nested
    final class Equals {

        @Test
        @DisplayName("Equals basic tests")
        void equals() {
            var csv1 = csvTemplate();
            var csv2 = csvTemplate();
            assertAll("Equals basic tests",
                    () -> assertNotSame(csv1, csv2),
                    () -> assertEquals(csv1, csv2),
                    () -> {
                        csv1.set(0, new SkCsvRow());
                        assertNotEquals(csv1, csv2);
                    },
                    () -> assertNotEquals(null, csv1)
            );
        }

    }

    @Nested
    final class HashCode {

        @Test
        @DisplayName("HashCode basic tests")
        void hashcode() {
            var csv1 = csvTemplate();
            var csv2 = csvTemplate();
            var csv3 = new SkCsv();
            assertAll("Equals basic tests",
                    () -> assertEquals(csv1.hashCode(), csv2.hashCode()),
                    () -> {
                        csv1.set(0, new SkCsvRow());
                        assertNotEquals(csv1.hashCode(), csv2.hashCode());
                    },
                    () -> assertNotEquals(csv1.hashCode(), csv3.hashCode()),
                    () -> assertNotEquals(csv2.hashCode(), csv3.hashCode())
            );
        }

    }

    @Nested
    final class From {

        @Test
        @DisplayName("From text with default config")
        void fromText() {
            var text = "\"Hello\"\"\";world;!;\";\";";
            var csv1 = SkCsv.from(Collections.singleton(text));
            var csv2 = SkCsv.from(Collections.singleton(";\"Hello\";world"));
            assertAll("from text default",
                    () -> assertEquals(1, csv1.size()),
                    () -> assertEquals(1, csv2.size()),
                    () -> assertEquals(5, csv1.getFirst().size()),
                    () -> assertEquals("Hello\"", csv1.getFirst().get(0)),
                    () -> assertEquals("world", csv1.getFirst().get(1)),
                    () -> assertEquals("!", csv1.getFirst().get(2)),
                    () -> assertEquals(";", csv1.getFirst().get(3)),
                    () -> assertEquals("", csv1.getFirst().get(4)),
                    () -> assertEquals(text, csv1.getFirst().toString()),
                    () -> assertEquals(3, csv2.getFirst().size()),
                    () -> assertEquals("", csv2.getFirst().get(0)),
                    () -> assertEquals("Hello", csv2.getFirst().get(1))
            );
        }

        @Test
        @DisplayName("From text complex tests")
        void fromTextComplex() {
            assertAll("From text complex",
                    () -> assertEquals("\n", SkCsv.from(Collections.singleton("\"\"")).toString()),
                    () -> assertEquals("\"", SkCsv.from(Collections.singleton("\"\"\"\"")).getFirst().getFirst()),
                    () -> assertEquals("\"\"\"\"", SkCsv.from(Collections.singleton("\"\"\"\"")).getFirst().toString()),
                    () -> assertEquals("\"\"\"\"\n", SkCsv.from(Collections.singleton("\"\"\"\"")).toString()),
                    () -> assertEquals(" ; ; ;\n", SkCsv.from(Collections.singleton(" ; ; ;")).toString())
            );
        }

        @Test
        @DisplayName("From text with custom config")
        void fromTextWithConfig() {
            var defaultText = Collections.singleton("Hello;world;!");
            var customText = Collections.singleton("'Hello,',world,'!'''");
            var config = new SkCsvConfig(',', '\'');
            var defaultRow = SkCsv.from(defaultText, config).getFirst();
            var customRow = SkCsv.from(customText, config).getFirst();
            assertAll("from text custom config",
                    () -> assertEquals(1, defaultRow.size()),
                    () -> assertEquals(3, customRow.size()),
                    () -> assertEquals('"' + "Hello;world;!" + '"', defaultRow.toString()),
                    () -> assertEquals("Hello;world;!", defaultRow.get(0)),
                    () -> assertEquals("Hello,;world;!'", customRow.toString()),
                    () -> assertEquals("Hello,", customRow.get(0)),
                    () -> assertEquals("world", customRow.get(1)),
                    () -> assertEquals("!'", customRow.get(2))
            );
        }

        @Test
        @DisplayName("Null assertions")
        void fromTextNull() {
            var config = new SkCsvConfig(' ', '@');
            var singleton = Collections.singleton("null");
            var path = Paths.get("src", "test", "resources", "temp.csv");
            assertAll("Null assertions",
                    () -> assertThrows(NullPointerException.class, () -> SkCsv.from((Iterable<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> SkCsv.from((List<String>) null, config)),
                    () -> assertThrows(NullPointerException.class, () -> SkCsv.from(singleton, null)),
                    () -> assertThrows(NullPointerException.class, () -> SkCsv.from(path, config, null))
            );
        }

        @Test
        @DisplayName("From text parsing exceptions")
        void fromTextParsingAssertions() {
            var config = new SkCsvConfig(',', '\'');
            assertAll("From text default",
                    () -> {
                        var singleton = Collections.singleton("Hello;\"world\"\";!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello;\"world\"!\";!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("\"Hello;world!;!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("'Hello;world!;!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("'Hello'';world!;!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello';'world!;!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello'';world!;!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("''';world!;!");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello;world!;!;'");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("'Hello,',world,'!''");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello,world,!''");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton, config)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("\"");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton)
                        );
                    },
                    () -> {
                        var singleton = Collections.singleton("\"\"\"");
                        assertThrows(CsvParsingException.class, () ->
                                SkCsv.from(singleton)
                        );
                    }
            );
        }

        @Test
        @DisplayName("From a file")
        void fromFile() throws IOException {
            var pathSemicolon = Paths.get("src", "test", "resources", "template.csv");
            var pathComma = Paths.get("src", "test", "resources", "template_comma.csv");
            var pathUselessQuotes = Paths.get("src", "test", "resources", "template_useless_quotes.csv");
            var csvSemicolon = SkCsv.from(pathSemicolon, SkCsvConfig.SEMICOLON);
            var csvComma = SkCsv.from(pathComma, SkCsvConfig.COMMA);
            var csvUselessQuotes = SkCsv.from(pathUselessQuotes, StandardCharsets.UTF_8);
            assertAll("From a file",
                    () -> assertEquals(6, csvSemicolon.size()),
                    () -> assertEquals(6, csvComma.size()),
                    () -> assertEquals(Files.readString(pathSemicolon).replace("\r", ""), csvSemicolon.toString()),
                    () -> assertEquals(Files.readString(pathComma).replace("\r", ""), csvComma.configure(SkCsvConfig.COMMA).toString()),
                    () -> assertEquals(csvSemicolon, csvComma),
                    () -> assertEquals(csvSemicolon.configure(new SkCsvConfig('@', '/')), csvComma),
                    () -> assertEquals(csvSemicolon, csvUselessQuotes)
            );
        }

    }

    @Nested
    final class Export {

        @Test
        @DisplayName("Export to a file")
        void export() throws IOException {
            var path = Paths.get("src", "test", "resources", "temp.csv");
            var csv = new SkCsv(
                    new SkCsvRow("", "world", "\njump\n", "cool"),
                    new SkCsvRow("yeah", " ok", "")
            );
            csv.export(path);
            assertEquals(Files.readString(path).replace("\r", ""), csv.toString());
            Files.deleteIfExists(path);
        }

        @Test
        @DisplayName("Export assertions")
        void exportAssertions() {
            var path = Paths.get("src", "test", "resources", "temp.csv");
            var csv = new SkCsv(
                    new SkCsvRow("", "world", "\njump\n", "cool"),
                    new SkCsvRow("yeah", " ok", "")
            );
            assertAll("Export assertions",
                    () -> assertThrows(NullPointerException.class, () -> csv.export(null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class, () -> csv.export(path, (OpenOption) null)),
                    () -> assertThrows(NullPointerException.class, () -> csv.export(path, StandardCharsets.UTF_8, (OpenOption) null)),
                    () -> assertThrows(NullPointerException.class, () -> csv.export(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, null))
            );
        }

    }

}
