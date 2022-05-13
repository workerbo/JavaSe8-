###  一级缓存

- MyBatis的一级查询缓存（也叫作本地缓存）是基于org.apache.ibatis.cache.impl.PerpetualCache 类的 HashMap本地缓存，其作用域是SqlSession，myBatis 默认一级查询缓存是开启状态，且不能关闭。
- 在同一个SqlSession中两次执行相同的 sql查询语句，第一次执行完毕后，会将查询结果写入到缓存中，第二次会从缓存中直接获取数据，而不再到数据库中进行查询，这样就减少了数据库的访问，从而提高查询效率。
- 基于PerpetualCache 的 HashMap本地缓存，其存储作用域为 Session，PerpetualCache 对象是在SqlSession中的Executor的localcache属性当中存放，当 Session flush 或 close 之后，该Session中的所有 Cache 就将清空。

- cutor的localcache属性当中存放，当 Session flush 或 close 之后，该Session中的所有 Cache 就将清空。

```
 //创建一个缓存对象，PerpetualCache并不是线程安全的
    //但SqlSession和Executor对象在通常情况下只能有一个线程访问，而且访问完成之后马上销毁。也就是session.close();
    this.localCache = new PerpetualCache("LocalCache");
```



###### 总结

1. 一级缓存只在同一个SqlSession中共享数据
2. 在同一个SqlSession对象执行相同的sql并参数也要相同，缓存才有效。
3. 如果在SqlSession中执行update/insert/detete语句或者**session.close();**的话，SqlSession中的executor对象会将一级缓存清空。

### 二级缓存

- 二级缓存与一级缓存其机制相同，默认也是采用 PerpetualCache，HashMap存储，不同在于其存储作用域为 Mapper(Namespace)，每个Mapper中有一个Cache对象，存放在Configration中，并且将其放进当前Mapper的所有MappedStatement当中，并且可自定义存储源，如 Ehcache。

- Mapper级别缓存，定义在Mapper文件的<cache>标签并需要开启此缓存

二级缓存构建在一级缓存之上，在收到查询请求时，MyBatis 首先会查询二级缓存。若二级缓存未命中，再去查询一级缓存。一个Mapper中有一个Cache，相同Mapper中的MappedStatement公用一个Cache，一级缓存则是和 SqlSession 绑定。这种情况下，会存在并发问题，比喻多个不同的SqlSession 会同时执行相同的SQL语句，参数也相同，那么CacheKey是相同的，就会造成多个线程并发访问相同CacheKey的值





 // Cache添加到Configuration中，这里会将生成的Cache包装到对应的MappedStatement

#### CacheKey

​     应该考虑使用一种复合对象，能涵盖可影响查询结果的因子。在 MyBatis 中，这种复合对象就是 CacheKey。CacheKey 最终要作为键存入 HashMap，因此它需要覆盖 equals 和 hashCode 方法。



**二级缓存的生效必须在session提交或关闭之后才会生效**

存储查询结果时，并不直接存储查询结果到共享缓存中，而是先存储到事务缓存中，也就是 entriesToAddOnCommit 集合。当事务提交时，再将事务缓存中的缓存项转存到共享缓存中。