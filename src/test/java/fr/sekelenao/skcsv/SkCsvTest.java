package fr.sekelenao.skcsv;

import fr.sekelenao.skcsv.csv.CsvConfiguration;
import fr.sekelenao.skcsv.csv.SkCsvRow;
import fr.sekelenao.skcsv.csv.SkCsv;
import fr.sekelenao.skcsv.exception.InvalidCsvValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
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

    private static SkCsv csvTemplate(int lineNumber){
        return new SkCsv(
                IntStream.range(0, lineNumber)
                        .mapToObj(i -> new SkCsvRow(String.valueOf(i)))
                        .collect(Collectors.toList())
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
                    () -> assertEquals(CsvConfiguration.SEMICOLON, csv.configuration()),
                    () -> {
                        csv.configure(new CsvConfiguration(',', '\''));
                        assertEquals("""
                                "Hello,world,!;
                                '''Hello,','"second,"',world,'!''',
                                """, csv.toString());
                    },
                    () -> assertEquals(new CsvConfiguration(',', '\''), csv.configuration())
            );
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Configuration assertions")
        void configAssertions(char wrongChar) {
            assertAll("Configuration assertions",
                    () -> assertThrows(InvalidCsvValueException.class, () -> new CsvConfiguration(wrongChar, '"')),
                    () -> assertThrows(InvalidCsvValueException.class, () -> new CsvConfiguration(';', wrongChar))
            );
        }
    }

    @Nested
    @DisplayName("Inserts and append")
    final class InsertAndAppend {

        @Test
        @DisplayName("Append")
        void append() {
            var csv = new SkCsv();
            csv.append(new SkCsvRow("Hello", "world", "!"));
            assertAll("Append",
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("Hello;world;!\n", csv.toString()),
                    () -> assertEquals(new SkCsvRow("Hello", "world", "!"), csv.get(0)),
                    () -> {
                        csv.append(new SkCsvRow("I", "love", "Java", ""));
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
        @DisplayName("Append null assertions")
        void appendNullAssertions() {
            var emptyCsv = new SkCsv();
            assertThrows(NullPointerException.class, () -> emptyCsv.append(null));
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
            csv.insertAll(csv.size(), new SkCsvRow("(", "and Meta-verse", ")"));
            csv.insertAll(csv.size(), List.of(new SkCsvRow("yes")));
            assertAll("Insert all at end",
                    () -> assertEquals(4, csv.size()),
                    () -> assertEquals("""
                                       ""\"Hello";world;"!;"
                                       'Hello,;""\"second,""\";world;!';
                                       (;and Meta-verse;)
                                       yes
                                       """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all at start")
        void insertAllAtStart() {
            var csv = csvTemplate();
            csv.insertAll(0, new SkCsvRow("(", "and Meta-verse", ")"));
            csv.insertAll(0, List.of(new SkCsvRow()));
            assertAll("Insert all at start",
                    () -> assertEquals(4, csv.size()),
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
            csv.insertAll(1, List.of(new SkCsvRow("(", "and Meta-verse", ")")));
            assertAll("Insert all",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals("""
                                       ""\"Hello";world;"!;"
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
            var lst = Collections.singleton((SkCsvRow) null);
            assertAll("Insert all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, (Iterable<SkCsvRow>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, (SkCsvRow[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, lst))
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
                    () -> assertEquals(new SkCsvRow(String.valueOf(index)), csv.set(index, new SkCsvRow("replaced"))),
                    () -> assertEquals("replaced", csv.get(index).getFirst())
            );
        }

        @Test
        @DisplayName("Set indices assertions")
        void setAllIndicesAssertions() {
            var csv = csvTemplate(8);
            var emptyCsv = new SkCsv();
            var emptyRow =  new SkCsvRow();
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
            assertAll("Remove basic tests",
                    () -> assertEquals(new SkCsvRow(String.valueOf(index)), csv.remove(index)),
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
                    () -> assertEquals(new SkCsvRow("'Hello,", "\"second,\"", "world", "!'", ""), lst.getFirst())
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
            csv.append(new SkCsvRow());
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
                    () -> assertEquals("HELLO;WORLD;!", csv.toString()),
                    () -> assertEquals(3, csv.size()),
                    () -> assertDoesNotThrow(() -> csv.map((Object s) -> "")),
                    () -> assertEquals(0, csv.stream().mapToInt(String::length).sum())
            );
        }

        @Test
        @DisplayName("Map null assertions")
        void mapAssertions() {
            var helloWorldRow = helloWorldRow();
            assertAll("Map null assertions",
                    () -> assertThrows(NullPointerException.class, () -> helloWorldRow.map(null)),
                    () -> assertThrows(NullPointerException.class, () -> helloWorldRow.map(s -> null))
            );
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Map invalid values assertions")
        void mapValueAssertions(char wrongChar) {
            var row = helloWorldRow();
            assertThrows(InvalidCsvValueException.class, () -> row.map(s -> "" + wrongChar));
        }

    }

}
