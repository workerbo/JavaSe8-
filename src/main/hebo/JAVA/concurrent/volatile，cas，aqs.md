### volatile

- 有序性：双重检验锁定：分配地址（volatile写）和初始化（普通写）乱序导致判断if(instance==null)时就会为true。【此时可见性是前提】

  ### 为什么在同步代码块内还要再检验一次？

  第一个if减少性能开销，第二个if避免生成多个对象实例。

  现有三个线程A，B，C，假设线程A和线程B同时调用getSingleton()时，判断第一层if判断都为空，这时线程A先拿到锁，线程B在代码块外层等待。线程A进行第二层if判断，条件成立后new了一个新对象，创建完成，释放锁，线程B拿到锁，进行第二层if判断，singleton不为空，直接返回singleton释放锁，避免生成多个对象实例。线程线C调用getSingleton时第一层判断不成立，直接拿到singleton对象返回，避免进入锁，减少性能开销。

  ### 为什么要用volatile关键字？

  singleton = new Singleton();这行代码并不是一个原子指令，可能会在JVM中进行指令重排；

  new 实例背后的指令，我们通过使用 javap -c指令，查看字节码如下：

  ```
  　　 // 创建 Singleton 对象实例，分配内存
         0: new           #5                 
      // 复制栈顶地址，并再将其压入栈顶
         3: dup
      // 调用构造器方法，初始化 Singleton对象
         4: invokespecial #6         // Method "<init>":()V
      // 存入局部方法变量表
         7: astore_1
  ```

  从字节码可以看到创建一个对象实例，可以分为三步：

  (1)分配对象内存(给singleton分配内存)。

  (2)调用构造器方法，执行初始化（调用 Singleton 的构造函数来初始化成员变量）。

  (3)将对象引用赋值给变量(执行完这步 singleton 就为非 null 了)。

  在 JVM 的即时编译器中存在指令重排序的优化。指令重排并不影响单线程内的执行结果，但是在多线程内可能会影响结果。也就是说上面的2和3的顺序是不能保证的，但是并不会重排序 1 的顺序，因为 2,3 指令需要依托 1 指令执行结果。最终的执行顺序可能是 1-2-3 也可能是 1-3-2。

  **1-3-2的情况**

  **![img](https://www.programminghunter.com/images/628/db/dbe4a9f5aa793f9dc48d2561fd9d2b0c.png)**

  上面多线程执行的流程中，如果线程A获取到锁进入创建对象实例，这个时候发生了指令重排序。当线程A 执行到 t3 时刻(singleton已经非null了，但是却没有初始化)，此时线程 B 抢占了，由于此时singleton已经不为 Null，会直接返回 singleton对象，然后使用singleton对象，然而该对象还未初始化，就会报错。我们只需将 singleton 变量声明成 volatile 就可以禁止指令重排，避免这种现象发生。 

     单例模式主要应用在避免重复创建对象造成内存浪费。

  

  **如果在本线程内观察，所有的操作都是有序的；如果在一个线程观察另一个线程，所有的操作都是无序的**。

  为了实现volatile内存语义时，编译器在生成字节码时，会在指令序列中插入内存屏障来禁止特定类型的**处理器重排序**。

  对于有数据依赖的指令不会重排序，这在单线程环境中是安全的。但是在多线程中，无数据依赖的指令重排序依旧会产生问题。

  JMM实现java正确的多线程编程对底层无数据依赖的指令重排序提供了上层约束（关键字和lock）；

  cas保证了不会覆盖，volatile保证可见性。【避免脏读】

  在锁常被短暂持有的场景下，线程阻塞挂起导致CPU上下文频繁切换，这可用自旋锁解决；但自旋期间它占用CPU空转，因此不适用长时间持有锁的场景

  

  volatile保证原子性，必须符合以下两条规则：

1. **运算结果并不依赖于变量的当前值，或者能够确保只有一个线程修改变量的值；**
  2. **变量不需要与其他的状态变量共同参与不变约束**



可见性
可见性与Java的内存模型有关，模型采用缓存与主存的方式对变量进行操作，也就是说，每个线程都有自己的缓存空间，对变量的操作都是在缓存中进行的，之后再将修改后的值返回到主存中，这就带来了问题，有可能一个线程在将共享变量修改后，还没有来的及将缓存中的变量返回给主存中，另外一个线程就对共享变量进行修改，那么这个线程拿到的值是主存中未被修改的值，这就是可见性的问题。

volatile很好的保证了变量的可见性，变量经过volatile修饰后，对此变量进行写操作时，汇编指令中会有一个LOCK前缀指令，这个不需要过多了解，但是加了这个指令后，会引发两件事情：

将当前处理器缓存行的数据写回到系统内存
这个写回内存的操作会使得在其他处理器缓存了该内存地址无效
什么意思呢？意思就是说当一个共享变量被volatile修饰时，它会保证修改的值会立即被更新到主存，当有其他线程需要读取时，它会去内存中读取新值，这就保证了可见性。

原子性
问题来了，既然它可以保证修改的值立即能更新到主存，其他线程也会捕捉到被修改后的值，那么为什么不能保证原子性呢？
首先需要了解的是，Java中只有对基本类型变量的赋值和读取是原子操作，如i = 1的赋值操作，但是像j = i或者i++这样的操作都不是原子操作，因为他们都进行了多次原子操作，比如先读取i的值，再将i的值赋值给j，两个原子操作加起来就不是原子操作了。

所以，如果一个变量被volatile修饰了，那么肯定可以保证每次读取这个变量值的时候得到的值是最新的，但是一旦需要对变量进行自增这样的非原子操作，就不会保证这个变量的原子性了。

举个栗子

一个变量i被volatile修饰，两个线程想对这个变量修改，都对其进行自增操作也就是i++，i++的过程可以分为三步，首先获取i的值，其次对i的值进行加1，最后将得到的新值写会到缓存中。
线程A首先得到了i的初始值100，但是还没来得及修改，就阻塞了，这时线程B开始了，它也得到了i的值，由于i的值未被修改，即使是被volatile修饰，主存的变量还没变化，那么线程B得到的值也是100，之后对其进行加1操作，得到101后，将新值写入到缓存中，再刷入主存中。根据可见性的原则，这个主存的值可以被其他线程可见。
问题来了，线程A已经读取到了i的值为100，也就是说读取的这个原子操作已经结束了，所以这个可见性来的有点晚，线程A阻塞结束后，继续将100这个值加1，得到101，再将值写到缓存，最后刷入主存，所以即便是volatile具有可见性，也不能保证对它修饰的变量具有原子性。



#### CAS

- CAS比较交换的过程可以通俗的理解为CAS(V,O,N)，包含三个值分别为：**V 内存地址存放的实际值；O 预期的值（旧值）；N 更新的新值**。当V和O相同时，也就是说旧值和内存中实际的值相同表明该值没有被其他线程更改过，即该旧值O就是目前来说最新的值了，自然而然可以将新值N赋值给V。
- 在内存中的实现就是每个线程的工作空间都有自己的数据，他们进行写数据时会去看主内存中看一下主内存的值是不是和自己工作空间的值相等如果不相等返回false，不赋值，如果相等直接进行赋值，更新主内存的值。这个类操作的时候不可以被打断，本身就是原子操作。cas是一个完全依赖于硬件的功能，是CPU的原子指令，不会造成所谓的数据不一致问题。它配合do while循环，在while条件里面就是cas比较并转换的结果，是否修改成功，他是类似一个自旋锁的东西，只要比较不成功，他就一直循环，在高并发的情况下，如果cas长时间不成功，非常耗费计算机CPU性能。【单独的volatile可能计算赋值不是原子性的】
- **1. ABA问题**  **2. 自旋时间过长**   **3. 只能保证一个共享变量的原子操作**【atomic中提供了AtomicReference来保证引用对象之间的原子性。】
- 而且现在CPU都内置了对CAS原子性操作的支持。

##### 应用

###### 原子类

###### 原子更新基本类型或引用类型

一是它的成员变量value是被volatile修饰的，value变量存储的就是这个对象具体的数值。volatile虽然不能保证原子性，但是可以保证可见性，即只要有线程对它修饰的数据进行了修改，其他线程读取这个数据的时候读取就是修改后的数据

AtomicInteger的compareAndSet方法以及其他的原子类都是依赖unsafe的compareAndSwap方法实现的，即CAS。

```
public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
}
```



//如果当前引用 等于 预期值并且 当前版本戳等于预期版本戳, 将更新新的引用和新的版本戳到内存
public boolean compareAndSet(V   expectedReference,
                                 V   newReference， int expectedStamp, int newStamp)

###### 原子更新数组中的元素

###### 原子更新对象中的字段

###### 高性能原子类

是java8中增加的原子类，它们使用分段的思想，把不同的线程hash到不同的段上去更新，最后再把这些段的值相加得到最终的值

#### AQS



通过lockspport阻塞唤醒线程，线程通过cas+轮询进入阻塞队列、获取锁芯的一个框架。通过原子 CAS 操作来控制某时只有 个线程可以追加元素到队列末尾 进行 CAS 竞争失败的线程会通过循环 次次尝试进行 CAS 操作，直到 CAS 成功才会返回

###### 多线程访问共享资源的同步器框架 全称 AbstractQueuedSynchronizers

同步器数据结构

- **节点的数据结构，即AQS的静态内部类Node,节点的等待状态等信息**；【双链表】
- **同步队列是一个双向队列，AQS通过持有头尾指针管理同步队列**
- 通过一个volatile int 记录同步状态。

- exclusiveOwnerThread 持有资源的线程

  对这四种的操作都要在cas的方法中。

  Node节点的五种状态，默认为零，在中断的时候取消状态1，在共享模式下，已经唤醒的中间状态为传播，在添加到末尾时停靠在signal节点之后【将0转化】。**在同步队列当中被阻塞**，头节点是获取资源的线程，在被唤醒的时候开始自旋获取锁。

  等待队列的线程被signal之后进入同步队列。


AQS 使用一个整型的 volatile 变量（命名为 state）来维护同步状态

Thread.interrupted()用于清除中断





- **AQS只是一个框架，具体资源的获取/释放方式交由锁的自定义同步器去实现**【获取和释放同步状态】

  ```java
  自定义同步器实现时主要实现以下几种方法：[阻塞队列的维护\同步状态的循环获取已经设计好了]
  
  isHeldExclusively()：该线程是否正在独占资源。只有用到condition才需要去实现它。
  tryAcquire(int)：独占方式。尝试获取资源，成功则返回true，失败则返回false。
  tryRelease(int)：独占方式。尝试释放资源，成功则返回true，失败则返回false。
  tryAcquireShared(int)：共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
  tryReleaseShared(int)：共享方式。尝试释放资源，成功则返回true，失败则返回false。
      
  ```

  

  

- 取锁失败进行入队操作，获取锁成功进行出队操作。【AQS框架已经设计好了】

  ```java
  acquire方法逻辑
  tryAcquire()尝试直接去获取资源，如果成功则直接返回；
  addWaiter()将该线程加入等待队列的尾部，并标记为独占模式【节点的两种模式】；
  acquireQueued()使线程在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回中断标识true，否则返回false。【CAS自旋（解决了原子性问题，失败则重试）volatile变量】
  selfInterrupt()自己产生了之前的中断状态
      
  (01) 先是通过tryAcquire()尝试获取锁。获取成功的话，直接返回；尝试失败的话，再通过acquireQueued()获取锁。
  (02) 尝试失败的情况下，会先通过addWaiter()来将“当前线程”加入到"CLH队列"末尾；然后调用acquireQueued()，在CLH队列中排序等待获取锁，在此过程中，线程处于休眠状态。直到获取锁了才返回。 如果在休眠等待过程中被中断过，则调用selfInterrupt()来自己产生一个中断。
  ```

  

- 获取独占式锁失败的线程包装成Node然后插入同步队列的过程

  - **在当前线程是第一个加入同步队列时，调用compareAndSetHead(new Node())方法，完成链式队列的头结点的初始化**；【**带头结点的链式存储结构**】
  - **自旋不断尝试CAS尾插入节点直至成功为止**

- 

- ```
  acquireInterruptibly立即响应【已经消除了中断标识，线程状态并标记为Cancel状态】lock支持在获取锁的过程中响应中断（在其他线程锁释放之后，尝试获取锁的过程中检查有中断立即   抛出中断异常，停止获取锁，finnally节点状态变为取消状态）
  acquire方法是设置中断标识供获取锁之后处理（忽略中断）  一个布尔字段标识
  ```

  ```java
     private final boolean parkAndCheckInterrupt() {
          LockSupport.park(this);
          //判断当前线程是否中断,且清除标志。
          return Thread.interrupted();
      }
      
  if (shouldParkAfterFailedAcquire(p, node) &&
                      parkAndCheckInterrupt())
                      throw new InterruptedException();
              }
  
  finally {
              //线程响应中断处理
              if (failed)
                  cancelAcquire(node);
          }
  ```

  共享模式与独占模式

  公平锁每次获取到锁为同步队列中的第一个节点，**保证请求资源时间上的绝对顺序**，而非公平锁有可能刚释放锁的线程下次继续获取该锁，则有可能导致其他线程永远无法获取到锁，**造成“饥饿”现象** 【可能有上下文切换】

##### Condition

**Object的wait和notify/notify是与对象监视器配合完成线程间的等待/通知机制，而Condition与Lock配合完成等待通知机制，前者是java底层级别的，后者是语言级别的，具有更高的可控制性和扩展性**。

**ConditionObject**该类是AQS（[AQS的实现原理的文章](https://juejin.im/post/5aeb07ab6fb9a07ac36350c8)）的一个内部类

当当前线程调用condition.await()方法后，会使得当前线程释放lock然后加入到等待队列中，直至被signal/signalAll后会使得当前线程从等待队列中移至到同步队列中去，直到获得了lock后才会从await方法返回，或者在等待时被中断会做中断处理

正如在基础篇中讲解的， noti fy wait ，是配 synchroni ze 内置锁实现线程间同步 的基础设施 样，条件变 signal、await 方法也 用来配合锁 （使 AQ 实现的锁〉 线程间同 基础设施。【生产者、消费者模型】



#### LockSupport

LockSupport.park()、unpark()是JUC中LockSupport类中提供的一个用于线程挂起和唤醒的方法。

这是因为`park和unpark`会对每个线程维持一个许可（boolean值）

1. unpark调用时，如果当前线程还未进入park，则许可为true【不可重叠】
2. park调用时，判断许可是否为true，如果是true，则继续往下执行；如果是false，则等待，直到许可为true

【中断、虚假唤醒也可以唤醒，所以park时条件要循环判断】

###### LockSupport.park unpark 和wait、notify的区别

1、LockSupport.park()和unpark()随时随地都可以调用。而wait和notify只能在synchronized代码段中调用

2、LockSupport允许先调用unpark(Thread t)，后调用park()。,如果thread1先调用unpark(thread2)，然后线程2后调用park()，线程2是不会阻塞的。
如果线程1先调用notify，然后线程2再调用wait的话，线程2是会被阻塞的。





#### 常见的java锁

公平锁是按照aqs队列顺序获取锁【后面加入竞争锁的线程必须加入aqs等待】

读写锁【适合读多写少的场景】  当有线程获取读锁，则不能获取写锁【不能确定是哪个线程获取了读锁】。

CountDownLatch 构造函数中有一个 count 参数，表示有多少个线程需要被等待，对这个变量的修改是在其它线程中调用 countDown 方法，每一个不同的线程调用一次 countDown 方法就表示有一个被等待的线程到达，count 变为 0 时，latch（门闩）就会被打开，处于等待状态的那些线程接着可以执行；

CyclicBarrier也叫同步屏障，在JDK1.5被引入，可以让一组线程达到一个屏障时被阻塞，直到最后一个线程达到屏障时，所以被阻塞的线程才能继续执行。

Semaphore也叫信号量，在JDK1.5被引入，可以用来控制同时访问特定资源的线程数量，通过协调各个线程，以保证合理的使用资源。



###### 应用

从同步集合（synchronized）到并发集合【无锁或者轻量级锁】，提高了性能。

两种方式实现同步1.cas+轮询 2.锁【关键字或者并发包中的锁】（并发包中是在阻塞队列并且被lockspport支持阻塞在原地，在不被阻塞时通过cas+轮询获取锁，acquireQueued(addWaiter(Node.EXCLUSIVE), arg))）