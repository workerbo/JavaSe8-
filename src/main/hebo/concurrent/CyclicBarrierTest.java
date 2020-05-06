package hebo.concurrent;

import java.util.Map;
import java.util.concurrent.*;

/**
 * description
 *
 * @author workerbo 2020/05/05 16:54
 */
public  class CyclicBarrierTest {
    private static CyclicBarrier c = new CyclicBarrier(2);//屏障拦截的线程数量，

    public static void main(String[] args) {
        System.out.println(" 1 -- " + System.currentTimeMillis());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(" 2 -- " + System.currentTimeMillis());
                    Thread.sleep(1000);
                    System.out.println(" 3 -- " + System.currentTimeMillis());
                    c.await();//到达了屏障，然后当前线程被阻塞。
                    System.out.println(" 4 -- " + System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            System.out.println(" 5 -- " + System.currentTimeMillis());
            c.await();
            System.out.println(" 6 -- " + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" 7 -- " + System.currentTimeMillis());
    }
}
  class CyclicBarrierTest2 {
    static CyclicBarrier c = new CyclicBarrier(2, new A());

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    c.await();
                } catch (Exception e) {
                }
                System.out.println(1);
            }
        }).start();
        try {
            c.await();
        } catch (Exception e) {
        }
        System.out.println(2);
    }

    static class A implements Runnable {
        @Override
        public void run() {
            System.out.println(3);
        }
    }
}

 class BankWaterService {
    /**
     * 创建4个屏障，处理完之后执行当前类的run方法
     */
    private CyclicBarrier c = new CyclicBarrier(4, new MyRunnable());
     //计数器可以使用reset()方法重置
    /**
     * 假设只有4个sheet，所以只启动4个线程
     */
    private Executor executor = Executors.newFixedThreadPool(4);
    /**
     * 保存每个sheet计算出的银流结果
     */
    private ConcurrentHashMap<String, Integer> sheetBankWaterCount = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        BankWaterService bankWaterCount = new BankWaterService();
        bankWaterCount.count();
    }

    private void count() {

        for (int i = 0; i < 4; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // 计算当前sheet的银流数据，计算代码省略
                    sheetBankWaterCount.put(Thread.currentThread().getName(), 1);
                    // 银流计算完成，插入一个屏障
                    try {
                        c.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    class MyRunnable implements Runnable {

        @Override
        public void run() {
            int result = 0;
            // 汇总每个sheet计算出的结果
            for (Map.Entry<String, Integer> sheet : sheetBankWaterCount.entrySet()) {
                result += sheet.getValue();
            }
            // 将结果输出
            sheetBankWaterCount.put("result", result);
            System.out.println("result = " + result);
        }
    }
}
