package ru.mephi.rxjava;

public class Schedulers {

    private static final Scheduler IO_SCHEDULER = new IOThreadScheduler();
    private static final Scheduler COMPUTATION_SCHEDULER = new ComputationScheduler();
    private static final Scheduler SINGLE_THREAD_SCHEDULER = new SingleThreadScheduler();

    public static Scheduler io() {
        return IO_SCHEDULER;
    }

    public static Scheduler computation() {
        return COMPUTATION_SCHEDULER;
    }

    public static Scheduler single() {
        return SINGLE_THREAD_SCHEDULER;
    }
}
