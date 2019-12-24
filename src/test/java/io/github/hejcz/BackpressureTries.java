package io.github.hejcz;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.Test;
import org.reactivestreams.Subscription;

import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

public class BackpressureTries {

    @Test
    public void name() throws InterruptedException {
        Hooks.onOperatorDebug();

        Flux.interval(Duration.of(100, ChronoUnit.MILLIS))
//                .onBackpressureDrop(l -> System.out.println("Dropped " + l))
//                .onBackpressureBuffer(2, l -> System.out.println("no more storage in buffer " + l), BufferOverflowStrategy.DROP_OLDEST)
//                .onBackpressureLatest()
                .bufferTimeout(20, Duration.ofMillis(200))
                .onBackpressureDrop(l -> System.out.println("Dropped buffer " + l))
                .subscribe(System.out::println, System.err::println, () -> {
                }, sub -> sub.request(3));

        Thread.sleep(20_000);
    }

    @Test
    public void name2() throws InterruptedException {
//        0
//        1
//        2
//        no more storage in buffer 5
//        3
//        4
//        reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)

        // initial request is 3
        // interval produces 0,1,2 which are accepted
        // no more elements are requested so buffer stores maxSize - 2 - of them
        // then backpressure callback is called - it requests 5 more
        // meanwhile backpressure operator drops each onNext
        // 2 buffered elements are forwarded downstream
        // an error is signaled

        Hooks.onOperatorDebug();

        AtomicReference<Subscription> sub1 = new AtomicReference<>();
        Flux.interval(Duration.of(100, ChronoUnit.MILLIS))
                .onBackpressureBuffer(2, l -> {
                    System.out.println("no more storage in buffer " + l);
                    sub1.get().request(5);
                }, BufferOverflowStrategy.ERROR)
                .onErrorResume(t -> Mono.just(1L))
                .subscribe(
                        System.out::println,
                        System.err::println,
                        () -> System.out.println("completed"),
                        sub -> {
                            sub1.set(sub);
                            sub1.get().request(3);
                        });

        Thread.sleep(20_000);
    }

}
