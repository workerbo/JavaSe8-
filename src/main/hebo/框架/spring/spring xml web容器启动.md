###### spring容器的初始化过程

1、web应用程序启动时，tomcat会读取web.xml文件中的context-parm（含有配置文件的路径）和listener节点，接着会为应用程序创建一个ServletContext，为全局共享，Spring ioc容器就是存储在这里

2、tomcat将context-param节点转换为键值对，写入到ServletContext中

3、创建listener节点中的ContextLoaderListener实例，调用该实例，初始化webapplicationContext，这是一个接口，其实现类为XmlWebApplicationContext（即spring的IOC容器），其通过ServletContext.getinitialParameter（"contextConfigLoaction"）从ServletContext中获取context-param中的值（即spring ioc容器配置文件的路径），这就是为什么要有第二步的原因。接着根据配置文件的路径加载配置文件信息（其中含有Bean的配置信息）到WebApplicationContext（即spring ioc容器）中，将WebApplicationContext以WebApplicationContext.ROOTWEBAPPLICATIONCONTEXTATTRIBUTE为属性Key，将其存储到ServletContext中，便于获取。至此，spring ioc容器初始化完毕

4、容器初始化web.xml中配置的servlet，为其初始化自己的上下文信息servletContext，并加载其设置的配置信息到该上下文中。创建SpringMVC 容器，将WebApplicationContext（即spring ioc容器）设置为它的父容器。



Web项目如何初始化SpringIOC容器 ：思路：当服务启动时（tomcat），通过监听器将SpringIOC容器初始化一次（该监听器 spring-web.jar已经提供）


```
 <!-- 指定 Ioc容器（applicationContext.xml）的位置-->
  <context-param>
  		<!--  监听器的父类ContextLoader中有一个属性contextConfigLocation，该属性值 保存着 容器配置文件applicationContext.xml的位置 -->
  		<param-name>contextConfigLocation</param-name>
  		<param-value>classpath:applicationContext.xml</param-value>
  </context-param>  
  <listener>
  	<!-- 配置spring-web.jar提供的监听器，此监听器 可以在服务器启动时 初始化Ioc容器。
  		初始化Ioc容器（applicationContext.xml） ，
  			1.告诉监听器 此容器的位置：context-param
  			2.默认约定的位置	:WEB-INF/applicationContext.xml
  	 -->
  	<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
```

