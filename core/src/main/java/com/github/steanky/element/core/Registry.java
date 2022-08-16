package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 * Represents a store of some kind of persistent value associated with a {@link Key}.
 *
 * @param <TRegistrant> the type of object registered by this class
 * @implSpec Registry implementations must:<br>
 * <ul>
 *     <li>Safely support concurrent lookups (reads) and registrations (writes)</li>
 *     <li>Be "append-only" (objects may be registered but not unregistered)</li>
 *     <li>Not support null keys or values</li>
 *     <li>Throw exceptions when looking up registrants that don't exist, rather than returning null</li>
 * </ul>
 */
public interface Registry<TRegistrant> {
    /**
     * Registers a key-registrant pair. If a registrant is already associated with the key, an exception is thrown.
     *
     * @param key        the key associated with the registrant
     * @param registrant the registrant to associate with the key
     * @throws IllegalArgumentException if a registrant is already associated with the key
     */
    void register(final @NotNull Key key, final @NotNull TRegistrant registrant);

    /**
     * Obtains the registrant associated with the given key. If none exists, an exception is thrown.
     *
     * @param key the key associated with the registrant
     * @return the registrant associated with the key
     * @throws NoSuchElementException if no registrant exists under the given key
     */
    @NotNull TRegistrant lookup(final @NotNull Key key);

    /**
     * Determines if this Registry contains a registrant associated with the given key.
     *
     * @param key the key to test for
     * @return true if a registrant exists under this key, false otherwise
     */
    boolean contains(final @NotNull Key key);

    /**
     * Atomically checks for registration and registers the given key-registrant pair. If a registrant is already
     * associated with the given key, returns the old registrant (the backing map is unchanged). Otherwise, returns
     * null.
     *
     * @param key        the key to associate with the registrant
     * @param registrant the registrant associated with the key
     * @return the old registrant if one is already registered under the given key, null otherwise
     */
    TRegistrant registerIfAbsent(final @NotNull Key key, final @NotNull TRegistrant registrant);
}
