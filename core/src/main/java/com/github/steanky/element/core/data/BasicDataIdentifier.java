package com.github.steanky.element.core.data;

import com.github.steanky.element.core.annotation.Data;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.element.ElementTypeIdentifier;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link DataIdentifier}. Can correctly identify all subclasses of {@link Keyed}, {@link Key}
 * objects themselves, {@link String} objects (if they can be parsed into a valid key), objects whose type has the
 * {@link Data} annotation which itself provides a valid key, or types whose classes which are nested in an
 * {@link Model} class which provides a valid key, and are also annotated with ElementData.
 */
public class BasicDataIdentifier implements DataIdentifier {
    private final KeyParser keyParser;
    private final ElementTypeIdentifier typeIdentifier;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser      the parser used to parse keys from strings, when necessary
     * @param typeIdentifier the {@link ElementTypeIdentifier} used to extract keys from element objects
     */
    public BasicDataIdentifier(final @NotNull KeyParser keyParser,
            final @NotNull ElementTypeIdentifier typeIdentifier) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.typeIdentifier = Objects.requireNonNull(typeIdentifier);
    }

    @Override
    public @NotNull Key identifyKey(final @NotNull Object data) {
        if (data instanceof Keyed keyed) {
            return keyed.key();
        } else if (data instanceof Key key) { //necessary until adventure 4.12.0
            return key;
        } else if (data instanceof @Subst(Constants.NAMESPACE_OR_KEY)String keyString) {
            return keyParser.parseKey(keyString);
        }

        final Class<?> dataClass = data.getClass();
        final Data elementData = dataClass.getDeclaredAnnotation(Data.class);
        if (elementData == null) {
            throw elementException(dataClass, "must supply an ElementData annotation");
        }

        if (!elementData.value().equals(Data.DEFAULT_VALUE)) {
            @Subst(Constants.NAMESPACE_OR_KEY) final String value = elementData.value();
            return keyParser.parseKey(value);
        }

        final Class<?> declaring = dataClass.getDeclaringClass();
        if (declaring != null) {
            return typeIdentifier.identify(declaring);
        }

        throw elementException(dataClass, "must specify a key");
    }
}
