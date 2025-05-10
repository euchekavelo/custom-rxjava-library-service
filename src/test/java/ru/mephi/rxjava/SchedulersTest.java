package ru.mephi.rxjava;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulersTest {

    @Test
    public void testSubscribeOn() throws InterruptedException {
        AtomicReference<String> subscriberThreadName = new AtomicReference<>();
        AtomicReference<String> observerThreadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(observer -> {
            subscriberThreadName.set(Thread.currentThread().getName());
            observer.onNext(1);
            observer.onComplete();
        });

        observable
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        observerThreadName.set(Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertNotNull(subscriberThreadName.get());
        assertNotNull(observerThreadName.get());
        assertTrue(subscriberThreadName.get().contains("pool"));
        assertEquals(subscriberThreadName.get(), observerThreadName.get());
        assertNotEquals(Thread.currentThread().getName(), subscriberThreadName.get());
    }

    @Test
    public void testObserveOn() throws InterruptedException {
        AtomicReference<String> subscriberThreadName = new AtomicReference<>();
        AtomicReference<String> observerThreadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(observer -> {
            subscriberThreadName.set(Thread.currentThread().getName());
            observer.onNext(1);
            observer.onComplete();
        });

        observable.observeOn(Schedulers.computation())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        observerThreadName.set(Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertNotNull(subscriberThreadName.get());
        assertNotNull(observerThreadName.get());
        assertEquals(Thread.currentThread().getName(), subscriberThreadName.get());
        assertTrue(observerThreadName.get().contains("pool"));
        assertNotEquals(subscriberThreadName.get(), observerThreadName.get());
    }

    @Test
    public void testSubscribeOnAndObserveOn() throws InterruptedException {
        AtomicReference<String> subscriberThreadName = new AtomicReference<>();
        AtomicReference<String> observerThreadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(observer -> {
            subscriberThreadName.set(Thread.currentThread().getName());
            observer.onNext(1);
            observer.onComplete();
        });

        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        observerThreadName.set(Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertNotNull(subscriberThreadName.get());
        assertNotNull(observerThreadName.get());
        assertTrue(subscriberThreadName.get().contains("pool"));
        assertTrue(observerThreadName.get().contains("pool"));
        assertNotEquals(Thread.currentThread().getName(), subscriberThreadName.get());
        assertNotEquals(Thread.currentThread().getName(), observerThreadName.get());
        assertNotEquals(subscriberThreadName.get(), observerThreadName.get());
    }

    @Test
    public void testSingleThreadScheduler() throws InterruptedException {
        List<String> threadNames = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);
        AtomicBoolean completed = new AtomicBoolean(false);

        Observable<Integer> observable = Observable.create(observer -> {
            for (int i = 0; i < 3; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
        });

        observable.subscribeOn(Schedulers.single())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        threadNames.add(Thread.currentThread().getName());
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                    }
                });

        latch.await(1, TimeUnit.SECONDS);

        assertEquals(3, threadNames.size());
        String firstThreadName = threadNames.get(0);
        assertTrue(threadNames.stream().allMatch(name -> name.equals(firstThreadName)));
        assertTrue(firstThreadName.contains("pool"));
    }
}
