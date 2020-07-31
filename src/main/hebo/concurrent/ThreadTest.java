package hebo.concurrent;

/**
 * description
 *  对共享数据加了同步锁的，其他线程会等待持有锁的线程执行完。
 * @author workerbo 2020/07/21 22:15
 */
public class ThreadTest implements Runnable{

    @Override
    public synchronized void run(){
  for(int i=0;i<10;i++) {
      System.out.print(" " + i);

  }
    }

    public static void main(String[] args) {
Runnable r1 = new ThreadTest(); //也可写成ThreadTest r1 = new ThreadTest();
Runnable r2 = new ThreadTest();
Thread t1 = new Thread(r1);
Thread t2 = new Thread(r1);
t1.start();
t2.start();
    }}