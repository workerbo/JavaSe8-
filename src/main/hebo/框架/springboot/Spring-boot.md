快速的启动Spring应用

要知道在application.properties中的配置是通过`BeanPostProcessor`进行注入的，具体完成该功能的`BeanPostProcessor`实现类是`ConfigurationPropertiesBindingPostProcessor`。

###### [常用注解](https://cloud.tencent.com/developer/article/1442150)

- `@Import` 注解支持导入普通 java 类【无】，并将其声明成一个bean。主要用于将多个分散的 java config 配置类融合成一个更大的 config 类。
- `ImportSelector` 是一个接口，该接口中只有一个 selectImports 方法，用于返回全类名数组。所以利用该特性我们可以给容器**动态导入 N 个** Bean。【启动过程的@EnableAutoImportConfiguration注解】
- `ImportBeanDefinitionRegistrar` 也是一个接口，它可以**手动注册bean到容器中**，从而我们可以对类进行个性化的定制。(需要搭配 @Import 与 @Configuration 一起使用。）
- `@Conditional` 注释可以实现只有在特定条件满足时才启用一些配置。
- 创建 Condition 实现类，@Conditional 注解只有一个 Condition 类型的参数，Condition 是一个接口，该接口只有一个返回布尔值的 matches() 方法，该方法返回 true 则条件成立，配置类生效。反之，则不生效。在该例子中我们直接返回 true。
- @EnableConfigurationProperties 注解表示对 @ConfigurationProperties 的内嵌支持，默认会将对应 Properties Class 作为 bean 注入的 IOC 容器中，即在相应的 Properties 类上不用加 @Component 注解。

###### springboot启动过程

SpringApplication 只是将一个典型的Spring应用的启动流程进行了扩展

1. SpringBoot的启动过程其实是包含两个过程，即SpringApplication类的初始化，以及run方法执行。
2. ApplicationContextInitializer接口的实现类主要是在SpringApplication类的构造函数中进行加载和初始化
3. 在ApplicationContextInitializer接口的子类中重要的方法就是重写接口中的initialize，在SpringBoot启动的时候真正执行是在run()方法内的prepareContext(…）方法内执行的。



Spring MVC是基于 Servlet 的一个 MVC 框架 主要解决 WEB 开发的问题

###### spring事件

Spring有两大类事件 ，一类是 ApplicationEvent.一类是ApplicationContextEvent。



###### spring IOC

第一阶段，所有 bean 定义都通过 BeanDefinition 的方式注册到 BeanDefinitionRegistry 中。





###### spring常见接口

- ###### ApplicationContextAware

当一个类实现了这个接口（ApplicationContextAware）之后，这个类就可以方便获得ApplicationContext中的所有bean。换句话说，就是这个类可以直接获取spring配置文件中，所有引用到的bean对象。

- BeanFactory。基础类型IoC容器，提供完整的IoC服务支持。如果没有特殊指定，默认采用延 迟初始化策略（lazy-load）

- ###### ApplicationContext

 ApplicationContext接口,它由BeanFactory接口派生而来，因而提供BeanFactory所有的功能。ApplicationContext以一种更向面向框架的方式工作以及对上下文进行分层和实现继承，ApplicationContext包还提供了以下的功能： 
  • MessageSource, 提供国际化的消息访问  
  • 资源访问，如URL和文件  （统一的资源文件读取方式）

 事件传播 ，有强大的事件机制(Event)  
  • 载入多个（有继承关系）上下文 ，使得每一个上下文都专注于一个特定的层次，比如应用的web层  

- ###### ApplicationContextInitializer

1. Sprng上下文的回调接口，在refresh()之前执行
2. 通常用于需要对应用程序上下文进行编程初始化的web应用程序中。例如，根据上下文环境注册属性源或激活配置文件等。
   可排序的（实现Ordered接口，或者添加@Order注解）
   　　看完这段解释，为了讲解方便，我们先看自定义 ApplicationContextInitializer 的三种方式。再通过SpringBoot的源码，分析生效的时间以及实现的功能等。

ApplicationListener

是一个接口，里面只有一个onApplicationEvent方法。

所以自己的类在实现该接口的时候，要实装该方法。

如果在上下文中部署一个实现了ApplicationListener接口的bean,

那么每当在一个ApplicationEvent发布到 ApplicationContext时，
这个bean得到通知。其实这就是标准的Oberver设计模式。

ApplicationEvent

是个抽象类，里面只有一个构造函数和一个长整型的timestamp。