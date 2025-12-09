package com.myriadcode.languagelearner.concurnas_like_library;

import java.util.function.Function;

public interface Value<T> {

    /**
     * Access the computed value.
     * Blocks if the underlying computation is not finished yet.
     */
    T value();

    /**
     * Transform this value, keeping the same execution kind by default.
     * (IO -> IO, CPU -> CPU)
     */
    <U> Value<U> map(Function<? super T, ? extends U> fn);

    /**
     * Transform this value, and explicitly run the transformation
     * on the CPU executor.
     */
    <U> Value<U> mapCpu(Function<? super T, ? extends U> fn);

    <U> Value<U> mapCpuParallel(Function<? super T, ? extends U> fn);


    /**
     * Transform this value, and explicitly run the transformation
     * on the IO executor (e.g., if the transformation does DB calls).
     */
    <U> Value<U> mapIo(Function<? super T, ? extends U> fn);
}
