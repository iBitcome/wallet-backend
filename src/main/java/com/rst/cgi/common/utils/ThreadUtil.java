package com.rst.cgi.common.utils;

import java.util.Arrays;
import java.util.concurrent.*;

/**
 * @author hujia
 */
public class ThreadUtil {
    private static ExecutorService fixExecutor;
    /**线上环境调整到100*/
    private static final int EXECUTOR_COUNT = 100;
    private static BlockingQueue<Runnable> blockingQueue;

    private static void initExecutor() {
        if (fixExecutor != null) {
            fixExecutor.shutdownNow();
        }

        blockingQueue = new LinkedBlockingQueue<>();

        fixExecutor = new ThreadPoolExecutor(EXECUTOR_COUNT, EXECUTOR_COUNT,
                0L, TimeUnit.MILLISECONDS, blockingQueue);
    }

    /**
     * 在其他线程异步执行任务，不阻塞当前线程
     * @param runnable
     * @return
     */
    public static Future runOnOtherThread(Runnable runnable) {
        if (fixExecutor == null) {
            initExecutor();
        }

        return fixExecutor.submit(runnable);
    }

    /**
     * 另开一个线程异步执行或者线程不足时直接在当前线程同步执行
     * 可能阻塞当前线程
     * @param runnable
     * @return
     */
    public static Future runOnCurrentOrOtherThread(Runnable runnable) {
        if (fixExecutor == null) {
            initExecutor();
        }

        if (blockingQueue.size() > EXECUTOR_COUNT) {
            FutureTask future = new FutureTask(runnable, null);
            future.run();
            return future;
        }

        return fixExecutor.submit(runnable);
    }

    /**
     * 等待所有任务执行完成
     * @param mills 最多等待多少毫秒
     * @param cancelTimeoutFuture 超时的任务是否取消执行
     * @param futures
     */
    public static void waitFutures(long mills, boolean cancelTimeoutFuture, Future... futures) {
        long waitCount = 100;
        long sleepMills = mills / 100;

        do {
            boolean allDone = true;

            for (Future future : futures) {
                if (!future.isDone()) {
                    allDone = false;
                    break;
                }
            }

            if (allDone) {
                break;
            }

            sleep(sleepMills);
        } while (--waitCount > 0);

        if (cancelTimeoutFuture) {
            Arrays.stream(futures).forEach(future -> {
                if (future.isDone()) {
                    future.cancel(true);
                }
            });
        }
    }

    public static void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
