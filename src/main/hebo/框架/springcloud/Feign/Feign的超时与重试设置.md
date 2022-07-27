###### Feign的重试、超时自定义

Feign的重试机制在源码中默认关闭的，因为Ribbon的重试机制和Fiegn原来的重试机制冲突，所以在一般情况下，Feign的重试机制指的就是Ribbon的重试机制，本文也是如此。

实际应用中， 在Fallback之前，需要对服务配置重试机制，当多次重试服务，还是服务不可用的情况下，就触发Fallback。

```
feign时间的默认配置Request.Options 连接超时10s 读取超时60s，重试次数默认为5次（包含首次请求）
如果我们没有配置feign超时时间，上面的时间也会被ribbon覆盖,请求连接时间和超时时间，默认为1秒，在RibbonClientConfiguration类定义。
```



```
//超时时间设置,开启重试机制，默认为5次（包含首次请求）
package com.example.demo;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfigure {
     public static int connectTimeOutMillis = 12000;//超时时间
     public static int readTimeOutMillis = 12000;
     @Bean
     public Request.Options options() {
            return new Request.Options(connectTimeOutMillis, readTimeOutMillis);
     }

     @Bean
     public Retryer feignRetryer(){
          Retryer retryer = new Retryer.Default(100, 1000, 4);
          return retryer;
     }
     表示每间隔100ms，最大间隔1000ms重试一次，最大重试次数是1，因为第三个参数包含了第一次请求。
}
```



实现Feign重试机制有两种方式：

1.全局方案

在自定义类FeignRetryConfig上加注解@Configuration

2.局部方案

@FeignClient注解增加 configuration = FeignRetryConfig.class

调用次数 = `(ribbon.MaxAutoRetriesNextServer + 1) \* (ribbon.MaxAutoRetries + 1)`**





- Feign 自带重试机制，默认不开启，原理是捕获异常，发现超时异常，会进行重试，直到达到最大重试次数，退出循环请求
- Ribbon 也实现了自己的重试机制，基于RxJava，异步处理超时异常，默认也是不开启，Ribbon的默认配置类 DefaultClientConfigImpl 读取超时时间5s 连接超时时间2s，如果Ribbon没有配置重试时间和次数，默认重试**1次**。
- 推荐使用Ribbon 重试机制，需要注意关闭OkToRetryOnAllOperations，不然很容易出现接口幂等性问题，而且下游服务的GET请求，是要求只做查询功能
  