package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Map}-based Registry implementation. Uses a {@link ConcurrentHashMap} to ensure synchronous access.
 *
 * @param <TRegistrant> the kind of object stored as a registrant
 */
public class HashRegistry<TRegistrant> implements Registry<TRegistrant> {
    private final Map<Key, TRegistrant> map;

    /**
     * Creates a new HashRegistry implementation given initial size and load factor.
     *
     * @param initialSize the initial size of the underlying hashmap
     * @param loadFactor  the load factor of the underlying hashmap
     */
    public HashRegistry(final int initialSize, final float loadFactor) {
        this.map = new ConcurrentHashMap<>(initialSize, loadFactor);
    }

    /**
     * Creates a new HashRegistry implementation given initial size and using the default load factor.
     *
     * @param initialSize the initial size of the underlying hashmap
     */
    public HashRegistry(final int initialSize) {
        this(initialSize, 0.75F);
    }

    /**
     * Creates a new HashRegistry with the default initial size (16) and load factor (0.75).
     */
    public HashRegistry() {
        this(16, 0.75F);
    }

    @Override
    public void register(final @NotNull Key key, final @NotNull TRegistrant registrant) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(registrant);
        if (map.putIfAbsent(key, registrant) != null) {
            throw new IllegalArgumentException("a registrant already exists under key " + key);
        }
    }

    @Override
    public @NotNull TRegistrant lookup(final @NotNull Key key) {
        Objects.requireNonNull(key);
        final TRegistrant registrant = map.get(key);
        if (registrant == null) {
            throw new NoSuchElementException("no registrant under key " + key);
        }

        return registrant;
    }

    @Override
    public boolean contains(final @NotNull Key key) {
        Objects.requireNonNull(key);
        return map.containsKey(key);
    }

    @Override
    public TRegistrant registerIfAbsent(final @NotNull Key key, final @NotNull TRegistrant registrant) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(registrant);
        return map.putIfAbsent(key, registrant);
    }
}
