

Java断言的特点是：断言失败时会抛出`AssertionError`，导致程序结束退出。因此，断言不能用于可恢复的程序错误，只应该用于开发和测试阶段。JVM默认关闭断言指令。

`Error`和`Exception`，`Error`表示严重的错误，程序对此一般无能为力

某些异常是应用程序逻辑处理的一部分，应该捕获并处理。例如：

- `NumberFormatException`：数值类型的格式错误
- `FileNotFoundException`：未找到文件
- `SocketException`：读取网络失败



还有一些异常是程序逻辑编写不对造成的，应该修复程序本身。【：编译器对RuntimeException及其子类不做强制捕获要求，非`RuntimeException`（Checked Exception）需强制捕获，或者用`throws`在调用处的方法声明；不推荐捕获了异常但不进行任何处理。】

当某个方法抛出了异常时，如果当前方法没有捕获异常，异常就会被抛到上层调用方法，直到遇到某个`try ... catch`被捕获为止：

捕获到异常并再次抛出时，一定要留住原始异常，否则很难定位第一案发现场【异常做了转化】

在代码中获取原始异常可以使用`Throwable.getCause()`方法。如果返回`null`，说明已经是“根异常”了。

在`catch`中抛出异常，不会影响`finally`的执行。JVM会先执行`finally`，然后抛出异常。

这说明`finally`抛出异常后，原来在`catch`中准备抛出的异常就“消失”了，因为只能抛出一个异常。没有被抛出的异常称为“被屏蔽”的异常



因此，我们总结出编写Fixture的套路如下：

1. 对于实例变量，在`@BeforeEach`中初始化，在`@AfterEach`中清理，它们在各个`@Test`方法中互不影响，因为是不同的实例；
2. 对于静态变量，在`@BeforeAll`中初始化，在`@AfterAll`中清理，它们在各个`@Test`方法中均是唯一实例，会影响各个`@Test`方法。

大多数情况下，使用`@BeforeEach`和`@AfterEach`就足够了。只有某些测试资源初始化耗费时间太长，以至于我们不得不尽量“复用”时才会用到`@BeforeAll`和`@AfterAll`。

最后，注意到每次运行一个`@Test`方法前，JUnit首先创建一个`XxxTest`实例，因此，每个`@Test`方法内部的成员变量都是独立的，不能也无法把成员变量的状态从一个`@Test`方法带到另一个`@Test`方法。



Java标准库提供了动态代理功能，允许在运行期动态创建一个接口的实例；

动态代理是通过`Proxy`创建代理对象，然后将接口方法“代理”给`InvocationHandler`完成的。加载子类并实例化。

```
public class Main {
    public static void main(String[] args) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println(method);
                if (method.getName().equals("morning")) {
                    System.out.println("Good morning, " + args[0]);
                }
                return null;
            }
        };
        Hello hello = (Hello) Proxy.newProxyInstance(
            Hello.class.getClassLoader(), // 传入ClassLoader
            new Class[] { Hello.class }, // 传入要实现的接口
            handler); // 传入处理调用方法的InvocationHandler
        hello.morning("Bob");
    }
}

interface Hello {
    void morning(String name);
}

public class HelloDynamicProxy implements Hello {
    InvocationHandler handler;
    public HelloDynamicProxy(InvocationHandler handler) {
        this.handler = handler;
    }
    public void morning(String name) {
        handler.invoke(
           this,
           Hello.class.getMethod("morning", String.class),
           new Object[] { name });
    }
}
```

servlet3.0 首先提供了 `@WebServlet` ，`@WebFilter` 等注解，这样便有了抛弃 `web.xml` 的第一个途径，凭借注解声明 servlet 和 filter 来做到这一点。

除了这种方式，servlet3.0 规范还提供了更强大的功能，可以在运行时动态注册 servlet ，filter，listener。以 servlet 为例，过滤器与监听器与之类似。ServletContext 为动态配置 Servlet 增加了如下方法

###### Spring MVC是如何逐步简化Servlet的编程的**



过滤器的触发时机是容器后，servlet之前，所以过滤器的doFilter的入参是ServletRequest ，而不是httpservletrequest。因为过滤器是在httpservlet之前。【拦截器执行在过滤器中】

拦截器【通过反射被调用】可以获取IOC容器中的各个bean，而过滤器就不行，这点很重要，在拦截器里注入一个service，可以调用业务逻辑。

实上调用Servlet的doService()方法是在chain.doFilter(request, response);这个方法中进行的。

springMVC的机制是由同一个Servlet来分发请求给不同的Controller，其实这一步是在Servlet的service()方法中执行的。



![image-20210111175729186](https://gitee.com/workerbo/gallery/raw/master/2020/image-20210111175729186.png)



- `1]` 处，配置了 `org.springframework.web.context.ContextLoaderListener` 对象。这是一个 `javax.servlet.ServletContextListener` 对象，会初始化一个**Root** Spring WebApplicationContext 容器。这个过程，详细解析，见 [「3. Root WebApplicationContext 容器」](http://svip.iocoder.cn/Spring-MVC/context-init-Root-WebApplicationContext/#) 。
- `[2]` 处，配置了 `org.springframework.web.servlet.DispatcherServlet` 对象。这是一个 `javax.servlet.http.HttpServlet` 对象，它除了拦截我们制定的 `*.do` 请求外，也会初始化一个**属于它**的 Spring WebApplicationContext 容器。并且，这个容器是以 `[1]` 处的 Root 容器作为父容器。是在 DispatcherServlet 初始化的过程中执行。

Spring是父容器，SpringMVC是其子容器，

子容器可以访问父容器对象，而父容器不可以访问子容器对象。

DispatcherServlet是前端控制器设计模式的实现，提供Spring Web MVC的集中访问点，而且负责职责的分派，而且与Spring IoC容器无缝集成，从而可以获得Spring的所有好处。

可以自定义servlet.xml配置文件的位置和名称，默认为WEB-INF目录下，名称为[<servlet-name>]-servlet.xml，如spring-servlet.xml。【DispatcherServlet单独的配置文件】

```
<!-- 启用spring mvc 注解,用于@RequestMapping -->
    <context:annotation-config />
```

在Controller的方法中，如果需要WEB元素HttpServletRequest，HttpServletResponse和HttpSession，只需要在给方法一个对应的参数，那么在访问的时候SpringMVC就会自动给其传值

![image-20210111182121439](https://gitee.com/workerbo/gallery/raw/master/2020/image-20210111182121439.png)

​                                                                                      spring  web应用

- Root WebApplicationContext：这是对J2EE三层架构中的service层、dao层进行配置，如业务bean，数据源(DataSource)等。通常情况下，配置文件的名称为applicationContext.xml。在web应用中，其一般通过ContextLoaderListener来加载。调用ServletContext的setAttribute方法，将其设置到ServletContext中，属性的key为”org.springframework.web.context.WebApplicationContext.ROOT”，最后的”ROOT"字样表明这是一个 Root WebApplicationContext。会调用ServletContext的getAttribute方法来判断是否存在Root WebApplicationContext。如果存在，则将其设置为自己的parent。这就是父子上下文(父子容器)的概念。

![image-20210111181935280](https://gitee.com/workerbo/gallery/raw/master/2020/image-20210111181935280.png)

​                                                     spring 父子容器概览：在web层则有多种选择，



###### 为什么不能在Spring的applicationContext.xml中配置全局扫描

如果都在spring容器中，这时的SpringMVC容器中没有对象，所以加载处理器，适配器的时候就会找不到映射对象，映射关系，因此在页面上就会出现404的错误。因为在解析@ReqestMapping解析过程中，initHandlerMethods()函数只是对Spring MVC 容器中的bean进行处理的，并没有去查找父容器的bean。因此不会对父容器中含有@RequestMapping注解的函数进行处理，更不会生成相应的handler。所以当请求过来时找不到处理的handler，导致404

springmvc配置文件只有一个Servlet，方便与spring融合？