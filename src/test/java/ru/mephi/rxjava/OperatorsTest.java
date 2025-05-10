package ru.mephi.rxjava;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperatorsTest {

    @Test
    public void testMapOperator() throws InterruptedException {
        List<String> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        source.map(i -> "Number " + i)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed.get());
        assertEquals(3, results.size());
        assertEquals("Number 1", results.get(0));
        assertEquals("Number 2", results.get(1));
        assertEquals("Number 3", results.get(2));
    }

    @Test
    public void testFilterOperator() throws InterruptedException {
        List<Integer> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onNext(4);
            observer.onComplete();
        });

        source.filter(i -> i % 2 == 0)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed.get());
        assertEquals(2, results.size());
        assertEquals(2, results.get(0));
        assertEquals(4, results.get(1));
    }

    @Test
    public void testFlatMapOperator() throws InterruptedException {
        List<String> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        List<String> synchronizedResults = Collections.synchronizedList(results);

        source.flatMap((Function<Integer, Observable<String>>)i -> Observable.create(innerObserver -> {
                    innerObserver.onNext("A" + i);
                    innerObserver.onNext("B" + i);
                    innerObserver.onComplete();
                }))
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        synchronizedResults.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                        latch.countDown();
                    }
                });

        latch.await(2, TimeUnit.SECONDS);

        assertTrue(completed.get());
        assertEquals(4, results.size());

        assertTrue(results.contains("A1"));
        assertTrue(results.contains("B1"));
        assertTrue(results.contains("A2"));
        assertTrue(results.contains("B2"));
    }

    @Test
    public void testChainedOperators() throws InterruptedException {
        List<String> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onNext(4);
            observer.onNext(5);
            observer.onComplete();
        });

        source.filter(i -> i % 2 == 0)
                .map(i -> i * 10)
                .map(i -> "Value: " + i)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed.get());
        assertEquals(2, results.size());
        assertEquals("Value: 20", results.get(0));
        assertEquals("Value: 40", results.get(1));
    }
}
