package com.myriadcode.languagelearner.concurnas_like_library;

import java.util.concurrent.Semaphore;

final class CpuPermits {
    static final int CORES =
        Runtime.getRuntime().availableProcessors();

    static final Semaphore SEM =
        new Semaphore(CORES, true); // FIFO
}
