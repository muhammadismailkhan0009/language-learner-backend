package com.myriadcode.languagelearner.concurnas_like_library;

public enum ExecutionKind {
    IO,     // I/O-bound work (DB, HTTP, disk…) – perfect for virtual threads
    CPU,     // CPU-bound work (computations, transformations)
    CPU_PARALLEL
}
