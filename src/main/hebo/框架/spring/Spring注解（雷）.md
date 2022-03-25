## Spring注解驱动开发

> ### 	容器类型
>

#### 		AnnotationConfigApplicationContext

​			配置类
​			       包扫描

- `AnnotationConfigApplicationContext`与`ClassPathXmlApplicationContext`作用一样，前者对应的是采用`JavaConfig`技术的应用，后者对应的是`XML`配置的应用

#### 		组件添加



##### 			@ComponentScan【@ComponentScans】

​         	@Component、@Service、	@Controller、@Repository

​            可以使用@ComponentScan注解来指定Spring扫描哪些包，可以使用excludeFilters()方法来指定扫描时排除哪些组件，也可以使用includeFilters()方法来指定扫描时只包含哪些组件。当使用includeFilters()方法指定只包含哪些组件时，需要禁用掉默认的过滤规则。【内置的过滤规则是扫描包下含有对应注解的类】
​            通过实现org.springframework.core.type.filter.TypeFilter接口来自定义过滤规则，此时，将@Filter中的type属性设置为FilterType.CUSTOM，classes属性设置为自定义规则的类所对应的Class对象。**自定义的规则则满足条件就会被扫描到容器，和其他注解无关。**

  

​      除了Spring内置的bean的名称之外，只输出了mainConfig和person，而没有输出使用@Repository、@Service、@Controller这些注解标注的组件的名称。这是因为当前MainConfig类上标注的@ComponentScan注解是使用的自定义规则，而在自定义规则的实现类（即MyTypeFilter类）中，直接返回了false，那么就是一个都不匹配了，自然所有的bean就都没被包含进去容器中了。


​        **配置类可以走另外一套流程。**【JavaConfig 实例化】

##### 			@Bean

​       @Bean注解是给IOC容器中注册一个bean，类型自然就是返回值的类型，id默认是用方法名作为id，如果在@Bean注解中明确指定了bean的名称，那么就会使用@Bean注解中指定的名称来作为bean的名称。

###### 			

###### 				

​			@Configuration  用于标注配置类【@Component的子类】


​			      @Conditional({Condition}):按照一定的条件进行判断,满足条件给容器中注册Bean,传入Condition数组,，使用时需自己创建类继承Condition然后重写match方法。springboot已经提供大量的子注解和对应的Condition类。【用于元注解，组件类上，@bean方法上】

​		、

​			@Lazy   懒加载：使用@Lazy注解标注后，单实例bean对象只是在第一次从Spring容器中获取时被创建，以后每次获取bean对象时，直接返回创建好的对象。只对单例有效。

​			@Scope：设置组件作用域 1.prototype:多例的【在获取时创建】2.singleton:单例的【在容器启动时创建】（默认值）

​            对象在Spring容器中默认是单实例的，Spring容器在启动时就会将实例对象加载到Spring容器中，之后，每次从Spring容器中获取实例对象，都是直接将对象返回，而不必再创建新的实例对象了。SpringMVC中的Controller默认是单例的，有些开发者在Controller中创建了一些变量，那么这些变量实际上就变成共享的了，Controller又可能会被很多线程同时访问，这些线程并发去修改Controller中的共享变量，此时很有可能会出现数据错乱的问题，所以使用的时候需要特别注意。
​           自定义Scope主要分为三个步骤，如下所示。

第一步，实现Scope接口。第二步，将自定义Scope注册到容器中。第三步，使用自定义的作用域。也就是在定义bean的时候，指定bean的scope属性为自定义的作用域名称。例如：线程级别的单例。

​           @value:给属性赋值,也可以使用SpEL和外部文件的值

​			@Import【快速给容器中导入一个组件】
​		从源码里面可以看出@Import可以配合Configuration、ImportSelector以及ImportBeanDefinitionRegistrar来使用，下面的or表示也可以把Import当成普通的bean来使用。

注意：@Import注解只允许放到类上面，不允许放到方法上。





​	FactoryBean:工厂Bean,交给spring用来生产Bean到spring容器中.可以通过前缀&来获取工厂Bean本身.

##### 注册bean的方式



1. 包扫描+给组件标注注解（@Controller、@Servcie、@Repository、@Component），但这种方式比较有局限性，局限于我们自己写的类
2. @Bean注解，通常用于导入第三方包中的组件
3. @Import注解，快速向Spring容器中导入一个组件
4. 

@Import注解的三种用法主要包括：

- 直接填写class数组的方式

- import一个组件，组件实现了ImportSelector接口，即批量导入，是Spring中导入外部配置的核心接口，【springboot自动配置和@EnableXxx功能性注解】，在ImportSelector接口的selectImports()方法中，存在一个AnnotationMetadata类型的参数，这个参数能够获取到当前标注@Import注解的类的所有注解信息，也就是说不仅能获取到@Import注解里面的信息，还能获取到其他注解的信息。

- import一个组件，组件实现了ImportBeanDefinitionRegistrar接口，即手工注册bean到容器中

     Spring官方在动态注册bean时，大部分套路其实是使用ImportBeanDefinitionRegistrar接口。

  所有实现了该接口的类都会被ConfigurationClassPostProcessor处理，ConfigurationClassPostProcessor实现了BeanDefinitionRegistryPostProcessor接口[能动态添加BeanDefinition到容器，添加了就会被实例化]



​        Spring 3.0之前，创建bean可以通过XML配置文件与扫描特定包下面的类来将类注入到Spring [IOC](https://so.csdn.net/so/search?q=IOC&spm=1001.2101.3001.7020)容器内。而在Spring 3.0之后提供了JavaConfig的方式，也就是将IOC容器里面bean的元信息以Java代码的方式进行描述

#####  Bean生命周期:

在属性赋值之后

###### **单实例bean初始化和销毁**

1. 通过@Bean 指定init-method和destroy-method

2. 实现InitializingBean定义初始化逻辑,实现DisposableBean定义销毁方法

3. ​				 JSR250：@PostConstruct	@PreDestroy【InitDestroyAnnotationBeanPostProcessor类主要用来处理@PostConstruct注解和@PreDestroy注解。】

4. 实现BeanPostProcessor接口的后置拦截器放入容器中，可以拦截bean初始化，并可以在被拦截的Bean的初始化前后进行一些处理工作。

   ​    Spring为bean提供了两种初始化的方式，第一种方式是实现InitializingBean接口（也就是要实现该接口中的afterPropertiesSet方法），第二种方式是在配置文件或@Bean注解中通过init-method来指定，这两种方式可以同时使用，同时使用先调用afterPropertiesSet方法，后执行init-method指定的方法。
   

   ​     多实例的bean在容器关闭的时候是不进行销毁的，也就是说你每次获取时，IOC容器帮你创建出对象交还给你，至于要什么时候销毁这是你自己的事，Spring容器压根就不会再管理这些多实例的bean了。

   

   ###### BeanPostProcessor

   postProcessBeforeInitialization方法会在bean实例化和属性赋值之后，自定义初始化方法之前被调用，而postProcessAfterInitialization方法会在自定义初始化方法之后被调用。

   

   

   ```
   代码流程
   populateBean(beanName, mbd, instanceWrapper); // 实例化后给bean进行属性赋值【来源于配置文件的值或者注解的值】
   initializeBean(beanName, exposedObject, mbd)
   {
   	applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);【@Autowired注解、通过XxxxAware接口注入spring底层组件】
   	invokeInitMethods(beanName, wrappedBean, mbd); // 执行自定义初始化
   	applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);【AOP】
   }
   
   doCreateBean
   -populateBean（）：给bean的各种属性赋值
   -initializeBean（）：初始化bean
   -处理Aware方法
   -applyBeanPostProcessorsBeforeInitialization：后置处理器的实例化前拦截
   -invokeInitMethods:执行@Bean指定的initMethod
   -applyBeanPostProcessorsAfterInitialization：后置处理器的实例化后拦截
   
   ```

   

spring底层常用的BeanPostProcessor：

```asciidoc
* BeanValidationPostProcessor用来实现数据校验

* ApplicationContextProcessor实现ApplicationContextAware接口的类的ApplicationContext自动注入。自定义组件注入spring底层组件
AutowiredAnnotationBeanPostProcessor用于@Autowired注解的实现，AnnotationAwareAspectJAutoProxyCreator用于Spring AOP的动态代理【判断当前对象是否注册了切面	】
```





- @Bean 结合@Configuration（full mode）使用或结合@Component（light mode）使用。可以导入第三方组件,方法有参数默认从IOC容器中获取，可以指定initMethod和destroyMethod 指定初始化和销毁方法,多实例对象不会调用销毁方法.

- **Spring对@Configuration注解标注的类会做特殊处理，多次调用给IOC容器中添加组件的方法，都只是从IOC容器中找组件而已。**

  

> #### 		组件赋值
>

​			@Value【外部文件，其他bean，spel】

​            @PropertySource
​			        @PropertySources

在springboot当中@ConfigationProperties替代了这些注解引用属性文件的值。@PropertySource:读取外部配置文件中的k/v保存到运行环境中,结合@value使用,或使用ConfigurableEnvironment获取

- @Autowried 装配优先级如下:【**能添加`required=false`属性**】

  1. 使用按照类型去容器中找对应的组件
  2. 按照属性名称去作为组件id去找对应的组件

    标注在方法上，参数自动从容器当中获取。@Bean方法的参数默认从容器当中获取。

- @Qualifier:指定默认的组件,结合@Autowried使用【优先级最高】
  --标注在构造器:spring创建对象调用构造器创建对象
  --标注在方法上:

- @Primary:spring自动装配的时候,默认首先bean,配合@Bean使用

​				2.其他方式
​					@Resources（JSR250）：按照名称
​					@Inject（JSR330，需要导入javax.inject）：按照名称
​			
​			@Profile：需要通过代码或者参数	Environment    -Dspring.profiles.active=test  显示指定环境才会生效。默认为default环境

​    Spring 3.0也有一些和@Conditional相似的注解，它们是**Spring SPEL表达式和Spring Profiles注解**，但是Spring 4.0之后的@Conditional注解要比@Profile注解更加高级。@Profile注解上有@Conditional注解



> #### 		组件注入
>



###### 环境（Environment）

```
public interface EnvironmentCapable {
	Environment getEnvironment();
}
```

它其实代表了当前Spring容器的运行环境，比如JDK环境，系统环境；每个环境都有自己的配置数据，如System.getProperties()可以拿到JDK环境数据、System.getenv()可以拿到系统变量，ServletContext.getInitParameter()可以拿到Servlet环境配置数据。Spring抽象了一个Environment来表示Spring应用程序环境配置，它整合了各种各样的外部环境，并且提供统一访问的方法。

`Environment`对象，除了能操作profile对象之外，通过之前的继承结构我们知道，他还能进行一些关于属性的操作。



​			方法参数
​			构造器注入
​			ApplicationContextAware
​				ApplicationContextAwareProcessor

> #### 		声明式事务
>

@EnableTransactionManagement *// 它是来开启基于注解的事务管理功能的* 。并且需要配置事务管理器来控制事务，这个注解导入一些类。

在xml中  <tx:annotation-driven/>

   导入数据库驱动、数据源、spring-jdbc、@Transactional



AutoProxyRegistrar向容器中注入了一个自动代理创建器，即InfrastructureAdvisorAutoProxyCreator.利用后置处理器机制在对象创建以后进行包装，然后返回一个代理对象，并且该代理对象里面会存有所有的增强器。最后，代理对象执行目标方法，在此过程中会利用拦截器的链式机制，依次进入每一个拦截器中进行执行。

ProxyTransactionManagementConfiguration会利用@Bean注解向容器中注册各种组件，而且注册的第一个组件就是BeanFactoryTransactionAttributeSourceAdvisor，这个Advisor可是事务的核心内容，可以暂时称之为事务增强器。

向容器中注册事务增强器时，除了需要事务注解信息[会使用到一个叫AnnotationTransactionAttributeSource的类，用它来解析事务注解。]，还需要一个事务的拦截器，看到那个transactionInterceptor方法。

执行代理逻辑时，**如果事先没有添加指定任何TransactionManager，那么最终会从容器中按照类型来获取一个PlatformTransactionManager。**

```
事务拦截器逻辑
final TransactionAttribute txAttr = this.getTransactionAttributeSource().getTransactionAttribute(method, targetClass);  //获取当前方法的事务信息
        final PlatformTransactionManager tm = this.determineTransactionManager(txAttr);//获取事务管理器

createTransactionIfNecessary  ---> tm.getTransaction((TransactionDefinition)txAttr)  [返回TransactionStatus]

commitTransactionAfterReturning-->txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());

```

用AutoProxyRegistrar向Spring容器里面注册一个后置处理器，这个后置处理器会负责给我们包装代理对象。然后，使用ProxyTransactionManagementConfiguration（配置类）再向Spring容器里面注册一个事务增强器，此时，需要用到事务拦截器。最后，代理对象执行目标方法，在这一过程中，便会执行到当前Spring容器里面的拦截器链，而且每次在执行目标方法时，如果出现了异常，那么便会利用事务管理器进行回滚事务，如果执行过程中一切正常，那么则会利用事务管理器提交事务




> ### 	扩展原理
>

#### 		BeanFactoryPostProcessor

BeanFactoryPostProcessor的调用时机是在BeanFactory标准初始化之后，这样一来，我们就可以来定制和修改BeanFactory里面的一些内容了，此时，所有的bean定义已经保存加载到BeanFactory中了，但是bean的实例还未创建。



​			Spring容器标准初始化之后执行（BeanPostProcessor之前），此时bean还未创建
​			Spring容器初始化两大步
​				1、加载保存和读取所有bean配置
​				2、按照之前的配置创建bean

#### 		BeanDefinitionRegistryPostProcessor

​			BeanFactoryPostProcessor子类，可自定义添加bean定义
​			     BeanDefinetionRegistry
​				BeanDefinetionBuilder

#### 		ApplicationListener

​	



第一步，写一个监听器来监听某个事件。把监听器加入到容器中，这样Spring才能知道有这样一个监听器。

第二步，当然了，监听的这个事件必须是ApplicationEvent及其子类。

第三步，只要容器中有相关事件发布，通过多播器获取到相应事件类型的监听器进行回调【applicationContext.publishEvent()】



**不管是容器发布的事件，还是咱们自己发布的事件，都会走以上这个事件发布流程，即先拿到事件多播器，然后再拿到所有的监听器，接着再挨个回调它的方法**。

**在整个事件派发的过程中，我们可以自定义事件多播器**。没有就新new 一个。

```
initApplicationEventMulticaster()  
registerListeners    // Check for listener beans and register them.

```

使用@EventListener注解，我们就可以让任意方法都能监听事件，而不是让它去实现ApplicationListener这个接口。

**Spring会使用EventListenerMethodProcessor这个处理器来解析方法上的@EventListener注解**。

EventListenerMethodProcessor实现了一个接口，叫SmartInitializingSingleton,preInstantiateSingletons**获取所有创建好的单实例bean，然后判断每一个bean对象是否是SmartInitializingSingleton这个接口类型的，如果是，那么便调用它里面的afterSingletonsInstantiated方法**

遍历容器中的bean找到有@EventListener注解的方法拿到Method对象和对应bean的beanName,将两个参数封装到ApplicationListener的实现类ApplicationListenerMethodAdapter里,并注册到容器中,事件发布时通过Method和beanName反射执行@EventListener注解的方法

> #### 		Spring容器创建过程
>
> Spring IOC容器在启动的时候，会先保存所有注册进来的bean的定义信息，将来，BeanFactory就会按照这些bean的定义信息来为我们创建对象。

```
public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
   this(); //创建容器，在this()中通过调用父类构造器初始化了BeanFactory，以及向容器将配置类注册进BeanDefinitionMap中中注册了7个后置处理器。
   register(annotatedClasses);  //// 将配置类注册进BeanDefinitionMap中
   refresh();  //刷新初始化容器
}
public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
// Prepare this context for refreshing.
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);
	         try {		

                // Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);
				
					// Invoke factory processors registered as beans in the context.
                invokeBeanFactoryPostProcessors() 
                
              // Register bean processors that intercept bean creation.
	             registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.[先判断容器中有没有对应实例，没有直接new一个默认实例，并添加到容器和赋值给this.messageSource]
				initMessageSource();

				// Initialize event multicaster for this context.
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				回调beanFactory生命周期处理器的onRefresh方法,发布容器刷新完成事件
				finishRefresh();
				}
   
```





添加上`depends-on="book,user"`这样一个属性之后，那么在创建名字为person的bean之前，得先把名字为book和user的bean给创建出来。也就是说，depends-on属性决定了bean的创建顺序。

创建Bean后置处理器【先分为不同级别分组（不同的接口），然后每组排序后创建并注册】， Finally, re-register all internal BeanPostProcessors.

preInstantiateSingletons---getBean-doGetBean-getSingleton--[获取不到时，那么就会调用singletonFactory的getObject()方法。并添加到singletonObjects这个map当中]-- createBean-docreateBean|resolveBeforeInstantiation(**会在任何bean创建之前，先尝试返回bean的实例（通过实例Bean后置处理器）。**)---createBeanInstance |  属性赋值|初始化|



1. finishBeanFactoryInitialization(beanFactory)：初始化所有剩下的单实例bean
2. beanFactory.preInstantiateSingletons()：初始化所有剩下的单实例bean
   - 获取容器中所有的bean，然后依次进行初始化和创建对象
   - 获取bean的定义注册信息
   - 根据bean的定义注册信息判断bean是否是抽象的、单实例的、懒加载的
     

- 单实例bean的创建流程
                      
  - 在创建bean实例之前，会执行InstantiationAwareBeanPostProcessor这种类型的后置处理器中的两个方法，即postProcessBeforeInstantiation方法和postProcessAfterInitialization方法【前一个方法返回代理对象为前提】
  - 创建bean实例
    
  -  遍历获取到的所有后置处理器，若是MergedBeanDefinitionPostProcessor这种类型，则调用其postProcessMergedBeanDefinition方法
     
  -  为bean实例的属性赋值
    -  遍历获取到的所有后置处理器，若是InstantiationAwareBeanPostProcessor这种类型，则调用其postProcessAfterInstantiation方法
       
    -  再来遍历获取到的所有后置处理器，若是InstantiationAwareBeanPostProcessor这种类型，则调用其postProcessPropertyValues方法
       
    - 正式开始为bean的属性赋值
  - 初始化bean
  - 执行xxxAware接口的方法
    - 执行后置处理器初始化之前的方法（即postProcessBeforeInitialization方法）
    - 执行初始化方法
    - 执行后置处理器初始化之后的方法（即postProcessAfterInitialization方法）
  - 注册bean的销毁方法

- 将创建出的单实例bean添加到缓存中
- 来遍历所有的bean，并来判断遍历出来的每一个bean是否实现了SmartInitializingSingleton接口的











BeanFactoryPostProcessor先完成组件BeanDefinetion的扫描

在前面创建IOC容器时，需要先传入配置类，而我们在解析配置类的时候，由于这个配置类里面有一个@EnableAspectJAutoProxy注解，对于该注解，我们之前也说过，它会为我们容器中注册一个AnnotationAwareAspectJAutoProxyCreator（后置处理器）的beanDefinetion，这还仅仅是这个@EnableAspectJAutoProxy注解做的事，除此之外，容器中还有一些默认的后置处理器的定义。【在refresh实例化bean单例之前beanDefinetion已经注册完成。】





## spring 声明式事务

**基本步骤**

1. 配置数据源：DataSource
2. 配置事务管理器来控制事务：PlatformTransactionManager
3. @EnableTransactionManagement开启基于注解的事务管理功能
4. 给方法上面标注@Transactional标识当前方法是一个事务方法

**声明式事务实现原理**

1. @EnableTransactionManagement利用TransactionManagementConfigurationSelector给spring容器中导入两个组件：AutoProxyRegistrar和ProxyTransactionManagementConfiguration
2. AutoProxyRegistrar给spring容器中注册一个InfrastructureAdvisorAutoProxyCreator，InfrastructureAdvisorAutoProxyCreator实现了InstantiationAwareBeanPostProcessor,InstantiationAwareBeanPostProcessor是一个BeanPostProcessor。它可以拦截spring的Bean初始化(Initialization)前后和实例化(Initialization)前后。利用后置处理器机制在被拦截的bean创建以后包装该bean并返回一个代理对象代理对象执行方法利用拦截器链进行调用（同**springAop**的原理）
3. ProxyTransactionManagementConfiguration：是一个spring的配置类,它为spring容器注册了一个BeanFactoryTransactionAttributeSourceAdvisor,是一个事务事务增强器。它有两个重要的字段：AnnotationTransactionAttributeSource和TransactionInterceptor。
   1. AnnotationTransactionAttributeSource：用于解析事务注解的相关信息
   2. TransactionInterceptor：事务拦截器，在事务方法执行时，都会调用TransactionInterceptor的invoke->invokeWithinTransaction方法，这里面通过配置的PlatformTransactionManager控制着事务的提交和回滚。

## Spring 扩展(钩子)

1. BeanFactoryPostProcessor：beanFactory后置处理器，的拦截时机：所有Bean的定义信息已经加载到容器，但还没有被实例化。可以对beanFactory进行一些操作。
2. BeanPostProcessor：bean后置处理器，拦截时机：bean创建对象初始化前后进行拦截工作。可以对每一个Bean进行一些操作。
3. BeanDefinitionRegistryPostProcessor：是BeanFactoryPostProcessor的子接口，拦截时机：所有Bean的定义信息已经加载到容器，但还没有被实例化，可以对每一个Bean的BeanDefinition进行一些操作。
4. ApplicationListener,自定义ApplicationListener实现类并加入到容器中,可以监听spring容器中发布的事件。spring在创建容器的时候（finishRefresh（）方法）会发布ContextRefreshedEvent事件，关闭的时候（doClose()）会发布ContextClosedEvent事件。也可以通过spring容器的publishEvent发布自己的事件。
   1. 事件发布流程：publishEvent方法
      1. 获取事件的多播器，getApplicationEventMulticaster()。
      2. 调用multicastEvent(applicationEvent, eventType)派发事件。获取到所有的ApplicationListener,即getApplicationListeners()，然后同步或者异步的方式执行监听器的onApplicationEvent。
   2. 事件的多播器的初始化中（initApplicationEventMulticaster（）），如果容器中没有配置applicationEventMulticaster，就使用SimpleApplicationEventMulticaster。然后获取所有的监听器，并把它们注册到SimpleApplicationEventMulticaster中。
5. @EventListener(class={})：在普通的业务逻辑的方法上监听事件特定的事件。原理：EventListenerMethodProcessor是一个SmartInitializingSingleton，当所有的单例bean都初始化完以后， 容器会回调该接口的方法afterSingletonsInstantiated(),该方法里会遍历容器中所有的bean，并判断每一个bean里是否带有@EventListener注解的Method，然后创建ApplicationListenerMethodAdapter存储并包装该Method，最后将ApplicationListenerMethodAdapter添加到spring容器中。

## Spring源代码分析

spring核心逻辑AbstractApplicationContext的refresh()方法如下

```java
public void refresh() {
    synchronized (this.startupShutdownMonitor) {
        // 刷新前的预准备工作
        prepareRefresh();
        // 提取bean的配置信息并封装成BeanDefinition实例，然后将其添加到注册中心。注册中心是一个ConcurrentHashMap<String,BeanDefinition>类型，key为Bean的名字，value为BeanDefinition实例。
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
       //对beanFactory进行一些配置，注册一些BeanPostProcessor和一些特殊的Bean。
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
            //最后一步
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
1. refreshBeanFactory：如果当前容器已经有了BeanFactory就销毁原来的BeanFactory。然后创建一个DefaultListableBeanFactory();
    * 对BeanFactory并进行配置，主要配置是否允许BeanDefinition覆盖，是否允许Bean间的循环引用。
    * 加载BeanDefinition，解析XML文件和配置文件，将其转换为BeanDefinition，然后保存到DefaultListableBeanFactory的beanDefinitionMap字段中。
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

执行BeanFactoryPostProcessor类型的监听方法。

```markdown
* BeanFactoryPostProcessor是beanFactory后置处理器，在整个BeanFactory标准初始化完成后进行拦截调用， 
* BeanDefinitionRegistryPostProcessor继承了BeanFactoryPostProcessor，在beanFactory解析完所有的BeanDefinition后拦截调用。
* BeanFactoryPostProcessor来源
    * 通过ApplicationContent的addBeanFactoryPostProcessor()方法手动添加自己的拦截器
    * 系统默认了一些BeanFactoryPostProcessor。例如：ConfigurationClassPostProcessor用来处理@Configuration标注的Spring配置类。
* 调用顺序 
    1. 先调用BeanDefinitionRegistryPostProcessor类型的拦截器，
    2. 然后再依次调用实现了PriorityOrdered,Ordered接口的BeanFactoryPostProcessor
    3. 最后调用普通的BeanFactoryPostProcessor
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

### finishRefresh()：

最后一步。

```markdown
1. 初始化和生命周期有关的后置处理器；LifecycleProcessor，如果容器中没有指定处理就创建一个DefaultLifecycleProcessor加入到容器。
2. 获取容器中所有的LifecycleProcessor回调onRefresh()方法。
3. 发布容器刷新完成事件ContextRefreshedEvent。
```

### beanFactory.getBean方法执行的过程

1. 首先将方法传入的beanName进行转换：先去除FactoryBean前缀（&符）如果传递的beanName是别名，则通过别名找到bean的原始名称。
2. 根据名称先从singletonObjects（一个Map类型的容）获取bean实例。如果能获取到就先判断该bean实例是否实现了FactoryBean，如果是FactoryBean类型的bean实例，就通过FactoryBean获取Bean。然后直接返回该bean实例。getBean方法结束。
3. 如果从singletonObjects没有获取到bean实例就开始创建Bean的过程。
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



### @Autowire 实现原理

上面介绍**beanFactory.getBean方法执行的过程**中提到：populateBean为bean实例赋值。在赋值之前执行InstantiationAwareBeanPostProcessor的postProcessAfterInstantiation和postProcessPropertyValues方法。@Autowire由AutowiredAnnotationBeanPostProcessor完成，它实现了InstantiationAwareBeanPostProcessor。
AutowiredAnnotationBeanPostProcessor执行过程：

1. postProcessAfterInstantiation方法执行，直接return null。
2. postProcessPropertyValues方法执行，主要逻辑在此处理。待补充。。。。。

​       

##### 参考

1.[笔记](https://liayun.blog.csdn.net/category_10613855_2.html)