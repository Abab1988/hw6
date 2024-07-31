package com.github.javarar.limit.scheduler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

public class LimitSchedulerThreadExecutorTest {

    // Переменная-счетчик количества запусков задачи
    private int num = 0;

    @BeforeEach
    private void refresh() {
        this.num = 0;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 10})
    void runNTimesWithFixedDelayTest(int retries) throws InterruptedException {
        long delay = 100;
        LimitSchedulerExecutor ex = new LimitSchedulerThreadExecutor(3);
        ex.runNTimesWithFixedDelay(
                () -> System.out.println("Выполнение задачи. Запуск #" + ++num),
                retries,
                delay,
                TimeUnit.MILLISECONDS
        );
        Thread.sleep(delay * (retries + 1));
        Assertions.assertEquals(retries, num);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 6, 8, 11})
    void runNTimesAtFixedRateTest(int retries) throws InterruptedException {
        long delay = 100;
        LimitSchedulerExecutor ex = new LimitSchedulerThreadExecutor(3);
        ex.runNTimesAtFixedRate(
                () -> System.out.println("Выполнение задачи. Запуск #" + ++num),
                retries,
                delay,
                TimeUnit.MILLISECONDS
        );
        Thread.sleep(delay * (retries + 1));
        Assertions.assertEquals(retries, num);
    }

}
