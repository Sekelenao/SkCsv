package io.github.sekelenao.skcsv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class SkAssertionsTest {

    @Nested
    final class Constructor {

        @Test
        @DisplayName("Constructor is private and throw")
        void privateConstructor() throws NoSuchMethodException {
            var constructor = SkAssertions.class.getDeclaredConstructor();
            assertThrows(IllegalAccessException.class, constructor::newInstance);
            constructor.setAccessible(true);
            assertThrows(InvocationTargetException.class, constructor::newInstance);
        }

    }

}
