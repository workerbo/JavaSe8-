在[微服务](https://so.csdn.net/so/search?q=微服务&spm=1001.2101.3001.7020)项目中使用threadLocal存放了用户信息，在使用feign调用的时候需要将用户参数发送给另一个微服务，在从threadLocal中获取用户信息时发现是null，原因是feign开启hystrix默认会新建一个线程，而threadLocal存取数据时是根据线程来的。

服务间调用时需传递 JWT Token，希望在Feign发起的所有请求中都自动加上token以在各服务中传递环境信息。**将token从tomcat工作线程传递到hystrix线程池线程。**

【JWT Token 一是用于确保访问的安全，二是存储用户context信息。】

HystrixRequestContext内部：

```
private static ThreadLocal<HystrixRequestContext> requestVariables = new ThreadLocal<HystrixRequestContext>();
```

当前线程下创建一个HystrixRequestContext对象，会存在threadLocal当中。HystrixRequestContext通过切面在调用前初始化

```
public static HystrixRequestContext initializeContext() {
    HystrixRequestContext state = new HystrixRequestContext();
    requestVariables.set(state);
    return state;
}
```

使用完毕之后关闭

```
if (HystrixRequestContext.isCurrentThreadInitialized()) {
    HystrixRequestContext.getContextForCurrentThread().shutdown();
}
```

参考：https://blog.csdn.net/alex_xfboy/article/details/87989995