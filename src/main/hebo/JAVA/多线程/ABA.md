##  AtomicReference的ABA问题

我们看到compareAndSet的源码：

```
public final boolean compareAndSet(V expect, V update) {
    return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
}
```

解决ABA问题： AtomicStampedReference<T>