package ru.mephi.rxjava;

public interface Predicate<T> {

    boolean test(T t) throws Exception;
}
