package org.xue.netty.base;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryImpl implements ThreadFactory {
    private final AtomicInteger index = new AtomicInteger(0);
    private final String nameFormat;

    public ThreadFactoryImpl(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, String.format(nameFormat, index.incrementAndGet()));
        return thread;
    }
}
