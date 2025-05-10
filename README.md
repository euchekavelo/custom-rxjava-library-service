# Отчет по реализации пользовательской RxJava-библиотеки

## Обзор

Данный проект представляет собой реализацию собственной версии библиотеки RxJava, основанной на принципах реактивного программирования. Библиотека предоставляет инструменты для работы с асинхронными потоками данных, включая их создание, преобразование, фильтрацию и управление потоками выполнения.

## Архитектура системы

Архитектура библиотеки построена вокруг паттерна "Наблюдатель" (Observer pattern) и основана на концепции реактивных потоков данных. Ключевыми компонентами системы являются:

### Основные интерфейсы

1. **Observer<T>** - интерфейс, определяющий методы для получения уведомлений от Observable:
    - `onNext(T item)` - вызывается при появлении нового элемента в потоке
    - `onError(Throwable t)` - вызывается при возникновении ошибки
    - `onComplete()` - вызывается при завершении потока данных

2. **ObservableOnSubscribe<T>** - функциональный интерфейс, определяющий логику создания Observable:
    - `subscribe(Observer<T> observer)` - метод, вызываемый при подписке Observer на Observable

3. **Function<T, R>** - функциональный интерфейс для преобразования элементов из типа T в тип R

4. **Predicate<T>** - функциональный интерфейс для проверки условия на элементе типа T

5. **Disposable** - интерфейс для отмены подписки:
    - `dispose()` - метод для отмены подписки
    - `isDisposed()` - метод для проверки состояния подписки

6. **Scheduler** - интерфейс для управления потоками выполнения:
    - `execute(Runnable task)` - метод для выполнения задачи в определенном потоке

### Основные классы

1. **Observable<T>** - центральный класс библиотеки, представляющий поток данных:
    - `create(ObservableOnSubscribe<T> source)` - статический метод для создания Observable
    - `subscribe(Observer<T> observer)` - метод для подписки Observer на Observable
    - `map(Function<T, R> mapper)` - оператор для преобразования элементов
    - `filter(Predicate<T> predicate)` - оператор для фильтрации элементов
    - `flatMap(Function<T, Observable<R>> mapper)` - оператор для преобразования элементов в новые Observable
    - `subscribeOn(Scheduler scheduler)` - метод для указания Scheduler для подписки
    - `observeOn(Scheduler scheduler)` - метод для указания Scheduler для обработки элементов

2. **Schedulers** - утилитный класс для работы с различными типами планировщиков:
    - `io()` - возвращает IOThreadScheduler для операций ввода-вывода
    - `computation()` - возвращает ComputationScheduler для вычислительных операций
    - `single()` - возвращает SingleThreadScheduler для последовательного выполнения

3. **IOThreadScheduler** - реализация Scheduler на основе CachedThreadPool для операций ввода-вывода

4. **ComputationScheduler** - реализация Scheduler на основе FixedThreadPool для вычислительных операций

5. **SingleThreadScheduler** - реализация Scheduler на основе одного потока для последовательного выполнения

## Принципы работы Schedulers

Schedulers в реактивном программировании отвечают за управление потоками выполнения. Они позволяют контролировать, в каком потоке будут выполняться различные операции, такие как создание Observable, обработка элементов и доставка результатов Observer'у.

### Типы Schedulers и их применение

1. **IOThreadScheduler**
    - **Реализация**: Основан на `CachedThreadPool` из `java.util.concurrent.Executors`
    - **Применение**: Оптимизирован для операций ввода-вывода (I/O), таких как чтение/запись файлов, сетевые запросы, доступ к базе данных
    - **Особенности**: Создает потоки по мере необходимости и переиспользует ранее созданные потоки, что делает его эффективным для задач с блокировками и ожиданием

2. **ComputationScheduler**
    - **Реализация**: Основан на `FixedThreadPool` из `java.util.concurrent.Executors`
    - **Применение**: Предназначен для CPU-интенсивных вычислений, обработки данных, алгоритмических задач
    - **Особенности**: Использует фиксированное количество потоков (обычно равное количеству доступных процессоров), что предотвращает чрезмерное создание потоков и переключение контекста

3. **SingleThreadScheduler**
    - **Реализация**: Основан на `SingleThreadExecutor` из `java.util.concurrent.Executors`
    - **Применение**: Используется для задач, требующих последовательного выполнения, или когда важен порядок операций
    - **Особенности**: Гарантирует, что все задачи будут выполняться в одном потоке, обеспечивая последовательность и предсказуемость выполнения

### Методы управления потоками

1. **subscribeOn(Scheduler scheduler)**
    - Определяет, в каком потоке будет происходить подписка на Observable и создание потока данных
    - Влияет на поток, в котором будет вызван метод `subscribe` у `ObservableOnSubscribe`
    - Вызов этого метода несколько раз не имеет эффекта - учитывается только первый вызов

2. **observeOn(Scheduler scheduler)**
    - Определяет, в каком потоке будут обрабатываться элементы и доставляться Observer'у
    - Влияет на потоки, в которых будут вызываться методы `onNext`, `onError` и `onComplete` у Observer
    - Может вызываться несколько раз в цепочке операторов, изменяя поток выполнения для последующих операторов

### Пример работы Schedulers

```
Observer<Integer> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Integer item) {
        System.out.println("Received on thread: " + Thread.currentThread().getName() + ", item: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error);
    }

    @Override
    public void onComplete() {
        System.out.println("Completed on thread: " + Thread.currentThread().getName());
    }
};

Observable.create(observer -> {
    // Выполняется в потоке IO (благодаря subscribeOn)
    System.out.println("Subscribing on thread: " + Thread.currentThread().getName());
    observer.onNext(1);
    observer.onNext(2);
    observer.onComplete();
})
.subscribeOn(Schedulers.io()) // Подписка происходит в IO потоке
.map(i -> {
    // Также выполняется в потоке IO
    System.out.println("Mapping on thread: " + Thread.currentThread().getName());
    return ((Integer) i) * 10;
})
.observeOn(Schedulers.computation()) // Переключаемся на Computation поток
.filter(i -> {
    // Выполняется в Computation потоке
    System.out.println("Filtering on thread: " + Thread.currentThread().getName());
    return i > 10;
})
.subscribe(simulationObserver); // Все колбэки Observer выполняются в Computation потоке
```

## Операторы преобразования данных

### map(Function<T, R> mapper)

Оператор `map` преобразует каждый элемент потока данных из типа T в тип R с помощью предоставленной функции преобразования.

### filter(Predicate<T> predicate)

Оператор `filter` пропускает только те элементы потока, которые удовлетворяют заданному условию.

### flatMap(Function<T, Observable<R>> mapper)

Оператор `flatMap` преобразует каждый элемент потока в новый Observable, а затем объединяет все эти Observable в один поток.

## Процесс тестирования

Тестирование библиотеки проводилось с использованием JUnit для проверки корректности работы всех компонентов и операторов. Тесты разделены на три основные категории:

### 1. Тестирование базовых функций Observable (ObservableTest)

- **Тестирование создания Observable и подписки**
    - Проверка корректной передачи элементов от Observable к Observer
    - Проверка вызова onComplete при завершении потока
    - Проверка обработки ошибок через onError

- **Тестирование Disposable**
    - Проверка отмены подписки через dispose()
    - Проверка, что после отмены подписки элементы не доставляются Observer'у

### 2. Тестирование операторов преобразования (OperatorsTest)

- **Тестирование оператора map**
    - Проверка корректного преобразования элементов
    - Проверка обработки ошибок в функции преобразования

- **Тестирование оператора filter**
    - Проверка корректной фильтрации элементов
    - Проверка обработки ошибок в предикате

- **Тестирование оператора flatMap**
    - Проверка преобразования элементов в новые Observable
    - Проверка объединения результатов всех внутренних Observable
    - Проверка корректного завершения при завершении всех внутренних Observable

### 3. Тестирование Schedulers (SchedulersTest)

- **Тестирование IOThreadScheduler**
    - Проверка выполнения задач в пуле потоков для I/O операций
    - Проверка корректной работы с блокирующими операциями

- **Тестирование ComputationScheduler**
    - Проверка выполнения задач в пуле потоков для вычислений
    - Проверка эффективности при CPU-интенсивных операциях

- **Тестирование SingleThreadScheduler**
    - Проверка последовательного выполнения задач в одном потоке
    - Проверка сохранения порядка операций

- **Тестирование методов subscribeOn и observeOn**
    - Проверка корректного переключения потоков при подписке
    - Проверка корректного переключения потоков при обработке элементов
    - Проверка комбинирования subscribeOn и observeOn

### Основные сценарии тестирования

1. **Синхронные операции**
    - Создание Observable, эмиссия элементов и обработка их в том же потоке

2. **Асинхронные операции**
    - Создание Observable в одном потоке, обработка элементов в другом

3. **Обработка ошибок**
    - Проверка корректной передачи исключений через onError
    - Проверка прекращения эмиссии элементов после ошибки

4. **Отмена подписки**
    - Проверка корректного прекращения обработки после вызова dispose()

5. **Цепочки операторов**
    - Проверка корректной работы последовательности операторов (map -> filter -> flatMap)
    - Проверка переключения потоков в середине цепочки операторов

## Примеры использования библиотеки

### Пример 1: Базовое использование

```java
Observer<Integer> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Integer item) {
        System.out.println("Received: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error);
    }

    @Override
    public void onComplete() {
        System.out.println("Completed");
    }
};

// Создание Observable, эмитирующего числа от 1 до 5
Observable<Integer> observable = Observable.create(observer -> {
    for (int i = 1; i <= 5; i++) {
        observer.onNext(i);
    }
    
    observer.onComplete();
});

// Подписка на Observable
observable.subscribe(simulationObserver);

// Вывод:
// Received: 1
// Received: 2
// Received: 3
// Received: 4
// Received: 5
// Completed
```

### Пример 2: Использование операторов преобразования

```java
Observer<Integer> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Integer item) {
        System.out.println("Received: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error);
    }

    @Override
    public void onComplete() {
        System.out.println("Completed");
    }
};

Observable<Integer> observable = Observable.create(observer -> {
    for (int i = 1; i <= 10; i++) {
        observer.onNext(i);
    }

    observer.onComplete();
});

observable.map(i -> i * 2) // Умножаем каждое число на 2
    .filter(i -> i % 4 == 0)   // Оставляем только числа, делящиеся на 4
    .subscribe(simulationObserver);

// Вывод:
// Received: 4
// Received: 8
// Received: 12
// Received: 16
// Received: 20
// Completed
```

### Пример 3: Использование flatMap

```java
Observer<Object> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Object item) {
        System.out.println("Received: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error);
    }

    @Override
    public void onComplete() {
        System.out.println("Completed");
    }
};

Observable<Integer> observable = Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onComplete();
});

observable.flatMap(i -> Observable.create(innerObserver -> {
            for (int j = 1; j <= 3; j++) {
                innerObserver.onNext(i * 10 + j);
            }
        
            innerObserver.onComplete();
        }))
        .subscribe(simulationObserver);

// Вывод:
// Received: 11
// Received: 12
// Received: 13
// Received: 21
// Received: 22
// Received: 23
// Completed
```

### Пример 4: Использование Schedulers

```java
Observer<Integer> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Integer item) {
        System.out.println("Received on thread: " + Thread.currentThread().getName() + ", item: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error);
    }

    @Override
    public void onComplete() {
        System.out.println("Completed on thread: " + Thread.currentThread().getName());
    }
};

Observable<Integer> observable = Observable.create(observer -> {
    System.out.println("Emitting on thread: " + Thread.currentThread().getName());
    for (int i = 1; i <= 3; i++) {
        observer.onNext(i);
    }

    observer.onComplete();
});

observable.subscribeOn(Schedulers.io()) // Эмиссия в IO потоке
        .map(i -> {
            System.out.println("Mapping on thread: " + Thread.currentThread().getName());
            return i * 10;
        })
        .observeOn(Schedulers.computation()) // Переключаемся на Computation поток
        .subscribe(simulationObserver);

// Примерный вывод:
// Emitting on thread: io-thread-1
// Mapping on thread: io-thread-1
// Mapping on thread: io-thread-1
// Mapping on thread: io-thread-1
// Received on thread: computation-thread-1, item: 10
// Received on thread: computation-thread-1, item: 20
// Received on thread: computation-thread-1, item: 30
// Completed on thread: computation-thread-1
```

### Пример 5: Обработка ошибок

```java
 Observer<Integer> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Integer item) {
        System.out.println("Received: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("Completed");
    }
};

Observable<Integer> observable = Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onError(new RuntimeException("Something went wrong"));
    observer.onNext(3);
    observer.onComplete();
});

observable.subscribe(simulationObserver);
```

### Пример 6: Отмена подписки

```java
Observer<Integer> simulationObserver = new Observer<>() {
    @Override
    public void onNext(Integer item) {
        System.out.println("Received: " + item);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error);
    }

    @Override
    public void onComplete() {
        System.out.println("Completed");
    }
};

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

Disposable disposable = observable.subscribe(simulationObserver);

// Отменяем подписку после получения нескольких элементов
try {
    Thread.sleep(350); // Ждем, чтобы получить первые 3-4 элемента
    disposable.dispose();
    System.out.println("Subscription disposed");
} catch (InterruptedException e) {
    e.printStackTrace();
}

// Примерный вывод:
// Received: 1
// Received: 2
// Received: 3
// Received: 4
// Subscription disposed
```

## Заключение

Реализованная библиотека представляет собой упрощенную версию RxJava-библиотеки, но включает все основные концепции реактивного программирования: Observable, Observer, операторы преобразования и Schedulers. Она позволяет создавать асинхронные потоки данных, управлять их обработкой и контролировать потоки выполнения.

Основные преимущества реализованной библиотеки:
- Простота использования
- Поддержка основных операторов преобразования
- Гибкое управление потоками выполнения
- Обработка ошибок
- Возможность отмены подписки

Библиотека может быть расширена дополнительными операторами, а также дополнительными типами Schedulers для более специфичных сценариев использования.