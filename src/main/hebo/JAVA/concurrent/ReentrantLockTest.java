package hebo.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: Liruilong
 * @Date: 2019/7/27 10:59
 */

//ReentrantLock 响应中断【避免死锁】
public class ReentrantLockTest {
    static Lock lock1 = new ReentrantLock();
    static Lock lock2 = new ReentrantLock();
    public static void main(String[] args)
          throws InterruptedException {
        //两个线程传入的时候位置相反
        Thread thread = new Thread(new ThreadDemo(lock1, lock2));
        Thread thread1 = new Thread(new ThreadDemo(lock2, lock1));
        thread.start();
        thread1.start();
      thread.interrupt();//是第一个线程中断
    }
    static class ThreadDemo implements Runnable {
        Lock firstLock;
        Lock secondLock;
        public ThreadDemo(Lock firstLock, Lock secondLock) {
            this.firstLock = firstLock;
            this.secondLock = secondLock;
        }
        @Override
        public void run() {
            try {
                firstLock.lock();
                TimeUnit.SECONDS.sleep(2);
                 secondLock.lock();

                //if(!lock1.tryLock()) {TimeUnit.MILLISECONDS.sleep(10);}
                //TimeUnit.MILLISECONDS.sleep(50);
                //if(!lock2.tryLock()) {TimeUnit.MILLISECONDS.sleep(10);}
            } catch (InterruptedException e) {
                e.printStackTrace();

            } finally {

                firstLock.unlock();
                secondLock.unlock();
                System.out.println(Thread.currentThread().getName()
                      +"获取到了资源，正常结束!");

            }
        }
    }
}