在执行过程中，如果执行 SQL 是 `select for update`，则会使用 SelectForUpdateExecutor 类，如果执行方法中带有 `@GlobalTransactional` or `@GlobalLock`注解，则会检查是否有全局锁，如果当前存在全局锁，则会回滚本地事务，通过 while 循环不断地重新竞争获取本地锁和全局锁。



参考：https://blog.csdn.net/weixin_38582851/article/details/123042833