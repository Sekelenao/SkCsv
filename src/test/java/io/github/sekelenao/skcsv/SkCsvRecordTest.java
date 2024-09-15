package io.github.sekelenao.skcsv;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

final class SkCsvRecordTest {

    public record Animal(@CsvColumn String name, float ignored, @CsvColumn int legs) {

        public Animal {
            Objects.requireNonNull(name);
            if(legs < 0) {
                throw new IllegalArgumentException("legs cannot be negative");
            }
        }

    }

    public record Food(@CsvColumn String name, @CsvColumn String color, String secretRecipe) {

        public Food {
            Objects.requireNonNull(name);
            Objects.requireNonNull(color);
            Objects.requireNonNull(secretRecipe);
        }

        @Override
        public String toString() {
            return name + " !";
        }
    }

    public record BankAccount(
            @CsvColumn String bankName,
            @CsvColumn UUID uuid,
            @CsvColumn double balance,
            @CsvColumn Food favoriteFood
    ){

        public BankAccount {
            Objects.requireNonNull(uuid);
            Objects.requireNonNull(bankName);
            if(balance < 0) {
                throw new IllegalArgumentException("balance cannot be negative");
            }
        }

    }

    private static final Path PRODUCED_PATH = Paths.get("src", "test", "resources", "produced.csv");

    private static final List<Animal> ANIMALS = List.of(
            new Animal("Dog", 0f, 4),
            new Animal("\"Cat\"\n", 0f, 4),
            new Animal("Spider;", 0f, 8)
    );

    private static final List<Food> FOODS = List.of(
            new Food("Soup", "Brown", "Contains Java coffee"),
            new Food("Burger", "Multicolor", "Contains iceberg salad"),
            new Food("Fish", "Blue", "Contains fish...")
    );

    private static final Iterable<BankAccount> BANK_ACCOUNT_ITERABLE = () -> new Iterator<>() {

        private int index;

        private static final Random RANDOM = new Random();

        @Override
        public boolean hasNext() {
            return index < 1_000_000;
        }

        @Override
        public BankAccount next() {
            if (!hasNext()) throw new NoSuchElementException();
            index++;
            return new BankAccount("OnlyBank", UUID.randomUUID(), RANDOM.nextDouble(), FOODS.get(RANDOM.nextInt(FOODS.size())));
        }

    };

    @Nested
    final class Export {

        @Test
        @DisplayName("Export with annotation and default methods")
        void exportWithAnnotation() throws IOException {
            SkCsvRecords.export(PRODUCED_PATH, ANIMALS, StandardOpenOption.CREATE);
            var csv = SkCsv.from(PRODUCED_PATH);
            assertAll("With annotation",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(ANIMALS.get(0).name, csv.get(0).getFirst()),
                    () -> assertEquals(ANIMALS.get(1).name, csv.get(1).getFirst()),
                    () -> assertEquals(ANIMALS.get(2).name, csv.get(2).getFirst()),
                    () -> assertEquals(ANIMALS.get(ANIMALS.size() - 1).legs, Integer.parseInt(csv.getLast().getLast()))
            );
            Files.deleteIfExists(PRODUCED_PATH);
            SkCsvRecords.export(PRODUCED_PATH, ANIMALS, SkCsvConfig.COMMA, StandardOpenOption.CREATE);
            var csv2 = SkCsv.from(PRODUCED_PATH, SkCsvConfig.COMMA);
            assertAll("With annotation and config",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(PRODUCED_PATH);
        }

        @Test
        @DisplayName("Export with annotation and override toString")
        void exportWithAnnotationOverride() throws IOException {
            SkCsvRecords.export(PRODUCED_PATH, FOODS, SkCsvConfig.SEMICOLON, StandardOpenOption.CREATE);
            var csv = SkCsv.from(PRODUCED_PATH);
            assertAll("With annotation",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(FOODS.get(0).name, csv.get(0).getFirst()),
                    () -> assertEquals(FOODS.get(1).name, csv.get(1).getFirst()),
                    () -> assertEquals(FOODS.get(2).name, csv.get(2).getFirst()),
                    () -> assertEquals(FOODS.get(FOODS.size() - 1).color, csv.getLast().getLast())
            );
            Files.deleteIfExists(PRODUCED_PATH);
            SkCsvRecords.export(PRODUCED_PATH, FOODS, SkCsvConfig.COMMA, StandardOpenOption.CREATE);
            var csv2 = SkCsv.from(PRODUCED_PATH, SkCsvConfig.COMMA);
            assertAll("With annotation and config",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(PRODUCED_PATH);
        }

        @Test
        @DisplayName("Export a lot")
        @Timeout(3)
        @Order(1)
        void exportALot() {
            assertDoesNotThrow(() -> SkCsvRecords.export(PRODUCED_PATH, BANK_ACCOUNT_ITERABLE, SkCsvConfig.SEMICOLON, StandardOpenOption.CREATE));
        }

        @Test
        @DisplayName("Import a lot")
        @Timeout(3)
        @Order(2)
        void importALot() throws IOException {
            var csv = SkCsv.from(PRODUCED_PATH);
            assertEquals(1_000_000, csv.size());
            Files.deleteIfExists(PRODUCED_PATH);
        }

    }

    @Nested
    final class Constructor {

        @Test
        @DisplayName("Constructor is private and throw")
        void privateConstructor() throws NoSuchMethodException {
            var constructor = SkCsvRecords.class.getDeclaredConstructor();
            assertThrows(IllegalAccessException.class, constructor::newInstance);
            constructor.setAccessible(true);
            assertThrows(InvocationTargetException.class, constructor::newInstance);
        }

    }

}
