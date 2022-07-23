package com.github.steanky.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a store of some kind of persistent value.
 * @param <TRegistrant>
 */
public interface Registry<TRegistrant> {
    void register(final @NotNull Key key, final @NotNull TRegistrant registrant);

    @NotNull TRegistrant lookup(final @NotNull Key key);

    boolean contains(final @NotNull Key key);


}
