package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.key.BasicKeyParser;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleDependencyProviderIntegrationTest {
    @Test
    void simpleModule() {
        final DependencyProvider dependencyProvider = new ModuleDependencyProvider(new BasicKeyParser(),
                new SimpleModule());
        final int first = dependencyProvider.provide(DependencyProvider.key(int.class, Key.key("test:non_static_method")));
        final int second = dependencyProvider.provide(DependencyProvider.key(int.class, Key.key("test:static_method")));

        assertEquals(69, first);
        assertEquals(69420, second);
    }

    @Test
    void nonPublicModule() {
        assertThrows(ElementException.class,
                () -> new ModuleDependencyProvider(new BasicKeyParser(), new NonPublicModule()));
    }

    @Test
    void voidMethod() {
        assertThrows(ElementException.class,
                () -> new ModuleDependencyProvider(new BasicKeyParser(), new VoidMethod()));
    }

    @Test
    void tooManyParameters() {
        assertThrows(ElementException.class,
                () -> new ModuleDependencyProvider(new BasicKeyParser(), new TooManyParameters()));
    }

    @Test
    void memoized() {
        final DependencyProvider dependencyProvider = new ModuleDependencyProvider(new BasicKeyParser(),
                new MemoizingModule());
        final DependencyProvider.TypeKey key = DependencyProvider.key(Object.class, Key.key("test:memoized"));
        final Object object = dependencyProvider.provide(key);
        assertSame(dependencyProvider.provide(key), object);

        final DependencyProvider.TypeKey key2 = DependencyProvider.key(Object.class, Key.key("test:memoized_static"));
        final Object object2 = dependencyProvider.provide(key2);
        assertSame(dependencyProvider.provide(key2), object2);
    }

    @Test
    void notMemoized() {
        final DependencyProvider dependencyProvider = new ModuleDependencyProvider(new BasicKeyParser(),
                new NotMemoizing());

        final DependencyProvider.TypeKey key = DependencyProvider.key(Object.class, Key.key("test:non_static"));
        final Object object = dependencyProvider.provide(key);
        assertNotSame(dependencyProvider.provide(key), object);

        final DependencyProvider.TypeKey key2 = DependencyProvider.key(Object.class, Key.key("test:static"));
        final Object object2 = dependencyProvider.provide(key2);
        assertNotSame(dependencyProvider.provide(key2), object2);
    }

    public static class NotMemoizing implements DependencyModule {
        @DependencySupplier("test:static")
        public static @NotNull Object memoizedStatic() {
            return new Object();
        }

        @DependencySupplier("test:non_static")
        public @NotNull Object memoized() {
            return new Object();
        }
    }

    public static class MemoizingModule implements DependencyModule {
        @DependencySupplier("test:memoized_static")
        @Memoize
        public static @NotNull Object memoizedStatic() {
            return new Object();
        }

        @DependencySupplier("test:memoized")
        @Memoize
        public @NotNull Object memoized() {
            return new Object();
        }
    }

    public static class SimpleModule implements DependencyModule {
        @DependencySupplier("test:static_method")
        public static int staticMethod() {
            return 69420;
        }

        @DependencySupplier("test:non_static_method")
        public int nonStaticMethod() {
            return 69;
        }
    }

    static class NonPublicModule implements DependencyModule {}

    public static class VoidMethod implements DependencyModule {
        @DependencySupplier("test:void_method")
        public static void testMethod() {

        }
    }

    public static class TooManyParameters implements DependencyModule {
        @DependencySupplier("test:too_many_parameters")
        public static int testMethod(Key first) {
            return 0;
        }
    }
}
