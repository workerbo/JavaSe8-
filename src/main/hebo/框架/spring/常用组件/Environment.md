Environment的存储容器PropertySources，MutablePropertySources实现了PropertySources接口。容器内部是一个**CopyOnWriteArrayList**集合

PropertySource：spring存储环境变量属性的基类只有name属性和泛型的source属性.需要关注的有两个点
第一是该对象重写了hashCode方法和equals方法 ，只是对属性name做了处理。说明一个PropertySource对象绑定到一个唯一的name属性上面

**简单理解PropertySource是存储单元 类似Map中的Entry，他是由多个键值对构成的一个组。 而PropertySources是一个容器类似于Map**



###### springboot当中

容器中发送了一个ApplicationEnvironmentPreparedEvent事件。这里在**SimpleApplicationEventMulticaster**中断点查看监听该事件的监听器是**ConfigFileApplicationListener**



Binder的使用其实比较简单 有点类似注解ConfigurationProperties的作用，都是将属性绑定到某个具体的对象上。 但是有一点区别 ConfigurationProperties是在容器启动时绑定的，而Binder是我们手动编码动态的绑定上去的。



PropertySource 的顺序非常重要，因为 Spring 只要读到属性值就返回。**

Environment 是对 JDK 环境、Servlet 环境、Spring 环境的抽象；

它主要为我们的应用程序环境的两个方面的支持：profiles and properties

StandardEnvironment：标准环境，普通 Java 应用时使用，会自动注册 System.getProperties() 【JVM】和 System.getenv()【OS】到环境；

StandardServletEnvironment：标准 Servlet 环境，其继承了 StandardEnvironment，Web 应用时使用默认除了 StandardEnvironment 的两个属性外，还有另外三个属性：servletContextInitParams（ServletContext）、servletConfigInitParams（ServletConfig）、jndiProperties（JNDI）。





可以通过spring的Environment 提供的getProperty方法获取对应的值，除此之外，spring还提供了非常方便的注解@Value供我们使用。可以通过spring的Environment 提供的getProperty方法获取对应的值，除此之外，spring还提供了非常方便的注解@Value供我们使用。或者springboot的@ConfigPropertys



指定外部配置文件？在jar的当前目录或者子目录config之下，或者通过参数指定。



###### [spring xml版](https://blog.csdn.net/weixin_38441454/article/details/123753421)  

<context:property-placeholder location=“”>     指定配置文件的路径

通过**PropertySourcesPlaceholderConfigurer**  这个Factory后置处理器去解析。



1. spring在启动的时候会在上下文中创建Environment，并将系统变量放入environment对象中。
2. 通过aware接口调用，将environment对象传给PropertySourcesPlaceholderConfigurer。
3. 将environment对象放入MutablePropertySources中，并重写getProperty方法。
4. 收集本地配置文件中的属性值包装成properties对象后，最终包装成PropertySource对象。
5. 遍历所有的BeanDefinition，修改BeanDefinition中的PropertyValues中的每一个属性值，把属性值有${XXX}修改成真正的参数值
6. 如果是@Value注解，则先收集注解，再进行属性的依赖注入，解析过程和标签的类似
   

###### spring注解版

通过@PropertySource 加载配置文件  ConfigurationClassPostProcessor，通过解析注解去加载

######   springboot版

通过监听器去扫描、有默认的文件名。

就是springapplication自动装配ApplicationListener（ConfigFileApplicationListener）,利用观察者模式，监听ApplicationEvent。SpringApplication创建好了environment之后，发布ApplicationEnvironmentPreparedEvent事件。ConfigFileApplicationListener监听到此事件之后，加载配置文件，设置到environment中






