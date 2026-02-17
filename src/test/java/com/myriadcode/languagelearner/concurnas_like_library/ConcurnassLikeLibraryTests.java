package com.myriadcode.languagelearner.concurnas_like_library;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class ConcurnassLikeLibraryTests {

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    void cpuTasksNeverExceedCoreCount() {
        int cores = Runtime.getRuntime().availableProcessors();

        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxSeen = new AtomicInteger();

        int tasks = cores * 5;

        List<Value<Object>> values = IntStream.range(0, tasks)
                .mapToObj(i ->
                        Vals.cpu(() -> {
                            int r = running.incrementAndGet();
                            maxSeen.updateAndGet(m -> Math.max(m, r));
                            sleep(100);
                            running.decrementAndGet();
                            return null;
                        })
                ).toList();

        values.forEach(Value::value);

        assertTrue(
                maxSeen.get() <= cores,
                "CPU concurrency exceeded core count"
        );
    }

    @Test
    void nestedCpuTasksDoNotDeadlock() {
        Value<Integer> v =
                Vals.cpu(() -> 1)
                        .mapCpu(x ->
                                Vals.cpu(() -> x + 1)
                                        .value()
                        )
                        .mapCpu(x ->
                                Vals.cpu(() -> x + 1)
                                        .value()
                        );

        assertThat(v.value()).isEqualTo(3);
    }



    @Test
    void cpuParallelIsExclusive() {
        AtomicBoolean overlap = new AtomicBoolean(false);
        AtomicBoolean parallelRunning = new AtomicBoolean(false);

        Value<Void> parallel =
                Vals.cpuParallel(() -> {
                    parallelRunning.set(true);
                    sleep(200);
                    parallelRunning.set(false);
                    return null;
                });

        Value<Void> competing =
                Vals.cpu(() -> {
                    if (parallelRunning.get()) {
                        overlap.set(true);
                    }
                    return null;
                });

        parallel.value();
        competing.value();

        assertFalse(overlap.get(), "cpuParallel overlapped with cpu");
    }

    @Test
    void ioIsNotBlockedByCpuSaturation() throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();
        CountDownLatch blockCpu = new CountDownLatch(1);
        CountDownLatch ioRan = new CountDownLatch(1);

        // Saturate CPU
        for (int i = 0; i < cores; i++) {
            Vals.cpu(() -> {
                try {
                    blockCpu.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }

        // IO must still work
        Vals.io(() -> {
            ioRan.countDown();
            return null;
        });

        assertTrue(
                ioRan.await(200, TimeUnit.MILLISECONDS),
                "IO was blocked by CPU work"
        );

        blockCpu.countDown();
    }

    @Test
    void chainingPreservesLogicalOrder() {
        List<String> events = Collections.synchronizedList(new ArrayList<>());

        Value<String> v =
                Vals.io(() -> {
                            events.add("io");
                            return "data";
                        })
                        .mapCpu(d -> {
                            events.add("cpu");
                            return d + "!";
                        })
                        .mapIo(d -> {
                            events.add("io2");
                            return d;
                        });

        assertEquals("data!", v.value());
        assertEquals(List.of("io", "cpu", "io2"), events);
    }

    @Test
    void combineWaitsForBothValues() {
        AtomicBoolean aDone = new AtomicBoolean(false);
        AtomicBoolean bDone = new AtomicBoolean(false);

        Value<Integer> a =
                Vals.cpu(() -> {
                    sleep(100);
                    aDone.set(true);
                    return 10;
                });

        Value<Integer> b =
                Vals.cpu(() -> {
                    sleep(100);
                    bDone.set(true);
                    return 20;
                });

        Value<Integer> combined =
                Vals.combine(a, b, Integer::sum);

        int result = combined.value();

        assertEquals(30, result);
        assertTrue(aDone.get());
        assertTrue(bDone.get());
    }

    @Test
    void manyTasksCompleteSuccessfully() {
        int tasks = 1_000;

        List<Value<Integer>> values =
                IntStream.range(0, tasks)
                        .mapToObj(i ->
                                Vals.io(() -> i)
                                        .mapCpu(x -> x + 1)
                        )
                        .toList();

        int sum = values.stream().mapToInt(Value::value).sum();

        assertEquals(tasks * (tasks + 1) / 2, sum);
    }

    @Test
    void ioCpuPipelineIsOverlappingAndBusy() {
        int emails = 10;

        long ioMillis = 100;
        long cpuMillis = 200;

        long expectedSerial =
                emails * (ioMillis + cpuMillis);

        long start = System.currentTimeMillis();

        List<Value<Void>> values = new ArrayList<>();

        for (int i = 0; i < emails; i++) {
            values.add(
                    Vals.io(() -> {
                        sleep(ioMillis); // simulate SMTP
                        return "ok";
                    }).mapCpu(result -> {
                        sleep(cpuMillis); // simulate CPU work
                        return null;
                    })
            );
        }

        values.forEach(Value::value);

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Elapsed = " + elapsed +
                ", serial = " + expectedSerial);

        // pipeline must beat serial by a wide margin
        assertTrue(
                elapsed < expectedSerial * 0.65,
                "Pipeline did not overlap IO and CPU"
        );
    }

    @Test
    void ioAndCpuRunConcurrentlyInPipeline() {
        int emails = 20;

        AtomicInteger activeIo = new AtomicInteger();
        AtomicInteger activeCpu = new AtomicInteger();
        AtomicBoolean overlapObserved = new AtomicBoolean(false);

        List<Value<Void>> values = new ArrayList<>();

        for (int i = 0; i < emails; i++) {
            values.add(
                    Vals.io(() -> {
                        activeIo.incrementAndGet();
                        try {
                            sleep(80);
                        } finally {
                            activeIo.decrementAndGet();
                        }
                        return "mail";
                    }).mapCpu(result -> {
                         activeCpu.incrementAndGet();
                        try {
                            if (activeIo.get() > 0) {
                                overlapObserved.set(true);
                            }
                            sleep(150);
                        } finally {
                            activeCpu.decrementAndGet();
                        }
                        return null;
                    })
            );
        }

        values.forEach(Value::value);

        assertTrue(
                overlapObserved.get(),
                "No IO/CPU overlap observed — pipeline stalled"
        );
    }


}
