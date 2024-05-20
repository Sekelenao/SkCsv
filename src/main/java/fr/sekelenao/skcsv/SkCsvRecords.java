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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class SkCsvRecords {

    /**
     * Private constructor to prevent instantiation of the SkCsvRecords class.
     *
     * <p>This constructor throws an AssertionError with a message indicating that
     * the SkCsvRecords class cannot be instantiated.
     *
     * @throws AssertionError always thrown to indicate that instantiation is not allowed
     */
    private SkCsvRecords() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    /**
     * Invokes the specified accessor method on the given record instance in a secure manner.
     *
     * <p>This method invokes the specified accessor method on the provided record instance.
     * It handles potential exceptions that may occur during the invocation, such as illegal access errors,
     * invocation target exceptions, and undeclared throwable exceptions.
     *
     * @param instance the record instance on which to invoke the accessor method
     * @param accessor the accessor method to invoke
     * @return the result of invoking the accessor method on the record instance
     * @throws IllegalAccessError if illegal access occurs while invoking the accessor method
     * @throws RuntimeException if the invocation target exception contains a runtime exception
     * @throws Error if the invocation target exception contains an error
     * @throws UndeclaredThrowableException if an undeclared throwable exception occurs during the invocation
     */
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

    /**
     * Returns a function that converts the specified record component to a string representation.
     *
     * <p>This method creates and returns a function that takes a record instance as input
     * and returns a string representation of the specified record component using its accessor method.
     *
     * @param component the record component for which to create the conversion function
     * @return a function that converts the specified record component to a string
     */
    private static Function<Record, String> componentToString(RecordComponent component){
        return instance -> String.valueOf(secureInvoke(instance, component.getAccessor()));
    }

    /**
     * Class-value that maintains a cache of conversion functions for record components annotated with CsvColumn.
     *
     * <p>This class-value stores a list of conversion functions for each record type. When a record type is provided
     * to the computeValue method, it retrieves the components annotated with CsvColumn from that class,
     * creates a conversion function for each component using componentToString, and stores these functions in a list.
     */
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


    /**
     * Exports the provided records to a CSV file at the specified path using the given configuration.
     *
     * <p>This method exports the records from the provided iterable to a CSV file located at the specified path.
     * It uses the specified CSV configuration to format the exported data.
     *
     * @param path the path to the CSV file to export the records to
     * @param records the iterable of records to export
     * @param config the CSV configuration to use for formatting the exported data
     * @param options the open options specifying how the file is opened
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void export(Path path, Iterable<? extends Record> records, SkCsvConfig config, OpenOption... options) throws IOException {
        SkAssertions.requireNonNulls(path, records, config, options);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)) {
            for (var rcd : records) {
                var values = CACHE.get(rcd.getClass()).stream()
                        .map(f -> f.apply(rcd))
                        .toList();
                writer.write(formatter.toCsvString(values));
                writer.newLine();
            }
        }
    }

    /**
     * Exports the provided records to a CSV file at the specified path using the default configuration.
     *
     * <p>This method exports the records from the provided iterable to a CSV file located at the specified path.
     * It uses the default configuration {@link SkCsvConfig#SEMICOLON SEMICOLON} with a semicolon as the delimiter and
     * double quotes for quoting fields.
     *
     * @param path the path to the CSV file to export the records to
     * @param records the iterable of records to export
     * @param options the open options specifying how the file is opened
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void export(Path path, Iterable<? extends Record> records, OpenOption... options) throws IOException {
        SkAssertions.requireNonNulls(path, records, options);
        export(path, records, SkCsvConfig.SEMICOLON, options);
    }

}