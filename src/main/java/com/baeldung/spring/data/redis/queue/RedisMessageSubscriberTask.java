package com.baeldung.spring.data.redis.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageSubscriberTask implements MessageListener {

    final static Logger logger = LoggerFactory.getLogger(RedisMessageSubscriberTask.class);
    final ConcurrentHashMap<Long, Long> state;
    final RedisMessagePublisherAck redisMessagePublisher;

    public RedisMessageSubscriberTask(ConcurrentHashMap<Long, Long> state,
                                      @Qualifier("publisherAck") RedisMessagePublisherAck redisMessagePublisher) {
        this.state = state;
        this.redisMessagePublisher = redisMessagePublisher;
    }

    @Override public void onMessage(Message message, byte[] bytes) {
        Long key = Long.valueOf(message.toString());
        synchronized (state){
            if (state.containsKey(key)) {
                Long value = state.get(key);
                if(value < 2) {
                    state.put(key, value + 1L);
                }
                System.out.println("TaskMess: " + message);
            } else {
                state.put(key, 1L);
            }
        }
        logger.info("Task: " + message.toString() + " state " + (state.get(key)==1 ? "start" : "end"));
        redisMessagePublisher.publish(key);
    }
}
