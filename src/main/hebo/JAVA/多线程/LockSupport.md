LockSupport类是Java6(JSR166-JUC)引入的一个类，提供了基本的[线程同步](https://so.csdn.net/so/search?q=线程同步&spm=1001.2101.3001.7020)原语。LockSupport实际上是调用了Unsafe类里的函数，归结到Unsafe里，只有两个函数：

```
  public native void unpark(Thread jthread);
  public native void park(boolean isAbsolute, long time);
  
```

unpark函数为线程提供“许可(permit)”，线程调用park函数则等待“许可”。这个有点像信号量，但是这个“许可”是不能叠加的，“许可”是一次性的。



比如线程B连续调用了三次unpark函数，当线程A调用park函数就使用掉这个“许可”，如果线程A再次调用park，则进入等待状态。

注意，unpark函数可以先于park调用。比如线程B调用unpark函数，给线程A发了一个“许可”，那么当线程A调用park时，它发现已经有“许可”了，那么它会马上再继续运行。

实际上，park函数即使没有“许可”，有时也会无理由地返回，这点等下再解析。

##### park和unpark的灵活之处

**park/unpark模型真正解耦了线程之间的同步，线程之间不再需要一个Object或者其它变量来存储状态，不再需要关心对方的状态。**

在Java5里是用wait/notify/notifyAll来同步的。wait/notify机制有个很蛋疼的地方是，比如**线程B要用notify通知线程A，那么线程B要确保线程A已经在wait调用上等待了，否则线程A可能永远都在等待。**编程的时候就会很蛋疼。



