##### java并发编程之美

[并发编程的锁原理]()

**线程安全 工作原理**： jvm中有一个main memory对象，每一个线程也有自己的working memory，一个线程对于一个变量variable进行操作的时候， 都需要在自己的working memory里创建一个copy,操作完之后再写入main memory。 

当多个线程操作同一个变量variable，就可能出现不可预知的结果。 

而用synchronized的关键是建立一个监控monitor，这个monitor可以是要修改的变量，也可以是其他自己认为合适的对象(方法)，然后通过给这个monitor加锁来实现线程安全，每个线程在获得这个锁之后，要执行完加载load到working memory 到 use && 指派assign 到 存储store 再到 main memory的过程。才会释放它得到的锁。这样就实现了所谓的线程安全。

 JVM 级别。大多数现代处理器对并发对 某一硬件级别提供支持，通常以 compare-and-swap （CAS）指令形式。CAS 是一种低级别的、细粒度的技术，它允许多个线程更新一个内存位置，**同时能够检测其他线程的冲突并进行恢复**

低级实用程序

高级实用程序类



###### 处理器保证原子性

##### 1、总线锁保证原子性

使用处理器提供的一个LOCK#信号，当一个处理器在总线上输出此信号时，其它处理器的请求将被阻塞，那么该处理器就能独自共享内存。

##### 2、缓存锁保证原子性

“缓存锁定”指内存区域如果被缓存在处理器的缓存行中，并且在Lock操作期间被锁定，那么当它执行锁操作回写到内存时，处理器不需要在总线上声言LOCK#信号，而是修改内部的内存地址，通过缓存一致性机制保证操作的原子性。
 例外：当操作的数据不能被缓存在处理器内部，或操作的数据跨多个缓存行，处理器会调用总线锁定。

###### 缓存一致性

缓存一致性会阻止同时修改由两个以上处理器的内存区域数据，当其他处理器回写被锁定的缓存行数据时，会使其它处理器的缓存行无效。

###### 等待和通知

获取监视器锁后检查是否满足运行条件，不满足就放弃锁【判断条件用循环，防止虚假唤醒】

当一个线程调用一个共享变量 wait(）方法时， 该调用线程会被阻塞挂起， 直到发生 下面几件事情 才返回。

了当线程调用共享对象的 wait（）方法时，当前线程只 会释放当前共享对象的锁，当前线程持有的其他共享变量的监视器锁并不会被释放。

一个线程调用共享对象的 notify （）方法后，会唤醒一个在该共享变量上调用 wait 系列 方法后被挂起的线程。【notifyall会将共享变量上所有阻塞的线程都变为就绪状态】

在一个线程中调用其他线程的join方法，当前线程被阻塞。

Thread.sleep释放指定时间的cpu资源，不会释放锁。

当线程调用 sleep方法时调用线程会被阻塞挂起指定的时间，在这期间线程调度器不会去调度该线程 而调用 yield方法时，线程只是让出自己剩余的时间片，并没有被阻塞挂起，而是处于就绪状态，线程调度器下一次调度 时就有可能调度到当前线程执行。

Java 中的线程中断是 一种线程间的协作模式，通过设置线程的中断标志并不能直接终止该线程的执行 而是被中断的线程根据中断状态自行处理。

设置标志仅仅是设置标志 ，线程 A实际并没有被中断， 会继续往 执行 ，自行判断处理。如果线程A 因为调用了 wait 系列函数、join 方法或者 sleep方法阻塞挂起。则调用处抛出异常。【运行时中断（转到阻塞或者死亡）、阻塞时中断（转到运行时状态）】

intercepted方法获取的是当前线程中断标识情况并清除。isIntercepted获取调用对象的中断标识。

资源的有序性破坏 资源的请求并持有条件和环路等待条件 因此避免了死锁。

子线程的生命 周期并不受父线程的影响。这也说明了在用户线程还存在的情况下 JVM 进程并不会终止。

ThreadLocal是一个工具类，获取当前线程的ThreadLocalmap设置和取值。

在每个线程 都有 threadLocals 的成员变量 该变量类型为 HashMap， key 为我定义 ThreadLocal 的变量的 this引用， value 则为 使用 set 方法设置 。每个线程 变量存放在线程自己的内存变量 threadLocals 中， 如果当前线程一直不消亡 那么这些本地变量会一直存在 所以可能会造成内存溢出。 使用完毕后要记得 ThreadLocal remove 方法删除对应线程 threadLocals 中的 变量。

![image-20200914195300177](D:\Hand资料\截图图片\image-20200914195300177.png)

InheritableThreadLocal 继承自 ThreadLocal 其提供了一个特性，就是让子线程可以访问在父线程中设置的本地变量

###### synchronized 

进程存在方法区和堆，各个线程独立栈和程序计数器。

单CPU并发没有意义，多CPU同时存在并发和并行【线程数量多于CPU数量】。

共享资源，就是说该资源被多个线程所持有。

线程安全问题是指当多个线程同时读写一个共享资源并且没有任何同步措施时，导致出现脏数据或者其他不可预见的结果的问题。【至少一个线程修改了共享资源】

Java 内存模型规定，将所有的变量都存放在主内存中，当线程使用变量时，会把主内存里面的变量复制到工作内存，然后对工作内存的变量进行处理， 处理完后将变量值更新到主内存。每个核都有自己的多级缓存， 在有些架构里面还有一个所有 CPU 共享缓存。 那么 Java 内存模型里面的工作内 存，就对应这里的 Ll 或者 L2 缓存或者 CPU 寄存器。

**由于CPU一级缓存【各个CPU独立，二级缓存共享】的存在，导致内存不可见**【是由于线程的工作内存导致的】。

synchronized 【原子性、可见性】是排他锁。其他线程会被阻塞。

进入synchronized 块的内存语义是把在synchronized块内使用到的变量从线程的工作内存中清除。退出 synchronized 块的内存语义是把在synchronized 块内对共享变量修改刷新到主内存。

synchronized 会引起线程上下文切换并带来线程调度开销。【被阻塞挂起。进入阻塞队列】

synchronized 读写都要加锁。读之所以加是为了内存可见性。

###### volatile

【volatile 更加轻量】当线程写入了volatile 值时就等价于线程退出 synchronized 同步块（把 写入工作内存的变量值同步到主内存），读取 volatile 值时就相当于进入同步代码块，先清理本地内存变量值，再从主内存获取最新值）



volatile 不保证原子性，所以适合于直接赋值的时候。

保证变量在线程之间的可见性。可见性的保证是基于CPU的内存屏障指令，被JSR-133抽象为happens-before原则。

阻止编译时和运行时的指令重排。编译时JVM编译器遵循内存屏障的约束，运行时依靠CPU屏障指令来阻止重排。

**StoreStore屏障**将保障上面所有的普通写在volatile写之前刷新到主内存。【主要针对共享变量】

CPU一级缓存的缓存一致性协议。修改一个缓存行的变量，会使其他CPU的缓存行的所有变量失效。【缓存伪共享】

自旋锁则是，当前线程在获取锁时，如果发现锁已经被其他线程占有， 它不马上阻塞自己，在不放弃 CPU 使用权的情况下，多次尝试获取。【指定次数】

volatile满足多线程重排序语义

###### 重排序

重排序需要遵守happens-before规则【指前一个操作的执行结果必须对后一个操作可见】

###### [JMM](http://ifeve.com/java-memory-model-0/)

Java的并发采用的是共享内存模型，Java线程之间的通信总是隐式进行,同步是显式进行的。

在java中，所有实例域、静态域和数组元素存储在堆内存中，堆内存在线程之间共享（本文使用“共享变量”这个术语代指实例域，静态域和数组元素）

Java线程之间的通信由Java内存模型（本文简称为JMM）控制，JMM决定一个线程对共享变量的写入何时对另一个线程可见。

JMM的编译器重排序规则会禁止特定类型的编译器重排序（不是所有的编译器重排序都要禁止）。对于处理器重排序，JMM的处理器重排序规则会要求java编译器在生成指令序列时，插入特定类型的内存屏障（memory barriers，intel称之为memory fence）指令

###### juc

1.**CopyOnWriteArrayList**写时复制策略来保证 list 致性，而获取一修改一写 入三步操作并不是原子性的，所以在增删改的过程中都使用了独占锁。迭代器遍历的数组是一个快照【查询可能也是对某一时刻的快照查询（其他线程在查询中已经增删改成功）】，产生的弱一致性问题。

2.原子类-cas的应用【共享变量线程安全】   cas保证只有一个线程成功

2.**锁**【cas保证锁的线程安全】



![](D:\Hand资料\截图图片\微信截图_20200916173718.png)

如果 park 方法是因为被中断而返回，则忽略中断，并且重置中断标志，做个标记，然后再判断当前线程是不是队首元素或者当前锁是否己经被其 他线程获取，如果是则继续调用 park 方法挂起自己。【locked作用是防止除了unlock之外的唤醒】

当多个线程同时调用 lock.lock （）方法获取锁时，只有一个线程获取到了锁，其他线程会被转换为 Node 节点插入到 lock 锁对应的 AQS 阻塞（就绪）队列里面，并做自旋 CAS 尝试获取锁。

如果获取到锁的线程又调用了对应的条件变量的 await（）方法，则该线程会释放获取 到的锁，并被转换为 Node 节点插入到条件变量对应的条件队列里面。



lock中只有一个线程会获取锁，其他线程阻塞（就绪），unlock会释放锁，并激活阻塞队列的一个线程。


