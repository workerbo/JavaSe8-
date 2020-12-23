public class SynchronizedDemo {
    public static void main(String[] args) {
        synchronized (SynchronizedDemo.class) {
                   method();
        }
       
    }

    private synchronized  static void method() {
    }
}