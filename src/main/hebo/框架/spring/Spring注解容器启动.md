### AnnotationConfigApplicationContext加载Bean过程

```java
第一种先注册主类的Bean然后在refresh中再去扫描其他Bean

public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
		//初始化reader和scanner，beanFactory
		this();
		//2.将配置类注册到容器中
		register(componentClasses);
		//3.刷新容器    和ClassPathXmlApplicationContext中开始一样了
		refresh();
	}
第二种是直接先把扫描路径下的bean全都注册好，再去执行后续步骤
public AnnotationConfigApplicationContext(String... basePackages) {
	this();
	//扫描包，并注册其中的beandefinition
	scan(basePackages);
	refresh();
}
```



1. ###### 第一步调用无参构造器，因为java继承的特性，会默认先执行父类的无参构造器

   AnnotationConfigApplicationContext->GenericApplicationContext->AbstractApplicationContext

- 在AbstractApplicationContext中会初始化初一个ResourcePatternResolver解析器
- 最后在AbstractApplicationContext自己初始化reader和scanner，并初始化出环境配置StandardEnvironment，以及确定需要被扫描的注解
- 在GenericApplicationContext中会直接new出一个DefaultListableBeanFactory

![image-20210915112156830](../../../../../../../../Programfile/Typora/upload/image-20210915112156830.png)

![在这里插入图片描述](https://gitee.com/workerbo/gallery/raw/master/2020/2020080310274060.png)









###### ApplicationContext 使用

1. spring默认的扫描器其实不是这个scanner对象，而是在后面自己又重新new了一个ClassPathBeanDefinitionScanner

2. spring在执行工程后置处理器 ConfigurationClassPostProcessor 时，去扫描包时会 new一个ClassPathBeanDefinitionScanner

3. 这里的 scanner仅仅是为了程序员可以手动调用 AnnotationConfigApplicationContext 对象的 scan 方法

   ```
   public AnnotationConfigApplicationContext() {
       //准备环境配置，加入一些配置的后置处理器，注解解析器等
   private final AnnotatedBeanDefinitionReader reader;
   //扫描器，用于过滤出带注解的类，如Component、Configuration注解
   private final ClassPathBeanDefinitionScanner scanner;
   
   }
   ```

   ![在这里插入图片描述](https://gitee.com/workerbo/gallery/raw/master/2020/20200803165148498.png)



###### ConfigurationClassPostProcessor 使用

org.springframework.context.annotation.ComponentScanAnnotationParser#parse











#### Full和Lite模式

为BeanDefinition设置属性为lite或者full。如果加了@Configuration，那么对应的BeanDefinition为full，如果加了@Bean，@Component，@ComponentScan，@Import，@ImportResource这些注解，则为lite。lite和full均表示这个BeanDefinition对应的类是一个配置类。
运行时会给该类生成一个CGLIB子类放进容器，有一定的性能、时间开销（这个开销在Spring Boot这种拥有大量配置类的情况下是不容忽视的，这也是为何Spring 5.2新增了proxyBeanMethods属性的最直接原因）
[正因为被代理了，所以@Bean方法 不可以是private、不可以是final](https://blog.csdn.net/demon7552003/article/details/107988310)

###### 配置类为什么要添加@Configuration注解

​     调用的一个普通的java method创建的普通对象。这个对象不被Spring所管理意味着，首先它的**域（Scope）定义失效了**，**其次它没有经过一个完整的生命周期，那么我们所定义所有的Bean的后置处理器都没有作用到它身上，其中就包括了完成AOP的后置处理器，所以AOP也失效了**。



`@DependsOn`注解主要用于指定当前bean所依赖的beans。任何被指定依赖的bean都由Spring保证在当前bean之前创建。在少数情况下，bean不是通过**属性**或**构造函数参数**显式依赖于另一个bean，但却需要要求另一个bean优先完成初始化，则可以使用`@DependsOn`这个注解。



[Resource框架体系介绍](https://zhuanlan.zhihu.com/p/70460173)






##### 学习过程

视频配合笔记的雷神视频，重点在流程，debug。

spring官方文档重点在每一个主要功能点的细节





