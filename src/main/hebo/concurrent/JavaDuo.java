package hebo.concurrent;

/**
 * description
 *余额不足!
 * 账户余额：0
 *
 *
 * 余额不足!
 * 1595339860805存进：100
 * 账户余额：100
 *
 *
 * 账户余额：100
 *
 * 多个线程同时读写造成数据紊乱，同一个方法里前后变量值不一致，且是随机发生
 * @author workerbo 2020/07/21 21:37
 */
public class JavaDuo {
    public static void main(String args[]){
        final Bank bank=new Bank();

        Thread tadd=new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while(true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    bank.addMoney(100);
                    bank.lookMoney();
                    System.out.println(bank.a);
                    System.out.println("\n");

                }
            }
        },"A");

        Thread tsub = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while(true){
                    bank.subMoney(100);
                    bank.a=88;
                    bank.lookMoney();
                    System.out.println("\n");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        },"B");
        tsub.start();

        tadd.start();
    }


}
class Bank {

    private int count =0;//账户余额
    public  int  a=99;
    //存钱
    public  void addMoney(int money){
        count +=money;
        System.out.println(System.currentTimeMillis()+"存进："+money);
    }

    //取钱
    public  void subMoney(int money){
        if(count-money < 0){
            System.out.println("余额不足!");
            return;
        }
        count -=money;
        System.out.println(+System.currentTimeMillis()+"取出："+money);
    }

    //查询
    public void lookMoney(){
        System.out.println("账户余额："+Thread.currentThread().getName()+count);
    }
}


 class VolatileTest {
    private int i = 0;
    private int j = 0;
    public long exceptionCount = 0;

    //线程1调用这个方法
    public void f1() {
        for(int k = 0; k < Integer.MAX_VALUE; k++) {
            i = k;
            j = i;
        }

    }

    //线程2调用这个方法
    public void f2() {
        while (true) {
            //如果线程1对i,j的修改都是可见的，就不会出现j>i的情况了，一旦出现这种情况就能说明线程1对i,j的修改不可见
            if(j > i) {
                exceptionCount++;
                break;
            }
        }
    }

    public static void main(String[] args) {
        final VolatileTest volatileTest = new VolatileTest();
        //线程1
        new Thread(new Runnable() {
            @Override
            public void run() {
                volatileTest.f1();
            }
        }).start();
        //线程2
        new Thread(new Runnable() {
            @Override
            public void run() {
                volatileTest.f2();
            }
        }).start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        System.out.println("exceptionCount:" + volatileTest.exceptionCount);
    }
}