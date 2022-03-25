# ConfigurationClassPostProcessor

ConfigurationClassPostProcessor是Factory的后置处理器，在这个类中，会解析加了@Configuration的配置类，还会解析@ComponentScan、@ComponentScans注解扫描的包，以及解析@Import，@Bean等注解。【JavaConfig 和 组件扫描】

ConfigurationClassPostProcessor 中重写 了postProcessBeanDefinitionRegistry() 方法和 postProcessBeanFactory() 方法



@Configuration 里@Bean方法会被封装成FactoryBean的BeanDefinition。最后反射调用。

### **1.ConfigurationClassPostProcessor加载与注册BeanDefinition**

### **2.ConfigurationClassPostProcessor实例化与调用**	



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