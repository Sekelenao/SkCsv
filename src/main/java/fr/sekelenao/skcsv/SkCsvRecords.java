package fr.sekelenao.skcsv;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class SkCsvRecords {

    private SkCsvRecords() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    private static Object secureInvoke(Record instance, Method accessor) {
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError();
        } catch (InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new UndeclaredThrowableException(e);
        }
    }

    private static Function<Record, String> componentToString(RecordComponent component){
        return instance -> String.valueOf(secureInvoke(instance, component.getAccessor()));
    }

    private static final ClassValue<List<Function<Record, String>>> CACHE = new ClassValue<>() {

        @Override
        protected List<Function<Record, String>> computeValue(Class<?> type) {
            Objects.requireNonNull(type);
            return Arrays.stream(type.getRecordComponents())
                    .filter(rc -> rc.isAnnotationPresent(CsvColumn.class))
                    .map(SkCsvRecords::componentToString)
                    .toList();
        }

    };

    public static void export(Path path, Iterator<? extends Record> records, SkCsvConfig config, OpenOption... options) throws IOException {
        SkAssertions.requireNonNulls(path, records, config, options);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)) {
            while (records.hasNext()) {
                var rcd = records.next();
                var values = CACHE.get(rcd.getClass()).stream()
                        .map(f -> f.apply(rcd))
                        .toList();
                writer.write(formatter.toCsvString(values));
                writer.newLine();
            }
        }
    }

    public static void export(Path path, Iterable<? extends Record> records, SkCsvConfig config, OpenOption... openOptions) throws IOException {
        SkAssertions.requireNonNulls(path, records, config, openOptions);
        export(path, records.iterator(), config, openOptions);
    }

}
