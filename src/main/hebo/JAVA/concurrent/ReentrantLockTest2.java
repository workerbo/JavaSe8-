package hebo.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * description
 *
 * @author workerbo 2020/04/26 17:12
 */
public class ReentrantLockTest2 {
    //公平锁  按等待时间执行
    private static final Lock lock = new ReentrantLock(true);
    public static void main(String[] args) {
        new Thread(() -> test(),"线程A").start();
        new Thread(() -> test(),"线程B").start();
        new Thread(() -> test(),"线程C").start();
        new Thread(() -> test(),"线程D").start();
        new Thread(() -> test(),"线程E").start();
    }
    public static void  test()  {
        for(int i=0;i<2;i++) {
            try {
                lock.lock();  //没有释放锁会独占运行
                System.out.println(Thread.currentThread().getName()+"获取了锁");
                TimeUnit.SECONDS.sleep(2);//消耗时间让一次循环后其他线程执行

            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                //锁运行的
                System.out.println(Thread.currentThread().getName()+"释放了锁");
                lock.unlock();
            }
        }
    }
}