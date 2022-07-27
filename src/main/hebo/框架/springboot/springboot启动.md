spring-boot的启动方式主要有三种:

1. 运行带有main方法类

2. 通过[命令行](https://so.csdn.net/so/search?q=命令行&spm=1001.2101.3001.7020) java -jar 的方式

3. 通过spring-boot-plugin的方式

> ### 嵌入式Servlet容器启动原理；

[springboot jar启动](https://blog.csdn.net/csdnjiamin/article/details/109559161)

- BOOT-INF：这个文件夹下有两个文件夹classes用来存放用户类，也就是原始jar.original里的类；还有一个是lib，就是这个原始jar.original引用的依赖。
- META-INF：这里是通过java -jar启动的入口信息，记录了入口类的位置等信息。
- org:Springboot loader的代码，通过它来启动。如果将`SpringBoot Class Loader` 也放到lib文件下，是根本无法被加载到的，因为它根本不符合jar文件的一个标准规范。
-  [因为`SpringBoot`实现了Jar包的嵌套](https://blog.csdn.net/kaihuishang666/article/details/108405691)，一个Jar包就可以完成整个程序的运行。引入自定义类加载器就是为了能解决jar包嵌套jar包的问题，系统自带的AppClassLoarder不支持读取嵌套jar包。



springboot 主类启动

1）、SpringBoot应用启动运行run方法

2）、refreshContext(context);SpringBoot刷新IOC容器【创建IOC容器对象，并初始化容器，创建容器中的每一 

个组件】；如果是web应用创建**AnnotationConfigEmbeddedWebApplicationContext**，否则： 

**AnnotationConfigApplicationContext** 

1. 调用构造函数AnnotationConfigServletWebServerApplicationContext

2. 调用父类构造函数ServletWebServerApplicationContext

3. 调用父类构造函数GenericWebApplicationContext

4. 调用父类构造函数GenericApplicationContext

5. 帮我们初始了一个DefaultListableBeanFactory，也就是beanFactory

   

3）、refresh(context);刷新刚才创建好的ioc容器；

4）、 onRefresh(); web的ioc容器重写了onRefresh方法 

5）、webioc容器会创建嵌入式的Servlet容器；**createEmbeddedServletContainer**(); 

6）、获取嵌入式的****Servlet****容器工厂

EmbeddedServletContainerFactory containerFactory = getEmbeddedServletContainerFactory(); 

从ioc容器中获取EmbeddedServletContainerFactory 组件；**TomcatEmbeddedServletContainerFactory**创建 

对象，后置处理器一看是这个对象，就获取所有的定制器来先定制Servlet容器的相关配置； 

7）、**使用容器工厂获取嵌入式的****Servlet****容器**：

this.embeddedServletContainer = containerFactory 

.getEmbeddedServletContainer(getSelfInitializer()); 

8）、嵌入式的Servlet容器创建对象并启动Servlet容器； 

**先启动嵌入式的****Servlet****容器，再将****ioc****容器中剩下没有创建出的对象获取出来**







> ### 使用外置的Servlet容器



1）、必须创建一个war项目；（利用idea创建好目录结构） 

2）、将嵌入式的Tomcat指定为provided； 

3）、必须编写一个**SpringBootServletInitializer**的子类，并调用confifigure方法 



**服务器启动**SpringBoot应用【SpringBootServletInitializer】，启动ioc容器； 



servlet3.0（Spring注解版）： 8.2.4 Shared libraries / runtimes pluggability： 规则：

 1）、服务器启动（web应用启动）会创建当前web应用里面每一个jar包里面ServletContainerInitializer实例：

 2）、ServletContainerInitializer的实现放在jar包的META-INF/services文件夹下，有一个名为 javax.servlet.ServletContainerInitializer的文件，内容就是ServletContainerInitializer的实现类的全类名 

3）、还可以使用@HandlesTypes，在应用启动的时候加载我们感兴趣的类；

 流程： 1）、启动Tomcat

 2）、org\springframework\spring-web\4.3.14.RELEASE\spring-web-4.3.14.RELEASE.jar!\META- INF\services\javax.servlet.ServletContainerInitializer： Spring的web模块里面有这个文件：org.springframework.web.SpringServletContainerInitializer 

3）、SpringServletContainerInitializer将@HandlesTypes(WebApplicationInitializer.class)标注的所有这个类型 的类都传入到onStartup方法的Set>；为这些WebApplicationInitializer类型的类创建实例；

 4）、每一个WebApplicationInitializer都调用自己的onStartup； 

5）、相当于我们的SpringBootServletInitializer的类会被创建对象，并执行onStartup方法



RegistrationBean【继承的org.springframework.boot.web.servlet.ServletContextInitializer类】 是 Spring Boot 中广泛应用的一个注册类，负责把 servlet，filter，listener 给容器化，使他们被 Spring 托管，并且完成自身对 Web 容器的注册。




ApplicationContextInitializer是Spring框架原有的概念, 这个类的主要目的就是在ConfigurableApplicationContext类型（或者子类型）的ApplicationContext做refresh之前，允许我们对ConfigurableApplicationContext的实例做进一步的设置或者处理。通常用于需要对应用程序进行某些初始化工作的`web程序中`。

通常用于需要对应用程序上下文进行编程初始化的web应用程序中。例如，根据上下文环境注册属性源或激活概要文件。

springboot是利用SPI机制找的，spring web应用是在利用Servlet启动机制创建容器之后被调用的。





> ## springboot启动类执行过程

创建 **SpringApplication**

- 保存一些信息。
- 判定当前应用的类型。ClassUtils。Servlet
- **bootstrappers****：初始启动引导器（**List<Bootstrapper>**）：去spring.factories文件中找** org.springframework.boot.**Bootstrapper**
- 找 **ApplicationContextInitializer**；去**spring.factories****找** **ApplicationContextInitializer**
- - List<ApplicationContextInitializer<?>> **initializers**

- **找** **ApplicationListener  ；应用监听器。**去**spring.factories****找** **ApplicationListener**

- - List<ApplicationListener<?>> **listeners**


运行 **SpringApplication**

- **StopWatch**
- **记录应用的启动时间**
- **创建引导上下文（Context环境）****createBootstrapContext()**
- - 获取到所有之前的 **bootstrappers 挨个执行** intitialize() 来完成对引导启动器上下文环境设置

- 让当前应用进入**headless**模式。**java.awt.headless**
- **获取所有** **RunListener****（运行监听器）【为了方便所有Listener进行事件感知】**
- - getSpringFactoriesInstances 去**spring.factories****找** **SpringApplicationRunListener**. 

- 遍历 **SpringApplicationRunListener 调用 starting 方法；**

- - **相当于通知所有感兴趣系统正在启动过程的人，项目正在 starting。**

- 保存命令行参数；ApplicationArguments
- 准备环境 prepareEnvironment（）;
- - 返回或者创建基础环境信息对象。**StandardServletEnvironment**
  - **配置环境信息对象。**

- - - **读取所有的配置源的配置属性值。**

- - 绑定环境信息
  - 监听器调用 listener.environmentPrepared()；通知所有的监听器当前环境准备完成

- 创建IOC容器（createApplicationContext（））

- - 根据项目类型（Servlet）创建容器，
  - 当前会创建 **AnnotationConfigServletWebServerApplicationContext**

- **准备ApplicationContext IOC容器的基本信息**  **prepareContext()**

- - 保存环境信息
  - IOC容器的后置处理流程。
  - 应用初始化器；applyInitializers；

- - - 遍历所有的 **ApplicationContextInitializer 。调用** **initialize.。来对ioc容器进行初始化扩展功能**
    - 遍历所有的 listener 调用 **contextPrepared。EventPublishRunListenr；通知所有的监听器****contextPrepared**

- - **所有的监听器 调用** **contextLoaded。通知所有的监听器** **contextLoaded；**

- **刷新IOC容器。**refreshContext

- - 创建容器中的所有组件（Spring注解）

- 容器刷新完成后工作？afterRefresh
- 所有监听 器 调用 listeners.**started**(context); **通知所有的监听器** **started**
- **调用所有runners；**callRunners()
- - **获取容器中的** **ApplicationRunner** 
  - **获取容器中的**  **CommandLineRunner**
  - **合并所有runner并且按照@Order进行排序**
  - **遍历所有的runner。调用 run** **方法**

- **如果以上有异常，**

- - **调用Listener 的 failed**

- **调用所有监听器的 running 方法**  listeners.running(context); **通知所有的监听器** **running** 
- **running如果有问题。继续通知 failed 。****调用所有 Listener 的** **failed；****通知所有的监听器** **failed**



##### SpringApplicationRunListener

SpringApplicationRunListener 接口的作用主要就是在Spring Boot 启动初始化的过程中可以通过SpringApplicationRunListener接口回调来让用户在启动的各个流程中可以加入自己的逻辑。
 Spring Boot启动过程的关键事件（按照触发顺序）包括：

1. 开始启动
2. Environment构建完成
3. ApplicationContext构建完成
4. ApplicationContext完成加载
5. ApplicationContext完成刷新并启动
6. 启动完成
7. 启动失败

通过spring factories机制在容器创建之前创建实例。



## Spring事件触发过程

Spring的事件触发过程是交由SpringApplicationRunListener接口的实现类EventPublishingRunListener来代理实现的。在构造EventPublishingRunListener实例的过程中，会将application关联的所有ApplicationListener实例关联到initialMulticaster中，以方便initialMulticaster将事件传递给所有的监听器。【根据事件类型处理】



#### CommandLineRunner、ApplicationRunner

CommandLineRunner、ApplicationRunner 接口是在容器启动成功后的最后一步回调（类似开机自启动）。

有预加载数据需求——提前加载到缓存中或类的属性中，并且希望执行操作的时间是在容器启动末尾时间执行操作。针对这种场景，SpringBoot提供了两个接口，分别是CommandLineRunner和ApplicationRunner。





#### Jar的启动过程



# 自定义starter

## 1、starter启动原理

- starter-pom引入 autoconfigurer 包

![img](https://cdn.nlark.com/yuque/0/2020/png/1354552/1606995919308-b2c7ccaa-e720-4cc5-9801-2e170b3102e1.png)

- autoconfigure包中配置使用 **META-INF/spring.factories** 中 **EnableAutoConfiguration 的值，使得项目启动加载指定的自动配置类**
- **编写自动配置类 xxxAutoConfiguration -> xxxxProperties**

- - **@Configuration**
  - **@Conditional**
  - **@EnableConfigurationProperties**
  - **@Bean**
  - ......

**引入starter** **--- xxxAutoConfiguration --- 容器中放入组件 ---- 绑定xxxProperties ----** **配置项**

## 2、自定义starter

启动器依赖自动配置；别人只需要引入启动器（starter）

**atguigu-hello-spring-boot-starter（启动器）**【<!‐‐引入spring‐boot‐starter；所有starter的基本配置‐‐> 】

**atguigu-hello-spring-boot-starter-autoconfigure（自动配置包）**







Spring & Spring boot所有的扩展接口，以及各个扩展点的使用场景。https://nowjava.com/article/44059