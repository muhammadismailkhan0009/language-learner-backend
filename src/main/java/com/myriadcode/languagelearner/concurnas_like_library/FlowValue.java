package com.myriadcode.languagelearner.concurnas_like_library;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

final class FlowValue<T> implements Value<T> {

    private final CompletableFuture<T> future;
    private final ExecutionKind kind;
    private final FlowRuntime runtime;

    private FlowValue(CompletableFuture<T> future,
                      ExecutionKind kind,
                      FlowRuntime runtime) {
        this.future = future;
        this.kind = kind;
        this.runtime = runtime;
    }

    static <T> FlowValue<T> of(
            Supplier<T> supplier,
            ExecutionKind kind,
            FlowRuntime runtime
    ) {
        Executor executor = executorFor(kind, runtime);

        CompletableFuture<T> cf =
                CompletableFuture.supplyAsync(
                        () -> {
                            if (kind == ExecutionKind.CPU) {
                                runtime.cpuPermits().acquireUninterruptibly();
                                try {
                                    return supplier.get();
                                } finally {
                                    runtime.cpuPermits().release();
                                }
                            } else if (kind == ExecutionKind.CPU_PARALLEL) {
                                int all = runtime.totalCpuPermits(); // logical total
                                runtime.cpuPermits().acquireUninterruptibly(all);
                                try {
                                    return supplier.get();
                                } finally {
                                    runtime.cpuPermits().release(all);
                                }
                            }
                            return supplier.get();
                        },
                        executor
                );

        return new FlowValue<>(cf, kind, runtime);
    }


    static <A, B, R> FlowValue<R> combine(
            Value<A> a,
            Value<B> b,
            BiFunction<? super A, ? super B, ? extends R> fn,
            FlowRuntime runtime
    ) {
        FlowValue<A> fa = (FlowValue<A>) a;
        FlowValue<B> fb = (FlowValue<B>) b;

        // naive: run combination on CPU executor by default
        CompletableFuture<R> cf =
                fa.future.thenCombineAsync(
                        fb.future,
                        fn,
                        runtime.cpuExecutor()
                );

        return new FlowValue<>(cf, ExecutionKind.CPU, runtime);
    }

    private static Executor executorFor(ExecutionKind kind,
                                        FlowRuntime runtime) {
        return switch (kind) {
            case IO -> runtime.ioExecutor();
            case CPU, CPU_PARALLEL -> runtime.cpuExecutor();
        };
    }

    @Override
    public T value() {
        try {
            return future.join(); // join = unwrapped CompletionException
        } catch (RuntimeException ex) {
            // you can unwrap here if you like
            throw ex;
        }
    }

    @Override
    public <U> Value<U> map(Function<? super T, ? extends U> fn) {
        return mapInternal(fn, this.kind);
    }

    @Override
    public <U> Value<U> mapCpu(Function<? super T, ? extends U> fn) {
        return mapInternal(fn, ExecutionKind.CPU);
    }

    @Override
    public <U> Value<U> mapCpuParallel(Function<? super T, ? extends U> fn) {
        return mapInternal(fn, ExecutionKind.CPU_PARALLEL);
    }

    @Override
    public <U> Value<U> mapIo(Function<? super T, ? extends U> fn) {
        return mapInternal(fn, ExecutionKind.IO);
    }

    private <U> Value<U> mapInternal(
            Function<? super T, ? extends U> fn,
            ExecutionKind targetKind
    ) {
        Executor executor = executorFor(targetKind, runtime);

        CompletableFuture<U> mapped =
                future.thenApplyAsync(
                        t -> {
                            if (targetKind == ExecutionKind.CPU) {
                                runtime.cpuPermits().acquireUninterruptibly();
                                try {
                                    return fn.apply(t);
                                } finally {
                                    runtime.cpuPermits().release();
                                }
                            } else if (targetKind == ExecutionKind.CPU_PARALLEL) {
                                int total = runtime.totalCpuPermits();
                                runtime.cpuPermits().acquireUninterruptibly(total);
                                try {
                                    return fn.apply(t);
                                } finally {
                                    runtime.cpuPermits().release(total);
                                }
                            }

                            return fn.apply(t);
                        },
                        executor
                );

        return new FlowValue<>(mapped, targetKind, runtime);
    }

}
