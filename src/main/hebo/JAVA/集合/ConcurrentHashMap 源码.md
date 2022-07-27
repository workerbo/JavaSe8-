ConcurrentHashMap 的底层数据结构，和方法的实现细节和 HashMap 大体一致，但两者在类结构上却没有任何关联。都实现了 Map 接口，继承了 AbstractMap 抽象类，所以大多数的方法也都是相同的，HashMap 有的方法，ConcurrentHashMap 几乎都有，所以当我们需要从 HashMap 切换到 ConcurrentHashMap 时，无需关心两者之间的兼容问题。

ConcurrentHashMap 不支持 key 或者 value 为 null ，因为 ConcurrentHashMap 是用于多线程的 ，如果`ConcurrentHashMap.get(key)`得到了 null ，这就无法判断，是映射的value是 null ，还是没有找到对应的key而为 null ，就有了二义性。

Hashtable 是使用 synchronized来实现线程安全的，给整个哈希表加了一把大锁，多线程访问时候，只要有一个线程访问或操作该对象，那其他线程只能阻塞等待需要的锁被释放，在竞争激烈的多线程场景中性能就会非常差！

###### ConcurrentHashMap 迭代器是弱一致性。

 ConcurrentHashMap的get，clear，iterator 都是弱一致性的。

ConcurrentHashMap 的迭代器创建后，就会按照哈希表结构遍历每个元素，但在遍历过程中，内部元素可能会发生变化，如果变化发生在已遍历过的部分，迭代器就不会反映出来，而如果变化发生在未遍历过的部分，迭代器就会发现并反映出来，这就是弱一致性。

