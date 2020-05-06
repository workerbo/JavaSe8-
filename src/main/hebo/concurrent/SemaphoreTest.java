package hebo.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * description
 *Semaphore（信号量）是用来控制同时访问特定资源的线程数量
 * @author workerbo 2020/05/05 17:09
 */
public  class SemaphoreTest {
    private static final int THREAD_COUNT = 20;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
    private static Semaphore s = new Semaphore(5);//虽然有20个线程在执行，但是只允许5个并发执行

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            threadPool.execute(new MyRunnable(i));
        }
        threadPool.shutdown();
    }

    private static class MyRunnable implements Runnable {

        private int index;

        MyRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                s.acquire();
                System.out.println("save data -- " + index);
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
