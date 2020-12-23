#### SQL编写实践记录

update delete语句set中不能有当前表的查询语句【用中间表解决同表更新和查询的冲突】

set @@autocommit = 0; select @@autocommit;

当执行一个START TRANSACTION指令时，会隐式的执行一个commit操作，就是开启新事务时，自动提交原来的旧事务，只能通过savepoint来实现嵌套事务。

因为异常，事务回滚，保存点的异常向上传播到主事务，主事务也回滚。

无异常的回滚到对应的保存点

事务中同一个连接可以查看更改的数据，其他事务根据隔离性读取数据。

**如果事务中所有sql语句执行正确则需要自己手动提交commit；否则有任何一条执行错误，需要自己提交一条rollback，这时会回滚所有操作，而不是commit会给你自动判断和回滚。**

mysql和oracle不太一样，不支持直接的sequence，所以需要创建一张table来模拟sequence的功能。