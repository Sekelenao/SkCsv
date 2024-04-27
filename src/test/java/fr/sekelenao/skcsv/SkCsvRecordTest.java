package fr.sekelenao.skcsv;

import fr.sekelenao.skcsv.csv.SkCsv;
import fr.sekelenao.skcsv.csv.SkCsvConfig;
import fr.sekelenao.skcsv.csv.SkCsvRecord;
import fr.sekelenao.skcsv.csv.SkCsvRecords;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class SkCsvRecordTest {

    public record Animal(@CsvColumn String name, float ignored,
                         @CsvColumn int legs) implements SkCsvRecord {
    }

    public record Food(String name, String color) implements SkCsvRecord {

        @Override
        public Iterable<String> skRecordValues() {
            return List.of(name, color.toUpperCase());
        }

    }

    private static final Path PATH = Paths.get("src", "test", "resources", "produced.csv");

    private static final List<Animal> ANIMALS = List.of(
            new Animal("Dog", 0f, 4),
            new Animal("\"Cat\"\n", 0f, 4),
            new Animal("Spider;", 0f, 8)
    );

    private static final List<Food> FOODS = List.of(
            new Food("Banana", "yellow"),
            new Food("Burger", "idk"),
            new Food("Fish", "blue for sure")
    );

    @Nested
    final class Export {

        @Test
        @DisplayName("Export with annotation and default methods")
        void exportWithAnnotation() throws IOException {
            SkCsvRecords.export(PATH, ANIMALS, SkCsvConfig.SEMICOLON, StandardOpenOption.CREATE);
            var csv = SkCsv.from(PATH);
            assertAll("With annotation and header",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(ANIMALS.get(0).name, csv.get(0).getFirst()),
                    () -> assertEquals(ANIMALS.get(1).name, csv.get(1).getFirst()),
                    () -> assertEquals(ANIMALS.get(2).name, csv.get(2).getFirst()),
                    () -> assertEquals(ANIMALS.get(ANIMALS.size() - 1).legs, Integer.parseInt(csv.getLast().getLast()))
            );
            Files.deleteIfExists(PATH);
            SkCsvRecords.export(PATH, ANIMALS, SkCsvConfig.COMMA, StandardOpenOption.CREATE);
            var csv2 = SkCsv.from(PATH, SkCsvConfig.COMMA);
            assertAll("With annotation and config",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(PATH);
        }

        @Test
        @DisplayName("Export with annotation and override methods")
        void exportWithAnnotationOverride() throws IOException {
            SkCsvRecords.export(PATH, FOODS, SkCsvConfig.SEMICOLON, StandardOpenOption.CREATE);
            var csv = SkCsv.from(PATH);
            System.out.println(csv);
            assertAll("With annotation",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(FOODS.get(0).name, csv.get(0).getFirst()),
                    () -> assertEquals(FOODS.get(1).name, csv.get(1).getFirst()),
                    () -> assertEquals(FOODS.get(2).name, csv.get(2).getFirst()),
                    () -> assertEquals(FOODS.get(FOODS.size() - 1).color.toUpperCase(), csv.getLast().getLast())
            );
            Files.deleteIfExists(PATH);
            SkCsvRecords.export(PATH, FOODS, SkCsvConfig.COMMA, StandardOpenOption.CREATE);
            var csv2 = SkCsv.from(PATH, SkCsvConfig.COMMA);
            assertAll("With annotation and config",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(PATH);
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
