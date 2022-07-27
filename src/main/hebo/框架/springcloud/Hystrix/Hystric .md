

在分布式系统中，每个服务都可能会调用很多其他服务，被调用的那些服务就是依赖服务，有的时候某些依赖服务出现故障也是很正常的。
        Hystrix 可以让我们在分布式系统中对服务间的调用进行控制，加入一些调用延迟或者依赖故障的容错机制。
Hystrix 通过将依赖服务进行资源隔离，进而阻止某个依赖服务出现故障时在整个系统所有的依赖服务调用中进行蔓延；同时Hystrix 还提供故障时的 fallback 降级机制。

##### Feign与Hystrix

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191012221343851.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NyYXp5bWFrZXJjaXJjbGU=,size_16,color_FFFFFF,t_70)

在Spring Cloud中使用Feign进行微服务调用分为两层：Hystrix的调用和Ribbon的调用，Feign自身的配置会被覆盖。那么Ribbon的超时时间配置与Hystrix的超时时间配置则存在依赖关系，因为涉及到Ribbon的重试机制，所以一般情况下都是Ribbon的超时时间小于Hystrix的超时时间。

```
Hystrix的超时时间=Ribbon的重试次数(包含首次) * (ribbon.ReadTimeout + ribbon.ConnectTimeout)
```

在Ribbon超时但Hystrix没有超时的情况下，Ribbon便会采取重试机制；而重试期间如果时间超过了Hystrix的超时配置则会立即被熔断（fallback）。









当对特定的服务的调用的不可用达到一个阀值（Hystric 是5秒20次） 断路器将会被打开。

hystrix 是通过隔离服务访问点阻止联动故障，从而提高整个分布式系统的高可用；

###### hystrix设计原则

1 快速失败（如果一个服务出现故障，则调用该服务的请求快速失败，而不是线程等待）
2 提供回退[fallback]方案（在请求发生故障时，提供设定好的回退方案）
3 提供熔断机制，防止故障扩散到其他服务
4 提供熔断器监控组件 hystrix dashboard，实时监控熔断器状态

###### hystrix 工作机制

当服务的某个API失败次数在一定时间内小于设置的阈值时，熔断器处于- 关闭状态，该API正常提供服务。
失败次数 > 设定阈值，hystrix判定API接口出现故障，打开熔断器
一定时间后出于半打开状态，处理一定数量请求，失败，则熔断器继续打开；成功，则关闭熔断器【具有自我修复】

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916181917681.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FpYXppbGlwaW5n,size_16,color_FFFFFF,t_70#pic_center)



当hystrix command的隔离策略配置为线程，也就是execution.isolation.strategy设置为THREAD时，command中的代码会放到线程池里执行，跟发起command调用的线程隔离开。