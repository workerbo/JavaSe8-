#### spring  ioc

######  bean放入容器

```java
class` `Hello{
  ``public` `void` `print(){
    ``System.out.println(``"HelloWorld!"``);
  ``}
}
public` `class` `SpringIoc {
public` `static` `HashMap contextmap=``new` `HashMap();
    /**
 ``* 初始化一个bean放到spring容器中
 ``*/
public` `static` `void` `initBean(Class clazz){
  ``try` `{
    ``Object obj = clazz.newInstance();
    ``contextmap.put(clazz.getSimpleName(), obj);
  ``} ``catch` `(Exception e) {
  ``} 
}
/**
 ``* 这是SpringIOC的核心方法
 ``* 参考:http://www.tuicool.com/articles/qYfYJ3E
 ``*/
public` `static` `void` `componentScan(String packagePath){
  ``//扫描packagePath下的所有java文件及其子package，调用initBean()方法
}
public` `static` `void` `main(String[] args) {
  ``initBean(Hello.``class``);
  ``Hello he=(Hello) contextmap.get(``"Hello"``);
  ``he.print();
}
}
```

###### spring容器的初始化过程

1、web应用程序启动时，tomcat会读取web.xml文件中的context-parm（含有配置文件的路径）和listener节点，接着会为应用程序创建一个ServletContext，为全局共享，Spring ioc容器就是存储在这里

2、tomcat将context-param节点转换为键值对，写入到ServletContext中

3、创建listener节点中的ContextLoaderListener实例，调用该实例，初始化webapplicationContext，这是一个接口，其实现类为XmlWebApplicationContext（即spring的IOC容器），其通过ServletContext.getinitialParameter（"contextConfigLoaction"）从ServletContext中获取context-param中的值（即spring ioc容器配置文件的路径），这就是为什么要有第二步的原因。接着根据配置文件的路径加载配置文件信息（其中含有Bean的配置信息）到WebApplicationContext（即spring ioc容器）中，将WebApplicationContext以WebApplicationContext.ROOTWEBAPPLICATIONCONTEXTATTRIBUTE为属性Key，将其存储到ServletContext中，便于获取。至此，spring ioc容器初始化完毕

4、容器初始化web.xml中配置的servlet，为其初始化自己的上下文信息servletContext，并加载其设置的配置信息到该上下文中。将WebApplicationContext（即spring ioc容器）设置为它的父容器。其中便有SpringMVC（假设配置了SpringMVC），这就是为什么spring ioc是springmvc ioc的父容器的原因

###### bean的生命周期

#### spring cxf

Apache CXF可以发布多种协议的WebService，Spring支持整合cxf到项目中，可以简化后台构架，例如cxf发布SOAP协议WebService和RestFul WebService。

##### spring  AOP

spring aop 

spring整合aspectJ

使用四种方式：1、基于接口的增强。2.基于shema的增加  3.基于shema和遗留代码【已有增强】  4.基于注解的增强

##### spring事务

A如果没有受事务管理：  则线程内的connection 的 autoCommit为true。
B得到事务时事务传播特性依然生效，得到的还是A使用的connection，但是 不会改变autoCommit的属性。所以B当中是按照每条sql进行提交的。

检查性异常不指定就不会回滚，事务嵌套方法必须用代理对象。

线程绑定资源，通过资源获取工具类访问事务同步器获取线程本地化资源。持久化模板的执行方法严格调用工具获取和释放资源。【不要自己从数据源获取资源】。无事务方法中，工具类获取的是新资源，需要手动释放。事务中启动新线程会启动新事务。

spring事务通过TransactionManager接口提供SPI，提供实现类对不同持久化框架的事务实现做了代理。	

###### 事务是XML到注解

　使用@Transactional对类或方法进行事务增强的标注。

　在配置文件中加入<tx:annotation-driven transaction-manager="txManager"/>,对标注@Transactional的Bean进行加工处理，以织入事务管理切面。

##### spring使用Mybaits

以sqlSessionFactory为核心，configLocation指定配置文件的路径。mapperLocations指定映射文件的扫描路径。

通过接口名称+接口方法与映射命名空间+映射ID可以一一匹配。mapperScannerConfigurer的basePackage指定路径下的所有接口类转化为spring管理的bean。

数据批量插入，减少数据量。



#### spring  ioc

##### spring容器的初始化过程

1、web应用程序启动时，tomcat会读取web.xml文件中的context-parm（含有配置文件的路径）和listener节点，接着会为应用程序创建一个ServletContext，为全局共享，Spring ioc容器就是存储在这里

2、tomcat将context-param节点转换为键值对，写入到ServletContext中

3、创建listener节点中的ContextLoaderListener实例，调用该实例，初始化webapplicationContext，这是一个接口，其实现类为XmlWebApplicationContext（即spring的IOC容器），其通过ServletContext.getinitialParameter（"contextConfigLoaction"）从ServletContext中获取context-param中的值（即spring ioc容器配置文件的路径），这就是为什么要有第二步的原因。接着根据配置文件的路径加载配置文件信息（其中含有Bean的配置信息）到WebApplicationContext（即spring ioc容器）中，将WebApplicationContext以WebApplicationContext.ROOTWEBAPPLICATIONCONTEXTATTRIBUTE为属性Key，将其存储到ServletContext中，便于获取。至此，spring ioc容器初始化完毕

4、容器初始化web.xml中配置的servlet，为其初始化自己的上下文信息servletContext，并加载其设置的配置信息到该上下文中。将WebApplicationContext（即spring ioc容器）设置为它的父容器。其中便有SpringMVC（假设配置了SpringMVC），这就是为什么spring ioc是springmvc ioc的父容器的原因

##### bean的生命周期





Spring事务异常rollback-[only](https://blog.csdn.net/sgls652709/article/details/49472719)