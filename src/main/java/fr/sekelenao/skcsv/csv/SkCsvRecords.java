package fr.sekelenao.skcsv.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public final class SkCsvRecords {

    private SkCsvRecords() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    private static <R extends Record> Iterable<String> generateHeaders(Class<R> type) {
        Objects.requireNonNull(type);
        var headers = new ArrayList<String>();
        var fields = type.getDeclaredFields();
        for (var field : fields) {
            if (field.isAnnotationPresent(SkCsvRecord.CsvColumn.class)) {
                var annotation = field.getAnnotation(SkCsvRecord.CsvColumn.class);
                var header = annotation.value();
                if (header.isEmpty()) {
                    headers.add(field.getName());
                } else {
                    headers.add(header);
                }
            }
        }
        return headers;
    }

    public static <R extends Record & SkCsvRecord> void export(
            Path path, Iterable<R> records, CsvConfiguration config, OpenOption... openOptions
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

    public static <R extends Record & SkCsvRecord> void exportWithHeaders(
            Path path, Iterable<R> records, CsvConfiguration config, OpenOption... openOptions
    ) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(records);
        Objects.requireNonNull(config);
        Objects.requireNonNull(openOptions);
        var iterator = records.iterator();
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, openOptions)) {
            if(iterator.hasNext()) {
                var firstRecord = iterator.next();
                writer.write(formatter.toCsvString(generateHeaders(firstRecord.getClass())));
                writer.newLine();
                writer.write(formatter.toCsvString(firstRecord.skRecordValues()));
            }
            while (iterator.hasNext()) {
                writer.write(formatter.toCsvString(iterator.next().skRecordValues()));
                writer.newLine();
            }
        }
    }

}
