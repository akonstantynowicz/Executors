public class MyTask implements Runnable {

    Type type;
    Integer id;

    public MyTask(Integer id, Type type) {
        this.type = type;
        this.id = id;
    }

    @Override public void run() {
        //send request about start
        //wait for confirmation and sleep time

        System.out.println("Executed " + id + " " + type.name());
        //Sleep for given time

        //send request about end
        //wait for confirmation
    }
}

