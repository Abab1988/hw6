package com.github.javarar.limit.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitSchedulerThreadExecutor implements LimitSchedulerExecutor {

    private final ScheduledExecutorService executor;

    public LimitSchedulerThreadExecutor(int threads) {
        this.executor = Executors.newScheduledThreadPool(threads);
    }

    public void runNTimesAtFixedRate(Runnable task, int retries, long period, TimeUnit unit) {
        checkRetries(retries);
        new FixedExecutionRunnable(task, retries).runNTimesAtFixedRate(this.executor, period, unit);
    }

    public void runNTimesWithFixedDelay(Runnable task, int retries, long delay, TimeUnit unit) {
        checkRetries(retries);
        new FixedExecutionRunnable(task, retries).runNTimesWithFixedDelay(this.executor, delay, unit);
    }
    private void checkRetries(int retries) {
        if (retries < 1) throw new IllegalArgumentException("Задача не может запускаться менее 1 раза");
    }

    private static class FixedExecutionRunnable implements Runnable {
        private final AtomicInteger runCount = new AtomicInteger();
        private final Runnable delegate;
        private volatile ScheduledFuture<?> self;
        private final int maxRunCount;

        public FixedExecutionRunnable(Runnable delegate, int maxRunCount) {
            this.delegate = delegate;
            this.maxRunCount = maxRunCount;
        }

        @Override
        public void run() {
            delegate.run();
            if(runCount.incrementAndGet() == maxRunCount) {
                boolean interrupted = false;
                try {
                    while(self == null) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                    self.cancel(false);
                } finally {
                    if(interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        }

        public void runNTimesAtFixedRate(ScheduledExecutorService executor, long period, TimeUnit unit) {
            self = executor.scheduleAtFixedRate(this, 0, period, unit);
        }
        public void runNTimesWithFixedDelay(ScheduledExecutorService executor, long period, TimeUnit unit) {
            self = executor.scheduleWithFixedDelay(this, 0, period, unit);
        }

    }

}
