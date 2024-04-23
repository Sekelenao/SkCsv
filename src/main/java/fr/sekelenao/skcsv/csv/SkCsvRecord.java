package fr.sekelenao.skcsv.csv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

public interface SkCsvRecord {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT})
    @interface CsvColumn {
        String value() default "";
    }

    private static Object secureInvoke(Method accessor, Object instance) {
        Objects.requireNonNull(accessor);
        Objects.requireNonNull(instance);
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError();
        } catch (InvocationTargetException exception) {
            switch (exception.getCause()) {
                case RuntimeException e -> throw e;
                case Error e -> throw e;
                default -> throw new AssertionError();
            }
        }
    }

    default Iterable<String> skRecordValues() {
        var components = this.getClass().getRecordComponents();
        var values = new ArrayList<String>(components.length);
        for (var component : components) {
            if (component.isAnnotationPresent(SkCsvRecord.CsvColumn.class)) {
                var obj = secureInvoke(component.getAccessor(), this);
                var value = obj == null ? "" : String.valueOf(obj);
                values.add(value);
            }
        }
        return values;
    }

}
