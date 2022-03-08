package com.baeldung.spring.data.redis.queue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageSubscriberAck implements MessageListener {

    Map<Long, Long> ack;
    //lockobjectcondition

    public RedisMessageSubscriberAck() {
        this.ack =  new HashMap<>();
    }

    @Override public synchronized void onMessage(Message message, byte[] bytes) {
        Long key = Long.valueOf(message.toString());
        if (ack.containsKey(key)) {
            Long value = ack.get(key);
            if(value < 2) {
                ack.put(key, value + 1L);
                notifyAll();
            }
            System.out.println("AckMess: " + message);
        } else {
            ack.put(key, 1L);
            notifyAll();
        }
    }

    public synchronized void waitOnStart(Long key) throws InterruptedException {
        // change to lock / notify scenario - non active waiting
        while(!ack.containsKey(key) || ack.get(key)!=1L) {
            wait();
        }
    }

    public synchronized void waitOnEnd(Long key) throws InterruptedException {
        // change to lock / notify scenario - non active waiting
        while(!ack.containsKey(key) || ack.get(key)!=2L) {
            wait();
        }
    }

    public synchronized void waitForAll(long size) throws InterruptedException {
        // change to lock / notify scenario - non active waiting
        while(ack.size() < size || ack.values().stream().allMatch(value -> value == 2) == false){
            wait();
            System.out.println(ack.size() + " < " + size + " " + ack.keySet() +ack.values());
        }
    }
}
