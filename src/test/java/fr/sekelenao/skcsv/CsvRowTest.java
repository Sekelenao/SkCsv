package fr.sekelenao.skcsv;

import fr.sekelenao.skcsv.csv.CsvRow;
import fr.sekelenao.skcsv.csv.RowConfiguration;
import fr.sekelenao.skcsv.exception.CsvParsingException;
import fr.sekelenao.skcsv.exception.InvalidCsvValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

final class CsvRowTest {

    private static CsvRow helloWorldRow() {
        return new CsvRow("Hello", "world", "!");
    }

    @Nested
    final class Constructors {

        static Stream<Arguments> wrongArraysProvider() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of((Object) new String[]{null}),
                    Arguments.of((Object) new String[]{"wrong", null})
            );
        }

        static Stream<String> escapeStringsProvider() {
            return Stream.of("Hello\n", "w\0rld", "I\r", "love\b", "\fJava !");
        }

        @Test
        @DisplayName("Empty after default constructor")
        void byEmpty() {
            assertEquals(0, new CsvRow().size());
            assertEquals("", new CsvRow().toString());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 100, 1000, 10000})
        @DisplayName("Constructor by size")
        void bySize(int size) {
            var row = new CsvRow(size);
            assertEquals(size, row.size());
            assertEquals(";".repeat(Math.max(size - 1, 0)), row.toString());
        }

        @Test
        @DisplayName("Constructor by size assertions")
        void bySizeAssertions() {
            assertThrows(IllegalArgumentException.class, () -> new CsvRow(-1));
        }

        @Test
        @DisplayName("VarArgs constructor")
        void byVarArgs() {
            var array = new String[]{"", "Hello", "world", "!", "", ""};
            var row = new CsvRow(array);
            assertAll("Simple operations",
                    () -> assertEquals(4, new CsvRow("", "Hello", "world", "!").size()),
                    () -> assertEquals(";Hello;world;!;;", row.toString()),
                    () -> assertEquals(0, new CsvRow(new String[]{}).size()),
                    () -> assertEquals("", new CsvRow(new String[]{}).toString()),
                    () -> assertEquals(1, new CsvRow("").size())
            );
            array[0] = "changes";
            assertEquals(";Hello;world;!;;", row.toString());
        }

        @ParameterizedTest
        @MethodSource("wrongArraysProvider")
        @DisplayName("VarArgs constructor null assertions")
        void byVarArgsAssertions(String[] wrongArray) {
            assertThrows(NullPointerException.class, () -> new CsvRow(wrongArray));
        }

        @ParameterizedTest
        @MethodSource("escapeStringsProvider")
        @DisplayName("VarArgs constructor values assertions")
        void byVarArgsValuesAssertions(String wrongString) {
            assertThrows(InvalidCsvValueException.class, () -> new CsvRow(wrongString));
        }

        @Test
        @DisplayName("Iterable constructor")
        void byIterable() {
            var helloWorldList = new ArrayList<>(List.of("", "Hello", "world", "!"));
            var row = new CsvRow(helloWorldList);
            assertAll("Simple operations",
                    () -> assertEquals(4, row.size()),
                    () -> assertEquals(";Hello;world;!", row.toString()),
                    () -> assertEquals(0, new CsvRow(Collections.emptyList()).size()),
                    () -> assertEquals(1, new CsvRow(List.of("")).size()),
                    () -> assertEquals("", new CsvRow(List.of("")).toString()),
                    () -> assertEquals(";", new CsvRow(List.of("", "")).toString())
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
                    () -> assertThrows(NullPointerException.class, () -> new CsvRow((List<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> new CsvRow(wrongList))
            );
        }

        @ParameterizedTest
        @MethodSource("escapeStringsProvider")
        @DisplayName("Iterable constructor values assertions")
        void byIterableAssertions(String wrongString) {
            var lst = List.of(wrongString);
            assertThrows(InvalidCsvValueException.class, () -> new CsvRow(lst));
        }

    }

    @Nested
    final class Configuration {

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

        static Stream<RowConfiguration> configurationsProvider() {
            return Stream.of(
                    new RowConfiguration(',', '\''),
                    new RowConfiguration(',', '"'),
                    new RowConfiguration('@', '_'),
                    new RowConfiguration(' ', '"'),
                    new RowConfiguration('\t', '"')
            );
        }

        @Test
        @DisplayName("Simples configuration tests")
        void configureSimple() {
            var row = new CsvRow("Hello;", "world !");
            assertEquals("\"Hello;\";world !", row.toString());
            row.configure(RowConfiguration.SEMICOLON);
            assertEquals("\"Hello;\";world !", row.toString());
            row.configure(new RowConfiguration(',', '"'));
            assertEquals("Hello;,world !", row.toString());
        }

        @ParameterizedTest
        @MethodSource("configurationsProvider")
        @DisplayName("Complex configuration tests")
        void configureComplex(RowConfiguration config) {
            var quote = config.quoteAsString();
            var delimiter = config.delimiterAsString();
            var row = new CsvRow("Hello;" + delimiter, "world!" + quote).configure(config);
            var expected = quote + "Hello;" + delimiter + quote + delimiter + quote + "world!" + quote.repeat(3);
            assertEquals(expected, row.toString());
        }

        @Test
        @DisplayName("Configuration null assertions")
        void configureAssertions() {
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.configure(null));
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Configuration args assertions")
        void configureAssertions(char wrongChar) {
            assertAll("escape chars",
                    () -> assertThrows(InvalidCsvValueException.class, () -> new RowConfiguration(wrongChar, ',')),
                    () -> assertThrows(InvalidCsvValueException.class, () -> new RowConfiguration(',', wrongChar)),
                    () -> assertThrows(InvalidCsvValueException.class, () -> new RowConfiguration(wrongChar, wrongChar)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new RowConfiguration(',', ','))
            );
        }

        @Test
        @DisplayName("Configuration getting is working")
        void configuration() {
            var row = new CsvRow();
            var byTextRow = CsvRow.from("Hello,world,!", RowConfiguration.COMMA);
            assertAll("Configuration",
                    () -> assertEquals(RowConfiguration.SEMICOLON, row.configuration()),
                    () -> assertEquals(RowConfiguration.SEMICOLON, byTextRow.configuration()),
                    () -> assertEquals(RowConfiguration.COMMA, row.configure(RowConfiguration.COMMA).configuration())
            );
        }

    }

    @Nested
    final class From {

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

        @Test
        @DisplayName("From text with default config")
        void fromText() {
            var text = "\"Hello\"\"\";world;!;\";\";";
            var row = CsvRow.from(text);
            var row2 = CsvRow.from(";\"Hello\";world");
            assertAll("from text default",
                    () -> assertEquals(5, row.size()),
                    () -> assertEquals("Hello\"", row.get(0)),
                    () -> assertEquals("world", row.get(1)),
                    () -> assertEquals("!", row.get(2)),
                    () -> assertEquals(";", row.get(3)),
                    () -> assertEquals("", row.get(4)),
                    () -> assertEquals(text, row.toString()),
                    () -> assertEquals(3, row2.size()),
                    () -> assertEquals("", row2.get(0)),
                    () -> assertEquals("Hello", row2.get(1))
            );
        }

        @Test
        @DisplayName("From text with custom config")
        void fromTextWithConfig() {
            var defaultText = "Hello;world;!";
            var customText = "'Hello,',world,'!'''";
            var config = new RowConfiguration(',', '\'');
            var defaultRow = CsvRow.from(defaultText, config);
            var customRow = CsvRow.from(customText, config).configure(config);
            assertAll("from text custom config",
                    () -> assertEquals(1, defaultRow.size()),
                    () -> assertEquals(3, customRow.size()),
                    () -> assertEquals('"' + defaultText + '"', defaultRow.toString()),
                    () -> assertEquals(defaultText, defaultRow.get(0)),
                    () -> assertEquals(defaultText, defaultRow.configure(config).toString()),
                    () -> assertEquals(defaultText, defaultRow.configure(config).get(0)),
                    () -> assertEquals(customText, customRow.toString()),
                    () -> assertEquals("Hello,", customRow.get(0)),
                    () -> assertEquals("world", customRow.get(1)),
                    () -> assertEquals("!'", customRow.get(2))
            );
        }

        @Test
        @DisplayName("Null assertions")
        void fromTextNull() {
            assertAll("Null assertions",
                    () -> assertThrows(NullPointerException.class, () -> CsvRow.from(null)),
                    () -> {
                        var config = new RowConfiguration(' ', '@');
                        assertThrows(NullPointerException.class, () -> CsvRow.from(null, config));
                    },
                    () -> assertThrows(NullPointerException.class, () -> CsvRow.from("null", null))
            );
        }

        @Test
        @DisplayName("From text parsing exceptions")
        void fromTextParsingAssertions() {
            var config = new RowConfiguration(',', '\'');
            assertAll("From text default",
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("Hello;\"world\"\";!")),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("Hello;\"world\"!\";!")),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("\"Hello;world!;!")),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("'Hello;world!;!", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("'Hello'';world!;!", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("Hello';'world!;!", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("Hello'';world!;!", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("''';world!;!", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("Hello;world!;!;'", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("'Hello,',world,'!''", config)),
                    () -> assertThrows(CsvParsingException.class, () -> CsvRow.from("Hello,world,!''", config))
            );
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("From text invalid value exception")
        void fromTextValuesAssertions(char wrongChar) {
            assertThrows(InvalidCsvValueException.class, () -> CsvRow.from("Hello;wo" + wrongChar + "rld;!"));
        }

    }

    @Nested
    final class Add {

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

        @Test
        @DisplayName("Add first")
        void addFirst() {
            var row = new CsvRow();
            row.addFirst("Hello");
            row.addFirst("world");
            row.addFirst("!");
            assertAll("Add first",
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("!;world;Hello", row.toString()),
                    () -> assertEquals("!", row.get(0)),
                    () -> assertEquals("world", row.get(1)),
                    () -> assertEquals("Hello", row.get(2))
            );
        }

        @Test
        @DisplayName("Add first null assertions")
        void addFirstAssertions() {
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.addFirst(null));
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Add first wrong values assertions")
        void addFirstWrongValues(char wrongChar) {
            var emptyRow = new CsvRow();
            assertThrows(InvalidCsvValueException.class, () -> emptyRow.addFirst("He" + wrongChar + "llo"));
        }

        @Test
        @DisplayName("Add last")
        void addLast() {
            var row = new CsvRow();
            row.addLast("Hello");
            row.addLast("world");
            row.addLast("!");
            assertAll("Add last",
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("Hello;world;!", row.toString()),
                    () -> assertEquals("Hello", row.get(0)),
                    () -> assertEquals("world", row.get(1)),
                    () -> assertEquals("!", row.get(2))
            );
        }

        @Test
        @DisplayName("Add last null assertions")
        void addLastAssertions() {
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.addLast(null));
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Add last wrong values assertions")
        void addLastWrongValues(char wrongChar) {
            var emptyRow = new CsvRow();
            assertThrows(InvalidCsvValueException.class, () -> emptyRow.addLast("He" + wrongChar + "llo"));
        }

        @Test
        @DisplayName("Add")
        void add() {
            var row = new CsvRow();
            row.add(0, "Hello");
            row.add(0, "world");
            row.add(1, "!");
            assertAll("Add",
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("world;!;Hello", row.toString()),
                    () -> assertEquals("world", row.get(0)),
                    () -> assertEquals("!", row.get(1)),
                    () -> assertEquals("Hello", row.get(2))
            );
        }

        @Test
        @DisplayName("Add null assertions")
        void addNullAssertions() {
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.add(0, null));
        }

        @Test
        @DisplayName("Add indices assertions")
        void addIndicesAssertions() {
            var row = new CsvRow();
            assertAll("Add indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.add(-1, "out")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.add(1, "out"))
            );
            row.addAll(row.size(), List.of("Hello", "world", "!"));
            assertAll("Add indices assertions 2",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.add(-1, "out")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.add(4, "out"))
            );
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Add wrong values assertions")
        void addWrongValues(char wrongChar) {
            var emptyRow = new CsvRow();
            assertThrows(InvalidCsvValueException.class, () -> emptyRow.add(0, "He" + wrongChar + "llo"));
        }

        @Test
        @DisplayName("Add all at end")
        void addAllAtEnd() {
            var row = helloWorldRow();
            row.addAll(row.size(), List.of("(", "and Meta-verse", ")"));
            var row2 = row.copy();
            row2.addAll(row2.size(), Collections.emptyList());
            assertAll("Add all at end",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;)", row.toString()),
                    () -> assertEquals("Hello;world;!;(;and Meta-verse;)", row2.toString())
            );
        }

        @Test
        @DisplayName("Add all at start")
        void addAllAtStart() {
            var row = helloWorldRow();
            row.addAll(0, List.of("(", "and Meta-verse", ")"));
            assertAll("Add all at start",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("(;and Meta-verse;);Hello;world;!", row.toString())
            );
        }

        @Test
        @DisplayName("Add all")
        void addAll() {
            var row = helloWorldRow();
            row.addAll(2, List.of("(", "and Meta-verse", ")"));
            assertAll("Add all",
                    () -> assertEquals(6, row.size()),
                    () -> assertEquals("Hello;world;(;and Meta-verse;);!", row.toString())
            );
        }

        @Test
        @DisplayName("Add all null assertions")
        void addAllNullAssertions() {
            var emptyRow = new CsvRow();
            var lst = Collections.singleton((String) null);
            assertAll("Add all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll(0, null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll(0, lst))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 4, 5, 100})
        @DisplayName("Add all position assertions")
        void addAllPositionAssertions(int index) {
            var row = helloWorldRow();
            var lst = new ArrayList<String>();
            assertThrows(IndexOutOfBoundsException.class, () -> row.addAll(index, lst));
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Add all wrong values assertions")
        void addAllWrongValues(char wrongChar) {
            var row = helloWorldRow();
            var lst = Collections.singleton("He" + wrongChar + "llo");
            for (int i = 0; i < 4; i++) {
                int finalI = i;
                assertThrows(InvalidCsvValueException.class, () -> row
                        .addAll(finalI, lst));
            }

        }

    }

    @Nested
    @DisplayName("Set")
    final class SetTest {

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Set all indices working")
        void setAllIndices(int index) {
            var row = CsvRow.from("0;1;2;3;4;5;6;7");
            assertAll("Set all indices",
                    () -> assertEquals(String.valueOf(index), row.set(index, "replaced")),
                    () -> assertEquals("replaced", row.get(index))
            );
        }

        @Test
        @DisplayName("Set indices assertions")
        void setAllIndicesAssertions() {
            var emptyRow = new CsvRow();
            var row = CsvRow.from("0;1;2;3;4;5;6;7");
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.set(-1, "wrong")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.set(8, "wrong")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyRow.set(0, "wrong"))
            );
        }

        @Test
        @DisplayName("Set null assertions")
        void setNullAssertions() {
            var row = new CsvRow("One");
            assertThrows(NullPointerException.class, () -> row.set(0, null));
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Set wrong values assertions")
        void setWrongValues(char wrongChar) {
            var row = new CsvRow("One");
            assertThrows(InvalidCsvValueException.class, () -> row.set(0, "He" + wrongChar + "llo"));
        }

    }

    @Nested
    final class Fill {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 8, 100, 1000})
        @DisplayName("Fill basic test")
        void fill(int size) {
            var row = new CsvRow();
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
            var emptyRow = new CsvRow();
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
            var row = CsvRow.from("0;1;2;3;4;5;6;7");
            assertEquals(String.valueOf(index), row.get(index));
        }

        @Test
        @DisplayName("Get indices assertions")
        void getAllIndicesAssertions() {
            var emptyRow = new CsvRow();
            var row = CsvRow.from("0;1;2;3;4;5;6;7");
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.get(8)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyRow.get(0))
            );
        }

        @Test
        @DisplayName("Get first and last")
        void getFirstAndLast() {
            var row = CsvRow.from("0;1;2;3;4;5;6;7");
            assertAll("Get first and last",
                    () -> assertEquals("0", row.getFirst()),
                    () -> assertEquals("7", row.getLast())
            );
        }

        @Test
        @DisplayName("Get indices assertions")
        void getFirstAndLastAssertions() {
            var emptyRow = new CsvRow();
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
            var row = new CsvRow("0", "1", "2");
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
        @DisplayName("RemoveIf basic tests")
        void removeIf() {
            var row = CsvRow.from("1;2;3;4;5;6;7;8;9;10");
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
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.removeIf(null));
        }

    }

    @Nested
    final class Contains {

        private static final CsvRow row = CsvRow.from("1;3;5;7;9");

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
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.forEach(null));
        }

    }

    @Nested
    @DisplayName("Iterable")
    final class IterableTest {

        static Stream<String> escapeStringsProvider() {
            return Stream.of("Hello\n", "w\0rld", "I\r", "love\b", "\fJava !");
        }

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
            var emptyIt = new CsvRow().iterator();
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

        @Test
        @DisplayName("List iterator is working")
        void listIterator() {
            var row = helloWorldRow();
            var it = row.listIterator();
            var emptyIt = new CsvRow().listIterator();
            assertAll("List iterator is working",
                    () -> assertEquals(-1, it.previousIndex()),
                    () -> assertEquals(0, it.nextIndex()),
                    () -> assertFalse(it.hasPrevious()),
                    () -> assertFalse(it.hasPrevious()),
                    () -> assertTrue(it.hasNext()),
                    () -> assertTrue(it.hasNext()),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            assertEquals(i, it.nextIndex());
                            assertEquals(row.get(i), it.next());
                            assertEquals(i, it.previousIndex());
                        }
                    },
                    () -> assertFalse(it::hasNext),
                    () -> assertThrows(NoSuchElementException.class, it::next),
                    () -> assertFalse(emptyIt.hasNext()),
                    () -> {
                        for (int i = row.size() - 1; it.hasPrevious(); i--) {
                            assertEquals(i, it.previousIndex());
                            assertEquals(row.get(i), it.previous());
                            assertEquals(i, it.nextIndex());
                        }
                    }
            );
        }

        @Test
        @DisplayName("List iterator remove")
        void listIteratorRemove() {
            var row = helloWorldRow();
            var it = row.listIterator();
            assertAll("List iterator remove",
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
        void listIteratorForEachRemaining() {
            var lst = new ArrayList<String>();
            var row = helloWorldRow();
            var it = row.listIterator();
            it.next();
            it.forEachRemaining(lst::add);
            assertAll("For each remaining working",
                    () -> assertEquals(2, lst.size()),
                    () -> assertEquals("world!", String.join("", lst))
            );
        }

        @Test
        @DisplayName("List iterator concurrent modifications")
        void listIteratorConcurrentModifications() {
            var row = helloWorldRow();
            var it = row.listIterator();
            row.remove(0);
            assertThrows(ConcurrentModificationException.class, it::next);
        }

        @Test
        @DisplayName("List iterator set")
        void setListIterator(){
            var row = helloWorldRow();
            var it = row.listIterator();
            assertAll("List iterator set",
                    () -> assertThrows(IllegalStateException.class, () -> it.set("")),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            if (i == 1) {
                                it.set("w0rld");
                                it.set("Hell0");
                            }
                            it.next();
                        }
                    },
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("Hell0;world;!", row.toString())
            );
        }

        @Test
        @DisplayName("List iterator set null assertions")
        void listIteratorSetNullAssertions(){
            var row = helloWorldRow();
            var it = row.listIterator();
            it.next();
            assertThrows(NullPointerException.class, () -> it.set(null));
        }

        @ParameterizedTest
        @MethodSource("escapeStringsProvider")
        @DisplayName("List iterator set value assertions")
        void listIteratorSetValueAssertions(String wrongString){
            var row = helloWorldRow();
            var it = row.listIterator();
            it.next();
            assertThrows(InvalidCsvValueException.class, () -> it.set(wrongString));
        }

        @Test
        @DisplayName("List iterator add")
        void addListIterator(){
            var row = helloWorldRow();
            var it = row.listIterator();
            it.add("beforeAll");
            assertAll("List iterator add",
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            if (i == 1) {
                                it.add("1");
                                it.add("2");
                            }
                            it.next();
                        }
                        it.add("afterAll");
                    },
                    () -> assertEquals(7, row.size()),
                    () -> assertEquals("beforeAll;Hello;1;2;world;!;afterAll", row.toString())
            );
        }

        @Test
        @DisplayName("List iterator set null assertions")
        void listIteratorAddNullAssertions(){
            var row = helloWorldRow();
            var it = row.listIterator();
            assertThrows(NullPointerException.class, () -> it.add(null));
        }

        @ParameterizedTest
        @MethodSource("escapeStringsProvider")
        @DisplayName("List iterator set value assertions")
        void listIteratorAddValueAssertions(String wrongString){
            var row = helloWorldRow();
            var it = row.listIterator();
            assertThrows(InvalidCsvValueException.class, () -> it.add(wrongString));
        }

    }

    @Nested
    @DisplayName("Stream")
    final class StreamTest {

        @Test
        @DisplayName("Stream basic tests")
        void stream() {
            var lst = new ArrayList<String>();
            var emptyRow = new CsvRow();
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

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

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
            var emptyRow = new CsvRow();
            assertThrows(NullPointerException.class, () -> emptyRow.map(null));
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Map invalid values assertions")
        void mapValueAssertions(char wrongChar) {
            var row = helloWorldRow();
            assertThrows(InvalidCsvValueException.class, () -> row.map(s -> "" + wrongChar));
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
            assertAll("Equals basic tests",
                    () -> assertNotSame(row, row2),
                    () -> assertEquals(row, row2),
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
                    () -> assertEquals("\\n;", new CsvRow("\\n", "").toString()),
                    () -> assertEquals("\t;tab", new CsvRow("\t", "tab").toString())
            );
        }

        @Test
        @DisplayName("toString basic tests")
        void toStringBasicTests() {
            var row = new CsvRow("Hello\"", "world", "!", ";", "");
            var rowAsString = row.toString();
            assertAll("toString basic tests",
                    () -> assertEquals("\"Hello\"\"\";world;!;\";\";", rowAsString),
                    () -> assertEquals(row, CsvRow.from("\"Hello\"\"\";world;!;\";\";")),
                    () -> assertEquals(row.toString(), rowAsString)
            );
        }
    }
}