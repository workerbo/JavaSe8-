

> ### 	web

#### 		servlet3.0

基于注解，取代了web.xml.[Servlet](https://so.csdn.net/so/search?q=Servlet&spm=1001.2101.3001.7020) 3.0规范里面的一些简单注解，利用它们可以来注册Servlet、Filter以及Listener等组件



​	ServletContainerInitializer
​			Registration
​			ServletRegistration
​			FilterRegistration
​			ServletContext

#### 		异步请求

​	servlet3.0异步处理
​			返回Callable
​			返回DeferredResult



Servlet容器为每个Web应用程序自动创建一个唯一的`ServletContext`实例，这个实例就代表了Web应用程序本身。

Shared libraries / runtimes pluggability



总结一下就是，**Servlet容器在启动应用的时候，会扫描当前应用每一个jar包里面的`META-INF/services/javax.servlet.ServletContainerInitializer`文件中指定的实现类【ServletContainerInitializer的实现类】，然后，再运行该实现类中的方法**。

我们可以在ServletContainerInitializer的实现类上使用一个`@HandlesTypes`注解，而且在该注解里面我们可以写上一个类型数组哟，也就是说可以指定各种类型。

Servlet容器在启动应用的时候，会将`@HandlesTypes`注解里面指定的类型下面的子类，包括实现类或者子接口等，全部给我们传递过来。是ServletContainerInitializer接口 onStartup方法的参数

如果是以注解【@WebServlet @Filter @Listener 由Servlet容器解析处理】的方式来注册web组件，那么前提是这些web组件是由我们自己来编写的。如果项目中导入的是第三方jar包，比如在项目中导入了阿里巴巴的连接池里面的Filter，现在的项目中是没有web.xml文件的，所以我们就要利用**ServletContext**将它们给注册进来了。


​	

	@HandlesTypes(value={HelloService.class})
	public class MyServletContainerInitializer implements ServletContainerInitializer {/*
	 * 参数：
	 *    ServletContext sc：代表当前web应用。一个web应用就对应着一个ServletContext对象，此外，它也是我们常说的四大域对象之一，
	 *    我们给它里面存个东西，只要应用在不关闭之前，我们都可以在任何位置获取到
	 *    
	 *    Set<Class<?>> arg0：我们感兴趣的类型的所有后代类型
	 *    
	 */
	@Override
	public void onStartup(Set<Class<?>> arg0, ServletContext sc) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println("我们感兴趣的所有类型：");
		// 好，我们把这些类型来遍历一下
		for (Class<?> clz : arg0) {
			System.out.println(clz);
		}
		
		// 注册Servlet组件
		ServletRegistration.Dynamic servlet = sc.addServlet("userServlet", new UserServlet());
		// 配置Servlet的映射信息
		servlet.addMapping("/user");
		// 注册Listener组件
			sc.addListener(UserListener.class);
			
			// 注册Filter组件
			FilterRegistration.Dynamic filter = sc.addFilter("userFilter", UserFilter.class);
	}
	}

并不是说，你只要拿到了ServletContext对象就能注册组件了，因为必须是在项目启动的时候，才能注册组件。

两处来使用ServletContext对象注册组件。

第一处就是利用基于运行时插件的ServletContainerInitializer机制得到ServletContext对象，然后再往其里面注册组件。本讲通篇所讲述的就是在这一处使用ServletContext对象来注册组件。

第二处，你可能想不到，我们上面不是编写过一个监听器（即UserListener）吗？它是来监听项目的启动和停止的，在监听项目启动的方法中，传入了一个ServletContextEvent对象，即事件对象，我们就可以通过该事件对象的getServletContext方法拿到ServletContext对象，拿到之后，是不是就可以往它里面注册组件



Servlet3.0整合spring mvc【SPI机制】

```
<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-war-plugin</artifactId>
				<version>2.4</version>
				<configuration>
					<failOnMissingWebXml>false</failOnMissingWebXml>
				</configuration>
			</plugin>
			告诉maven工程即使没有web.xml文件，也不要报错
```



web容器（即Tomcat服务器）在启动应用的时候，会扫描当前应用每一个jar包里面的META-INF/services/javax.servlet.ServletContainerInitializer文件中指定的实现类，然后再运行该实现类中的方法。

恰好在spring-web-4.3.11.RELEASE.jar中的META-INF/services/目录里面有一个javax.servlet.ServletContainerInitializer文件，并且在该文件中指定的实现类就是org.springframework.web.SpringServletContainerInitializer，打开该实现类，发现它上面标注了@HandlesTypes(WebApplicationInitializer.class)这样一个注解。

因此，web容器在启动应用的时候，便会来扫描并加载org.springframework.web.SpringServletContainerInitializer实现类，而且会传入我们感兴趣的类型（即WebApplicationInitializer接口）的所有后代类型，最终再运行其onStartup方法
        实际上就是先调用其（例如我们自定义的MyWebAppInitializer）最高父类的onStartup方法，创建根容器；然后再调用其次高父类的onStartup方法，创建web容器以及DispatcherServlet；接着，根据其重写的getServletMappings方法来为DispatcherServlet配置映射信息，差不多就是这样了。


```
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer
```

![image-20210827173313768](../../../../../../../../Programfile/Typora/upload/image-20210827173313768.png)

spring 与我们之前的 demo 不同，并没有在 SpringServletContainerInitializer 中直接对 servlet 和 filter 进行注册，而是委托给了一个陌生的类 `org.springframework.web.WebApplicationInitializer` 。

如果我们想以注解方式（也可以说是以配置类的方式）来整合Spring MVC，即再也不要在web.xml文件中进行配置，那么我们只需要自己来继承**AbstractAnnotationConfigDispatcherServletInitializer**这个抽象类【WebApplicationInitializer接口的子类】就行了。继承它之后，它里面会给我们预留一些抽象方法，**例如getServletConfigClasses、getRootConfigClasses以及getServletMappings等抽象方法**，我们只须重写这些抽象方法即可，这样就能指定DispatcherServlet的配置信息了，随即，DispatcherServlet就会被自动地注册到ServletContext对象中。

createRootApplicationContext，然后，根据创建的根容器创建上下文加载监听器（即ContextLoaderListener），接着，向ServletContext中注册这个监听器。listener.setContextInitializers(getRootApplicationContextInitializers());

调用createServletApplicationContext方法来创建一个web的IOC容器

调用createDispatcherServlet方法来创建一个DispatcherServlet，将创建好的DispatcherServlet注册到ServletContext中



- servlet web容器：也即子容器，只来扫描controller控制层组件（一般不包含核心业务逻辑，只有数据校验和视图渲染等工作）与视图解析器等等
- Root web容器：扫描业务逻辑核心组件，包括不同的数据源等等



至此Servlet3.0 利用ServletContainerInitializer机制，并通过无配置文件的方式整合Spring MVC 完毕。还知道了如何无配置文件方式来个性化定制Spring MVC。

如果要添加三大组件：1.监听器里，2ServletContainerInitializer机制，



定制和接管SpringMVC



第一步，首先你得写一个配置类，然后将`@EnableWebMvc`注解标注在该配置类上。

`@EnableWebMvc`注解的作用就是来开启Spring MVC的定制配置功能。相当于我们以前在xml配置文件中加上了``这样一个配置.

第二步，配置组件。让其继承WebMvcConfigurerAdapter抽象类



```
视图控制器
<mvc:view-controller path="/hello" view-name="hello"></mvc:view-controller>

<mvc:default-servlet-handler/>
将Spring MVC处理不了的请求交给Tomcat服务器，它是专门来针对静态资源的
```



Servlet异步操作：【目的？】

​	*// 1. 先来让该Servlet支持异步处理，即asyncSupported=true* 	

*// 2. 开启异步模式*，通过HttpServletRequest对象的startAsync方法即可开启异步模式

第三步，我们可以给返回的异步的AsyncContext对象里面设置一些东西。【start方法运行一个Runnable的实现类】

第四步，异步处理完了之后，就要给客户端一个响应了。调用一下AsyncContext对象的complete方法；然后，给客户端一个响应，要做到这一点，得先通过AsyncContext对象的getResponse方法获取到ServletResponse响应对象，再通过该响应对象给客户端来写数据。

Spring MVC中的异步请求处理是基于Servlet 3.0中的异步请求处理机制的，相当于做了一个简单的封装。



第一步，控制器返回Callable。其实，这里要说的是控制器中方法的返回值要写成Callable了，而再也不能是以前普通的字符串对象了。

第二步，控制器返回Callable以后，Spring MVC就会异步地启动一个处理方法（即Spring MVC异步处理），也即将Callable提交到TaskExecutor（任务执行器）里面，并使用一个隔离的线程进行处理。

第三步，与此同时，DispatcherServlet和所有的Filter将会退出Servlet容器的线程（即主线程），但是response仍然保持打开的状态。既然response依旧保持打开状态，那就表明还没有给浏览器以响应，因此我们还能给response里面写数据。

第四步，最终，Callable返回一个结果，并且Spring MVC会将请求重新派发给Servlet容器，恢复之前的处理。也就是说，之前的response依旧还保存着打开的状态，仍然还可以往其里面写数据。

第五步，如果还是把上一次的请求再发过来，假设上一次的请求是async01，那么DispatcherServlet依旧还是能接收到该请求，收到以后，DispatcherServlet便会再次执行，来恢复之前的处理。【重走一遍流程，此时目标方法不用执行了，正常拦截器pre会执行两次】

在异步处理的情况下，Spring MVC并不能拦截到真正的业务逻辑的整个处理流程，而想要做到这一点，那就得使用异步的拦截器了。在使用Spring MVC的情况下，那么你只须实现AsyncHandlerInterceptor接口即能编写一个异步拦截器了。

或者原生Servlet里面的AsyncListener。

**另外一个线程拿到临时保存的DeferredResult对象之后，只要将最终处理的结果给该对象设置进去，那么另一边的线程就能立即得到返回结果了**。

以上就是Spring MVC异步请求处理的第二种使用方式，即将方法的返回值写成DeferredResult。





**内嵌 Tomcat 的加载可能不依赖于 Servlet3.0 规范和 SPI** ！它完全走了一套独立的逻辑。 **jar 包的运行策略，不会按照 Servlet 3.0 的策略去加载 ServletContainerInitializer**！[所以ServletContainerInitializer的实现类是在ServletWebServerFactory中直接new启动的]

大体原理，`@ServletComponentScan` 注解上的 `@Import(ServletComponentScanRegistrar.class)` ，它会将扫描到的 `@WebServlet`、`@WebFilter`、`@WebListener` 的注解对应的类，最终封装成 FilterRegistrationBean、ServletRegistrationBean、ServletListenerRegistrationBean 对象，注册到 Spring 容器中。

```
第二种方法
@Bean 
public ServletRegistrationBean helloWorldServlet ()  {     
ServletRegistrationBean helloWorldServlet = new ServletRegistrationBean();     myServlet.addUrlMappings( "/hello" );     myServlet.setServlet( new HelloWorldServlet()); 返回helloWorldServlet；} 

```

在springboot的启动过程中创建容器后的onrefresh方法中创建了内嵌Servlet容器，并且创建了一个匿名的ServletContextInitializer【其中获取了容器中的ServletContextInitializer，会返回一个数组】。最后嵌套Servlet容器中new的ServletContextInitializer的子类TomcatStarter 中被回调。这样自定义的ServletContextInitializer子类中的三大组件会被注入到嵌套Servlet容器。

IDEA创建Servlet[项目](https://blog.csdn.net/wanvale/article/details/112790026)