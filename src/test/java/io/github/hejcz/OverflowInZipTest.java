package io.github.hejcz;

import org.junit.Test;
import org.reactivestreams.Subscription;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

public class OverflowInZipTest {

    @Test
    public void name() {
        Flux.zip(
                (Object[] values) -> (int) values[0] + (int) values[1],
                Flux.just(1, 2, 3, 4),
                new Flux<Integer>() {
                    @Override
                    public void subscribe(CoreSubscriber<? super Integer> actual) {
                        actual.onSubscribe(new Subscription() {
                            @Override
                            public void request(long n) {
                                for (int i = 0; i < 1_000_000; i++) {
                                    actual.onNext(i);
                                }
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                    }
                }
//                        .onBackpressureDrop()
        )
                .subscribe(System.out::println);

    }
}
