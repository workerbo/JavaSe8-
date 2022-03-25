

#### MySQL数据库--导读



##### MySQL

[最大字段数](https://blog.51cto.com/10574662/1693915)

**ON DUPLICATE KEY UPDATE**需要有在**INSERT**语句中有存在主键或者唯一索引的列，并且对应的数据已经在表中才会执行更新操作。而且如果要更新的字段是主键或者唯一索引，不能和表中已有的数据重复，否则插入更新都失败。

事务保证了某一时刻读已提交、可重复读。【更新丢失本质上是读的问题】。乐观锁和悲观锁保证了时段的读的正确性。

字符集utf-8 Utf-8mb4【兼容四字节】  字符排序从utf8_unicode_ci改到utf8_bin前面那个是比较字符，后面那个是比较二进制码。能区分大小写和全角半角。

[mysql锁问题【重要】](https://www.cnblogs.com/boblogsbo/p/5602122.html)

1.SQL[执行顺序](https://database.51cto.com/art/201911/605471.htm)

2.**GROUP BY 配合CUBE 和 ROLLUP**显示更多层次的分组

3.执行SQL后执行show profile得到执行时间

4.SQL[优化](https://blog.csdn.net/jie_liang/article/details/77340905)。要点是使用索引，避免全表扫描

5.多表查询后排序慢【排序字段已经有了索引】，但是myql会自动根据小表驱动大表进行优化【驱动表才能使用索引，导致排序索引失效】，所以可以将join变成到select的子查询

6.MySQL可以运行不同的SQL Mode（SQL模式）下。SQL Mode定义了MySQL应支持的SQL语法、数据校验等，

7.只是对某些语句需要进行事务控制，则使用START TRANSACTION语句开始一个事务比较方便，这样事务结束之后可以自动回到自动提交的方式

8.MySQL 的外键功能仅对 InnoDB 存储引擎的表有作用，其他类型存储引擎的表虽然 可以建立外键，但是并不能起到外键的作用。

9. MySQL 最常用的 4 种日志类型：错误日志【默认开启】、二进制日志、查询日志和慢查询日志。
10. mysql的innodb引擎支持行锁，事务，外键。MyISAM支持表锁，串行执行【读锁写锁互斥】。
11. ，MyISAM 总是一次获得 SQL 语句所需要的全部锁。【表锁不会死锁】
12. InnoDB 的行锁是基于索引实现的，如果不通过索引访问数据，InnoDB 会使用表锁。
13. innoDB并发执行导致的问题【多个事务可以交叉执行】
14. 更新丢失可以通过乐观锁或者悲观锁解决。读取问题通过读之前加锁或抓MCC解决
15. 在mysql设置了自动提交之后可以通过语句start  transaction开启手动提交。【事务启动手动提交或者回滚，会释放同一会话中的锁】
16. 在用范围条件更新记录时，无论在 Read Commited 或 是 Repeatable Read 隔离级别下，InnoDB 都要使用**间隙锁【不允许出现幻读】**。
17. MySQL 的恢复是 SQL 语句级的，MySQL的 Binlog是按照事务ᨀ交的先后顺序记录的，恢复也是按这个顺序进行的。在一个事务未ᨀ交前，其他并发事务不 能插入满足其锁定条件的任何记录，也就是不允许出现幻读
18. 在不同的隔离级别下【还有数据恢复和复制机制】，InnoDB 处理 SQL 时采用的一致性读策略和需要的锁是不同的。
19. 对于许多 SQL，隔离级别越高，InnoDB 给记录集加的锁就越严格（尤 其是使用范围条件的时候），产生锁冲突的可能性也就越高，从而对并发性事务处理性能的 影响也就越大。因此，我们在应用中，应该尽量使用较低的隔离级别，以减少锁争用的机率。
20. 在用 LOCK TABLES 对 InnoDB 表加锁时要注意，要将 AUTOCOMMIT 设为 0，否则 MySQL 不会给表加锁；事务结束前，不要用 UNLOCK TABLES 释放表锁，因为 UNLOCK TABLES 会隐含地ᨀ交事务；COMMIT 或 ROLLBACK 并不能释放用 LOCK TABLES 加的表级锁，必须用 UNLOCK TABLES 释放表锁
21. MyISAM 总是一次获得所需的全部锁， 要么全部满足，要么等待，因此不会出现死锁。但在 InnoDB 中，除单个 SQL 组成的事务外， 锁是逐步获得的，这就决定了在 InnoDB 中发生死锁是可能的。
22. 发生死锁后，InnoDB 一般都能自动检测到，并使一个事务释放锁并回退，另一个事务获得 锁，继续完成事务【行锁】
23. 涉及外部锁，或涉及表锁的情况下，InnoDB 并不能完全自动检测 到死锁，这需要通过设置锁等待超时参数 innodb_lock_wait_timeout 来解决
24. 对于 UPDATE、DELETE 和 INSERT 语句，InnoDB 会自动给涉及数据集加排他锁（X)；对于普通 SELECT 语句，InnoDB 不会加任何锁；事务可 以通过以下语句显示给记录集加共享锁或排他锁。对于锁定行记录后需要进行更新操作 的应用，应该使用 SELECT... FOR UPDATE 方式获得排他锁。
25. MySQL 的字符集和校对规则有 4 个级别的默认设置：服务器级、数据库级、表级和字 段级。同时客户端、连接和返回结果的字符集
26. MySQL 中，参数的初始化是通过参数文件来进行设置，当参数需要修改时，可以选择以下 3 种修改方式。session 级修改（只对本 session 有效）。全局级修改（对所有新的连接都有效,但是对本 session 无效，数据库重启后失效）。永久修改。将参数在 my.cnf 中增加或者修改，数据库重启后生效。
27. SHOW VARIABLES 和 SHOW STATUS 命令查看 MySQL 的 服务器静态参数值和动态运行状态信息。其中前者是在数据启动后不会动态更改的值，比如缓冲区大小、字符集、数据文件名称等；后者是数据库运行期间的动态变化的信息，比如锁等待、当前连接数等。同时可以通过select查看变量。
28. 通过备份和binlog实现完整的恢复
29. 比较使用 EXISTS 和 IN 的查询。注意两个查询返回相同的结果。
    select * from TableIn where exists(select BID from TableEx where BNAME=TableIn.ANAME)
    select * from TableIn where ANAME in(select BNAME from TableEx)
30. Navicat连接[Oracle](https://blog.csdn.net/gxp1182893781/article/details/79815573)
31. PLSQL[安装](https://www.cnblogs.com/zhangchao0515/p/11353868.html)
32. 客户端显示Oracle数据库的数据乱码
33. select * from  hceb_ce_banks_v
     select * from V$NLS_PARAMETERS 
     NLS_LANGUAGE 表示“语言”，NLS_TERRITORY  表示“地域”，NLS_CHARACTSET 表示“字符集”，将他们三个按照“语言_地域.字符集”的格式拼接起来



> 锁的概念

MySQL的innode引擎的事务通过锁和MVCC来实现不同的隔离级别【并发能力不同】+同时通过乐观锁或悲观锁强制读取最新数据！【绝对并发安全】

举例：RR会导致A事务没有得到B事务刚提交的最新数据。

###### MySQL上的锁

排他锁指的是一个事务在一行数据加上排他锁后，其他事务不能再在其上加其他的锁。mysql InnoDB引擎默认的修改数据语句，update,delete,insert都会自动给涉及到的数据加上排他锁，select语句默认不会加任何锁类型，如果加排他锁可以使用select ...for update语句，加共享锁可以使用select ... lock in share mode语句。所以加过排他锁的数据行在其他事务种是不能修改数据的，也不能通过for update和lock in share mode锁的方式查询数据，但可以直接通过select ...from...查询数据，因为普通查询没有任何锁机制。锁是加在索引上的。

###### 悲观锁【逻辑上的概念】

因为悲观锁会影响系统吞吐的性能，所以适合应用在写为居多的场景下。

###### 乐观锁【逻辑上的概念】

因为乐观锁就是为了避免悲观锁的弊端出现的，所以适合应用在读为居多的场景下。

在保证逻辑正确的情况，乐观锁是指没有其他事务会争抢，然后不提前加锁的更新。悲观锁指的是会有其他事务争抢，提前锁定资源去更新。  mysql引擎自动给增删改加的锁是保证多个事务的更新有序，确保可见。

查询的时候是锁住，还是获取版本号

查询的时候是锁住【适合于写】，还是获取版本号【适合于读】

乐观锁因为时通过我们人为实现的，它仅仅适用于我们自己业务中，如果有外来事务插入，那么就可能发生错误。【即其他事务没有加版本号去更新】



###### 数据库隔离级别实现

为了实现可重复读，MySQL 采用了 MVVC (多版本并发控制) 的方式。

数据库表中看到的一行记录可能实际上有多个版本，每个版本的记录除了有数据本身外，还要有一个表示版本的字段，记为 row trx_id

在上面介绍读提交和可重复读的时候都提到了一个词，叫做快照，学名叫做一致性视图，这也是可重复读和不可重复读的关键，可重复读是在事务开始的时候生成一个当前事务全局性的快照，而读提交则是每次执行语句的时候都重新生成一次快照。

对于一个快照来说，它能够读到那些版本数据，要遵循以下规则：

1. 当前事务内的更新，可以读到；
2. 版本未提交，不能读到；
3. 版本已提交，但是却在快照创建后提交的，不能读到；
4. 版本已提交，且是在快照创建前提交的，可以读到；



READ UNCOMMITTED(RU) ： 对于修改的项加排它锁，直到事务结束释放；没有快照读，只能读最新版本的数据。

READ COMMITTED(RC) ：对于修改的项加排它锁，直到事务结束释放；有快照读，快照的粒度是语句级【每次读取数据前都生成一个快照】。

REPEATABLE READ(RR) ：对于修改的项加排它锁，直到事务结束释放；有快照读，快照的粒度是事务级。【MySQL通过Next-key【行锁和Gap lock解决幻读】】

并发写通过行锁保持先后顺序。通过乐观锁【update更新失败，返回0】或者悲观锁【保证读取到最新的数据，一次更新成功】策略保证**当前读**是最新版本的数据。

#### SQL编写实践记录

update delete语句set中不能有当前表的查询语句【用中间表解决同表更新和查询的冲突】

set @@autocommit = 0; select @@autocommit;

当执行一个START TRANSACTION指令时，会隐式的执行一个commit操作，就是开启新事务时，自动提交原来的旧事务，只能通过savepoint来实现嵌套事务。

因为异常，事务回滚，保存点的异常向上传播到主事务，主事务也回滚。

无异常的回滚到对应的保存点

事务中同一个连接可以查看更改的数据，其他事务根据隔离性读取数据。

**如果事务中所有sql语句执行正确则需要自己手动提交commit；否则有任何一条执行错误，需要自己提交一条rollback，这时会回滚所有操作，而不是commit会给你自动判断和回滚。**

mysql和oracle不太一样，不支持直接的sequence，所以需要创建一张table来模拟sequence的功能。





===

	SELECT User, Host FROM mysql.user;


mysql - it's a commandline (administration) tool 
命令行工具
mysqld - it's a server daemon. this is the database server main binary (executable)
启动MYSQL服务


全局、会话、局部变量
show  GLOBAL variables like 'character_set_%';
show  @@GLOBAL.variables like 'character_set


SHOW DATABASES
SHOW TABLE STATUS LIKE 'user'

SHOW COLUMNS FROM `mysql`.`user`
describe mysql.user;


1.修改字符集（在修改之前建的库不生效）



查看系统当前隔离级别
select @@global.tx_isolation;

连接层  连接和安全
服务层  解析
引擎层  提取和存储数据（客插播，以myisam innodb【事务，外键，行锁支持】）
存储层  存到裸设备上


从from开始解析  select  order by  limit在最后
七种join语句：A和B中单独A  a left join on B where b.key is null

索引：拍好序的快速查找数据结构（B+）  在其上实现查找算法  where子句（降低io使用）和order by子句（减少cpu使用）
索引需要维护和重建，防止失效
劣势：占空间、更新慢[需要同步更新索引]、需要花时间优化得到最佳索引
show index from table
单索引 复合索引  唯一索引
索引结构 B+树 层次越少IO成本越少
适合建索引（记录太少的不用，重复数据太多的不用【不会提高查找的速度】）：经常作为where中的字段  外键 主键自动建唯一索引  频繁更新的字段不适合建索引  排序字段  分组和统计字段


优化SQL
优化器  服务器硬件  explain【执行过程分析】
1.查找出问题SQL
2.【ID决定表的加载顺序】如果是子查询，id越大越先执行。相同的从上加载表【from子句从右到左】。衍生表=drived数字  小表驱动大表
3.select——type  simple 不包含子查询或者union  drived  from 子句中的子查询（临时表存在）   primary 内部包含复杂部分
4.type【决定查询速度】  all【全表】  eq_ref【相等一条】  ref  range(范围)  index【从索引】   
5.possible keys【可能涉及】  key【涉及】  看索引使用情况  覆盖索引：select所查询的字段和某个复合索引一致。【进出线在key中】
6.key_len 索引字段的长度（理论值）
7.索引列对应的【值】那一列【一般外键】或者具体常量被使用
8.extra usering filesort 【内部重新排序了，没有用到索引（必须完全字段）】  Usering  temporary[常见于orderby 和group by]  using where 索引用于键值查找

show enginer；
myiasm【表锁】偏读，因为写锁会导致查询大量阻塞  并发会话
show open tables;
lock table t1 read(write)  t2 read(write)
unlock tables;
按操作类型划分
共享读锁  都可以读，都不能更改（当前erro，其他的被阻塞），在没有解锁前加锁者不能读其他未加锁的的表
排他写锁 会阻塞气筒写锁和读锁  当前会查询，更新，不能查询其他未加锁的表。其他读写都会阻塞

inodb  并发事务带来的问题
set autocommit 0;
航所定义【读已提交】：同一数据当前事务可以更新，查询，其他事务更新被阻塞，查询得到旧数据   
更新丢新（事务互不知道对方的存在）

无索引行锁升级为表锁【自动做类型转化后索引失效】
间隙锁 当条件是范围时（锁定所有，即使不存在）  可能造成很大危害
show status like '%inodb_row_lock%'
如何锁定一行 for update

主从复制
配置
停止slave后，从机重新授权需要重查一次position

行锁
表锁




Oracle，SqlServer中都是选择读已提交(Read Commited)作为默认的隔离级别
mysql主从复制存在了大量的不一致，故选用repeatable（默认），但是间隙较大，容易死锁

把数据库系统的隔离级别设为Read Committed。它能够避免脏读取，而且具有较好的并发性能。尽管它会导致不可重复读、幻读和第二类丢失更新这些并发问题，在可能出现这类问题的个别场合，可以由应用程序采用悲观锁或乐观锁来控制。

使用悲观锁，我们必须关闭mysql数据库的自动提交属性，因为MySQL默认使用autocommit模式 set autocommit=0;
使用了select…for update的方式，这样就通过数据库实现了悲观锁。其他事务必须使用类似语句查询才能阻塞
MySQL InnoDB默认Row-Level Lock，所以只有「明确」地指定主键或者索引，MySQL 才会执行Row lock (只锁住被选取的数据) ，否则MySQL 将会执行Table Lock (将整个数据表单给锁住)


锁（应用层）（也只有数据库层提供的锁机制才能真正保证数据访问的排他性、可以动态更改事务隔离级别。）
乐观锁（适合查询）：每行记录一个版本号字段 必须大于当前版本号。（其他人依旧可以操作，所以当前命令可能失败）
悲观锁（适合修改）：再操作之前加锁（其他用户被阻塞）
例子：https://blog.csdn.net/qq_36537108/article/details/88259425

watch监控加锁，可以监控多个key（乐观锁，不一致【有其他人先执行了】事务执行失败，unwatch取消对所有key的监控。执行exec后也取消）

不保证原子性：失败一条语句也不回滚
因此，事务一旦开始就会执行成功，不会被打断，也没有隔离级别的概念【事务内外的查询得到的数据一致】

关系型数据库的隔离级别【数据库层】（
1.Read uncommitted
：写事务则禁止其他写事务，该隔离级别可以通过“排他写锁”实现。这样就避免了更新丢失，却可能出现脏读。也就是说事务B读取到了事务A未提交的数据。
 、
 2.Read committed ：写事务则禁止任何其他事务  事务内外查询结果不强求一致，事务可能会回滚
 、3.Repeatable read 读事务将会禁止写事务【共享读锁】，写事务则禁止任何其他事务、
 4、Serializable：事务只能一个接着一个地执行），避免并发问题。
https://blog.csdn.net/qq_33290787/article/details/51924963
https://blog.csdn.net/qq_21294095/article/details/84888802

隔离级别目的是隔离事务，避免更新丢失
其他事务不能读取某一事务未提交的数据 、不能读取某一修改事务的数据，不能读取



###### 数据类型

```
bigint已经有长度了，在mysql建表中的length，只是用于显示的位数
```





[mysql通过binlog恢复](https://blog.csdn.net/weixin_34203832/article/details/86350387?spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-2.pc_relevant_paycolumn_v3&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-2.pc_relevant_paycolumn_v3&utm_relevant_index=5)