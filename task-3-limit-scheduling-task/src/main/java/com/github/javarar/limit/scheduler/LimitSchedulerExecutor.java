package com.github.javarar.limit.scheduler;

import java.util.concurrent.TimeUnit;

public interface LimitSchedulerExecutor {

    void runNTimesAtFixedRate(Runnable task, int retries, long period, TimeUnit unit);

    void runNTimesWithFixedDelay(Runnable task, int retries, long period, TimeUnit unit);

}
