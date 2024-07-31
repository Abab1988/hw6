package com.github.javarar.rejected.task;

import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class RetryJob implements Job {

    public static final String TASK_KEY = "task";
    public static final String EXECUTOR_KEY = "executor";
    public static final String TASK_ID_KEY = "id";

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String taskId = dataMap.getString(TASK_ID_KEY);
        Runnable r = (Runnable) dataMap.get(TASK_KEY);
        DelayedOnRejectedThreadExecutor executorService = (DelayedOnRejectedThreadExecutor) dataMap.get(EXECUTOR_KEY);
        System.out.println("Повторная попытка публикации задачи - " + taskId);
        executorService.execute(r, context.getJobDetail().getKey());
    }

}