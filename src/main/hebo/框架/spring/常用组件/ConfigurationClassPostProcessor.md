# ConfigurationClassPostProcessor



ConfigurationClassPostProcessor是Factory后置处理器，在这个类中，会解析加了@Configuration的配置类，还会解析@ComponentScan、@ComponentScans注解扫描的包，以及解析@Import，@Bean等注解。【JavaConfig 和 组件扫描】

@Configuration 里@Bean方法会被封装成FactoryBean的BeanDefinition。最后反射调用。

### **1.ConfigurationClassPostProcessor加载与注册BeanDefinition**



### **2.ConfigurationClassPostProcessor实例化与调用**	

1、拿到Spring当中当前所有的Bean的名称

 2、循环遍历这些类

 2.1、如果当前Bean已经被解析过了，那么不操作，

 2..2、如果当前Bean未解析，那么判断其是否为配置类，如果是则加入到集合，不是则不操作。

 判断是否为配置类的依据为：是否加入了@Configuration、是否加入了@Import、@Component、@ImportResource、@ComponentScan其中一个，如果加入了@Configuration那么则不会再去判断后面的

 3、判断是否存在配置类，不存在则停止

 4、对配置类进行排序，排序规则为其@Order注解

 5、获取BeanName生成器，如果没配置则会获取默认的

 6、创建配置类解析器

 7、do...while的解析我们的这些类

 8、parser.parse(candidates);解析这些类，



 9、到了我们重点的line1当中，这里面对我们上面解析拿到的ImportBeanDefinitionRegistart实现类单独的拿去执行

### ConfigurationClassPostProcessor处理@Configuration的过程：

1. 先从主从中心取出所有的BeanDefinition。依次判断，筛选出所有候选配置BeanDefinition（FullMode和LightMode）

     // 如果加了@Configuration，那么对应的BeanDefinition为full； 如果加了@Bean,@Component,@ComponentScan,@Import,@ImportResource这些注解，则为lite。
     lite和full均表示这个BeanDefinition对应的类是一个配置类

2. 创建一个ConfigurationClassParser，调用parse方法解析每一个配置类。

   1. 解析@PropertySources,将解析结果设置到Environment

   2. 利用ComponentScanAnnotationParser，将@ComponentScans标签解析成BeanDefinitionHolder。再迭代解析BeanDefinitionHolder

   3. 解析@Import，@ImportResource

   4. 将@Bean解析为MethodMetadata，将结果保存到ConfigurationClass中。

      

      最终解析完成的ConfigurationClass会被保存到ConfigurationClassParser的configurationClasses中。

3. 调用ConfigurationClassParser的loadBeanDefinitions方法，加载解析结果到注册中。

   1. 利用ConfigurationClassParser的configurationClasses获取所有的ConfigurationClass，依次调用loadBeanDefinitionsForConfigurationClass方法。
   2. loadBeanDefinitionsForConfigurationClass会将每一个BeanMethod转为ConfigurationClassBeanDefinition，最后将其添加到spring的注册中心。



##### ClassPathBeanDefinitionScanner

SpringBoot项目中或者 Spring项目中配置`<context:component-scan base-package="com.example.demo" />`
 ，那么在IOC 容器初始化阶段（调用beanFactoryPostProcessor阶段）扫描指定包下的类通过一定规则过滤后 将Class 信息包装成 BeanDefinition 的形式注册到IOC容器中。

Mybatis 的Mapper注册器(ClassPathMapperScanner) 是同过继承ClassPathBeanDefinitionScanner,并且自定义了过滤器规则来实现的。

##### AnnotatedBeanDefinitionReader

用于编程的方式注册 Bean，仅对显式注册的类的注解解析，并且会读取这个类上的注解进行解析~



最大的不同在于`AnnotatedBeanDefinitionReader`支持注册单个的`BeanDefinition`，而`ClassPathBeanDefinitionScanner`会一次注册所有扫描到的`BeanDefinition`。

##### 2. 第二步注册bean，使用了`reader`的`register`方法

register()还是委托为 AnnotatedBeanDefinitionReader 去做了







###### ignoreDependencyInterface

1. 自动装配时忽略指定接口或类的依赖注入，使用ignoreDependencyType已经足够

2. ignoreDependencyInterface的真正意思是在自动装配时忽略指定接口的实现类中，对外的依赖。【[接口中有没有某个接口是拥有该bean属性的setter方法的](https://www.liangzl.com/get-article-detail-160715.html)，】

   

   	public interface BeanFactoryAware extends Aware {
      	void setBeanFactory(BeanFactory beanFactory) throws BeansException;
      	}
      	ignoreDependencyInterface(BeanFactoryAware.class);
      	是指实现类中属性类型BeanFactory的字段被忽略。

   

发现英语中的autowiring特定指的是通过beans标签default-autowire属性来依赖注入的方式。区别在于，使用default-autowire会自动给所有的类都会从容器中查找匹配的依赖并注入，而使用@Autowired注解只会给这些注解的对象字段从容器查找依赖并注入。

通过这种方式保证了ApplicationContextAware和BeanFactoryAware中的容器保证是生成该bean的容器。【在web应用中会有多个容器】

但在实践中我们什么时候会使用ignoreDependencyInterface接口？
笔者使用Spring经验有限，只能给出目前的应用场景很少，但起码想到一个：假如我们想自定义一个类似的xxAware接口，比如ApplicationEventMulticasterAware。那么调用ignoreDependencyInterface方法可以保证获取到的ApplicationEventMulticaster对象就是生成该bean容器中的ApplicationEventMulticaster对象。



#### Full和Lite模式

为BeanDefinition设置属性为lite或者full。如果加了@Configuration，那么对应的BeanDefinition为full，如果加了@Bean，@Component，@ComponentScan，@Import，@ImportResource这些注解，则为lite。lite和full均表示这个BeanDefinition对应的类是一个配置类。
运行时会给该类生成一个CGLIB子类放进容器，有一定的性能、时间开销（这个开销在Spring Boot这种拥有大量配置类的情况下是不容忽视的，这也是为何Spring 5.2新增了proxyBeanMethods属性的最直接原因）
[正因为被代理了，所以@Bean方法 不可以是private、不可以是final](https://blog.csdn.net/demon7552003/article/details/107988310)

###### 配置类为什么要添加@Configuration注解

​     调用的一个普通的java method创建的普通对象。这个对象不被Spring所管理意味着，首先它的**域（Scope）定义失效了**，**其次它没有经过一个完整的生命周期，那么我们所定义所有的Bean的后置处理器都没有作用到它身上，其中就包括了完成AOP的后置处理器，所以AOP也失效了**。

