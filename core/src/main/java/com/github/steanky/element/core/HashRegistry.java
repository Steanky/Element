package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link Map}-based Registry implementation. Uses a backing immutable map combined with copy-on-write semantics to
 * achieve high read performance at the cost of write performance.
 *
 * @param <TRegistrant> the kind of object stored in this registry
 */
public class HashRegistry<TRegistrant> implements Registry<TRegistrant> {
    private volatile Map<Key, TRegistrant> map;

    private final Object sync;

    /**
     * Creates a new instance of this class. The initial backing map will be empty with no capacity.
     */
    public HashRegistry() {
        this.map = Map.of();
        this.sync = new Object();
    }

    @SuppressWarnings("unchecked")
    private void register0(Key key, TRegistrant registrant) {
        if (map.isEmpty()) {
            map = Map.of(key, registrant);
            return;
        }

        Map.Entry<Key, TRegistrant>[] oldEntries = map.entrySet().toArray(Map.Entry[]::new);
        Map.Entry<Key, TRegistrant>[] newEntries = new Map.Entry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);

        newEntries[oldEntries.length] = Map.entry(key, registrant);
        map = Map.ofEntries(newEntries);
    }

    @Override
    public void register(final @NotNull Key key, final @NotNull TRegistrant registrant) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(registrant);

        synchronized (sync) {
            register0(key, registrant);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerBulk(final @NotNull Collection<? extends Map.Entry<? extends Key, ? extends TRegistrant>> entries) {
        Objects.requireNonNull(entries);
        final Map.Entry<Key, TRegistrant>[] additionalEntries = entries.toArray(Map.Entry[]::new);

        if (additionalEntries.length == 0) {
            return;
        }

        synchronized (sync) {
            final Map.Entry<Key, TRegistrant>[] oldEntries = map.entrySet().toArray(Map.Entry[]::new);
            final Map.Entry<Key, TRegistrant>[] newEntries = new Map.Entry[additionalEntries.length + map.size()];

            System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
            System.arraycopy(additionalEntries, 0, newEntries, oldEntries.length, additionalEntries.length);

            map = Map.ofEntries(newEntries);
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

        //first, try to grab the registrant without locking
        final Map<Key, TRegistrant> map = this.map;
        final TRegistrant firstTry = map.get(key);
        if (firstTry != null) {
            return firstTry;
        }

        synchronized (sync) {
            final Map<Key, TRegistrant> newMap = this.map;

            //if the map field changed, it means another thread wrote something, read again
            if (newMap != map) {
                final TRegistrant secondTry = map.get(key);
                if (secondTry != null) {
                    return secondTry;
                }
            }

            register0(key, registrant);
            return null;
        }
    }
}
