> #### 		Spring容器创建过程
>
> Spring IOC容器在启动的时候，会先保存所有注册进来的bean的定义信息，将来，BeanFactory就会按照这些bean的定义信息来为我们创建对象

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

![image-20210915112156830](../../../../../../../../../Programfile/Typora/upload/image-20210915112156830.png)

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





BeanFactoryPostProcessor先完成组件BeanDefinetion的扫描

在前面创建IOC容器时，需要先传入配置类，而我们在解析配置类的时候，由于这个配置类里面有一个@EnableAspectJAutoProxy注解，对于该注解，我们之前也说过，它会为我们容器中注册一个AnnotationAwareAspectJAutoProxyCreator（后置处理器）的beanDefinetion，这还仅仅是这个@EnableAspectJAutoProxy注解做的事，除此之外，容器中还有一些默认的后置处理器的定义。【在refresh实例化bean单例之前beanDefinetion已经注册完成。】

添加上`depends-on="book,user"`这样一个属性之后，那么在创建名字为person的bean之前，得先把名字为book和user的bean给创建出来。也就是说，depends-on属性决定了bean的创建顺序。

## Spring源代码分析

spring核心逻辑AbstractApplicationContext的refresh()方法如下

```java
public void refresh() {
    synchronized (this.startupShutdownMonitor) {
        // 刷新前的预准备工作
        prepareRefresh();
        // 提取bean的配置信息并封装成BeanDefinition实例，然后将其添加到注册中心。注册中心是一个ConcurrentHashMap<String,BeanDefinition>类型，key为Bean的名字，value为BeanDefinition实例。
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
       //对beanFactory进行一些配置，注册一些beanFactoryPostProcessor和一些特殊的Bean。
        prepareBeanFactory(beanFactory);
        
            //留给子类在BeanFactory准备工作完成后处理一些工作。
            postProcessBeanFactory(beanFactory);
           //调用 BeanFactory的后置处理器。
           invokeBeanFactoryPostProcessors(beanFactory);
           //注册Bean的后置处理器。
            registerBeanPostProcessors(beanFactory);
            //国际化相关功能
            initMessageSource();
            //初始化事件派发器；
            initApplicationEventMulticaster();
            // 提供给子容器类，供子容器去实例化其他的特殊的Bean
            onRefresh();
            // 处理容器中已有的ApplicationListener
            registerListeners();
            //初始化容器中剩余的单实例bean
            finishBeanFactoryInitialization(beanFactory);
            //最后一步，回调beanFactory生命周期处理器的onRefresh方法,发布容器刷新完成事件
            finishRefresh();
        
        }
    }
```

### prepareRefresh()

```markdown
1. 记录启动时间，设置容器的active和close状态。 
2. initPropertySources():提供给子容器类，子容器类可覆盖该方法进行一些自定义的属性设置。
3. getEnvironment().validateRequiredProperties()：检验属性的合法性
4. this.earlyApplicationEvents = new LinkedHashSet<ApplicationEvent>() ：保存容器中的一些早期的事件，待事件多播器创建后执行。
```

### obtainFreshBeanFactory()

提取bean的配置信息并封装成BeanDefinition实例，然后将其添加到注册中心。注册中心是一个ConcurrentHashMap<String,BeanDefinition>类型，key为Bean的名字，value为BeanDefinition实例。

```markdown
1. refreshBeanFactory：如果当前容器已经有了BeanFactory就销毁原来的BeanFactory。然后创建一个DefaultListableBeanFactory();【GenericApplicationContext已经重新了这个方法，因为在创建上下文的时候已经创建了BeanFactory】
    * 对BeanFactory并进行配置，主要配置是否允许BeanDefinition覆盖，是否允许Bean间的循环引用。
    * 加载BeanDefinition，解析 XML文件 和配置文件，将其转换为BeanDefinition，然后保存到DefaultListableBeanFactory的beanDefinitionMap字段中。
2. getBeanFactory() 简单的返回beanFactory，即DefaultListableBeanFactory。
```

### prepareBeanFactory（）

```markdown
1. 设置BeanFactory的类加载器、设置支持SPEL表达式的解析器。
2. 添加ApplicationContextAwareProcessor用于处理XXXAware接口的回调。 
3. 设置忽略一些接口。并注册一些类，这些类可以在bean里直接进行自动装配。
4. 添加ApplicationListenerDetector用于识别并保存ApplicationListener的子类。
```

### postProcessBeanFactory（）：

提供给子容器类，子容器类可以覆盖该方法在BeanFactory准备工作完成后处理一些工作。

### invokeBeanFactoryPostProcessors()

注册、执行BeanFactoryPostProcessor类型的监听方法。

```markdown
* BeanFactoryPostProcessor是beanFactory后置处理器，在整个BeanFactory标准初始化完成后进行拦截调用， 
* BeanDefinitionRegistryPostProcessor继承了BeanFactoryPostProcessor，在beanFactory解析完所有的BeanDefinition后拦截调用。
* BeanFactoryPostProcessor来源
    * 通过ApplicationContent的addBeanFactoryPostProcessor()方法手动添加自己的拦截器
    * 系统默认了一些BeanFactoryPostProcessor。例如：ConfigurationClassPostProcessor用来处理@Configuration
    * ConfigurationClassPostProcessor用来处理扫描添加的
    标注的Spring配置类。
* 调用顺序 【先调用手动注册的。然后是是系统内置的，待测试？】
    1. 先调用BeanDefinitionRegistryPostProcessor类型的拦截器，
    2. 然后再依次调用实现了PriorityOrdered,Ordered接口的BeanFactoryPostProcessor
    3. 最后调用普通的BeanFactoryPostProcessor
    
    使用OrderComparator.INSTANCE【单例模式】对实现了Order接口、PriorityOrder接口的进行排序。
```

### registerBeanPostProcessors()

注册Bean的后置处理器。

```markdown
1. 从beanFactory里获取所有BeanPostProcessor类型的Bean的名称。
2. 调用beanFactory的getBean方法并传入每一个BeanPostProcesso类型的Bean名称，从容器中获取该Bean的实例。
3. 
    1. 第一步向beanFactory注册实现了PriorityOrdered的BeanPostProcessor类型的Bean实例。
    2. 第二步向beanFactory注册实现了Ordered的BeanPostProcessor类型的Bean实例。
    3. 第三步向beanFactory注册普通的BeanPostProcessor类型的Bean实例。
    4. 最后一步向beanFactory重新注册实现了MergedBeanDefinitionPostProcessor的BeanPostProcessor类型的Bean实例

4. 向beanFactory注册BeanPostProcessor的过程就是简单的将实例保存到beanFactory的beanPostProcessors属性中。
```

### initMessageSource()

国际化相关功能

```markdown
1. 看容器中是否有id为messageSource的，类型是MessageSource的Bean实例。如果有赋值给messageSource，如果没有自己创建一个DelegatingMessageSource。
2. 把创建好的MessageSource注册在容器中，以后获取国际化配置文件的值的时候，可以自动注入MessageSource。
```

### initApplicationEventMulticaster()

初始化事件派发器；

```markdown
1. 看容中是否有名称为applicationEventMulticaster的，类型是ApplicationEventMulticaster的Bean实例。如果没有就创建一个SimpleApplicationEventMulticaster。
2. 把创建好的ApplicationEventMulticaster添加到BeanFactory中。
```

### onRefresh()：

提供给子容器类，供子容器去实例化其他的特殊的Bean。

### registerListeners()：

处理容器中已有的ApplicationListener。

```markdown
1. 从容器中获得所有的ApplicationListener
2. 将每个监听器添加到事件派发器（ApplicationEventMulticaster）中；
3. 处理之前步骤产生的事件；
```

### finishBeanFactoryInitialization()：

初始化容器中剩余的单实例bean：拿到剩余的所有的BeanDefinition，依次调用getBean方法（详看beanFactory.getBean的执行流程）



2. beanFactory.preInstantiateSingletons()：初始化所有剩下的单实例bean

   

   - 获取bean的定义注册信息
   - 根据bean的定义注册信息判断bean是否是抽象的、单实例的、懒加载的

- 单实例bean的创建流程
                      
  - 在创建bean实例之前，会执行InstantiationAwareBeanPostProcessor这种类型的后置处理器中的两个方法，即postProcessBeforeInstantiation方法
  - 创建bean实例

  - 遍历获取到的所有后置处理器，若是MergedBeanDefinitionPostProcessor这种类型，则调用其postProcessMergedBeanDefinition方法

  - 为bean实例的属性赋值
    -  遍历获取到的所有后置处理器，若是InstantiationAwareBeanPostProcessor这种类型，则调用其postProcessAfterInstantiation方法

    -  再来遍历获取到的所有后置处理器，若是InstantiationAwareBeanPostProcessor这种类型，则调用其postProcessPropertyValues方法

    -  正式开始为bean的属性赋值
  - 初始化bean
    - 执行xxxAware接口的方法
      - 执行后置处理器初始化之前的方法（即postProcessBeforeInitialization方法）
      - 执行初始化方法
      - 执行后置处理器初始化之后的方法（即postProcessAfterInitialization方法）
  - 注册bean的销毁方法

- 将创建出的单实例bean添加到缓存中
- 来遍历所有的bean，并来判断遍历出来的每一个bean是否实现了SmartInitializingSingleton接口的

preInstantiateSingletons---getBean-doGetBean-getSingleton--[获取不到时，那么就会调用singletonFactory的getObject()方法。并添加到singletonObjects这个map当中]-- createBean-docreateBean|resolveBeforeInstantiation(**会在任何bean创建之前，先尝试返回bean的实例（通过实例Bean后置处理器）。**)---createBeanInstance |  属性赋值|初始化|



### finishRefresh()：

最后一步。

```markdown
1. 初始化和生命周期有关的后置处理器；LifecycleProcessor，如果容器中没有指定处理就创建一个DefaultLifecycleProcessor加入到容器。
2. 获取容器中所有的LifecycleProcessor回调onRefresh()方法。【LifeCycle】
3. 发布容器刷新完成事件ContextRefreshedEvent。
```

### beanFactory.getBean方法执行的过程

1. 首先将方法传入的beanName进行转换：先去除FactoryBean前缀（&符）如果传递的beanName是别名，则通过别名找到bean的原始名称。
2. 根据名称先从singletonObjects（一个Map类型的容）获取bean实例。如果能获取到就先判断该bean实例是否实现了FactoryBean，如果是FactoryBean类型的bean实例，就通过FactoryBean获取Bean。然后直接返回该bean实例。getBean方法结束。
3. 如果从singletonObjects没有获取到bean实例就开始**创建Bean的过程**。
   1. 首先标记该Bean处于创建状态。
   2. 根据Bean的名称找到BeanDefinition。查看该Bean是否有前置依赖的Bean。若有则先创建该Bean前置依赖的Bean。
   3. spring调用AbstractAutowireCapableBeanFactory的createBean方法并传入BeanDefinition开始创建对象。先调用resolveBeforeInstantiation给BeanPostProcessor一个机会去返回一个代理对象去替代目标Bean的实例。
   4. 如果BeanPostProcessor没有返回Bean的代理就通过doCreateBean方法创建对象。
      1. 首先确定Bean的构造函数，如果有有参构造器，先自动装配有参构造器，默认使用无参数构造器。
      2. 选择一个实例化策略去实例化bean。默认使用CglibSubclassingInstantiationStrategy。该策略模式中,首先判断bean是否有方法被覆盖,如果没有则直接通过反射的方式来创建,如果有的话则通过CGLIB来实例化bean对象. 把创建好的bean对象包裹在BeanWrapper里。
      3. 调用MergedBeanDefinitionPostProcessor的postProcessMergedBeanDefinition
      4. 判断容器是否允许循环依赖，如果允许循环依赖，就创建一个ObjectFactory类并实现ObjectFactory接口的唯一的一个方法getObject（）用于返回Bean。然后将该ObjectFactory添加到singletonFactories中。
      5. 调用populateBean为bean实例赋值。在赋值之前执行InstantiationAwareBeanPostProcessor的postProcessAfterInstantiation和postProcessPropertyValues方法。
      6. 调用initializeBean初始化bean。如果Bean实现了XXXAware，就先处理对应的Aware方法。然后调用beanProcessor的postProcessBeforeInitialization方法。再以反射的方式调用指定的bean指定的init方法。最后调用beanProcessor的postProcessAfterInitialization方法。
      7. 调用registerDisposableBeanIfNecessary，将该bean保存在一个以beanName为key，以包装了bean引用的DisposableBeanAdapter，为value的map中，在spring容器关闭时，遍历这个map来获取需要调用bean来依次调用Bean的destroyMethod指定的方法。
   5. 将新创建出来的Bean保存到singletonObjects中