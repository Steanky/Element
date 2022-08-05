package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic implementation of {@link DataIdentifier}. Can correctly identify all subclasses of {@link Keyed}, {@link Key}
 * objects themselves, {@link String} objects (if they can be parsed into a valid key), objects whose type has the
 * {@link ElementData} annotation which itself provides a valid key, or types whose classes which are nested in an
 * {@link ElementModel} class which provides a valid key, and are also annotated with ElementData.
 */
public class BasicDataIdentifier implements DataIdentifier {
    private final KeyParser keyParser;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the parser used to parse keys from strings, when necessary
     */
    public BasicDataIdentifier(final @NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
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
        final ElementData elementData = dataClass.getDeclaredAnnotation(ElementData.class);
        if (elementData == null) {
            throw new ElementException("Data class " + dataClass + " must supply an ElementData annotation");
        }

        if (!elementData.value().equals(ElementData.DEFAULT_VALUE)) {
            @Subst(Constants.NAMESPACE_OR_KEY) final String value = elementData.value();
            return keyParser.parseKey(value);
        }

        final Class<?> declaring = dataClass.getDeclaringClass();
        if (declaring != null) {
            final ElementModel model = declaring.getDeclaredAnnotation(ElementModel.class);
            if (model != null) {
                @Subst(Constants.NAMESPACE_OR_KEY) final String value = model.value();
                return keyParser.parseKey(value);
            }
        }

        throw new ElementException("ElementData annotation must specify a key " + dataClass);
    }
}
