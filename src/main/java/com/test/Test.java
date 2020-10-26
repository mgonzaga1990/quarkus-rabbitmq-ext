package com.test;

import io.smallrye.mutiny.Multi;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletionStage;

//@ApplicationScoped
public class Test {

//    private Random random = new Random();
//
//    @Outgoing("person-register-request")
//    public Multi<Pojo> generate() {
//        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
//                .onOverflow().drop()
//                .map(tick -> new Pojo(random.nextInt(100),"testname"));
//    }
//
//    @Incoming("person-register-channel")
//    public CompletionStage<Void> receiveGenerate(Message<RabbitMQMessage> message){
//        final RabbitMQMessage payload = message.getPayload();
//        final Buffer body = payload.body();
//
//        final JsonObject jsonObject = body.toJsonObject();
//        final Pojo pojo = jsonObject.mapTo(Pojo.class);
//
//        System.out.println("message received : " + pojo.getName() + " id of " + pojo.getId());
//        return message.ack();
//    }

}
