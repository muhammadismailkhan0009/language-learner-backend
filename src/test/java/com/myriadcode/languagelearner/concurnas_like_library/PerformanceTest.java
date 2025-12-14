package com.myriadcode.languagelearner.concurnas_like_library;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

public class PerformanceTest {

    @Test
    void valsCpuBenchmark() {
        int tasks = 1_000_000;

        long start = System.currentTimeMillis();

        var values =
                IntStream.range(0, tasks)
                        .mapToObj(i ->
                                Vals.cpu(() -> {
                                    cpuHeavyWork();
                                    return null;
                                })
                        )
                        .toList();

        values.forEach(Value::value);

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Vals CPU: " + elapsed + " ms");
    }


    @Test
    void parallelStreamCpuBenchmark() {
        int tasks = 1_000_0000;

        long start = System.currentTimeMillis();

        IntStream.range(0, tasks)
                .parallel()
                .forEach(i -> cpuHeavyWork());

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Parallel stream: " + elapsed + " ms");
    }


    static void cpuHeavyWorkBatch(int batchSize) {
        for (int j = 0; j < batchSize; j++) {
            long sum = 0;
            for (int i = 0; i < 50_000_000; i++) {
                sum += i;
            }
        }
    }

    static void cpuHeavyWork() {
        long sum = 0;
        for (int i = 0; i < 50_000_000; i++) {
            sum += i;
        }
    }


    @Test
    void parallelStreamMixedBenchmark() {
        int tasks = 1_000;

        long start = System.currentTimeMillis();

        IntStream.range(0, tasks)
                .parallel()
                .forEach(i -> {
                    simulateIo();
                    cpuHeavyWork();
                });

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Parallel stream mixed: " + elapsed + " ms");
    }

    static void simulateIo() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    @Test
    void valsMixedBenchmark() {
        int tasks = 1_000;

        long start = System.currentTimeMillis();

        var values =
                IntStream.range(0, tasks)
                        .mapToObj(i ->
                                Vals.io(() -> {
                                    simulateIo();
                                    return null;
                                }).mapCpu(x -> {
                                    cpuHeavyWork();
                                    return null;
                                })
                        )
                        .toList();

        values.forEach(Value::value);

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Vals mixed: " + elapsed + " ms");
    }


}
