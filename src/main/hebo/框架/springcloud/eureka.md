1 首先需要一个服务注册中心eureka Server，服务提供者eureka client向服务注册中心eureka server注册，将自己的信息（服务名、服务的IP地址等）通过REST API的形式提交给服务注册中心eureka server；

2 同样，服务消费者eureka client也向服务注册中心eureka server注册，同时服务消费者获取一份服务注册列表的信息，这样服务消费者就知道服务提供者的IP地址，可以通过HTTP远程调度来消费服务提供者的服务







#### Eureka 自我保护机制

eureka client在默认情况下每个30秒发送一次心跳进行服务续约。通过服务续约来告知eureka server 该eureka client任然可用；如果eureka server90秒没有收到eureka client的心跳**，eureka server会将eureka client实例从注册列表删除**


Eureka Server 在运行期间会去统计心跳失败比例在 15 分钟之内是否低于 85%，**如果低于 85%，Eureka Server 会将这些实例保护起来，让这些实例不会过期**，但是在保护期内如果服务刚好这个服务提供者非正常下线了，此时服务消费者就会拿到一个无效的服务实例，此时会调用失败，对于这个问题需要服务消费者端要有一些容错机制，如重试，断路器等。

我们在单机测试的时候很容易满足心跳失败比例在 15 分钟之内低于 85%，这个时候就会触发 Eureka 的保护机制，一旦开启了保护机制，则服务注册中心维护的服务实例就不是那么准确了，此时我们可以使用`eureka.server.enable-self-preservation=false`来关闭保护机制，这样可以确保注册中心中不可用的实例被及时的剔除（**不推荐**）。