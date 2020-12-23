狂神说reidis[笔记](https://blog.csdn.net/beiluol/category_10009893.html)

> ###### redis常用数据类型

优势：内存读写、单线程【减少锁、上下文切换的开销】、IO复用【非阻塞IO、server轮询变成了事件通知】



常用命令
二进制安全（包含任意数据） 最多512M

###### String

```shell
SET KEY VALUE [EX seconds] [PX milliseconds] [NX|XX]
- `EX seconds` − 设置指定的到期时间(以秒为单位)。
- `PX milliseconds` - 设置指定的到期时间(以毫秒为单位)。
- `NX` - 仅在键不存在时设置键。
- `XX` - 只有在键已存在时才设置。
exists key
flushdb  
move key db
expire key  时间
type  key
del key

keys *

ttl  key    -1 永不过期  -2 已过期

select  数字  选择库

append  key  字符、strlen  key
incr  key
decr  key
incrby  key  数字
getrange key  0 -1
setrange  key  字符
setex key t  value
setnx  key  value
mset  
mget  key...
msentnx 全部不存在才成功

getset *#先get再set* 
```





------



###### list是字符串链表 【List在Redis底层采用的是**双向链表**实现的】

```
 没值得话删除key  有键则新增
lpush     左边入栈
rpush      右边入栈
lrange 0 -1  查看
lpop
rpop  出了以后就没有了
lindex 序列  索引   进行查看
lrem  key  删N个value  元素
ltrim key  start  end  
lset key  index  value   #将列表中指定下标的值替换成另一个值，更新操作
linsert  before、after key va newvalue
rpoplpush key1 key2   #移除列表的最后一个元素，将他移动到新的列表中
```

###### set  单key多值  仍然采用**散列表**

```
sadd  key  v1 v2
smembers sismenbers
scard  个数
srem key  value   指定删除
srandmenbers key 个数
spop key  随机删除  
smove key1 key2  某个值移动
sdiff  key1 key2 key1存在key2不存在的值集
sinter
sunion
```



###### hash  kv模式不变 但是v是一个键值对  适用于存对象【易于替换键值】Redis采用**散列表**来实现Hash

```
hset hget  
hmset  key  k1 v1 k2 v2
 hmget  key k1 k2
 hgetall key
 hdel key k1...
 hlen key #字段数量
 hexists key  k1
 hkeys  key 
 hvlues key
 hincrby key k1 数字
 hincrbyfloat 
 hsetnx
```

###### 有序集合

```
 zadd key   c1 v1 c2  v2
 zrange key 0 -1
 zrange key 0 -1 withscores
zrangebyscore  key soce1 socer2   加（符号不包含  limit start  rows
zrem key k1
zscard
zcount key socer区间
zscore key value   获得分数
zrank key value   获得下标
zrevrank key value  逆序下表
zrevrange 0 -1  
zrevrangebyscore  key  区间
```



[三种特殊的类型](https://blog.csdn.net/qq_35423154/article/details/109674794)Geospatial、Hyperloglog、Bitmap

> ###### 键驱逐策略

1.不丢弃  2.随机丢弃  3.LRU

> ###### 持久化

1.RDB记录数据快照，子进程执行。

2.AOF记录操作日志，需要超过大小就重写【**AOF重写**用于删除掉冗余的命令，比如用户对同一个key执行100遍SET操作，如果不AOF重写，那么AOF文件中就会有100条SET记录，数据恢复的时候也需要操作100次SET，但实际上只有最后一条SET命令是真正有意义的】

**即使有持久化措施，仍然会有少量数据丢失的问题，**因为备份是每隔一段时间进行的，如果两个备份操作之间机器坏了，那么这期间的数据修改就会因为没来得及备份就被丢失掉

持久化：rdb(最后一次可能丢失，通过fork一次子进程完成持久化，快照存储) aof(日志形式记录所有写操作，同时存在时先加载在aof文件，aof文件出现问题时用redis-check-aof命令修复  记录策略   体积过大时触发重写)
aof数据完整性、一致性更高，使用于恢复数据  rdb适用于备份

> ###### 流水线

再介绍一个Redis中常用的用来降低网络通信对于程序运行速度影响的小技巧：流水线。把命令打包一起发送，然后等服务器计算完了之后把结果一起返回来。

> ###### 事务

Redis事务本质：一组命令的集合！一个事务中的所有命令都会被序列化，在事务执行过程中,会按照顺序执行！一次性，顺序性，排他性!执行一些列的命令！

运行时异常（就是语法错误，比如1/0）,如果事务队列中存在语法行，那么执行命令的时候，其他命令式可以正常执行的，错误命令抛出异常！   

事务要么全部执行【即使执行失败】，要么全部不执行。

[WATCH](http://redisdoc.com/transaction/watch.html#watch) 命令可以为 Redis 事务提供 check-and-set （CAS）行为。[WATCH](http://redisdoc.com/transaction/watch.html#watch) 使得 [EXEC](http://redisdoc.com/transaction/exec.html#exec) 命令需要有条件地执行： 事务只能在所有被监视键都没有被修改的前提下执行【原子性、顺序性】

multi：事务开始标识，正确命令
入队
exec：执行
discard ：放弃

> ###### redis分布式锁

https://blog.csdn.net/hxpjava1/article/details/81068355

参考官网：https://redis.io/documentation
常规概念
https://my.oschina.net/u/3343218/blog/2989564
基本操作
https://www.cnblogs.com/EasonJim/p/7803067.html
Spring AOP实现监控所有被@RedisCache注解的方法缓存
先从Redis里获取缓存,查询不到，就查询MySQL数据库，然后再保存到Redis缓存里，下次查询时直接调用Redis缓存
redis做数据库查询的缓存
https://juejin.im/entry/5a31f3c5f265da43163d15a7

> ###### LUA脚本
>

组合多个Redis命令，并让这个组合也能够原子性的执行。

> ###### 主从复制+哨兵【增强读能力】
>

主从复制，读写分离（只有主机能写）

  info  server 

 info replication

slaveof ip 端口

一主多仆：
主机down了，从机待命
从机down了重启需要重新连接（除非写在了配置文件）
薪水相传  依次执行slaveof 
反客为主  在主机down后，通过slaveof No one 客变主，其他从机在重新slaveof

哨兵模式  反客为主 自动化
sentinel.conf 监控的主机  主机down后得票多变为主机
redis-sentinel  配置文件  
down的主机回来后变为从机



> ###### redis做消息中间件（很少，一般用activityMQ）

suscrible  c1 c2  客户端订阅渠道  和接收消息
publish   c1 message  发布消息

通过 SUBSCRIBE 命令订阅某频道后，redis-server 里维护了一个字典，字典的键就是一个个 频道！， 而字典的值则是一个链表，链表中保存了所有订阅这个 channel 的客户端。SUBSCRIBE 命令的关键， 就是将客户端添加到给定 channel 的订阅链表中。【消息中间件】



> ###### [分布式集群【增强写能力】](https://www.cnblogs.com/guolianyu/p/10345387.html)
>

分布式存储系统

1.数据分区【哈希函数】 2查询路由【需要一个查询路由，该路由根据给定的key，返回存储该键值的机器地址。Redis的每个节点中都存储着如下所示的整个集群的状态，集群状态中一个重要的信息就是每个桶的负责节点】

集群节点通信。Redis集群的Gossip协议需要兼顾信息交换实时性和成本开销。根据以上规则得出每个节点每秒需要发送ping消息的数量=1+10*num（node.pong_received>cluster_node_timeout/2）。M值的确定以及M个节点的选择就是随机选择。

> ###### 缓存穿透和缓存雪崩



