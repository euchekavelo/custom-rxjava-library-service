package ru.mephi.rxjava;

import java.util.concurrent.atomic.AtomicBoolean;

public class Observable<T> {

    private final ObservableOnSubscribe<T> source;
    private Scheduler subscribeOnScheduler;
    private Scheduler observeOnScheduler;

    private Observable(ObservableOnSubscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<T> observer) {
        AtomicBoolean isDisposed = new AtomicBoolean(false);
        Disposable disposable = new Disposable() {
            @Override
            public void dispose() {
                isDisposed.set(true);
            }

            @Override
            public boolean isDisposed() {
                return isDisposed.get();
            }
        };

        Observer<T> wrappedObserver = new Observer<>() {
            @Override
            public void onNext(T item) {
                if (!isDisposed.get()) {
                    if (observeOnScheduler != null) {
                        observeOnScheduler.execute(() -> observer.onNext(item));
                    } else {
                        observer.onNext(item);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!isDisposed.get()) {
                    if (observeOnScheduler != null) {
                        observeOnScheduler.execute(() -> observer.onError(t));
                    } else {
                        observer.onError(t);
                    }
                }
            }

            @Override
            public void onComplete() {
                if (!isDisposed.get()) {
                    if (observeOnScheduler != null) {
                        observeOnScheduler.execute(observer::onComplete);
                    } else {
                        observer.onComplete();
                    }
                }
            }
        };

        Runnable subscribeTask = () -> {
            try {
                source.subscribe(wrappedObserver);
            } catch (Exception e) {
                wrappedObserver.onError(e);
            }
        };

        if (subscribeOnScheduler != null) {
            subscribeOnScheduler.execute(subscribeTask);
        } else {
            subscribeTask.run();
        }

        return disposable;
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return create(observer -> {
            subscribe(new Observer<>() {
                @Override
                public void onNext(T item) {
                    try {
                        R result = mapper.apply(item);
                        observer.onNext(result);
                    } catch (Exception e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return create(observer -> {
            subscribe(new Observer<>() {
                @Override
                public void onNext(T item) {
                    try {
                        if (predicate.test(item)) {
                            observer.onNext(item);
                        }
                    } catch (Exception e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return create(observer -> {
            AtomicBoolean completed = new AtomicBoolean(false);
            AtomicBoolean hasError = new AtomicBoolean(false);

            subscribe(new Observer<>() {
                @Override
                public void onNext(T item) {
                    try {
                        Observable<R> innerObservable = mapper.apply(item);
                        innerObservable.subscribe(new Observer<>() {
                            @Override
                            public void onNext(R innerItem) {
                                observer.onNext(innerItem);
                            }

                            @Override
                            public void onError(Throwable t) {
                                if (!hasError.getAndSet(true)) {
                                    observer.onError(t);
                                }
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
                    } catch (Exception e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (!hasError.getAndSet(true)) {
                        observer.onError(t);
                    }
                }

                @Override
                public void onComplete() {
                    if (!hasError.get()) {
                        completed.set(true);
                        observer.onComplete();
                    }
                }
            });
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        Observable<T> observable = new Observable<>(source);
        observable.subscribeOnScheduler = scheduler;
        observable.observeOnScheduler = observeOnScheduler;

        return observable;
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        Observable<T> observable = new Observable<>(source);
        observable.subscribeOnScheduler = subscribeOnScheduler;
        observable.observeOnScheduler = scheduler;

        return observable;
    }
}
