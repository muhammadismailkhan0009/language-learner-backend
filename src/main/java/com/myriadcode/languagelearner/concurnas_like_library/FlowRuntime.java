package com.myriadcode.languagelearner.concurnas_like_library;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class FlowRuntime {

    private final Executor ioExecutor;
    private final Executor cpuExecutor;
    private final Semaphore cpuPermits;
    private final int totalCpuPermits;


    private static final FlowRuntime GLOBAL = createDefault();

    private FlowRuntime(Executor ioExecutor, Executor cpuExecutor, Semaphore cpuPermits,
                        int totalCpuPermits) {
        this.ioExecutor = ioExecutor;
        this.cpuExecutor = cpuExecutor;
        this.cpuPermits = cpuPermits;
        this.totalCpuPermits = totalCpuPermits;

    }

    public int totalCpuPermits() {
        return totalCpuPermits;
    }

    public static FlowRuntime global() {
        return GLOBAL;
    }

    public Executor ioExecutor() {
        return ioExecutor;
    }

    public Executor cpuExecutor() {
        return cpuExecutor;
    }

    public Semaphore cpuPermits() {
        return cpuPermits;
    }

    private static FlowRuntime createDefault() {
        Executor io = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("flow-io-", 0).factory()
        );

        int cores = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

        Executor cpu = Executors.newFixedThreadPool(
                cores,
                Thread.ofPlatform().name("flow-cpu-", 0).factory()
        );

        Semaphore permits = new Semaphore(cores, true);

        return new FlowRuntime(io, cpu, permits, cores);
    }
}
