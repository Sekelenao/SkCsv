package fr.sekelenao.skcsv;

import fr.sekelenao.skcsv.csv.SkCsv;
import fr.sekelenao.skcsv.csv.SkCsvConfig;
import fr.sekelenao.skcsv.csv.SkCsvRecord;
import fr.sekelenao.skcsv.csv.SkCsvRecords;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SkCsvRecordTest {

    public record Animal(@CsvColumn String name, float ignored,
                         @CsvColumn("Number of legs") int legs) implements SkCsvRecord {
    }

    private static final Path PATH = Paths.get("src", "test", "resources", "produced.csv");

    private static final List<Animal> animals = List.of(
            new Animal("Dog", 0f, 4),
            new Animal("\"Cat\"\n", 0f, 4),
            new Animal("Spider;", 0f, 8)
    );

    @Nested
    final class Export {

        @Test
        void exportWithAnnotation() throws IOException {
            SkCsvRecords.exportWithHeaders(PATH, animals, SkCsvConfig.SEMICOLON, StandardOpenOption.CREATE);
            var csv = SkCsv.from(PATH);
            assertAll("With annotation and header",
                    () -> assertEquals(4, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(animals.getFirst().name, csv.get(1).getFirst()),
                    () -> assertEquals(animals.get(1).name, csv.get(2).getFirst()),
                    () -> assertEquals(animals.get(2).name, csv.get(3).getFirst()),
                    () -> assertEquals(animals.getLast().legs, Integer.parseInt(csv.getLast().getLast()))
            );
            Files.deleteIfExists(PATH);
            SkCsvRecords.export(PATH, animals, SkCsvConfig.COMMA, StandardOpenOption.CREATE);
            var csv2 = SkCsv.from(PATH, SkCsvConfig.COMMA);
            assertAll("With annotation without header",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(PATH);
        }

    }

}
