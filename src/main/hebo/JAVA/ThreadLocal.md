

threadLocal通过get方法Thread.currentThread()获取到当前线程的threadLocals【threadLocalMap】，以threadLocal的hash值查找Entry对象，在以threadLocal为key判断后获取当前线程变量value

## 内存泄露

广义并通俗的说，就是：不再会被使用的对象或者变量占用的内存不能被回收，就是内存泄露。

## 强引用与弱引用

**强引用**，使用最普遍的引用，一个对象具有强引用，不会被垃圾回收器回收。当内存空间不足，Java虚拟机宁愿抛出OutOfMemoryError错误，使程序异常终止，也不回收这种对象。

**如果想取消强引用和某个对象之间的关联，可以显式地将引用赋值为null，这样可以使JVM在合适的时间就会回收该对象。**

**弱引用**，JVM进行垃圾回收时，无论内存是否充足，都会回收被弱引用关联的对象。在java中，用java.lang.ref.WeakReference类来表示。可以在缓存中使用弱引用。

## GC回收机制-如何找到需要回收的对象

JVM如何找到需要回收的对象，方式有两种：

- 引用计数法：每个对象有一个引用计数属性，新增一个引用时计数加1，引用释放时计数减1，计数为0时可以回收，
- 可达性分析法：从 GC Roots 开始向下搜索，搜索所走过的路径称为引用链。当一个对象到 GC Roots 没有任何引用链相连时，则证明此对象是不可用的，那么虚拟机就判断是可回收对象。

> 引用计数法，可能会出现A 引用了 B，B 又引用了 A，这时候就算他们都不再使用了，但因为相互引用 计数器=1 永远无法被回收。

## ThreadLocal的内存泄露分析



## 实现原理

```text
static class ThreadLocalMap {

    static class Entry extends WeakReference<ThreadLocal<?>> {
        /** The value associated with this ThreadLocal. */
        Object value;

        Entry(ThreadLocal<?> k, Object v) {
            super(k);
            value = v;
        }
    }
    ...
   }
```

ThreadLocal的实现原理，每一个Thread维护一个ThreadLocalMap，key为使用**弱引用**的ThreadLocal实例，value为线程变量的副本。这些对象之间的引用关系如下,

![img](https://pic2.zhimg.com/v2-e2d4b8eac152596232d3e32313927d59_b.jpg)

> 实心箭头表示强引用，空心箭头表示弱引用【弱引用保证了栈中没有强引用ThreadLocal时可以被回收，然后需要调用方法区设置value=null】

## ThreadLocal 内存泄漏的原因

从上图中可以看出，threadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal不存在外部**强引用**时，Key(ThreadLocal)势必会被GC回收，这样就会导致ThreadLocalMap中key为null， 而value还存在着强引用，只有thead线程退出以后,value的强引用链条才会断掉。

但如果当前线程再迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：

> Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value

永远无法回收，造成内存泄漏。



## key 使用强引用

当threadLocalMap的key为强引用回收ThreadLocal时，因为ThreadLocalMap还持有ThreadLocal的强引用，如果没有手动删除，ThreadLocal不会被回收，导致Entry内存泄漏。

## key 使用弱引用

当ThreadLocalMap的key为弱引用回收ThreadLocal时，由于ThreadLocalMap持有ThreadLocal的弱引用，即使没有手动删除，ThreadLocal也会被回收。当key为null，在下一次ThreadLocalMap调用set(),get()，remove()方法的时候会被清除value值。

## ThreadLocalMap的remove()分析

在这里只分析remove()方式，其他的方法可以查看源码进行分析：

```text
private void remove(ThreadLocal<?> key) {
    //使用hash方式，计算当前ThreadLocal变量所在table数组位置
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
    //再次循环判断是否在为ThreadLocal变量所在table数组位置
    for (Entry e = tab[i];
         e != null;
         e = tab[i = nextIndex(i, len)]) {
        if (e.get() == key) {
            //调用WeakReference的clear方法清除对ThreadLocal的弱引用
            e.clear();
            //清理key为null的元素
            expungeStaleEntry(i);
            return;
        }
    }
}
```

再看看清理key为null的元素expungeStaleEntry(i):

```text
private int expungeStaleEntry(int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;

    // 根据强引用的取消强引用关联规则，将value显式地设置成null，去除引用
    tab[staleSlot].value = null;
    tab[staleSlot] = null;
    size--;

    // 重新hash，并对table中key为null进行处理
    Entry e;
    int i;
    for (i = nextIndex(staleSlot, len);
         (e = tab[i]) != null;
         i = nextIndex(i, len)) {
        ThreadLocal<?> k = e.get();
        //对table中key为null进行处理,将value设置为null，清除value的引用
        if (k == null) {
            e.value = null;
            tab[i] = null;
            size--;
        } else {
            int h = k.threadLocalHashCode & (len - 1);
            if (h != i) {
                tab[i] = null;
                while (tab[h] != null)
                    h = nextIndex(h, len);
                tab[h] = e;
            }
        }
    }
    return i;
}
```

## 总结

ThreadLocal内存泄漏的根源是：由于ThreadLocalMap的生命周期跟Thread一样长，如果没有手动删除对应key就会导致内存泄漏，而不是因为弱引用。

使用**弱引用**可以多一层保障：弱引用ThreadLocal不会内存泄漏，对应的value在下一次ThreadLocalMap调用set(),get(),remove()的时候会被清除。否则value值也会泄漏。



## ThreadLocal正确的使用方法【避免内存泄漏】

- 每次使用完ThreadLocal都调用它的remove()方法清除数据（防止key在GC被回收后，value无法被访问到造成内存泄漏）
- 将ThreadLocal变量定义成private static，这样就一直存在ThreadLocal的强引用，也就能保证任何时候都能通过ThreadLocal的弱引用访问到Entry的value值，进而清除掉 。

[清除分析](https://www.cnblogs.com/cy0628/p/15086201.html)





threadlocal跨线程传递解决方案

线程之间是父子关系：

InheritableThreadLocal实现父子线程之间的数据传递。InheritableThreadLocal实例会使得线程Thread中的成员变量inheritableThreadLocal这个Map有值，而在子线程实例化的过程中会将父线程的ThreadLocalMap复制到自己的ThreadLocalMap里面来。

创建子线程时，当父亲线程中对inherThreadLocals进行了赋值，就会把当前线程的本地变量（也就是父线程的inherThreadLocals）进行createInheritedMap方法操作。

ThreadLocal并不是为了解决线程安全问题，而是提供了一种将变量绑定到当前线程的机制，类似于隔离的效果。ThreadLocal跟线程安全基本不搭边：线程安全or不安全取决于绑上去的实例是怎样的：

每个线程独享一份new出来的实例 -> 线程安全
多个线程共享一份“引用类型”实例 -> 线程不安全
**`ThreadLocal`最大的用处就是用来把实例变量共享成全局变量**，在程序的任何方法中都可以访问到该实例变量而已。

```
public static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new InheritableThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }
};
```


这么处理后再使用DateFormat这个实例，就是绝对安全的。理由是每次调用set方法进行和线程绑定的时候，都是new一个新的SimpleDateFormat实例，而并非全局共享一个，不存在了数据共享那必然就线程安全喽。



在实际开发中，多线程就**离不开线程池**的使用，因为线程池能够**复用线程**，减少线程的频繁创建与销毁。倘若合格时候使用`InheritableThreadLocal`来传递数据，那么线程池中的线程拷贝的数据始终来自于**第一个提交任务的外部线程**，这样非常容易造成**线程本地变量混乱**【`Thread#init`方法在Thread的构造方法中被调用。复用的话没有被调用】



TransmittableThreadLocal继承于InheritableThreadLocal，并拥有了 InheritableThreadLocal对子线程传递上下文的特性，只需解决线程池上下文传递问题。它使用TtlRunnable包装了任务的运行，被包装的run方法执行异步任务之前，会使用replay进行设置父线程里的本地变量给当前子线程，任务执行完毕，会调用restore恢复该子线程原生的本地变量，当然重点还是稍显复杂的上下文管理部分。

[比对](https://juejin.cn/post/6998552093795549191)

ThreadLocal：父子线程不会传递threadLocal副本到子线程中

InheritableThreadLocal：在子线程创建的时候，父线程会把threadLocal拷贝到子线中（但是线程池的子线程不会频繁创建，就不会传递信息）

TransmittableThreadLocal：解决了2中线程池无法传递线程本地副本的问题，在构造类似Runnable接口对象时进行初始化。

两种使用方式：1.代码包裹 2.javaagent

