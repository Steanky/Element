package com.github.steanky.element.core.element;

import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.*;

public class BasicElementTypeIdentifier implements ElementTypeIdentifier {
    private final KeyParser keyParser;

    public BasicElementTypeIdentifier(@NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull Key identify(@NotNull Class<?> elementType) {
        final ElementModel model = elementType.getDeclaredAnnotation(ElementModel.class);
        if (model != null) {
            @Subst(Constants.NAMESPACE_OR_KEY) final String value = model.value();
            return keyParser.parseKey(value);
        }

        throw formatException(elementType, "no ElementModel annotation");
    }
}
