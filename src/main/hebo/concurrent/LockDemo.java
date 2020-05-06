package hebo.concurrent;

import java.util.concurrent.locks.ReentrantLock;

/**
 * description
 *
 * @author workerbo 2020/04/29 17:38
 */
public class LockDemo {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        //断点或者等待后，第一个线程获得锁【头指针指向】，前四个线程被循环设置为signal状态【释放锁后通知后续线程竞争】
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                lock.lock();
                try {
                    //让线程睡眠是想模拟出当线程无法获取锁时进入同步队列的情况。
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            });
            thread.start();
        }
    }
}

