package com.baeldung.spring.data.redis.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageSubscriberAck implements MessageListener {

    Map<Long, Long> ack;
    private ReentrantLock lockStart;
    private ReentrantLock lockEnd;
    private static Condition startTaskLock;
    private static Condition endTaskLock;


    public RedisMessageSubscriberAck() {
        this.ack =  new HashMap<>();
        this.lockStart = new ReentrantLock();
        this.startTaskLock = lockStart.newCondition();
        this.lockEnd = new ReentrantLock();
        this.endTaskLock = lockEnd.newCondition();
    }

    @Override public void onMessage(Message message, byte[] bytes) {
        Long key = Long.valueOf(message.toString());
        if (ack.containsKey(key)) {
            Long value = ack.get(key);
            if(value < 2) {
                lockEnd.lock();
                try {
                    System.out.println("Prepare to end..");
                    ack.put(key, value + 1L);
                    endTaskLock.signalAll();
                    System.out.println("Sent signal to end!");
                }finally {
                    lockEnd.unlock();
                }
            }
            System.out.println("AckMess: " + message);
        } else {
            lockStart.lock();
            try {
                System.out.println("preparing to start..");
                ack.put(key, 1L);
                startTaskLock.signalAll();
                System.out.println("Sent signal to start!");
            }finally{
                lockStart.unlock();
            }
        }
    }

    public void waitOnStart(Long key) throws InterruptedException {
        // change to lock / notify scenario - non active waiting
        lockStart.lock();
            try {
                while(!ack.containsKey(key) || ack.get(key)!=1L) {
                    System.out.println("wait for task start");
                    startTaskLock.await();
                    System.out.println("task started!");
                }
            }finally {
                lockStart.unlock();
            }
    }

    public void waitOnEnd(Long key) throws InterruptedException {
        // change to lock / notify scenario - non active waiting
        lockEnd.lock();
            try{
                while(!ack.containsKey(key) || ack.get(key)!=2L) {
                    System.out.println("wait for task end...");
                    endTaskLock.await();
                    System.out.println("task ended");
                }
            }finally {
                lockEnd.unlock();
            }
    }

    public void waitForAll(long size) throws InterruptedException {
        // change to lock / notify scenario - non active waiting
        lockEnd.lock();
        try{
            while(ack.size() < size || ack.values().stream().allMatch(value -> value == 2) == false){
                endTaskLock.await();
                System.out.println(ack.size() + " < " + size + " " + ack.keySet() +ack.values());
            }
        }finally {
            lockEnd.unlock();
        }

    }
}
