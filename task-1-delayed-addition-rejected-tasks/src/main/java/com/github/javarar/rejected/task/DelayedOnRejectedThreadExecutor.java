package com.github.javarar.rejected.task;

import lombok.Getter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Getter
public final class DelayedOnRejectedThreadExecutor implements Executor {

    private Executor self = this;

    private final ExecutorService executorService;

    // Мапа отклонённых задач, необходимо для того, чтобы создавать RetryJob только при первом отклонении задачи от обработки
    // А также дает возможность их удалять
    private final Map<Runnable, JobKey> abortedTasks;

    private final boolean enableRetries;

    private final int retryCount;

    private final int corePoolSize;

    private final int maximumPoolSize;

    private final long keepAliveTime;

    private final TimeUnit timeUnit;

    private final int queueSize;

    private final long delay;

    // Множество задач, принятых в обработку
    private final Set<Runnable> inWorkTasks;

    private final Scheduler scheduler;

    public DelayedOnRejectedThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit,
                                           int queueSize, boolean enableRetries, int retryCount, long interval) throws SchedulerException {
        this.retryCount = retryCount;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.enableRetries = enableRetries;
        this.delay = interval;
        this.abortedTasks = new ConcurrentHashMap<>();
        this.inWorkTasks = ConcurrentHashMap.newKeySet();

        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        this.scheduler = schedulerFactory.getScheduler();
        scheduler.start();

        this.executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit,
                new ArrayBlockingQueue<>(queueSize),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        // Удаляем задачу из множества, находящихся в работе
                        // Если задача успешно добавится в очередь, то она не удалится из множества
                        inWorkTasks.remove(r);
                        // Нельзя назначить менее 1 попытки retry
                        if (retryCount < 1) throw new IllegalArgumentException("Недопустимое количество попыток");
                        System.out.println("Очередь заполнена. Новая задача отклонена");
                        try {
                            // Если задача повторно откланена - не создаём новую Job!
                            if (enableRetries && !abortedTasks.containsKey(r)) {
                                String id = UUID.randomUUID().toString();
                                JobDetail job = JobBuilder.newJob(RetryJob.class)
                                        .withIdentity("task-" + id, "group1")
                                        .build();
                                JobDataMap jobDataMap = job.getJobDataMap();
                                jobDataMap.put(RetryJob.TASK_KEY, r);
                                jobDataMap.put(RetryJob.EXECUTOR_KEY, self);
                                jobDataMap.put(RetryJob.TASK_ID_KEY, id);
                                Trigger simpleTrigger = TriggerBuilder.newTrigger().withIdentity("trigger-" + id, "group1")
                                        .withSchedule(simpleSchedule()
                                                .withIntervalInMilliseconds(interval)
                                                .withRepeatCount(retryCount)
                                        )
                                        .forJob(job)
                                        .startNow()
                                        .build();
                                abortedTasks.put(r, job.getKey());
                                scheduler.scheduleJob(job, simpleTrigger);
                            }
                        } catch(SchedulerException e) {
                            System.out.println("SchedulerException: " + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }

    // Метод вызывается из RetryJob, не предназначен для использования клиентами
    protected void execute(Runnable command, JobKey key) throws SchedulerException {
        // Если задача уже есть во множестве принятых в работу, то не берём повторно
        // Защита от дубликатов, когда пытаемся повторно взять в работу задачу, которая была обработана при предыдущей попытке
        // Удаляем созданную Job, чтобы прекратить retry
        if (!inWorkTasks.contains(command)) {
            inWorkTasks.add(command);
            executorService.execute(command);
        } else {
            // Если задача уже попала в обработку при предыдущей попытке отправки
            // то удаляем Job, чтобы прекратить retries
            scheduler.deleteJob(abortedTasks.get(command));
        }
    }

}
