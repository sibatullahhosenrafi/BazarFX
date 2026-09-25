package com.bazarfx.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for any component that does its work on its own background
 * thread pool and hands the result back to the JavaFX Application Thread.
 * A concrete subclass only has to say how many worker threads it wants,
 * what its pool's threads should be named, and what it's currently doing
 * ({@link #describeWork()}); this class owns the pool's lifecycle
 * (creation in the constructor, teardown in {@link #shutdown()}).
 *
 * {@link ReportGenerator} is the concrete subclass used by the Dashboard's
 * "Generate Report" button.
 */
public abstract class BackgroundTaskRunner {

    protected final ExecutorService pool;

    protected BackgroundTaskRunner(int poolSize, String threadNamePrefix) {
        this.pool = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, threadNamePrefix);
            t.setDaemon(true);
            return t;
        });
    }

    /** A short, human-readable description of what this runner computes - used for logging/UI. */
    public abstract String describeWork();

    public void shutdown() {
        pool.shutdown();
    }
}
