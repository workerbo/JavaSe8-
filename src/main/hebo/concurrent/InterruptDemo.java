package hebo.concurrent;

/**
 * description
 * 1.中断处于阻塞/睡眠状态的线程,退出阻塞状态继续运行。没有占用CPU运行的线程是不可能给自己的中断状态置位的。
 * 这就会产生一个InterruptedException异常。[共享变量变化后，让子线程提前退出阻塞，退出循环结束子线程]
 * 2.中断正在运行的线程会设置中断标志。被中断线程可以决定如何应对中断.【例如结束循环退出】。
 * @author workerbo 2020/04/28 13:10
 */
public class InterruptDemo {
    public static void main(String[] args) throws InterruptedException {
        //sleepThread睡眠1000ms
        final Thread sleepThread = new Thread() {
            @Override
            public void run() {
                //判断当前线程是否中断,且清除标志。
                //Thread.interrupted();
                try {

                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    //当抛出InterruptedException时候，会清除中断标志位
                    //通过异常表示中断完成
                    e.printStackTrace();
                }

                super.run();
                //阻塞被中断，会被运行
                //有异常，但是他没有抛到外面去，所以要继续执行后面的。
                System.out.println("sleepThread Running!");
                //方法里执行的终止）仅限于以下情况：A.执行return语句；
                // B.所有语句执行完毕；C.抛到方法外的异常（run()除外，因为异常不能抛到run()外面去）
            }
        };
        //busyThread一直执行死循环
        Thread busyThread = new Thread() {
            @Override
            public void run() {
                //中断后也会一直运行

                while (true){
                    System.out.println("busyThread");
                }
            }

        };
        sleepThread.start();
        busyThread.start();
        //进行了中断操作,
        sleepThread.interrupt();
        busyThread.interrupt();

        while (sleepThread.isInterrupted()) ;
        //调用 isInterrupted（）来感知
        System.out.println("sleepThread isInterrupted: " + sleepThread.isInterrupted());
        System.out.println("busyThread isInterrupted: " + busyThread.isInterrupted());
        //结果 sleepThread isInterrupted: false
        // busyThread isInterrupted: true
        //boolean interrupted() :检测当前线程是否被中断，如果是返回true，否则返回false。
        // 与isInterrupted不同的是，该方法发现当前线程被中断后会清除中断标志。
    }
    // 一个线程在未正常结束之前, 被强制终止是很危险的事情. 因为它可能带来完全预料不到的严重后果
    // 比如会带着自己所持有的锁而永远的休眠，迟迟不归还锁等。
    // 所以你看到Thread.suspend, Thread.stop等方法都被Deprecated了
    //一个比较优雅而安全的做法是:使用等待/通知机制或者给那个线程一个中断信号, 让它自己决定该怎么办。
    //https://blog.csdn.net/canot/article/details/51087772
}