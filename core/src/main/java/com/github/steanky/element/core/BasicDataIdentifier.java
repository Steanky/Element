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

public class BasicDataIdentifier implements DataIdentifier {
    private final KeyParser keyParser;

    public BasicDataIdentifier(final @NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull Key identifyKey(final @NotNull Object data) {
        if (data instanceof Keyed keyed) {
            return keyed.key();
        }
        else if(data instanceof Key key) {
            return key;
        }
        else if(data instanceof @Subst(Constants.NAMESPACE_OR_KEY) String keyString) {
            return keyParser.parseKey(keyString);
        }

        final Class<?> dataClass = data.getClass();
        final ElementData elementData = dataClass.getDeclaredAnnotation(ElementData.class);
        if(elementData == null) {
            throw new ElementException("Data class " + dataClass + " must supply an ElementData annotation");
        }

        if(!elementData.value().equals(ElementData.DEFAULT_VALUE)) {
            @Subst(Constants.NAMESPACE_OR_KEY)
            final String value = elementData.value();
            return keyParser.parseKey(value);
        }

        final Class<?> host = dataClass.getNestHost();
        if(host != dataClass) { //getNestHost defined to return 'this' when class is not nested
            final ElementModel model = host.getDeclaredAnnotation(ElementModel.class);
            if(model != null) {
                @Subst(Constants.NAMESPACE_OR_KEY)
                final String value = model.value();
                return keyParser.parseKey(value);
            }
        }

        throw new ElementException("Unable to identify data object of type " + dataClass);
    }
}
