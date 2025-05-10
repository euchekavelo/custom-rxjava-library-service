package ru.mephi.rxjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {

    private final static int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
