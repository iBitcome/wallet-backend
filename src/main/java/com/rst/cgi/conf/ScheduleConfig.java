package com.rst.cgi.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

/**
 * @author hujia
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {
    public static final int MAX_WORK_THREAD_COUNT = 20;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(MAX_WORK_THREAD_COUNT));
    }
}