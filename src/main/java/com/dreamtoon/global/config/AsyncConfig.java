package com.dreamtoon.global.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 비동기 처리 설정 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

        /** Dream AI 처리를 위한 전용 ThreadPoolTaskExecutor */
        @Bean(name = "dreamProcessingExecutor")
        public Executor dreamProcessingExecutor() {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setCorePoolSize(2);
                executor.setMaxPoolSize(5);
                executor.setQueueCapacity(100);
                executor.setThreadNamePrefix("dream-async-");
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                executor.setWaitForTasksToCompleteOnShutdown(true);
                executor.setAwaitTerminationSeconds(60);
                executor.initialize();

                log.info("Initialized dreamProcessingExecutor with corePoolSize=2, maxPoolSize=5");
                return executor;
        }

        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                return new CustomAsyncExceptionHandler();
        }

        /** 비동기 작업 중 발생한 예외 처리 */
        @Slf4j
        static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
                @Override
                public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                        log.error(
                                        "Async method threw exception - method: {}, params: {}",
                                        method.getName(),
                                        Arrays.toString(params),
                                        ex);
                }
        }
}
