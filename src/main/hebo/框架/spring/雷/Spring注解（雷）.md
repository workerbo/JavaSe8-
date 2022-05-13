Spring注解驱动开发

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





​	FactoryBean:工厂Bean,交给spring用来生产Bean到spring容器中.可以通过前缀&来获取工厂Bean本身。

在通过@Autowired注解注入时注入的是工厂类对象，要注入实际对象直接用实际类型，而不是工厂类型。

##### 注册bean的方式



1. 包扫描+给组件标注注解（@Controller、@Servcie、@Repository、@Component），但这种方式比较有局限性，局限于我们自己写的类
2. @Bean注解，通常用于导入第三方包中的组件
3. @Import注解，快速向Spring容器中导入一个组件

   

@Import注解的三种用法主要包括：

- 直接填写class数组的方式

- import一个组件，组件实现了ImportSelector接口，即批量导入，是Spring中导入外部配置的核心接口，【springboot自动配置和@EnableXxx功能性注解】，在ImportSelector接口的selectImports()方法中，存在一个AnnotationMetadata类型的参数，这个参数能够获取到当前标注@Import注解的类的所有注解信息，也就是说不仅能获取到@Import注解里面的信息，还能获取到其他注解的信息。

     ​    需要逻辑代码校验来决定，那么就可以使用这个接口来进行逻辑代码校验并决定是否注入等。

- import一个组件，组件实现了ImportBeanDefinitionRegistrar接口，即手工注册bean到容器中

     Spring官方在动态注册bean时，大部分套路其实是使用ImportBeanDefinitionRegistrar接口。

  所有实现了该接口的类都会被ConfigurationClassPostProcessor处理，ConfigurationClassPostProcessor实现了BeanDefinitionRegistryPostProcessor接口[能动态添加BeanDefinition到容器，添加了就会被实例化]

​      场景：动态生成BeanDefinition，例如MyBaits的mapper接口

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

#### 	

#### 	





##### 参考

1.[笔记](https://liayun.blog.csdn.net/category_10613855_2.html)