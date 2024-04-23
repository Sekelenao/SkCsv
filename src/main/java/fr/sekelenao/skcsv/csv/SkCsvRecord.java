package fr.sekelenao.skcsv.csv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

public interface SkCsvRecord {

    private static Object secureInvoke(Method accessor, Object instance) {
        Objects.requireNonNull(accessor);
        Objects.requireNonNull(instance);
        try{
            return accessor.invoke(instance);
        }catch (IllegalAccessException e){
            throw new IllegalAccessError();
        } catch(InvocationTargetException exception) {
            switch (exception.getCause()){
                case RuntimeException e -> throw e;
                case Error e -> throw e;
                default -> throw new AssertionError();
            }
        }
    }

    private static <T extends Record & SkCsvRecord> Iterable<String> generateHeaders(Class<T> type) {
        Objects.requireNonNull(type);
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

    default Iterable<String> recordValues() {
        var components = this.getClass().getRecordComponents();
        var values = new ArrayList<String>(components.length);
        for(var component: components){
            if(component.isAnnotationPresent(CsvColumn.class)){
                var obj = secureInvoke(component.getAccessor(), this);
                var value = obj == null ? "" : String.valueOf(obj);
                values.add(value);
            }
        }
        return values;
    }

}
