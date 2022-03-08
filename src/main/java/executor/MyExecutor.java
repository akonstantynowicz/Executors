package executor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executor który uruchamia zadania każdego typu na osobnym wątku w kolejności przyjścia
 */
public class MyExecutor implements Executor {

    private Queue<Runnable> taskQueue = new LinkedList<>();
    private Runnable active = null;
    private ReentrantLock lock;
    // Condition pozwala nam wykonywac await i signal
    private Condition lockObjectCondition;
    private EnumMap<Type, Queue<MyTask>> inputQueues;
    // ThreadPool
    private final Set<Thread> threads;
    private ConcurrentHashMap<Long, List<Type>> workersTasksTypes;

    public MyExecutor() {
        // this setup need to be generalized so that we can parametrized it
        this.lock = new ReentrantLock();
        this.lockObjectCondition = lock.newCondition();
        this.inputQueues = new EnumMap<>(Type.class);
        this.workersTasksTypes = new ConcurrentHashMap<>();
        for (Type t:Type.values()){
            inputQueues.put(t, new LinkedList<>());
        }
        this.threads = new HashSet<>();
        MyWorker worker = new MyWorker(1);
        workersTasksTypes.put(worker.workerId, worker.current_types);
        MyWorker worker2 = new MyWorker(2);
        workersTasksTypes.put(worker2.workerId, worker2.current_types);
        MyWorker worker3 = new MyWorker(3);
        workersTasksTypes.put(worker3.workerId, worker3.current_types);
        Thread thread = Executors.defaultThreadFactory().newThread(worker);
        thread.start();
        worker.setRunning(true);
        Thread thread2 = Executors.defaultThreadFactory().newThread(worker2);
        thread2.start();
        worker2.setRunning(true);
        Thread thread3 = Executors.defaultThreadFactory().newThread(worker3);
        thread3.start();
        worker3.setRunning(true);
    }

    @Override
    public void execute(Runnable task) {
        MyTask myTask = (MyTask) task;
        lock.lock();
        try {
            inputQueues.get(myTask.type).add(myTask);
            lockObjectCondition.signalAll();
        } catch (NullPointerException aE) {
            System.err.println("Error");
        } finally {
            lock.unlock();

        }
    }

    // Klasa prywatna gdyż chcemy móc odnosić się do lockow zdefiniowanych w naszym executorze
    private class MyWorker implements Runnable
    {
        private long workerId;
        private List<Type> current_types = new ArrayList<>();
        private final AtomicBoolean running = new AtomicBoolean( false );
        public MyWorker( Integer workerId)
        {
            this.workerId = workerId;
            this.running.set( false );
        }

        private boolean isRunning()
        {
            return running.get();
        }

        private void setRunning( boolean aRunning )
        {
            running.set( aRunning );
        }

        public void shutdown()
        {
            //Todo
        }

        private Type checkForTaskType() {
            for(Map.Entry<Type, Queue<MyTask>> entry : inputQueues.entrySet()){
                if(!entry.getValue().isEmpty()){
                    if (!workersTasksTypes.get(Long.valueOf(1)).contains(entry.getKey()) &&
                            !workersTasksTypes.get(Long.valueOf(2)).contains(entry.getKey()) &&
                            !workersTasksTypes.get(Long.valueOf(3)).contains(entry.getKey())){
                        return entry.getKey();
                    };
                }
            }
            return null;
        }

        @Override
        public void run()
        {
            Type assignedType = null;
            while( running.get() )
            {
                try {
                    while (running.get()) {
                        MyTask task = null;
                        lock.lock();
                        try {
                            if(current_types.isEmpty()){
                                assignedType = checkForTaskType();
                            }else {
                                assignedType = current_types.get(0);
                            }
                            if(assignedType != null)
                            {
                                if(!current_types.contains(assignedType)) {
                                    current_types.add(assignedType);
                                }
                                task = inputQueues.get(assignedType).poll();
                                if(inputQueues.get(assignedType).isEmpty()){
                                    current_types.remove(assignedType);
                                }
                            }
                            else
                            {
                                lockObjectCondition.await(); //SUBSRIBER ACK
                            }
                        } catch (Exception e) {
                            System.out.println("worker thread interrupted");
                            break;
                        } finally {
                            lock.unlock();
                        }
                        if(task != null) {
                            System.out.println(task.id);
                            task.run();
                        }
                    }
                }
                catch( Exception aE )
                {
                    System.err.println(aE.getMessage() + "exception caught on worker thread" );
                }
            }
        }
    }
}
