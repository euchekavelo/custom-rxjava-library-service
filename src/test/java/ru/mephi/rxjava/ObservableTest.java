package ru.mephi.rxjava;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

    @Test
    public void testBasicObservable() throws InterruptedException {
        List<Integer> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
                latch.countDown();
            }

            @Override
            public void onComplete() {
                completed.set(true);
                latch.countDown();
            }
        });

        latch.await(1, TimeUnit.SECONDS);

        assertNull(error.get());
        assertTrue(completed.get());
        assertEquals(3, results.size());
        assertEquals(1, results.get(0));
        assertEquals(2, results.get(1));
        assertEquals(3, results.get(2));
    }

    @Test
    public void testErrorHandling() throws InterruptedException {
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            throw new RuntimeException("Test error");
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                System.out.println(item.toString());
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        latch.await(1, TimeUnit.SECONDS);

        assertNotNull(error.get());
        assertEquals("Test error", error.get().getMessage());
    }

    @Test
    public void testDisposable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Integer> results = new ArrayList<>();

        Observable<Integer> observable = Observable.create(observer -> {
            new Thread(() -> {
                for (int i = 1; i <= 10; i++) {
                    observer.onNext(i);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        observer.onError(e);
                        return;
                    }
                }
                observer.onComplete();
            }).start();
        });

        Disposable disposable = observable.subscribe(new Observer<>() {
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
                latch.countDown();
            }
        });

        latch.await(150, TimeUnit.MILLISECONDS);
        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertTrue(results.size() < 10);
    }
}
