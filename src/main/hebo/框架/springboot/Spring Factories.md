Spring中有一种非常解耦的扩展机制：Spring Factories。这种扩展机制实际上是仿照Java中的SPI扩展机制来实现的。

java SPI就是提供这样的一个机制：为某个接口寻找服务实现的机制。有点类似IOC的思想，就是将装配的控制权移到程序之外，在模块化设计中这个机制尤其重要。

在Spring中也有一种类似与Java SPI的加载机制。它在META-INF/spring.factories文件中配置接口的实现类名称，然后在程序中读取这些配置文件并实例化。



loadFactoryNames 根据接口获取其接口类的名称，这个方法返回的是类名的列表。

loadFactories 根据接口类获取其实现类的实例，这个方法返回的是对象列表。

```dart
loadSpringFactories
    在这个方法中会遍历整个ClassLoader中所有jar包下的spring.factories文件。也就是说我们可以在自己的jar中配置spring.factories文件，不会影响到其它地方的配置，也不会被别人的配置覆盖。

spring.factories的是通过Properties解析得到的，所以我们在写文件中的内容都是安装下面这种方式配置的：

    
```