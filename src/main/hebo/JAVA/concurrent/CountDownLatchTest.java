package hebo.concurrent;

import java.util.concurrent.CountDownLatch;

/**
 * description
 *
 * @author workerbo 2020/05/05 16:49
 */
public  class CountDownLatchTest {
    static CountDownLatch c = new CountDownLatch(3);//等待N个点完成，这里就传入N。

    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(1 + " -- " + System.currentTimeMillis());
                c.countDown();
                System.out.println(2 + " -- " + System.currentTimeMillis());
                c.countDown();
                try {
                    Thread.sleep(1000);
                    System.out.println(4 + " -- " + System.currentTimeMillis());
                    c.countDown();
                    Thread.sleep(1000);
                    System.out.println(5 + " -- " + System.currentTimeMillis());
                    c.countDown();
                    System.out.println(6 + " -- " + System.currentTimeMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        c.await();//阻塞当前线程，直到 N 变成零。
        System.out.println("3" + " -- " + System.currentTimeMillis());
    }
}
