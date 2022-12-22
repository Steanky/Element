package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModuleDependencyProviderIntegrationTest {
    @Test
    void simpleModule() {
        final DependencyProvider dependencyProvider = new ModuleDependencyProvider(new BasicKeyParser(),
                new SimpleModule());
        final int first = dependencyProvider.provide(DependencyProvider.key(Token.INTEGER, Key.key("test:non_static_method")));
        final int second = dependencyProvider.provide(DependencyProvider.key(Token.INTEGER, Key.key("test:static_method")));

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
        final DependencyProvider.TypeKey<Object> key = DependencyProvider.key(Token.OBJECT, Key.key("test:memoized"));
        final Object object = dependencyProvider.provide(key);
        assertSame(dependencyProvider.provide(key), object);

        final DependencyProvider.TypeKey<Object> key2 = DependencyProvider.key(Token.OBJECT, Key.key("test:memoized_static"));
        final Object object2 = dependencyProvider.provide(key2);
        assertSame(dependencyProvider.provide(key2), object2);
    }

    @Test
    void notMemoized() {
        final DependencyProvider dependencyProvider = new ModuleDependencyProvider(new BasicKeyParser(),
                new NotMemoizing());

        final DependencyProvider.TypeKey<Object> key = DependencyProvider.key(Token.OBJECT, Key.key("test:non_static"));
        final Object object = dependencyProvider.provide(key);
        assertNotSame(dependencyProvider.provide(key), object);

        final DependencyProvider.TypeKey<Object> key2 = DependencyProvider.key(Token.OBJECT, Key.key("test:static"));
        final Object object2 = dependencyProvider.provide(key2);
        assertNotSame(dependencyProvider.provide(key2), object2);
    }

    @Test
    void ambiguousThrows() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new BasicKeyParser(), new Ambiguous()));
    }

    @Test
    void resolutionByType() {
        DependencyProvider provider = new ModuleDependencyProvider(new BasicKeyParser(), new NotAmbiguous());
        String str = provider.provide(DependencyProvider.key(Token.STRING));
        int i = provider.provide(DependencyProvider.key(Token.INTEGER));

        assertEquals("value", str);
        assertEquals(10, i);
    }

    @Test
    void ambiguousSameNameThrows() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new BasicKeyParser(), new AmbiguousSameName()));
    }

    @Test
    void ambiguityResolvedByName() {
        DependencyProvider provider = new ModuleDependencyProvider(new BasicKeyParser(), new AmbiguityResolvedByName());

        Object first = provider.provide(DependencyProvider.key(Token.OBJECT, Key.key("test:first")));
        assertEquals("first", first);

        Object second = provider.provide(DependencyProvider.key(Token.OBJECT, Key.key("test:second")));
        assertEquals("second", second);
    }

    @Test
    void sameNameDifferentTypesWithKey() {
        DependencyProvider provider = new ModuleDependencyProvider(new BasicKeyParser(), new SameNameDifferentTypes());

        Object first = provider.provide(DependencyProvider.key(Token.OBJECT, Key.key("test:first")));
        assertEquals("first", first);

        String second = provider.provide(DependencyProvider.key(Token.STRING, Key.key("test:second")));
        assertEquals("second", second);
    }

    @Test
    void sameNameDifferentTypesNoKey() {
        DependencyProvider provider = new ModuleDependencyProvider(new BasicKeyParser(), new SameNameDifferentTypes());

        Object first = provider.provide(DependencyProvider.key(Token.OBJECT));
        assertEquals("first", first);

        String second = provider.provide(DependencyProvider.key(Token.STRING));
        assertEquals("second", second);
    }

    @Test
    void typesDifferingByGeneric() {
        DependencyProvider provider = new ModuleDependencyProvider(new BasicKeyParser(), new TypesDifferingByGeneric());

        List<String> stringList = provider.provide(DependencyProvider.key(new Token<>() {}));
        List<Integer> integerList = provider.provide(DependencyProvider.key(new Token<>() {}));
        List<?> wildcardList = provider.provide(DependencyProvider.key(new Token<>() {}));

        assertEquals(TypesDifferingByGeneric.stringList(), stringList);
        assertEquals(TypesDifferingByGeneric.integerList(), integerList);
        assertEquals(TypesDifferingByGeneric.wildcardList(), wildcardList);
    }

    @Test
    void identicalGenericTypesThrows() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new BasicKeyParser(),
                new IdenticalGenericTypes()));
    }

    @Test
    void primitiveWrapperBoxingAmbiguityThrows() {
        assertThrows(ElementException.class, () -> new ModuleDependencyProvider(new BasicKeyParser(),
                new PrimitiveWrapperBoxingAmbiguity()));
    }

    @Test
    void wrapperBoxing() {
        DependencyProvider provider = new ModuleDependencyProvider(new BasicKeyParser(), new WrapperBoxing());

        int value = provider.provide(DependencyProvider.key(Token.INTEGER, Key.key("test:wrapper")));

        assertEquals(WrapperBoxing.wrapper(), value);

        int value2 = provider.provide(DependencyProvider.key(Token.INTEGER, Key.key("test:primitive")));
        assertEquals(WrapperBoxing.primitive(), value2);
    }

    public static class WrapperBoxing implements DependencyModule {
        @Dependency("test:wrapper")
        public static @NotNull Integer wrapper() {
            return 10;
        }

        @Dependency("test:primitive")
        public static int primitive() {
            return 20;
        }
    }

    public static class PrimitiveWrapperBoxingAmbiguity implements DependencyModule {
        @Dependency
        public static @NotNull Integer wrapper() {
            return 0;
        }

        @Dependency
        public static int primitive() {
            return 0;
        }
    }

    public static class IdenticalGenericTypes implements DependencyModule {
        @Dependency
        public static @NotNull List<String> stringList() {
            return List.of("first");
        }

        @Dependency
        public static @NotNull List<String> stringList2() {
            return List.of("first");
        }
    }

    public static class TypesDifferingByGeneric implements DependencyModule {
        @Dependency
        public static @NotNull List<String> stringList() {
            return List.of("first");
        }

        @Dependency
        public static @NotNull List<Integer> integerList() {
            return List.of(10);
        }

        @Dependency
        public static @NotNull List<?> wildcardList() {
            return List.of("first", 1, 2);
        }
    }

    public static class SameNameDifferentTypes implements DependencyModule {
        @Dependency("test:first")
        public static @NotNull Object objectReturning() {
            return "first";
        }

        @Dependency("test:first")
        public static @NotNull String stringReturning() {
            return "second";
        }
    }

    public static class AmbiguityResolvedByName implements DependencyModule {
        @Dependency("test:first")
        public static @NotNull Object objectReturning() {
            return "first";
        }

        @Dependency("test:second")
        public static @NotNull Object objectReturning2() {
            return "second";
        }
    }

    public static class AmbiguousSameName implements DependencyModule {
        @Dependency("test:first")
        public static @NotNull Object objectReturning() {
            return new Object();
        }

        @Dependency("test:first")
        public static @NotNull Object objectReturning2() {
            return new Object();
        }
    }

    public static class NotAmbiguous implements DependencyModule {
        @Dependency
        public static @NotNull String string() {
            return "value";
        }

        @Dependency
        public static int integer() {
            return 10;
        }
    }

    public static class Ambiguous implements DependencyModule {
        @Dependency
        public static @NotNull Object objectReturning() {
            return new Object();
        }

        @Dependency
        public static @NotNull Object objectReturning2() {
            return new Object();
        }
    }

    public static class NotMemoizing implements DependencyModule {
        @Dependency("test:static")
        public static @NotNull Object memoizedStatic() {
            return new Object();
        }

        @Dependency("test:non_static")
        public @NotNull Object memoized() {
            return new Object();
        }
    }

    public static class MemoizingModule implements DependencyModule {
        @Dependency("test:memoized_static")
        @Memoize
        public static @NotNull Object memoizedStatic() {
            return new Object();
        }

        @Dependency("test:memoized")
        @Memoize
        public @NotNull Object memoized() {
            return new Object();
        }
    }

    public static class SimpleModule implements DependencyModule {
        @Dependency("test:static_method")
        public static int staticMethod() {
            return 69420;
        }

        @Dependency("test:non_static_method")
        public int nonStaticMethod() {
            return 69;
        }
    }

    static class NonPublicModule implements DependencyModule {}

    public static class VoidMethod implements DependencyModule {
        @Dependency("test:void_method")
        public static void testMethod() {

        }
    }

    public static class TooManyParameters implements DependencyModule {
        @Dependency("test:too_many_parameters")
        public static int testMethod(Key first) {
            return 0;
        }
    }
}
