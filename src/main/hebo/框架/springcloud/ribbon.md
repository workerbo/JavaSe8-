- 负载均衡是指将负载分摊到多个执行单元上，常见的负载均衡有两种方式。

  一种是独立进程单元，通过负载均衡策略，将请求转发到不同的执行单元上，例如Nginx。
  另一种是将负载均衡逻辑以代码的形式封装到服务消费者的客户端上,服务消费者客户端维护了一份服务提供者的信息列表，有了信息列表，通过负载均衡策略将请求分摊给多个服务提供者，从而达到负载均衡的目的

- 在spring cloud构建微服务系统中，ribbon有两种使用方式作为服务消费者负载均衡器：
  1、 通过RestTemplate相结合
   2、和Feign相结合。（feign默认集成了ribbon，下篇介绍Feign）

###### 常用配置属性

- hello-service.ribbon.MaxAutoRetriesNextServer：切换实例的重试次数。
- hello-service.ribbon.MaxAutoRetries：对当前实例的重试次数。

重试次数=（MaxAutoRetries+1）*（MaxAutoRetriesNextServer+1）

###### 测试

- 加上注解@EnableEurekaClient开启eureka client功能
- 启动注入 RestTemplate实例，并在RestTemplate的实例加上@LoadBalanced注解，结合并开启了Ribbon负载均衡功能



## LoadBalancerClient简介

负载均衡器的核心类为 LoadBalancerClient，LoadBalancerClient可以获取负载均的服务提供者的实例信息。

- RibbonController.java 注入LoadBalancerClient 实例，通过loadBalancerClient.choose()选择对应的客户端实例
- 负载均衡器LoadBalancerClient是怎么获取客户端的信息的
  1、负载均衡器LoadBalancerClient是从Eureka Client获取服务注册列表信息的，并将服务注册列表信息缓存了一份。
  2、在调用choose()方法时，根据负载均衡策略选择一个服务实例的信息，从而进行了负载均衡
  ————————————————
  版权声明：本文为CSDN博主「恰子李」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
  原文链接：https://blog.csdn.net/qiaziliping/article/details/108124266