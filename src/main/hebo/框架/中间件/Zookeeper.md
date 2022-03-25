#### 概念

Zookeeper 是一个开源的分布式的，为分布式应用提供协调服务的 Apache 项目。

zookeeper的存储结构极称为节点（znode），每个节点既能保存数据又有孩子节点。zookeeper的根节点都是“/"。

zookeeper有四种节点，临时节点，临时顺序节点，持久节点和持久顺序节点。

- **Zookeeper特点**：

  1）一个领导者（Leader），多个跟随者（Follower）组成的集群。

​        2）集群中只要有半数以上节点存活，Zookeeper 集群就能正常服务。

​		3）全局数据一致：每个Server保存一份相同的数据副本，Client无论连接到哪个Server，数据都是一致的。

​		4）更新请求顺序进行，来自同一个Client的更新请求按其发送顺序依次执行。

​		5）数据更新原子性，一次数据更新要么成功，要么失败。

​		6）实时性，在一定时间范围内，Client能读到最新数据。



- **zookeeper使用场景**

​         提供的服务包括：**统一集群管理**、**服务器节点动态上下线**、**统一配置管理**、统一命名服务、软负载均衡等。

​         主要修改项为dataDir和dataLogDir，dataDir是zookeeper存放数据的地方，dataLogDir是存放zookeeper日志的地方。

​         如果只配置dataDir，则数据和日志都会创建在dataDir目录下。默认情况下zookeeper会占有8080端口，如果你不想8080端口被占用，增加一行admin.serverPort=8082，指定你自己的端口。



-    [**zookeeper权限控制详解**](https://blog.csdn.net/u014630623/article/details/103749103)       

#### 启动

输入命令 ./zkServer.sh start 

接下来启动客户端。输入命令 ./zkCli.sh -server 127.0.0.1:2181 （-server参数就代表我们要连接哪个zookeeper服务端）

#### zookeeper的基本操作

create命令有两个参数，-s代表顺序节点【默认】，-e代表临时节点。

create /temp 123

get /temp

在zookeeper中，没有相对路径的概念，所有的节点都需要用绝对路径表示

修改temp节点的数据，set /temp 456    在zookeeper中，对于数据的修改都是全量修改，没有只修改某一部分这种说法。

如果要删除某个节点，则用delete path即可。

set path data [version]命令，如果我们多次修改，会发现  dataVersion ，也就是数据版本，在不停得发生变化（自增）如果我们在set的时候手动去指定了版本号，就必须和上一次查询出来的结果一致，否则 就会报错。【乐观锁】

ls命令用于列出给定路径下的zookeeper节点

stat path：查看节点状态  ，从ephemeralOwner的值可以判断这个节点是持久节点还是临时节点。0X0代表的是持久节点。如果节点为临时节点，那么它的值为这个节点拥有者的session ID。

 ls2 path：列出path节点的子节点及状态

zookeeper的watcher只触发一次，当节点状态改变一次之后，节点状态的第二次改变就不能监听到了

#### JAVA使用

在maven引入zookeeper依赖。

```
<dependency>
   <groupId>org.apache.hadoop</groupId>
   <artifactId>zookeeper</artifactId>
   <version>3.3.1</version>
</dependency>
```


   org.apache.zookeeper.Zookeeper是客户端入口主类，负责建立与server的会话。它提供了以下 所示几类主要方法。 


![img](https://gitee.com/workerbo/gallery/raw/master/2020/20180618173123579)

#### 原理

1.选举机制
  Zookeeper 虽然在配置文件中并没有指定 Master 和 Slave。但是在 Zookeeper 工作时，是有一个节点为 Leader，其他则为 Follower，Leader 是通过内部的选举机制临时产生的。

半数机制：集群中半数以上机器存活，集群可用。所以 Zookeeper 适合安装奇数台服务器。



假设有zookeeper集群有5台机器，它们的启动顺序是1~5。（三个状态：LOOKING、 FOLLOWING、LEADING）

step1：服务器 1 启动，发起一次选举。服务器 1 投自己一票。此时服务器 1 票数一票， 不够半数以上（3 票），选举无法完成，服务器 1 状态保持为 LOOKING；

step2：服务器 2 启动，再发起一次选举。服务器 1 和 2 分别投自己一票并交换选票信息： 此时服务器 1 发现服务器 2 的 ID 比自己目前投票推举的（服务器 1）大，更改选票为推举服务器 2。此时服务器 1 票数 0 票，服务器 2 票数 2 票，没有半数以上结果，选举无法完成， 服务器 1，2 状态保持 LOOKING；

step3：服务器 3 启动，发起一次选举。此时服务器 1 和 2 都会更改选票为服务器 3。此次投票结果：服务器 1 为 0 票，服务器 2 为 0 票，服务器 3 为 3 票。此时服务器 3 的票数已经超过半数，服务器 3 当选 Leader。服务器 1，2 更改状态为 FOLLOWING，服务器 3 更改状态为 LEADING；

step4：服务器 4 启动，发起一次选举。此时服务器 1，2，3 已经不是 LOOKING 状态，不会更改选票信息。交换选票信息结果：服务器 3 为 3 票，服务器 4 为 1 票。此时服务器 4 服从多数，更改选票信息为服务器 3，并更改状态为 FOLLOWING；

step5：服务器 5 启动，同 4 一样当小弟，将自己状态设置为 FOLLOWING。

2.角色

老大领导者Leader。

Leader在集群中只有一个节点，可以说是老大No.1，是zookeeper集群的中心，负责协调集群中的其他节点。从性能的角度考虑，leader可以选择不接受客户端的连接。

主要作用有：

1、发起与提交写请求。

所有的跟随者Follower与观察者Observer节点的写请求都会转交给领导者Leader执行。Leader接受到一个写请求后，首先会发送给所有的Follower，统计Follower写入成功的数量。当有超过半数的Follower写入成功后，Leader就会认为这个写请求提交成功，通知所有的Follower commit这个写操作，保证事后哪怕是集群崩溃恢复或者重启，这个写操作也不会丢失。

2、与learner保持心跳

3、崩溃恢复时负责恢复数据以及同步数据到Learner

 

老二跟随者Follower。

Follow在集群中有多个，主要的作用有：

1、与老大Leader保持心跳连接

2、当Leader挂了的时候，经过投票后成为新的leader。leader的重新选举是由老二Follower们内部投票决定的。

3、向leader发送消息与请求

4、处理leader发来的消息与请求

 

老三观察者Observer

可以说Observer是zookeeper集群中最边缘的存在。Observer的主要作用是提高zookeeper集群的读性能。通过leader的介绍我们知道zookeeper的一个写操作是要经过半数以上的Follower确认才能够写成功的。那么当zookeeper集群中的节点越多时，zookeeper的写性能就 越差。为了在提高zookeeper读性能（也就是支持更多的客户端连接）的同时又不影响zookeeper的写性能，zookeeper集群多了一个儿子Observer，只负责：

1、与leader同步数据

2、不参与leader选举，没有投票权。也不参与写操作的提议过程。

3、数据没有事务化到硬盘。即Observer只会把数据加载到内存。
