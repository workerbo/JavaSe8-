#### JVM

##### 垃圾回收器

-   垃圾回收的基本原理（算法思路都是一致的：把所有对象组成一个集合，或可以理解为树状、图结构，从树根开始找，只要可以找到的都是活动对象，如果找不到，这个对象就被回收了）
    主要处理步骤：将线程挂起→确定roots→创建reachable objects graph→对象回收→heap压缩→指针修复。
    标记 --> 压缩【使内存空间变得连续】
    https://www.jianshu.com/p/5261a62e4d29

-   分代收集，是基于这样一个事实：不同的对象的生命周期是不一样的。因此，可以将不同生命周期的对象分代，不同的代采取不同的回收算法（4.1-4.3）进行垃圾回收（GC），以便提高回收效率。
    整理算法：标记-整理  copy
    在年轻代【copy】中经历了N次垃圾回收后仍然存活的对象，就会被放到年老代【标记】中
    对永久代的回收主要回收两部分内容：废弃常量和无用的类。

- 对象在内存中的布局     Object对象16字节 
  markword  记录锁的信息 GC信心 8个字节       头1 
  类型指针  指向哪个类  4个字节  头2
  实例数据
  对齐（填充能被8整除）

  压缩类型指针 压缩对象指针  8字节压到4字节
  col工具 打印内存布局情况 

  并发问题源头：三大特性:可见性、原子性、有序性。
  可见性缓存再加上多颗CPU并发【可见性问题】
  原子性指的就是一个或多个操作在CPU中执行不会被中断的特性称为原子性。【可能导致修改被覆盖】
  指令重排
  https://www.jianshu.com/p/e662bb611360

  锁的本质实现就是:当一个线程获取到锁就是把它的线程id写入锁对象的对象头,来判断唯一。

  对一个资源的保护必须是同一个锁！但是呢如果是没有关联性的资源我们要细分粒度

  synchronized修饰的是静态方法，那表明锁的是当前的Class对象，如果修饰的不是静态方法，那表面锁的是当前的实例对象this。

  通知-等待机制 提升效率

  并发编程领域，有两大核心问题:互斥、同步

Synchronized进入阻塞之后就没办法唤醒它，
  所以Lock【破环不可抢占的条件：避免死锁】提供了这三种方式来弥补Synchronized的不足使得我们能写出更加安全，健壮的代码。


原子类的实现原理是volatile+cas

Semaphore 用来多线程互斥问题，相对于synchronized和Lock来说它允许多个线程访问一个临界区

有了Synchronized为什么还需要Lock：避免死锁  共享读

由于finalize()【释放非Java 资源】方法的调用时机具有不确定性，从一个对象变得不可到达开始，到finalize()方法被执行，所花费的时间这段时间是任意长的。我们并不能依赖finalize()方法能及时的回收占用的资源，可能出现的情况是在我们耗尽资源之前，gc却仍未触发，因而通常的做法是提供显示的close()方法供客户端手动调用




内部类使得多重继承的解决方案变得完整，内部类对象是外部类的成员变量。在任何非静态内部类中，都不能有静态数据
1.内部类与外部类可以方便的访问彼此的私有域
2.内部类是另外一种封装，对外部的其他类隐藏。

成员内部类是依附外部类的，只有创建了外部类才能创建内部类。

模拟多线程：thread.sleep(5000) 然后多次运行 



创建对象的并发考虑上，一种是分配内存空间动作的同时进行同步处理，CAS配上失败重试的方法保证更新操作的原子性。

