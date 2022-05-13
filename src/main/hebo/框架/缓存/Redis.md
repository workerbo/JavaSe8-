##### 哨兵模式

哨兵模式是一种特殊的模式，首先Redis提供了哨兵的命令，哨兵是一个独立的进程，作为进程，它会独立运行。 其原理是 哨兵通过发送命令，等待Redis服务器响应，从而监控运行的多个Redis实例。 通过发送命令，让Redis服务器返回监控其运行状态，包括主服务器和从服务器。 当哨兵监测到master宕机，会自动将slave切换成master，然后通过 发布订阅模式 通知其他的从服务器，修改配置文件，让它们切换主机。 然而一个哨兵进程对Redis服务器进行监控，可能会出现问题，为此，我们可以使用多个哨兵进行监控。 各个哨兵之间还会进行监控，这样就形成了多哨兵模式。 用文字描述一下 故障切换（failover） 的过程。

###### 配置：

配置一个哨兵，配置sentinel.conf，这个配置文件一般都是在安装目录下

```
# 禁止保护模式、此时外部网络可以直接访问
protected-mode no  
# 配置监听的主服务器，这里sentinel monitor代表监控，mymaster代表服务器的名称，可以自定义，192.168.11.128代表监控的主服务器，6379代表端口，2代表只有两个或两个以上的哨兵认为主服务器不可用的时候，才会进行failover操作。
sentinel monitor mymaster 192.168.11.128 6379 2
# sentinel author-pass定义服务的密码，mymaster是服务名称，123456是Redis服务器密码
# sentinel auth-pass <master-name> <password>
sentinel auth-pass mymaster 123456

```

redis本身无法限制【只有指定主机】连接到redis中，就像我上面说的一样，bind指定只是用来设置接口地址（interfaces）【指定接收通过本机哪个网卡来接收请求】。没有指定就是任意网卡

1、如果你的bind设置为：bind 127.0.0.1，这是非常安全的，因为只有本台主机可以连接到redis，就算不设置密码，也是安全的，除非有人登入到你的服务器上。

2、如果你的bind设置为：bind 0.0.0.0，表示所有主机都可以连接到redis。或者不绑定。（前提：你的服务器必须开放redis的端口）。这时设置密码，就会多一层保护，只有知道密码的才可以访问。也就是任何知道密码的主机都可以访问到你的redis。

protected-mode是redis本身的一个安全层，这个安全层的作用：就是只有【本机】可以访问redis，其他任何都不可以访问redis。这个安全层开启必须满足三个条件，不然安全层处于关闭状态：

（1）protected-mode yes（处于开启）

（2）没有bind指令。原文：The server is not binding explicitly to a set of addresses using the "bind" directive.

（3）没有设置密码。原文：No password is configured。



###### 启动

在启动redis节点后，redis-sentinel sentinel.conf

测试当6379主服务器宕机，哨兵会检测到信号，并从slave服务器重新选举，成为master服务器；就算6379服务器重新上线，也没有重新成为master服务器。

##### redis主从复制特点:

1.master可以拥有多个slave

2.多个slave可以连接同一个master外，还可以连接到其他slave

3.主从复制不会阻塞master，在同步数据时，master可以继续处理client请求

4.提高系统的伸缩性

5.可以在master禁用数据持久化，注释掉master配置文件中的所有save配置，只需在slave上配置数据持久化

如果希望长期保证这两个服务器之间的主从关系，在从库上配置slaveof 。配置了之后读写分离

```
# 使得Redis服务器可以跨网络访问
bind 0.0.0.0
# 设置密码
requirepass "123456"
# 指定主服务器，注意：有关slaveof的配置只是配置从服务器，主服务器不需要配置
slaveof 192.168.11.128 6379
# 主服务器密码，注意：有关slaveof的配置只是配置从服务器，主服务器不需要配置
masterauth 123456

```

### redis为什么是单线程及为什么快的总结

1、Redis是纯内存数据库，一般都是简单的存取操作，线程占用的时间很少，时间的花费主要集中在IO上，所以读取速度快。
        2、Redis使用的是非阻塞IO、IO多路复用，使用了单线程来轮询描述符，将数据库的开、关、读、写都转换成了事件，减少了线程切换时上下文的切换和竞争。
          3、Redis采用了单线程的模型，保证了每个操作的原子性，也减少了线程的上下文切换和竞争。
          4、Redis避免了多线程的锁的消耗。
          5、Redis采用自己实现的事件分离器，效率比较高，内部采用非阻塞的执行方式，吞吐能力比较大。