package ru.mephi.rxjava;

public interface Scheduler {

    void execute(Runnable task);
}
