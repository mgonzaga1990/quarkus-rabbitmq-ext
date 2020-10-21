package com.test;

import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;

@ApplicationScoped
public class Test {

    private Random random = new Random();

    @Outgoing("person-register-request")
    public Multi<Integer> generate() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().invoke(x-> System.out.println("sending test message"))
                .onOverflow().drop()
                .map(tick -> random.nextInt(100));
    }
}
