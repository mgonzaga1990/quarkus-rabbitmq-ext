package io.smallrye.reactive.messaging.rabbitmq;

import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.logging.Logger;

public class RabbitMQSender implements Subscriber<Message<String>>{

    private static final Logger log = Logger.getLogger(RabbitMQSender.class.getName());

    private Subscription subscription;
    private BroadcastProcessor<Message<String>> broadcastProcessor;

    RabbitMQSender(){
        this.broadcastProcessor = BroadcastProcessor.create();
    }

    static RabbitMQSender create(){
        return new RabbitMQSender();
    }

    public BroadcastProcessor<Message<String>> broadcastProcessor(){
        return this.broadcastProcessor;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Message<String> payload) {
        this.broadcastProcessor.onNext(payload);
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new RuntimeException(throwable);
    }

    @Override
    public void onComplete() {
        //TODO : mark as complete
        this.broadcastProcessor.onComplete();
    }
}
