package com.github.javarar.rejected.task;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DelayedOnRejectedThreadExecutorTest {

    /*
        В результате теста должны бать выполнены все задачи (6 сразу взяты в работу, 4 - полле повторных попыток)
        Взятие дубликатов задач в работу недопустимо
    */
    @Test
    void test() throws SchedulerException, InterruptedException {
        Executor ex = new DelayedOnRejectedThreadExecutor(1, 3, 5, TimeUnit.SECONDS, 3, true, 3, 1000);
        IntStream.range(0, 10).forEach(val -> ex.execute(
                () -> {
                    try {
                        System.out.println("Задача-" + val + " принята в обработку");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
        //Ждем когда все отработает
        Thread.sleep(5000);
    }
}
