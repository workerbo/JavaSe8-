HAP3.x技术点解析



技术栈：ssm+kendoUI

webservice开发  在webservice目录下【提供服务和发布服务】

配置文件：cxf-beans-demo1.xml

权限管理的 查询权限：通过拦截器对SQL追加条件过滤数据

​                     编辑权限：通过配置条件过滤判断当前用户是否可以编辑

​                     组织权限：通过用户和用户所属角色分配的部门过滤数据。

spring security 验证和鉴权

1.验证  配置文件standardSecurity.xml

标准登录验证  customUserDetailsService得到数据库的用户名密码信息供过滤器DaoAuthenticationProvider使用

认证成功后CustomAuthenticationSuccessHandler在DefaultAuthenticationSuccessListener类中设置session信息【用户信息和角色信息】

角色在系统配置中选择了角色合并：所以为了正确取值，在权限管理中默认按照所以角色过滤。

[工作流处理](https://www.cnblogs.com/hongwz/p/5529899.html)：

1.活动历史表   无活动运行表

2.一个流程中，流程执行对象（Execution）可以有多个，但是流程实例对象（ProcessInstance）只能有一个。

 当流程按照流程定义的规则只执行一次的时候，流程执行对象（Execution）即为流程实例对象（ProcessInstance）。

3.任务候选人【组】和签署人区别   多实例【多次循环、多个签署人】

4.流程设计候选人，在代码中连接审批规则【表达式中的变量通过processCandidate方法设置】  

5.ActivitiEntityServiceImpl实现了员工常用信息查询的接口,该类实现了IActivitiBean接口,返回”empService”,故可以在表达式中通过empService引用该实例.同样这个类也实现了ActivitiEntityService接口.

6.如果有旧的流程启动了，发布的新版不会影响旧流程的运行

7.当一个任务被拒绝时，如果一个人工任务 的下一个节点仍然是 任务（Task 类型），那么 HAP 将会自动插入一个选择网关和终止事件 来自动做结束流程的操作

nginx配置、mysql配置使得性能最大化。

配置文件更改之前先备份。







redis  3.2「Remote Dictionary Service」的首字母缩写

但是不要粗暴地使用kill-9强制杀死Redis服务，不但 不会做持久化操作，还会造成缓冲区等资源不能被优雅关闭。

Redis的8个特性：速度快、基于键值对的数据结构服务器、功能丰 富、简单稳定、客户端语言多、持久化、主从复制、支持高可用和分布式。

负载均衡下：每次用户 更新或者查询登录信息都直接从Redis中集中获取。

列表 有序可重复    

列表元素较少的情况下会使用一块连续的内存存储，这个结构是 ziplist

smembers和lrange、hgetall都属于比较重的命令，如果元素过多存在阻 塞Redis的可能性，这时候可以使用sscan来完成

对于字符串类型键，执行set命令会去掉过期时间，这个问题很容易 在开发中被忽视。

因为命令执行排队机 制，慢查询会导致其他命令级联阻塞，因此当客户端出现请求超时，需要检 查该时间点是否有对应的慢查询，

multi命令代表事务开始，exec命令代表事务结束

如果要停止事务的执行，可以使用discard命令代替exec命令即可。

Redis 分布式锁：多个客户端操作一个变量进行修改  加锁  谓原子操作是指不会被线程调度机制打断的操 作。【多个线程】

redis 事务：多个命令原子化和。【单独线程中、redis不支持回滚】

有些应用场景需要在事务之前，确保事务中的key没有被其他客户端修 改过，才执行事务，否则不执行（类似乐观锁）

Bitmaps本身不是一种数据结构，实际上它就是字符串（如图3-10所 示），但是它可以对字符串的位进行操作。

HyperLogLog 提供不精确的去重计数方案。

消息发布 者和订阅者不进行直接通信，发布者客户端向指定的频道（channel）发布消 息，订阅该频道的每个客户端都可以收到该消息

新开启的订阅客户端，无法收到该频道之前的消息，因为Redis不会对 发布的消息进行持久化。

RESP提供的发送命令和返回结果的协议格式

Jedis本身没有提供序列化的工具，也就是说开发者需要自己 引入序列化的工具

Redis的客户 端使用不当或者客户端本身的一些问题，造成没有及时释放客户端连接，可 能会造成大量的idle连接占据着很多连接资源，一旦超过maxclients；后果也 是不堪设想。所在在实际开发和运维中，需要将timeout设置成大于0

client list中的age和idle分别代表当前客户端已经连接的时间和最近一次 的空闲时间





====多线程

有状态对象(Stateful Bean)，就是有实例变量的对象 ，可以保存数据，是非线程安全的。一般是prototype scope。
无状态对象(Stateless Bean)，就是没有实例变量的对象，不能保存数据，是不变类，是线程安全的。一般是singleton scope。

对于那些会以多线程运行的单例类，局部变量不会受多线程影响，成员变量会受到多线程影响。

局部变量不会受多线程影响，成员变量会受到多线程影响。

对于成员变量的操作，可以使用ThreadLocal来保证线程安全。 

使用spring开发web 时要注意，默认Controller、Dao、Service都是单例的



=====IOC




spring集成springmvc时web.xml需要配置tomcat容器启动spring的监听器和spring配置文件【扫描bean和注入、事务管理器bean】路径。dispatcherserlet配置以及springmvc的配置文件。

ioc和di都是说将调用方的创建对象的控制权交给容器。【创建bean和注入bean  实例化和依赖关系】。spring通过配置文件描述，然后通过反射创建对象、设置属性值。

软件的分层，底层代码不要侵入上层。底层透明。

beanfactory 是ioc容器，面向spring  第一次获取时创建bean，之后缓存在hashmap中。

applicationContext面向使用，初始化所有单例

java接口可以多继承，**接口全都是抽象方法继承谁都无所谓，所以接口可以继承多个接口。**

类注解是spring的一部分

文件系统路径、类路径、应用上下文路径

spring  bea  context  web【web容器添加spring容器】

实例、初始化、后处理器  自身、bean级【接口】、容器级【后处理器】

尽量不使用接口级，侵入性强

配置文件中的<bean>在spring中被描述成BeanDefinition



反射实列化接口、【反射、动态代理、AOP】

setter注入通过属性名。构造器注入通过索引和类型

基于注解的配置方式默认通过autowire=‘byType’的方式自动装配。









```

```

=====AOP

aop织入【可以是注解标识接入点  】之后动态代理。

aop连接点【包含方位、区别一般动态代理】、切点【需要另外提供方位去匹配连接点】、增强

【逻辑和方位】

aspectj是语言级的AOP实现

spring AOP在动态代理的基础之上解耦硬编码【抽离方位和切点、】

增强【不同的接口】、切点【通过类和方法过滤，不同的表达式】组合成切面



静态语言需要事先编译完成，例如java语言需要先编译成字节码，才放入JVM，其程序结构和变量类型在运行期间不能更改。

spel  能获取代码进行修改【动态】

在JSON.parseObject 的时候，会去填充名称相同的属性。对于Json字符串中没有，而model类有的属性，会为null；对于model类没有，而Json字符串有的属性，不做任何处理。





spring dao 统一的异常体系【统一的DAO接口类】、模板类【开闭原则】支出类

数据的并发问题通过锁机制在一个事务操作相应数据的时候其他事务不能操作。







【spring MVC】

消息转换器  accept、context-type报文头属性  @ResponseBoby @RequestBoyd 【入参类型】

请求响应消息

factorybean接口隐藏了复杂的bean创建过程，

数据类型转换和数据格式化【实际还是类型转换】通过默认的转换服务实现。【不同类型的转换器】

spring  mvc 会在处理器前创建一个隐藏容器，之后放入request对象的属性列表

spring提供的freemark宏

组名.对象名 唯一指定scheduler的trigger jobdetail

jobdetail是通过得到job的clazz对象，在每次运行时新建一个job对象，并提供其他job信息。



src/main/java和/src/main/resources)，生成之后就跑到了根目录下的WEB-INF下的classes的路径下面了。webapp就相当于根目录了



1.redis缓存

2.工作流

3.单点跳转

4.[浏览器兼容](https://juejin.im/user/61228382688920/posts)

5.交接

6.hap模块整理

JAVA编程思想   Effective Java   MySQL技术内幕  Java并发编程的艺术  深入理解Java虚拟机

java核心技术卷一     JavaWeb开发详解

```
隐藏滚动条
<style>
    body::-webkit-scrollbar {
        display: none;
    }
</style>
```