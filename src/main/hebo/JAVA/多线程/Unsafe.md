Java Unsafe 包中的 **compareAndSwapObject** 方法，也就是比较并交换 Java Object 

在 Java API 中我们可以看到该方法具有四个参数。

- obj ：包含要修改的字段对象；
- offset ：字段在对象内的偏移量；
- expect ： 字段的期望值；
- update ：如果该字段的值等于字段的期望值，用于更新字段的新值；

```
public native boolean compareAndSwapObject(Object obj, long offset, Object expect, Object update);
```

底层的实现也比较简单，总结来说：

- 通过我们传入的字段在对象中的偏移量来获取到字段的地址（对象首地址 + 字段在对象中的偏移量）；
- 然后调用 **CompareAndSwap** 方法比较字段的地址是否与我们期望的地址相等，如果相等则使用我们传入的新地址更新字段的地址；