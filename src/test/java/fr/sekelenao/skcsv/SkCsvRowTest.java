package fr.sekelenao.skcsv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

final class SkCsvRowTest {

    private static SkCsvRow helloWorldRow() {
        return new SkCsvRow("Hello", "world", "!");
    }

    @Nested
    final class Constructors {

        @Test
        @DisplayName("Empty after default constructor")
        void byEmpty() {
            assertEquals(0, new SkCsvRow().size());
            assertEquals("", new SkCsvRow().toString());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 100, 1000, 10000})
        @DisplayName("Constructor by size")
        void bySize(int size) {
            var row = new SkCsvRow(size);
            assertEquals(size, row.size());
            assertEquals(";".repeat(Math.max(size - 1, 0)), row.toString());
        }

        @Test
        @DisplayName("Constructor by size assertions")
        void bySizeAssertions() {
            assertThrows(IllegalArgumentException.class, () -> new SkCsvRow(-1));
        }

        @Test
        @DisplayName("VarArgs constructor")
        void byVarArgs() {
            var array = new String[]{"", "Hello", "world", "!", "", ""};
            var row = new SkCsvRow(array);
            assertAll("Simple operations",
                    () -> assertEquals(4, new SkCsvRow("", "Hello", "world", "!").size()),
                    () -> assertEquals(";Hello;world;!;;", row.toString()),
                    () -> assertEquals(0, new SkCsvRow(new String[]{}).size()),
                    () -> assertEquals("", new SkCsvRow(new String[]{}).toString()),
                    () -> assertEquals(1, new SkCsvRow("").size())
            );
        }

        @Test
        @DisplayName("VarArgs constructor null assertions")
        void byVarArgsAssertions() {
            assertAll("VarArgs constructor null assertions",
                    () -> assertThrows(NullPointerException.class, () -> new SkCsvRow((String) null)),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsvRow(new String[]{null})),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsvRow(new String[]{"wrong", null}))
            );
        }

        @Test
        @DisplayName("Iterable constructor")
        void byIterable() {
            var helloWorldList = new ArrayList<>(List.of("", "Hello", "world", "!"));
            var row = new SkCsvRow(helloWorldList);
            assertAll("Simple operations",
                    () -> assertEquals(4, row.size()),
                    () -> assertEquals(";Hello;world;!", row.toString()),
                    () -> assertEquals(0, new SkCsvRow(Collections.emptyList()).size()),
                    () -> assertEquals(1, new SkCsvRow(List.of("")).size()),
                    () -> assertEquals("", new SkCsvRow(List.of("")).toString()),
                    () -> assertEquals(";", new SkCsvRow(List.of("", "")).toString())
            );
            helloWorldList.add("test");
            assertEquals(";Hello;world;!", row.toString());
        }

        @Test
        @DisplayName("Iterable constructor null assertions")
        void byIterableAssertions() {
            var wrongList = new ArrayList<String>();
            wrongList.add("wrong");
            wrongList.add(null);
            assertAll("NullPointer assertions",
                    () -> assertThrows(NullPointerException.class, () -> new SkCsvRow((List<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> new SkCsvRow(wrongList))
            );
        }

    }

    @Nested
    @DisplayName("Empty and blank tests")
    final class IsEmptyOrBlank {

        @Test
        @DisplayName("Is empty test")
        void isEmptyOrBlank() {
            var row = new SkCsvRow();
            assertAll("Empty tests",
                    () -> assertTrue(row.isEmpty()),
                    () -> assertTrue(row.isBlank()),
                    () -> {
                        row.addAll("     ", "\n", "\t");
                        assertTrue(row.isBlank());
                        assertFalse(row.isEmpty());
                    }
            );
        }

    }

    @Nested
    @DisplayName("Inserts and add")
    final class InsertAndAdd {


        @Test
        @DisplayName("add")
        void add() {
            var row = new SkCsvRow();
            row.add("Hello");
            row.add("world");
            row.add("!");
            assertAll("add",
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("Hello;world;!", row.toString()),
                    () -> assertEquals("Hello", row.get(0)),
                    () -> assertEquals("world", row.get(1)),
                    () -> assertEquals("!", row.get(2))
            );
        }

        @Test
        @DisplayName("add null assertions")
        void addNullAssertions() {
            var emptyRow = new SkCsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.add(null));
        }

        @Test
        @DisplayName("Insert")
        void insert() {
            var row = new SkCsvRow();
            row.insert(0, "Hello");
            row.insert(0, "world");
            row.insert(1, "!");
            assertAll("Insert",
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("world;!;Hello", row.toString()),
                    () -> assertEquals("world", row.get(0)),
                    () -> assertEquals("!", row.get(1)),
                    () -> assertEquals("Hello", row.get(2))
            );
        }

        @Test
        @DisplayName("Insert null assertions")
        void insertNullAssertions() {
            var emptyRow = new SkCsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.insert(0, null));
        }

        @Test
        @DisplayName("Insert indices assertions")
        void insertIndicesAssertions() {
            var row = new SkCsvRow();
            assertAll("Insert indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.insert(-1, "out")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.insert(1, "out"))
            );
            row.insertAll(row.size(), List.of("Hello", "world", "!"));
            assertAll("Insert indices assertions 2",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.insert(-1, "out")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.insert(4, "out"))
            );
        }

        @Test
        @DisplayName("Insert all at end")
        void insertAllAtEnd() {
            var row = helloWorldRow();
            row.insertAll(row.size(), List.of("(", "and Meta-verse", ")"));
            var row2 = row.copy();
            row2.insertAll(row2.size(), Collections.emptyList());
            var row3 = row.copy();
            row3.insertAll(row3.size(), "(", "and Meta-verse", ")");
            assertAll("Insert all at end",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;)", row.toString()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;)", row2.toString()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;);(;and Meta-verse;)", row3.toString())
            );
        }

        @Test
        @DisplayName("Insert all at start")
        void insertAllAtStart() {
            var row = helloWorldRow();
            row.insertAll(0, List.of("(", "and Meta-verse", ")"));
            var row2 = helloWorldRow();
            row2.insertAll(0, "(", "and Meta-verse", ")");
            assertAll("Insert all at start",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("(;and Meta-verse;);Hello;world;!", row.toString()),
                    () -> assertEquals(6, row2.size()),
                    () -> assertEquals("(;and Meta-verse;);Hello;world;!", row2.toString())
            );
        }

        @Test
        @DisplayName("Insert all")
        void insertAll() {
            var row = helloWorldRow();
            row.insertAll(2, List.of("(", "and Meta-verse", ")"));
            var row2 = helloWorldRow();
            row2.insertAll(2, "(", "and Meta-verse", ")");
            assertAll("Insert all",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("Hello;world;(;and Meta-verse;);!", row.toString()),
                    () -> assertEquals(6, row2.size()),
                    () -> assertEquals("Hello;world;(;and Meta-verse;);!", row2.toString())
            );
        }

        @Test
        @DisplayName("Insert all null assertions")
        void insertAllNullAssertions() {
            var emptyRow = new SkCsvRow();
            var lst = Collections.singleton((String) null);
            assertAll("Insert all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.insertAll(0, (Iterable<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.insertAll(0, (String[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.insertAll(0, lst))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 4, 5, 100})
        @DisplayName("Insert all position assertions")
        void insertAllPositionAssertions(int index) {
            var row = helloWorldRow();
            var lst = new ArrayList<String>();
            assertAll("Insert all position assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.insertAll(index, lst)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.insertAll(index, ""))
            );
        }

        @Test
        @DisplayName("Add all")
        void addAll() {
            var row = helloWorldRow();
            row.addAll(List.of("(", "and Meta-verse", ")"));
            var row2 = row.copy();
            row2.addAll(Collections.emptyList());
            var row3 = row.copy();
            row3.addAll("(", "and Meta-verse", ")");
            assertAll("addAll",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;)", row.toString()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;)", row2.toString()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;);(;and Meta-verse;)", row3.toString())
            );
        }

        @Test
        @DisplayName("Add all null assertions")
        void addAllNullAssertions() {
            var emptyRow = new SkCsvRow();
            var lst = Collections.singleton((String) null);
            assertAll("Add all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll((Iterable<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll((String[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll(lst))
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
            var row = SkCsv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Set all indices",
                    () -> assertEquals(String.valueOf(index), row.set(index, "replaced")),
                    () -> assertEquals("replaced", row.get(index))
            );
        }

        @Test
        @DisplayName("Set indices assertions")
        void setAllIndicesAssertions() {
            var emptyRow = new SkCsvRow();
            var row = SkCsv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.set(-1, "wrong")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.set(8, "wrong")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyRow.set(0, "wrong"))
            );
        }

        @Test
        @DisplayName("Set null assertions")
        void setNullAssertions() {
            var row = new SkCsvRow("One");
            assertThrows(NullPointerException.class, () -> row.set(0, null));
        }

    }

    @Nested
    final class Fill {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 8, 100, 1000})
        @DisplayName("Fill basic test")
        void fill(int size) {
            var row = new SkCsvRow();
            row.fill(size);
            assertAll("Fill",
                    () -> assertEquals(size, row.size()),
                    () -> assertTrue(row.stream().allMatch(String::isEmpty)),
                    () -> {
                        row.fill(size);
                        assertEquals(size * 2, row.size());
                    }
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -2, -100})
        @DisplayName("Fill assertions")
        void fillAssertions(int size) {
            var emptyRow = new SkCsvRow();
            assertAll("Fill assertions",
                    () -> assertThrows(IllegalArgumentException.class, () -> emptyRow.fill(size))
            );
        }

    }

    @Nested
    final class Get {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Get all indices working")
        void getAllIndices(int index) {
            var row = SkCsv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertEquals(String.valueOf(index), row.get(index));
        }

        @Test
        @DisplayName("Get indices assertions")
        void getAllIndicesAssertions() {
            var emptyRow = new SkCsvRow();
            var row = SkCsv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.get(8)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyRow.get(0))
            );
        }

        @Test
        @DisplayName("Get first and last")
        void getFirstAndLast() {
            var row = SkCsv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Get first and last",
                    () -> assertEquals("0", row.getFirst()),
                    () -> assertEquals("7", row.getLast())
            );
        }

        @Test
        @DisplayName("Get indices assertions")
        void getFirstAndLastAssertions() {
            var emptyRow = new SkCsvRow();
            assertAll("Get first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, emptyRow::getFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyRow::getLast)
            );
        }

    }

    @Nested
    final class Remove {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2})
        @DisplayName("Remove basic tests")
        void remove(int index) {
            var row = new SkCsvRow("0", "1", "2");
            assertAll("Remove basic tests",
                    () -> assertEquals(String.valueOf(index), row.remove(index)),
                    () -> assertEquals(IntStream.range(0, row.size() + 1)
                                    .filter(i -> i != index)
                                    .mapToObj(String::valueOf)
                                    .collect(Collectors.joining(";"))
                            , row.toString())
            );
        }

        @Test
        @DisplayName("Remove all values")
        void removeAll() {
            var row = helloWorldRow();
            var initialSize = row.size();
            for (int i = 0; i < initialSize; i++) row.remove(0);
            assertAll("Remove all",
                    () -> assertTrue(row.isEmpty()),
                    () -> assertEquals(0, row.size()),
                    () -> assertEquals("", row.toString())
            );
        }

        @Test
        @DisplayName("Remove assertions")
        void removeAssertions() {
            var row = helloWorldRow();
            row.remove(0);
            assertAll("Remove assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.remove(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.remove(3)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.remove(2))
            );
        }

        @Test
        @DisplayName("Remove first and last")
        void removeFirstAndLast() {
            int count = 0;
            var row1 = helloWorldRow();
            while (!row1.isEmpty()) {
                assertEquals(row1.getFirst(), row1.removeFirst());
                count++;
                assertEquals(3 - count, row1.size());
            }
            assertEquals(3, count);
            count = 0;
            var row2 = helloWorldRow();
            while (!row2.isEmpty()) {
                assertEquals(row2.getLast(), row2.removeLast());
                count++;
                assertEquals(3 - count, row2.size());
            }
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Remove first and last assertions")
        void removeFirstAndLastAssertions() {
            var row = helloWorldRow();
            var emptyRow = new SkCsvRow();
            row.removeFirst();
            row.removeLast();
            row.removeFirst();
            assertAll("Remove first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, row::removeFirst),
                    () -> assertThrows(NoSuchElementException.class, row::removeLast),
                    () -> assertThrows(NoSuchElementException.class, emptyRow::removeFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyRow::removeLast)
            );
        }

        @Test
        @DisplayName("RemoveIf basic tests")
        void removeIf() {
            var row = SkCsv.from(Collections.singleton("1;2;3;4;5;6;7;8;9;10")).getFirst();
            int expectedSize = row.size() / 2;
            assertAll("RemoveIf basic tests",
                    () -> assertTrue(row.removeIf(s -> (Integer.parseInt(s) & 1) == 0)),
                    () -> assertTrue(row.stream().allMatch(s -> (Integer.parseInt(s) & 1) == 1)),
                    () -> assertEquals(expectedSize, row.size()),
                    () -> assertFalse(() -> row.removeIf((Object value) -> false)),
                    () -> assertEquals(expectedSize, row.size()),
                    () -> {
                        row.removeIf(value -> true);
                        assertEquals(0, row.size());
                    }
            );
        }

        @Test
        @DisplayName("RemoveIf null assertions")
        void removeIfNullAssertions() {
            var emptyRow = new SkCsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.removeIf(null));
        }

    }

    @Nested
    final class Contains {

        private record Citation(String value) {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof String str && str.equals(value);
            }
        }

        private static final SkCsvRow row = SkCsv.from(Collections.singleton("1;3;5;7;9")).getFirst();

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9})
        @DisplayName("Contains basic tests")
        void contains(int value) {
            assertAll("Contains basic tests",
                    () -> assertEquals((value & 1) == 1, row.contains(String.valueOf(value))),
                    () -> assertFalse(row.contains(value)),
                    () -> assertFalse(row.contains(null))
            );
        }

        @Test
        @DisplayName("Contains with custom equals")
        void containsWithCustomEquals() {
            var row = new SkCsvRow("Hey", "a quote is a quote");
            assertAll("Contains with custom equals",
                    () -> assertTrue(row.contains(new Citation("a quote is a quote"))),
                    () -> assertTrue(row.contains("a quote is a quote")),
                    () -> assertFalse(row.contains("Yes")),
                    () -> assertFalse(row.contains(new Citation("Yes")))
            );
        }

    }

    @Nested
    final class ForEach {

        @Test
        @DisplayName("ForEach basic test")
        void forEach() {
            var row = helloWorldRow();
            var lst = new ArrayList<String>();
            row.forEach((Object value) -> lst.add((String) value));
            assertEquals(String.join("", row), String.join("", lst));
        }

        @Test
        @DisplayName("ForEach null assertions")
        void forEachNullAssertions() {
            var emptyRow = new SkCsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.forEach(null));
        }

    }

    @Nested
    @DisplayName("Iterable")
    final class IterableTest {

        @Test
        @DisplayName("For each loop is working")
        void iterableFor() {
            var lst = new ArrayList<String>();
            var row = helloWorldRow();
            for (String string : row) lst.add(string);
            assertAll("For each working",
                    () -> assertEquals(row.size(), lst.size()),
                    () -> assertEquals(String.join("", row), String.join("", lst))
            );
        }

        @Test
        @DisplayName("Iterator is working")
        void iterator() {
            var row = helloWorldRow();
            var it = row.iterator();
            var emptyIt = new SkCsvRow().iterator();
            assertAll("Iterator is working",
                    () -> assertTrue(it.hasNext()),
                    () -> assertTrue(it.hasNext()),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            assertEquals(row.get(i), it.next());
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
            var row = helloWorldRow();
            var it = row.iterator();
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
                    () -> assertEquals(2, row.size()),
                    () -> assertEquals("world;!", row.toString())
            );
        }

        @Test
        @DisplayName("Iterator for each remaining is working")
        void iterableForEachRemaining() {
            var lst = new ArrayList<String>();
            var row = helloWorldRow();
            var it = row.iterator();
            it.next();
            it.forEachRemaining(lst::add);
            assertAll("For each remaining working",
                    () -> assertEquals(2, lst.size()),
                    () -> assertEquals("world!", String.join("", lst))
            );
        }

        @Test
        @DisplayName("Iterator concurrent modifications")
        void iteratorConcurrentModifications() {
            var row = helloWorldRow();
            var it = row.iterator();
            row.remove(0);
            assertThrows(ConcurrentModificationException.class, it::next);
        }

    }

    @Nested
    @DisplayName("Stream")
    final class StreamTest {

        @Test
        @DisplayName("Stream basic tests")
        void stream() {
            var lst = new ArrayList<String>();
            var emptyRow = new SkCsvRow();
            var row = helloWorldRow();
            row.stream().filter(s -> s.length() > 1).forEach(lst::add);
            row.remove(row.size() - 1);
            assertAll("Stream basics",
                    () -> assertEquals(String.join("", row), String.join("", lst)),
                    () -> assertEquals(2, lst.size()),
                    () -> assertDoesNotThrow(emptyRow::stream)
            );
        }

    }

    @Nested
    @DisplayName("Map")
    final class MapTest {

        @Test
        @DisplayName("Map basic tests")
        void map() {
            var row = helloWorldRow();
            row.map(String::toUpperCase);
            assertAll("Map basics",
                    () -> assertEquals("HELLO;WORLD;!", row.toString()),
                    () -> assertEquals(3, row.size()),
                    () -> assertDoesNotThrow(() -> row.map((Object s) -> "")),
                    () -> assertEquals(0, row.stream().mapToInt(String::length).sum())
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

    }

    @Nested
    @DisplayName("Collector")
    final class CollectorTest {

        @Test
        @DisplayName("Collector is working")
        void collector() {
            var row = new SkCsvRow("x", "y", "z", "x", "z", "y");
            var row2 = row.stream()
                    .filter(s -> !s.equals("z"))
                    .collect(SkCsvRow.collector());
            var row3 = row.stream().collect(SkCsvRow.collector());
            assertAll("Collector",
                    () -> assertEquals(new SkCsvRow("x", "y", "x", "y"), row2),
                    () -> assertEquals(row, row3)
            );
        }

        @Test
        @DisplayName("Parallel collector")
        void ParallelCollector() {
            var result = IntStream.range(0, 1000).parallel()
                    .mapToObj(String::valueOf)
                    .collect(SkCsvRow.collector());
            assertTrue(IntStream.range(0, 1000).allMatch(i -> result.get(i).equals(String.valueOf(i))));
        }

    }

    @Nested
    final class Copy {

        @Test
        @DisplayName("Copy basic tests")
        void copy() {
            var row = helloWorldRow();
            var copy = row.copy();
            assertAll("Copy basic tests",
                    () -> assertEquals(row, copy),
                    () -> {
                        row.remove(row.size() - 1);
                        assertNotEquals(row, copy);
                    },
                    () -> assertEquals(2, row.size()),
                    () -> assertEquals(3, copy.size())
            );
        }

    }

    @Nested
    final class Equals {

        @Test
        @DisplayName("Equals basic tests")
        void equals() {
            var row = helloWorldRow();
            var row2 = helloWorldRow();
            var row3 = helloWorldRow();
            row3.set(0, "false");
            assertAll("Equals basic tests",
                    () -> assertNotSame(row, row2),
                    () -> assertEquals(row, row2),
                    () -> assertNotEquals(row, row3),
                    () -> {
                        row.set(0, "hello");
                        assertNotEquals(row, row2);
                    }
            );
        }

    }

    @Nested
    final class HashCode {

        @Test
        @DisplayName("HashCode basic tests")
        void hashcode() {
            var row = helloWorldRow();
            var row2 = helloWorldRow();
            assertAll("Equals basic tests",
                    () -> assertEquals(row.hashCode(), row2.hashCode()),
                    () -> {
                        row.set(0, "hello");
                        assertNotEquals(row.hashCode(), row2.hashCode());
                    }
            );
        }

    }

    @Nested
    class ToString {

        @Test
        @DisplayName("Escape chars toString")
        void toStringEscapeChars() {
            assertAll("Escape chars assertions",
                    () -> assertEquals("\\n;", new SkCsvRow("\\n", "").toString()),
                    () -> assertEquals("\t;tab", new SkCsvRow("\t", "tab").toString())
            );
        }

        @Test
        @DisplayName("toString basic tests")
        void toStringBasicTests() {
            var row = new SkCsvRow("Hello\"", "world", "!", ";", "");
            var rowAsString = row.toString();
            assertAll("toString basic tests",
                    () -> assertEquals("\"Hello\"\"\";world;!;\";\";", rowAsString),
                    () -> assertEquals(row, SkCsv.from(Collections.singleton("\"Hello\"\"\";world;!;\";\";")).getFirst()),
                    () -> assertEquals(row.toString(), rowAsString),
                    () -> assertEquals("\"\"\"\"", SkCsv.from(Collections.singleton("\"\"\"\"")).getFirst().toString())
            );
        }

        @Test
        @DisplayName("toString with custom config")
        void toStringWithConfig() {
            var row = new SkCsvRow("Hello\"", "world", "!", ";", "");
            var otherRow = new SkCsvRow("Hello'", "world", "!", ",", "'");
            assertAll("toString custom config",
                    () -> assertEquals("\"Hello\"\"\";world;!;\";\";", row.toString()),
                    () -> assertEquals("\"Hello\"\"\",world,!,;,", row.toString(SkCsvConfig.COMMA)),
                    () -> assertEquals("Hello\",world,!,;,", row.toString(new SkCsvConfig(',', '\''))),
                    () -> assertEquals("'Hello''',world,!,',',''''", otherRow.toString(new SkCsvConfig(',', '\'')))
            );
        }

        @Test
        @DisplayName("toString with newlines")
        void toStringWithNewlines() {
            var row = new SkCsvRow("hey", "cool\ncool");
            assertEquals("""
                    hey;"cool
                    cool\"""", row.toString());
        }

    }
}