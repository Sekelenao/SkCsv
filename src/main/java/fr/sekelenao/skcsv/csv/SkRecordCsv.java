package fr.sekelenao.skcsv.csv;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class SkRecordCsv<T extends Record & CsvRecord> {

    private boolean exportHeaders;

    private final Class<T> type;

    public SkRecordCsv(Class<T> type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public SkRecordCsv(Class<T> type, Iterator<T> records) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(records);
        this.type = type;
    }

    static Object secureInvoke(Method accessor, Object instance) {
        Objects.requireNonNull(accessor);
        Objects.requireNonNull(instance);
        try{
            return accessor.invoke(instance);
        }catch (IllegalAccessException e){
            throw new IllegalAccessError();
        } catch(InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException uncheckedException) {
                throw uncheckedException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new AssertionError(cause);
        }
    }

    public Iterable<String> generateHeaders(){
        var headers = new ArrayList<String>();
        var fields =  type.getDeclaredFields();
        for(var field: fields){
            if(field.isAnnotationPresent(CsvColumn.class)){
                var annotation = field.getAnnotation(CsvColumn.class);
                var header = annotation.value();
                if(header.isEmpty()){
                    headers.add(field.getName());
                } else {
                    SkAssertions.conformValue(header);
                    headers.add(header);
                }
            }
        }
        return headers;
    }

    public static void exportRecords(Path path, Iterable<CsvRecord> records, CsvConfiguration csvConfiguration, boolean withHeaders) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(records);
        Objects.requireNonNull(csvConfiguration);
        var it = records.iterator();
        var formatter = new CsvFormatter(csvConfiguration);
        try (var writer = Files.newBufferedWriter(path)) {
            while (it.hasNext()) {
                writer.write(formatter.toCsvString(it.next().recordValues()));
            }
        }
    }

}
