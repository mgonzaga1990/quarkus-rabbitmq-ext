package io.smallrye.reactive.messaging.rabbitmq.connector;

import com.test.Pojo;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.logging.Logger;

public class RabbitMQSender<T> implements Subscriber<T> {
    private static final Logger log = Logger.getLogger(RabbitMQSender.class.getName());

    private Subscription subscription;
    private BroadcastProcessor<T> broadcastProcessor;

    RabbitMQSender(){
        this.broadcastProcessor = BroadcastProcessor.create();
    }

    public static RabbitMQSender create(){
        return new RabbitMQSender();
    }

    public BroadcastProcessor<T> broadcastProcessor(){
        return this.broadcastProcessor;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(T payload) {
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
