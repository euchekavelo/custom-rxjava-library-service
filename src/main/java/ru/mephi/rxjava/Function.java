package ru.mephi.rxjava;

public interface Function<T, R> {

    R apply(T t) throws Exception;
}
