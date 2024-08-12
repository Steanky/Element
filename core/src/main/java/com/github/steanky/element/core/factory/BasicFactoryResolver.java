package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.path.ConfigPath;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.mutable.Mutable;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.*;

/**
 * Basic implementation of {@link FactoryResolver}. Can resolve classes with either explicit factories (a
 * {@code public static} method annotated with {@link FactoryMethod}) or factory constructors.
 */
public class BasicFactoryResolver implements FactoryResolver {
    private final KeyParser keyParser;
    private final ContainerCreator containerCreator;
    private final MappingProcessorSource processorSource;

    private final class GenericFactory implements ElementFactory<Object, Object> {
        private final Constructor<?> factoryConstructor;
        private final ElementParameter[] parameters;
        private final boolean requiresData;
        private final ConfigNode defaultValues;

        private GenericFactory(final Constructor<?> factoryConstructor, final ElementParameter[] parameters,
                final boolean requiresData, final ConfigNode defaultValues) {
            this.factoryConstructor = factoryConstructor;
            this.parameters = parameters;
            this.requiresData = requiresData;
            this.defaultValues = defaultValues;
        }

        @NotNull
        @Override
        public Object make(final Object objectData, final @NotNull ConfigPath configPath, final @NotNull ElementContext context,
                final @NotNull DependencyProvider dependencyProvider) {
            if (requiresData && objectData == null) {
                throw elementException(factoryConstructor.getDeclaringClass(), configPath,
                        "Element requires data, but none was provided");
            }

            if (!requiresData && objectData != null) {
                throw elementException(factoryConstructor.getDeclaringClass(), configPath,
                        "Element does not accept data, and data was provided");
            }

            if (!defaultValues.isEmpty()) {
                context.registerDefaults(configPath, defaultValues);
            }

            ConfigNode ourData = null;

            final Object[] args = new Object[parameters.length];

            try {
                for (int i = 0; i < args.length; i++) {
                    final ElementParameter parameter = parameters[i];

                    args[i] = switch (parameter.type) {
                        case DATA -> objectData;
                        case DEPENDENCY -> {
                            try {
                                yield dependencyProvider.provide(parameter.typeKey);
                            }
                            catch (ElementException exception) {
                                exception.setConfigPath(configPath);
                                exception.setElementClass(factoryConstructor.getDeclaringClass());
                                throw exception;
                            }
                        }
                        case CHILD -> {
                            if (ourData == null) {
                                ourData = context.follow(configPath);
                                if (ourData == null) {
                                    throw elementException(factoryConstructor.getDeclaringClass(), configPath,
                                            "Failure to follow path");
                                }
                            }

                            final ConfigPath absoluteChildDataPath = configPath.resolve(parameter.childPath);
                            final boolean isContainer = containerCreator
                                    .isContainerType(Token.ofType(parameter.parameter.getParameterizedType()));

                            //recursively resolve child elements
                            yield child(parameter, configPath, configPath, ourData, context, absoluteChildDataPath,
                                    dependencyProvider, isContainer);
                        }
                    };
                }
            }
            catch (ElementException exception) {
                exception.setElementClass(factoryConstructor.getDeclaringClass());
                exception.setConfigPath(configPath);
                throw exception;
            }

            try {
                return factoryConstructor.newInstance(args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw elementException(e, factoryConstructor.getDeclaringClass(), configPath, "Error instantiating element");
            }
        }

        @SuppressWarnings("unchecked")
        private Object child(ElementParameter parameter, ConfigPath dataPath, ConfigPath defaultingPath,
                ConfigNode defaultingData, ElementContext context, ConfigPath absoluteChildDataPath,
                DependencyProvider dependencyProvider, boolean isContainer) {
            final ConfigElement childData;

            try {
                if (absoluteChildDataPath.startsWith(defaultingPath)) {
                    childData = defaultingData.atOrThrow(dataPath.relativize(absoluteChildDataPath).toAbsolute());
                }
                else {
                    childData = context.root().atOrThrow(absoluteChildDataPath);
                }

                if (childData.isNode()) {
                    //simple case: child is a node
                    return context.provide(absoluteChildDataPath, childData.asNode(), dependencyProvider, false);
                }
            }
            catch (ConfigProcessException exception) {
                throw elementException(exception, factoryConstructor.getDeclaringClass(), absoluteChildDataPath,
                        "Failure to follow path");
            }

            if (childData.isList()) {
                final ConfigList childList = childData.asList();
                if (isContainer) {
                    final Collection<Object> listOutput;
                    try {
                        listOutput = (Collection<Object>) containerCreator
                                .createContainer(parameter.parameter.getType(), childList.size());
                    }
                    catch (ElementException exception) {
                        exception.setElementClass(factoryConstructor.getDeclaringClass());
                        exception.setConfigPath(absoluteChildDataPath);
                        throw exception;
                    }

                    for (int i = 0; i < childList.size(); i++) {
                        final ConfigPath absoluteConfigPath = absoluteChildDataPath.append(Integer.toString(i));
                        listOutput.add(child(parameter, dataPath, defaultingPath, defaultingData, context,
                                absoluteConfigPath, dependencyProvider, false));
                    }

                    return listOutput;
                }

                if (childList.isEmpty()) {
                    throw elementException(factoryConstructor.getDeclaringClass(), absoluteChildDataPath,
                            "Empty list provided for a non-container child");
                }

                return child(parameter, dataPath, defaultingPath, defaultingData, context,
                        absoluteChildDataPath.append("0"), dependencyProvider, false);
            }

            if (childData.isString()) {
                final ConfigPath childRedirect = absoluteChildDataPath.resolveSibling(childData.asString());
                if (!childRedirect.isAbsolute()) {
                    throw elementException(factoryConstructor.getDeclaringClass(), absoluteChildDataPath,
                            "Child redirect points outside of root");
                }

                return child(parameter, dataPath, defaultingPath, defaultingData, context, childRedirect,
                        dependencyProvider, isContainer);
            }

            throw elementException(factoryConstructor.getDeclaringClass(), absoluteChildDataPath,
                    "Unexpected element in child hierarchy " + childData);
        }
    }

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser         the {@link KeyParser} implementation used to interpret strings as keys
     * @param collectionCreator the {@link ContainerCreator} used to reflectively create collection instances when
     *                          necessary, when requiring multiple element dependencies
     * @param processorSource   the {@link MappingProcessorSource} used to create {@link ConfigProcessor}
     *                          implementations on-demand for data classes.
     *                          {@link MappingProcessorSource.Builder#ignoringLengths()} should be used to avoid issues
     *                          when deserializing composite elements
     */
    public BasicFactoryResolver(final @NotNull KeyParser keyParser,
            final @NotNull ContainerCreator collectionCreator, final @NotNull MappingProcessorSource processorSource) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.containerCreator = Objects.requireNonNull(collectionCreator);
        this.processorSource = Objects.requireNonNull(processorSource);
    }

    @Override
    public @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass,
            final @NotNull Mutable<ConfigProcessor<?>> processor) {
        final Constructor<?>[] constructors = elementClass.getConstructors();
        final Method[] methods = elementClass.getDeclaredMethods();

        final SearchResult<Constructor<?>, FactoryMethod> factoryConstructor = findSingleWithAnnotation(constructors,
                FactoryMethod.class, elementClass);
        final SearchResult<Method, FactoryMethod> factoryMethod = findSingleWithAnnotation(methods, FactoryMethod.class,
                elementClass);
        if (factoryConstructor != null && factoryMethod != null) {
            throw duplicateAnnotation(FactoryMethod.class, elementClass);
        }

        if (factoryConstructor == null && factoryMethod == null) {
            throw elementException("Missing factory method or constructor");
        }

        if (factoryMethod != null) {
            final Method method = factoryMethod.first;
            validateModifiersPresent(method, "Factory method must be `public static`", Modifier.PUBLIC, Modifier.STATIC);
            validateReturnType(method, ElementFactory.class, "Factory method must return an ElementFactory");

            final Token<?> returnType = Token.ofType(method.getGenericReturnType());
            if (!returnType.isParameterized()) {
                throw elementException(elementClass, "Factory method return type must be parameterized");
            }

            final ElementFactory<?, ?> factory;
            try {
                factory = (ElementFactory<?, ?>)method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw elementException(e, elementClass, "Error calling factory method");
            }

            if (factory == null) {
                throw elementException(elementClass, "Factory method returned null");
            }

            if (processor.getValue() == null) {
                processor.setValue(processorSource.processorFor(returnType.actualTypeParameters()[0].rawType()));
            }

            return factory;
        }

        final ElementParameter[] parameters = extractParameters(factoryConstructor.first);

        final ElementParameter dataParameter = extractDataParameter(parameters, elementClass);

        final Class<?> dataClass = identifyDataClass(elementClass, dataParameter);

        if (dataClass != null && processor.getValue() == null) {
            processor.setValue(processorSource.processorFor(dataClass));
        }

        final ConfigNode elementClassDefaults = extractDefaults(elementClass);
        final ConfigNode dataClassDefaults = dataClass != null ? extractDefaults(dataClass) : ConfigNode.EMPTY;

        final ConfigNode combinedDefaults = mergeDefaults(elementClassDefaults, dataClassDefaults);

        return new GenericFactory(factoryConstructor.first, parameters, dataClass != null, combinedDefaults);
    }

    private static ConfigNode mergeDefaults(ConfigNode highPriority, ConfigNode lowPriority) {
        if (highPriority.isEmpty()) {
            return lowPriority.isEmpty() ? ConfigNode.EMPTY : lowPriority;
        }

        if (lowPriority.isEmpty()) {
            return highPriority;
        }

        final ConfigNode finalNode = new LinkedConfigNode(highPriority.size() + lowPriority.size());
        finalNode.putAll(highPriority);
        finalNode.putAll(lowPriority);
        return finalNode.immutableCopy();
    }

    private static ConfigNode extractDefaults(Class<?> cls) {
        final Default classDefaultAnnotation = cls.getAnnotation(Default.class);
        if (classDefaultAnnotation == null) {
            return ConfigNode.EMPTY;
        }

        final ConfigElement element = ConfigElement.of(classDefaultAnnotation.value());
        if (!element.isNode()) {
            throw elementException(cls, "Default annotation must be a node");
        }

        final ConfigNode node = element.asNode();
        return node.isEmpty() ? ConfigNode.EMPTY : node;
    }

    private static ElementParameter extractDataParameter(ElementParameter[] parameters, Class<?> cls) {
        ElementParameter dataParameter = null;
        Map<String, ElementParameter> childMap = null;
        for (ElementParameter parameter : parameters) {
            if (parameter.type == ParameterType.DATA) {
                if (dataParameter != null) {
                    throw elementException(cls, "Multiple data parameters");
                }

                dataParameter = parameter;
            }
            else if (parameter.type == ParameterType.CHILD) {
                Child child = parameter.parameter.getAnnotation(Child.class);

                String childKeyString = child.value();
                if (childMap == null) {
                    childMap = new HashMap<>(parameters.length);
                }

                if (childMap.putIfAbsent(childKeyString, parameter) != null) {
                    throw elementException(cls, "Duplicate @Child key " + childKeyString);
                }
            }
        }

        return dataParameter;
    }

    private static Class<?> identifyDataClass(final Class<?> elementClass, final ElementParameter dataParameter) {
        if (dataParameter != null) {
            return dataParameter.parameter.getType();
        }

        Class<?> dataClass = null;
        for (Class<?> declaredClass : elementClass.getDeclaredClasses()) {
            if (declaredClass.isAnnotationPresent(DataObject.class)) {
                if (dataClass != null) {
                    throw elementException(elementClass, "Multiple @DataObject member classes");
                }

                dataClass = declaredClass;
            }
        }

        return dataClass;
    }

    private enum ParameterType {
        DATA,
        DEPENDENCY,
        CHILD
    }

    private record ElementParameter(Parameter parameter, ParameterType type, DependencyProvider.TypeKey<?> typeKey,
            ConfigPath childPath) {}

    private record SearchResult<T, V>(T first, V second) {}

    private ElementParameter[] extractParameters(final Executable executable) {
        final Parameter[] parameters = executable.getParameters();
        final ElementParameter[] elementParameters = new ElementParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            final boolean isData = isData(parameter);
            final Child childAnnotation = parameter.getDeclaredAnnotation(Child.class);

            final Depend classDepend = parameter.getType().getDeclaredAnnotation(Depend.class);
            final Depend parameterDepend = parameter.getDeclaredAnnotation(Depend.class);

            if (isData && childAnnotation != null) {
                throw elementException(executable.getDeclaringClass(), "Parameter " + parameter +
                        " is both element data and a composite element");
            }

            final ParameterType type;
            final DependencyProvider.TypeKey<?> typeKey;
            final ConfigPath childPath;
            if ((!isData && childAnnotation == null) || classDepend != null || parameterDepend != null) {
                type = ParameterType.DEPENDENCY;
                typeKey = DependencyProvider.key(Token.ofType(parameter.getParameterizedType()),
                        (classDepend == null && parameterDepend == null) ? null :
                                determineKey(Objects.requireNonNullElse(parameterDepend, classDepend),
                                        executable.getDeclaringClass()));
                childPath = null;
            }
            else if (isData) {
                type = ParameterType.DATA;
                typeKey = null;
                childPath = null;
            }
            else {
                type = ParameterType.CHILD;
                typeKey = null;
                childPath = ConfigPath.of(childAnnotation.value());
            }

            elementParameters[i] = new ElementParameter(parameter, type, typeKey, childPath == null ? null :
                    (childPath.isAbsolute() ? ConfigPath.EMPTY.relativize(childPath) : childPath));
        }

        return elementParameters;
    }

    private Key determineKey(Depend depend, Class<?> cls) {
        @Subst(Constants.NAMESPACE_OR_KEY)
        final String value = depend.value();

        if (value.equals(Constants.DEFAULT)) {
            return null;
        }
        else if (!keyParser.isValidKey(value)) {
            throw elementException(cls, "Invalid parameter key " + value);
        }

        return Key.key(value);
    }

    private static boolean isData(final Parameter parameter) {
        return parameter.getType().isAnnotationPresent(DataObject.class) ||
                parameter.isAnnotationPresent(DataObject.class);
    }

    private static ElementException duplicateAnnotation(final Class<?> duplicateAnnotation,
            final Class<?> elementClass) {
        return elementException(elementClass, "More than one annotation of type " + duplicateAnnotation);
    }


    @SuppressWarnings("SameParameterValue")
    private static <T extends AnnotatedElement, V extends Annotation> SearchResult<T, V>
    findSingleWithAnnotation(final T[] elements, final Class<? extends V> annotation, final Class<?> elementClass) {
        T actualElement = null;
        V actualAnnotation = null;
        for (T element : elements) {
            final V thisAnnotation = element.getAnnotation(annotation);
            if (thisAnnotation == null) {
                continue;
            }

            if (actualAnnotation != null) {
                throw duplicateAnnotation(annotation, elementClass);
            }

            actualElement = element;
            actualAnnotation = thisAnnotation;
        }

        return actualElement == null ? null : new SearchResult<>(actualElement, actualAnnotation);
    }
}