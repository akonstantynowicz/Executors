package com.baeldung.spring.data.redis;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.stream.LongStream;

import executor.MyTask;
import executor.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import com.baeldung.spring.data.redis.queue.RedisMessagePublisherTask;
import com.baeldung.spring.data.redis.queue.RedisMessageSubscriberAck;

@Service
public class ParallelProcessTester {
//po wykonaniu jeszcze raz podlaczyc listenera
    @Autowired
    @Qualifier("publisherTask")
    private RedisMessagePublisherTask redisMessagePublisher;

    @Autowired
    @Qualifier("listenerAck")
    private MessageListenerAdapter redisMessageSubscriberAck;

    public void testAllTaskFinish(int numberOfTasks, Executor executor) throws InterruptedException {
        LongStream.range(0, numberOfTasks).forEach(i -> {
            int randomType = new Random().nextInt(Type.values().length);
            executor.execute(new MyTask(i, Type.values()[randomType], redisMessagePublisher, redisMessageSubscriberAck));
        });
        // set up time limit after which the test is failed
        ((RedisMessageSubscriberAck) redisMessageSubscriberAck.getDelegate()).waitForAll(numberOfTasks);
    }
    //wiecej metod ktore moglyby testowac parametryzowane
}
