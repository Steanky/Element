package com.github.steanky.element.core.element;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContextManagerIntegrationTest {
    private static ContextManager manager() {
        final ContextManager contextManager = ContextManager.builder("test").build();
        for (Class<?> cls : ContextManagerIntegrationTest.class.getDeclaredClasses()) {
            contextManager.registerElementClass(cls);
        }

        return contextManager;
    }

    private static ElementContext context(ConfigContainer data) {
        return manager().makeContext(data);
    }

    private static ElementContext context(String data) {
        return manager().makeContext(ConfigElement.of(data).asContainer());
    }

    @Test
    void simple() {
        assertNotNull(context("{type='simple'}").provide());
    }

    @Test
    void simpleData() {
        assertEquals(10, ((SimpleData)context("{type='simple_data', value=10}").provide()).data.value);
    }

    @Test
    void simpleChild() {
        assertNotNull(((SimpleChild)context("{type='simple_child', child={type='simple'}}").provide()).simple);
    }

    @Test
    void simpleDefaultingChild() {
        assertNotNull(((SimpleDefaultingChild)context("{type='simple_defaulting_child'}").provide()).simple);
    }

    @Test
    void multipleChildren1() {
        MultipleChildren1 element = context("{type='multiple_children_1', children=[]}").provide();
        assertEquals(0, element.children.size());
    }

    @Test
    void multipleChildren2() {
        MultipleChildren1 element = context("{type='multiple_children_1', children=[{type='simple'}]}").provide();
        assertEquals(1, element.children.size());
    }

    @Test
    void multipleChildren3() {
        MultipleChildren1 element = context("{type='multiple_children_1', children=[{type='simple'}, {type='simple'}]}").provide();
        assertEquals(2, element.children.size());
    }

    @Test
    void multipleChildren4() {
        MultipleChildren1 element = context("{type='multiple_children_1', children='./deferredList', " +
                "deferredList=[]}").provide();
        assertEquals(0, element.children.size());
    }

    @Test
    void multipleChildren5() {
        MultipleChildren1 element = context("{type='multiple_children_1', children='./deferredList', " +
                "deferredList=[{type='simple'}]}").provide();
        assertEquals(1, element.children.size());
    }

    @Test
    void multipleChildren6() {
        MultipleChildren1 element = context("{type='multiple_children_1', children='./deferredList', " +
                "deferredList=[{type='simple'}, '../other'], other={type='simple'}}").provide();
        assertEquals(2, element.children.size());
    }

    @Test
    void multipleChildren7() {
        MultipleChildren1 element = context("{type='multiple_children_1', children='./deferredList', " +
                "deferredList=[{type='simple'}, '../other'], other=[{type='simple'}, {type='simple'}]}").provide();
        assertEquals(2, element.children.size());
    }

    @Test
    void defaultingChildren() {
        SimpleDefaultingChildren element = context("{type='simple_defaulting_children'}").provide();
        assertEquals(0, element.children.size());
    }

    @Test
    void backreferenceDefault() {
        SimpleDefaultingChildren element = context("{type='simple_defaulting_children', children='./backreference'}").provide();
        assertEquals(1, element.children.size());
    }

    @Test
    void nestedDefaultingChild() {
        NestedDefaultingChild child = context("{type='nested_defaulting_child'}").provide();
        assertNotNull(child);
        assertNotNull(child.simple);
        assertNotNull(child.simple.simple);
    }

    @Model("simple")
    public static class Simple {
        @FactoryMethod
        public Simple() {}
    }

    @Model("simple_data")
    public static class SimpleData {
        private final Data data;

        @FactoryMethod
        public SimpleData(Data data) {
            this.data = data;
        }

        @DataObject
        public record Data(int value) {}
    }

    @Model("nested_defaulting_child")
    @Default("""
            {
              child={type='simple_defaulting_child'}
            }
            """)
    public static class NestedDefaultingChild {
        private final SimpleDefaultingChild simple;

        @FactoryMethod
        public NestedDefaultingChild(@Child("child") SimpleDefaultingChild simple) {
            this.simple = simple;
        }
    }

    @Model("simple_child")
    public static class SimpleChild {
        private final Simple simple;

        @FactoryMethod
        public SimpleChild(@Child("child") Simple simple) {
            this.simple = simple;
        }
    }

    @Model("simple_defaulting_child")
    @Default("""
            {
              child={type='simple'}
            }
            """)
    public static class SimpleDefaultingChild {
        private final Simple simple;

        @FactoryMethod
        public SimpleDefaultingChild(@Child("child") Simple simple) {
            this.simple = simple;
        }
    }

    @Model("simple_defaulting_children")
    @Default("""
            {
              children=[],
              backreference=[{type='simple'}]
            }
            """)
    public static class SimpleDefaultingChildren {
        private final List<Simple> children;

        @FactoryMethod
        public SimpleDefaultingChildren(@Child("children") List<Simple> children) {
            this.children = children;
        }
    }

    @Model("multiple_children_1")
    public static class MultipleChildren1 {
        private final Set<Simple> children;

        @FactoryMethod
        public MultipleChildren1(@Child("children") List<Simple> children) {
            this.children = new HashSet<>(children);
        }
    }
}
