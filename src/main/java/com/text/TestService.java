package com.text;

import io.reactivex.Flowable;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TestService {

    @Inject
    @Channel("q2")
    Emitter<String> sampleThree;

//    @Inject
//    @Channel("q4")
//    Emitter<String> sampleFour;

    @Incoming("q1")
    public void sample(String payload){
        System.out.println(" got from one : " + payload + " ");
//        Flowable.interval(1,2, TimeUnit.SECONDS)
//                .map(x -> x = x+1)
//                .subscribe(aLong -> {
//                    sampleFour.send("from q4");
//                });
    }

    @Incoming("q3")
    public void sampleTwo(String payload){
        System.out.println(" got from two : " + payload + " ");
        Flowable.interval(1,2, TimeUnit.SECONDS)
                .map(x -> x = x+10)
                .subscribe(aLong -> {
                    sampleThree.send("hello jayson");
                });
    }


}