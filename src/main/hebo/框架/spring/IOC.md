#### spring  ioc

######  所依赖对象的获取被IOC容器给反转了：依赖注入。

######  ApplicationContext ，这个就是大名鼎鼎的 Spring 容器，它继承 BeanFactory

1. 继承 `org.springframework.context.MessageSource` 接口，提供国际化的标准访问策略。
2. 继承 `org.springframework.context.ApplicationEventPublisher` 接口，提供强大的**事件**机制。
3. 扩展 ResourceLoader ，可以用来加载多种 Resource ，可以灵活访问不同的资源。
4. 对 Web 应用的支持。



IoC 主要由 `spring-beans` 和 `spring-context` 项目，进行实现

`BeanFactory`的实现是按需创建，即第一次获取Bean时才创建这个Bean，而`ApplicationContext`会一次性创建所有的Bean。

###### 统一的资源**定义**和资源加载**策略**

















2. 