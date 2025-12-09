package com.myriadcode.languagelearner.concurnas_like_library;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class Vals {

    private static final FlowRuntime RUNTIME = FlowRuntime.global();

    private Vals() {
    }

    // ─────────────────────
    // Creation
    // ─────────────────────

    /**
     * Schedule IO-bound work (DB, HTTP, etc.).
     */
    public static <T> Value<T> io(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return FlowValue.of(supplier, ExecutionKind.IO, RUNTIME);
    }

    /**
     * Schedule CPU-bound work (pure or mostly computational).
     */
    public static <T> Value<T> cpu(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return FlowValue.of(supplier, ExecutionKind.CPU, RUNTIME);
    }

    public static <T> Value<T> cpuParallel(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return FlowValue.of(supplier, ExecutionKind.CPU_PARALLEL, RUNTIME);
    }


    public static Value<Void> runIo(Runnable runnable) {
        Objects.requireNonNull(runnable);
        return Vals.io(() -> {
            runnable.run();
            return null;
        });
    }

    public static Value<Void> runCpu(Runnable runnable) {
        Objects.requireNonNull(runnable);
        return Vals.cpu(() -> {
            runnable.run();
            return null;
        });
    }

    // ─────────────────────
    // Combination
    // ─────────────────────

    public static <A, B, R> Value<R> combine(
            Value<A> a,
            Value<B> b,
            BiFunction<? super A, ? super B, ? extends R> fn
    ) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Objects.requireNonNull(fn);

        return FlowValue.combine(a, b, fn, RUNTIME);
    }
}
