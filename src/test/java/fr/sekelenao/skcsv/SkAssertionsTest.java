package fr.sekelenao.skcsv;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
