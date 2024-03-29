package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link ElementTypeIdentifier}.
 */
public class BasicElementTypeIdentifier implements ElementTypeIdentifier {
    private final KeyParser keyParser;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the {@link KeyParser} implementation used to convert strings to keys
     */
    public BasicElementTypeIdentifier(final @NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull Key identify(final @NotNull Class<?> elementType) {
        final Model model = elementType.getDeclaredAnnotation(Model.class);
        if (model != null) {
            @Subst(Constants.NAMESPACE_OR_KEY) final String value = model.value();
            return keyParser.parseKey(value);
        }

        throw elementException(elementType, "No @Model annotation");
    }
}
