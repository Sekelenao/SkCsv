package fr.sekelenao.skcsv.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;

public final class SkCsvRecords {

    private SkCsvRecords() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    public static <R extends Record & SkCsvRecord> void export(
            Path path, Iterable<R> records, SkCsvConfig config, OpenOption... openOptions
    ) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(records);
        Objects.requireNonNull(config);
        Objects.requireNonNull(openOptions);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, openOptions)) {
            for (var r : records) {
                writer.write(formatter.toCsvString(r.skRecordValues()));
                writer.newLine();
            }
        }
    }

}
