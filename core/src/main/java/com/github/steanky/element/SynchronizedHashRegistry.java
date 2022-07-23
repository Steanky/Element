package com.github.steanky.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class SynchronizedHashRegistry<TRegistrant> implements Registry<TRegistrant> {
    private final Map<Key, TRegistrant> map;
    private final ReadWriteLock readWriteLock;

    public SynchronizedHashRegistry(final int initialSize) {
        this.map = new HashMap<>(initialSize);
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public void register(final @NotNull Key key, final @NotNull TRegistrant registrant) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(registrant);
        lock(readWriteLock.writeLock(), () -> map.put(key, registrant));
    }

    @Override
    public @NotNull TRegistrant lookup(final @NotNull Key key) {
        Objects.requireNonNull(key);
        return lock(readWriteLock.readLock(), () -> map.get(key));
    }

    @Override
    public boolean contains(final @NotNull Key key) {
        Objects.requireNonNull(key);
        return lock(readWriteLock.readLock(), () -> map.containsKey(key));
    }

    private static <T> T lock(Lock lock, Supplier<T> function) {
        try {
            lock.lock();
            return function.get();
        }
        finally {
            lock.unlock();
        }
    }
}
