Spring Cloud对Feign进行了增强，使Feign支持了Spring MVC注解，并整合了Ribbon和Eureka，从而让Feign的使用更加方便。**Feign是基于Ribbon实现的,具体的集群服务寻找其合适服务，负载均衡都是由ribbon实现，feign只是将调用方式封装为接口的方式**。

当程序启动时，会进行包扫描，扫描所有@FeignClients的注解的类，并且将这些信息注入Spring [IOC](https://so.csdn.net/so/search?q=IOC&spm=1001.2101.3001.7020)容器中，当定义的的Feign接口中的方法被调用时，通过JDK的代理方式，来生成具体的RequestTemplate.

Feign会为每个接口方法创建一个RequestTemplate对象，该对象封装了HTTP请求需要的全部信息，如请求参数名，请求方法等信息都是在这个过程中确定的。

然后RequestTemplate生成Request,然后把Request交给Client去处理，这里指的是Client可以是JDK原生的URLConnection,Apache的HttpClient,也可以是OKhttp，最后Client被封装到LoadBalanceClient类，这个类结合Ribbon负载均衡发起服务之间的调用。

1、首先加入依赖
		2、启动类加入@EnableFeignClients代表本服务可以为Feign客户端
		3、定义接口AClient，用于调用服务A的请求方法，我们定义接口，实际的实现是Feign做的。





###### Feign的拦截器RequestInterceptor

SpringCloud的微服务使用Feign进行服务间调用的时候可以使用RequestInterceptor统一拦截请求来完成设置header等相关请求，但RequestInterceptor和ClientHttpRequestInterceptor有点不同，它拿不到原本的请求，所以要通过其他方法来获取原本的请求

首先创建自定义的RequestInterceptor

这里通过RequestContextHolder获取到当前的request

###### Ribbon

SpringCloud中处理负载均衡，Feign 底层基于ribbon组件实现

###### Hystrix

Hystrix是处理熔断和降级的

- feign.hystrix.enabled = true，开启熔断机制【默认关闭的】

- 在@FeignClient的注解的fallback配置加上快速失败的处理类（该处理类是Feign熔断器的逻辑处理类，必须实现被@FeignClient修饰的接口）

###### 问题

Feign并没有全部实现SpringMVC的功能，如果使用GET请求到接口，就无法将参数绑定到POJO，但是可以使用以下的几种方式实现相应的功能。

通过RequestInterceptor将参数编成一个map







##### 注意点

1. ## **用对Http Client** 

   ​	**feign中http client**

   ​       如果不做特殊配置，OpenFeign默认使用jdk自带的HttpURLConnection，我们知道HttpURLConnection没有连接池、性能和效率比较低，如果采用默认，很可能会遇到性能问题导致系统故障。

   可以采用Apache HttpClient，properties文件中增加下面配置：

   ```javascript
   feign.httpclient.enabled=true
   ```

   

   pom文件中增加依赖：

   ```javascript
   <dependency>
       <groupId>io.github.openfeign</groupId>
       <artifactId>feign-httpclient</artifactId>
       <version>9.3.1</version>
   </dependency>
   ```

   

   也可以采用OkHttpClient，properties文件中增加下面配置：

   ```javascript
   feign.okhttp.enabled=true
   ```

   

   pom文件增加依赖：

   ```javascript
   <dependency>
       <groupId>io.github.openfeign</groupId>
       <artifactId>feign-okhttp</artifactId>
       <version>10.2.0</version>
   </dependency>
   ```

     **ribbon中的Http Client**

   通过OpenFeign作为[注册中心](https://cloud.tencent.com/product/tse?from=10680)的客户端时，默认使用Ribbon做[负载均衡](https://cloud.tencent.com/product/clb?from=10680)，Ribbon默认也是用jdk自带的HttpURLConnection，需要给Ribbon也设置一个Http client，比如使用okhttp，在properties文件中增加下面配置：

   ```javascript
   ribbon.okhttp.enabled=true
   ```

2. ## **全局超时时间** 

   OpenFeign可以设置超时时间，简单粗暴，设置一个全局的超时时间，如下：

   ```javascript
   feign.client.config.default.connectTimeout=2000
   feign.client.config.default.readTimeout=60000
   ```

   > 如果不配置超时时间，默认是连接超时10s，读超时60s，在源码feign.Request的内部类Options中定义。

   在一个系统中使用OpenFeign调用外部三个服务，每个服务提供两个接口，其中serviceC的一个接口需要60才能返回，那上面的readTimeout必须设置成60s。要防止这样的故障发生，就必须保证接口1能fail-fast。最好的做法就是给serviceC单独设置超时时间。

3. ## **单服务设置超时时间** 

     

   ```
   feign.client.config.serviceC.connectTimeout=2000
   feign.client.config.serviceC.readTimeout=60000
   ```

   

4. ## **熔断超时时间** 

   

5. 

6. ## **重试默认不开启** 

   ​		OpenFeign默认是不支持重试的，可以在源代码FeignClientsConfiguration中feignRetryer中看出。

   ```
   
       @Bean
       @ConditionalOnMissingBean
       public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
       }
   ```

   要开启重试，我们可以自定义Retryer，比如下面这行代码：

   ```javascript
   Retryer retryer = new Retryer.Default(100, 1000, 2);
   ```

   

   表示每间隔100ms，最大间隔1000ms重试一次，最大重试次数是1，因为第三个参数包含了第一次请求。
   
7. ###  **重试**

   Ribbon重试有不少需要注意的地方，这里分享4个。

   1.同一实例最大重试次数，不包括首次调用，配置如下：

   ```javascript
   serviceC.ribbon.MaxAutoRetries=1
   ```

   

   > 这个次数不包括首次调用，配置了1，重试策略会先尝试在失败的实例上重试一次，如果失败，请求下一个实例。

   2.同一个服务其他实例的最大重试次数，这里不包括第一次调用的实例。默认值为1：

   ```javascript
   serviceC.ribbon.MaxAutoRetriesNextServer=1
   ```

   

   3.是否对所有操作都重试，如果改为true，则对所有操作请求都进行重试,包括post，建议采用默认配置false。

   ```javascript
   serviceC.ribbon.OkToRetryOnAllOperations=false
   ```

   

   4.对指定的http状态码进行重试

   ```javascript
   serviceC.retryableStatusCodes=404,408,502,500
   ```

8. ## **hystrix超时** 

    hystrix默认不开启，但是如果开启了hystrix，因为hystrix是在Ribbon外面，所以超时时间需要符合下面规则：hystrix超时 >= (MaxAutoRetries + 1) * (ribbon ConnectTimeout + ribbon ReadTimeout)



参考：OpenFeign的9个坑，每个都能让你的系统奔溃https://cloud.tencent.com/developer/article/1866274

