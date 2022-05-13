Spring Cloud对Feign进行了增强，使Feign支持了Spring MVC注解，并整合了Ribbon和Eureka，从而让Feign的使用更加方便。**Feign是基于Ribbon实现的,具体的集群服务寻找其合适服务，负载均衡都是由ribbon实现，feign只是将调用方式封装为接口的方式**。

1、首先加入依赖
		2、启动类加入@EnableFeignClients代表本服务可以为Feign客户端
		3、定义接口AClient，用于调用服务A的请求方法，我们定义接口，实际的实现是Feign做的。