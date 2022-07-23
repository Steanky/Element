package com.github.steanky.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A {@link HashMap}-based Registry implementation.
 * @param <TRegistrant> the kind of object stored as a registrant
 */
public class HashRegistry<TRegistrant> implements Registry<TRegistrant> {
    private final Map<Key, TRegistrant> map;
    private final ReadWriteLock readWriteLock;

    /**
     * Creates a new HashRegistry implementation given initial size and load factor.
     *
     * @param initialSize the initial size of the underlying hashmap
     * @param loadFactor the load factor of the underlying hashmap
     */
    public HashRegistry(final int initialSize, final float loadFactor) {
        this.map = new HashMap<>(initialSize, loadFactor);
        this.readWriteLock = new ReentrantReadWriteLock();
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
        lock(readWriteLock.writeLock(), () -> {
            if(map.putIfAbsent(key, registrant) != null) {
                throw new IllegalArgumentException("A registrant already exists under key " + key);
            }

            return null;
        });
    }

    @Override
    public @NotNull TRegistrant lookup(final @NotNull Key key) {
        Objects.requireNonNull(key);
        return lock(readWriteLock.readLock(), () -> {
            TRegistrant registrant = map.get(key);
            if(registrant == null) {
                throw new NoSuchElementException("No registrant under key " + key);
            }

            return registrant;
        });
    }

    @Override
    public boolean contains(final @NotNull Key key) {
        Objects.requireNonNull(key);
        return lock(readWriteLock.readLock(), () -> map.containsKey(key));
    }

    @Override
    public TRegistrant registerIfAbsent(@NotNull Key key, @NotNull TRegistrant registrant) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(registrant);
        return lock(readWriteLock.writeLock(), () -> map.putIfAbsent(key, registrant));
    }

    private static <T> T lock(Lock lock, Supplier<? extends T> function) {
        try {
            lock.lock();
            return function.get();
        }
        finally {
            lock.unlock();
        }
    }
}
