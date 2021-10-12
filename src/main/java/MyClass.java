import java.util.concurrent.Executor;
public class MyClass {

    public static void main(String[] args) {
        // Executor ma wątki które uruchamiaja kod
        Executor executor = new MyExecutor();
        executor.execute(new MyTask(1, Type.A));
        executor.execute(new MyTask(2, Type.A));
        executor.execute(new MyTask(3, Type.A));
        executor.execute(new MyTask(4, Type.A));
        executor.execute(new MyTask(5, Type.A));
        executor.execute(new MyTask(6, Type.B));
        executor.execute(new MyTask(7, Type.C));
    }


}
