package com.github.steanky.element.dependency;

import com.github.steanky.element.ElementException;
import com.github.steanky.element.annotation.DependencySupplier;
import com.github.steanky.element.key.BasicKeyParser;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleDependencyProviderTest {
    public static class SimpleModule implements DependencyModule {
        @DependencySupplier("test:non_static_method")
        public int nonStaticMethod() {
            return 69;
        }

        @DependencySupplier("test:static_method")
        public static int staticMethod() {
            return 69420;
        }
    }

    static class NonPublicModule implements DependencyModule {
    }

    public static class KeyedModule implements DependencyModule {
        @DependencySupplier("test:non_static_method")
        public @NotNull Key nonStaticMethod(@NotNull Key name) {
            return name;
        }

        @DependencySupplier("test:static_method")
        public static @NotNull Key staticMethod(@NotNull Key name) {
            return name;
        }

        public int ignored(@NotNull String method1, int method2) {
            return 6974566;
        }
    }

    public static class VoidMethod implements DependencyModule {
        @DependencySupplier("test:void_method")
        public static void testMethod() {

        }
    }

    public static class TooManyParameters implements DependencyModule {
        @DependencySupplier("test:too_many_parameters")
        public static int testMethod(Key first, Key second) {
            return 0;
        }
    }

    public static class WrongType implements DependencyModule {
        @DependencySupplier("test:wrong_type")
        public static int testMethod(String first) {
            return 0;
        }
    }

    @Test
    void simpleModule() {
        DependencyProvider dependencyProvider = new ModuleDependencyProvider(new SimpleModule(), new BasicKeyParser());
        int first = dependencyProvider.provide(Key.key("test:non_static_method"));
        int second = dependencyProvider.provide(Key.key("test:static_method"));

        assertEquals(69, first);
        assertEquals(69420, second);
    }

    @Test
    void nonPublicModule() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new NonPublicModule(), new BasicKeyParser()));
    }

    @Test
    void namedDependencies() {
        DependencyProvider dependencyProvider = new ModuleDependencyProvider(new KeyedModule(), new BasicKeyParser());
        Key nonStatic = Key.key("test:non_static_method");
        Key staticKey = Key.key("test:static_method");

        Key first = dependencyProvider.provide(nonStatic, nonStatic);
        Key second = dependencyProvider.provide(staticKey, staticKey);

        assertEquals(nonStatic, first);
        assertEquals(staticKey, second);
    }

    @Test
    void nullKey() {
        DependencyProvider dependencyProvider = new ModuleDependencyProvider(new KeyedModule(), new BasicKeyParser());
        Key nonStatic = Key.key("test:non_static_method");

        assertThrows(ElementException.class, () -> dependencyProvider.provide(nonStatic));
    }

    @Test
    void nonNullKey() {
        DependencyProvider dependencyProvider = new ModuleDependencyProvider(new SimpleModule(), new BasicKeyParser());
        Key nonStatic = Key.key("test:non_static_method");

        assertThrows(ElementException.class, () -> dependencyProvider.provide(nonStatic, nonStatic));
    }

    @Test
    void voidMethod() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new VoidMethod(), new BasicKeyParser()));
    }

    @Test
    void tooManyParameters() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new TooManyParameters(),
                new BasicKeyParser()));
    }

    @Test
    void wrongType() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new WrongType(),
                new BasicKeyParser()));
    }
}