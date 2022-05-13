#### 		AOP

​	**使用步骤**

1. @EnableAspectJAutoProxy 开启基于注解的aop模式【 < aop:aspectj-autoproxy/>  xml版本 】（会向IOC容器中注册AnnotationAwareAspectJAutoProxyCreator）
2. @Aspect：定义切面类，切面类里定义通知
3. @PointCut 切入点，可以写切入点表达式，指定在哪个方法切入
4. 通知方法
   - @Before(前置通知)
   - @After(后置通知)
   - @AfterReturning(返回通知)
   - @AfterTrowing(异常通知)@Around(环绕通知)
5. JoinPoint：连接点,是一个类，配合通知使用，用于获取切入的点的信息



切面实现了横切关注点的模块化。AOP是指在程序的运行期间动态地将某段代码切入到指定方法、指定位置进行运行的编程方式。AOP的底层是使用动态代理实现的。

1.将切面类和业务逻辑组件（目标方法所在类）都加入到容器中，并且要告诉Spring哪个类是切面类（标注了@Aspect注解的那个类）。
       2.在切面类上的每个通知方法上标注通知注解，告诉Spring何时何地运行，当然最主要的是要写好切入点表达式，这个切入点表达式可以参照官方文档来写。
       3.开启基于注解的AOP模式，即加上@EnableAspectJAutoProxy注解，这是最关键的一点。





创建AnnotationAwareAspectJAutoProxyCreator组件本身，作为后置处理器拦截其他组件bean[finishBeanFactoryInitialization来创建剩下的单实例bean]。

**SpringAop原理**

1. @EnableAspectJAutoProxy：利用@EnableAspectJAutoProxy注解来开启AOP功能
   - @EnableAspectJAutoProxy 通过@Import(AspectJAutoProxyRegistrar.class  实现了ImportBeanDefinitionRegistrar接口。)给spring容器中导入了一个AnnotationAwareAspectJAutoProxyCreator。【beanDefinition】
   - AnnotationAwareAspectJAutoProxyCreator【**翻译过来就叫注解装配模式的AspectJ切面自动代理创建器。**】实现了InstantiationAwareBeanPostProcessor。它可以拦截spring的Bean初始化(Initialization)前后和实例化(Initialization)前后。
2. AnnotationAwareAspectJAutoProxyCreator的postProcessBeforeInstantiation(bean实例化前)：会通过调用isInfrastructureClass(beanClass)来判断 被拦截的类是否是基础类型的Advice、PointCut、Advisor、AopInfrastructureBean，或者是否是切面（@Aspect），若是则放入adviseBean集合。这里主要是用来处理我们的切面类。【标记组件类型】
3. AnnotationAwareAspectJAutoProxyCreator的postProcessAfterInitialization（bean初始化后）：
   1. 首先找到被拦截的Bean的匹配的增强器（通知方法），这里有切入点表达式匹配的逻辑
   2. 将增强器保存到proxyFactory中，
   3. 根据被拦截的Bean是否实现了接口，spring自动决定使用JdkDynamicAopProxy还是ObjenesisCglibAopProxy
   4. 最后返回被拦截的Bean的代理对象，注册到spring容器中
   5. 主要就是在组件创建完成之后，判断组件是否需要增强。如需要，则会把切面里面的通知方法包装成增强器，然后再为业务逻辑组件创建一个代理对象。在为业务逻辑组件创建代理对象的时候，使用的是cglib来创建动态代理的。当然了，如果业务逻辑类有实现接口，那么就使用jdk来创建动态代理。一旦这个代理对象创建出来了，那么它里面就会有所有的增强器。
4. 代理Bean的目标方法执行过程：CglibAopProxy.intercept();
   1. 保存所有的增强器，并处理转换为一个拦截器链，得到目标方法的拦截器链，所谓的拦截器链其实就是每一个通知方法又被包装为了方法拦截器，即MethodInterceptor
   2. 如果没有拦截器链，就直接执行目标方法
   3. 如果有拦截器链，就将目标方法，拦截器链等信息传入并创建CglibMethodInvocation对象，并调用proceed()方法获取返回值。proceed方法内部会依次执行拦截器链。
   4. 最终，整个的执行效果就会有两套：
      1. ​    目标方法正常执行：前置通知→目标方法→后置通知→返回通知
      2. ​    目标方法出现异常：前置通知→目标方法→后置通知→异常通知



​                 ![](https://gitee.com/workerbo/gallery/raw/master/2020/aHR0cHM6Ly9naXRlZS5jb20vd3hfY2MzNDdiZTY5Ni9ibG9nSW1hZ2UvcmF3L21hc3Rlci9pbWFnZS0yMDIwMDcwNTE1MjcwNDkxNy5wbmc)   









##### spring  AOP

spring aop 

spring整合aspectJ

使用四种方式：1、基于接口的增强。2.基于shema的增加  3.基于shema和遗留代码【已有增强】  4.基于注解的增强

在创建spring之前，spring会根据我们的配置（可能是xml、可能是注解）生成一个个Advisor

每一个AOP的代理对象在执行方法调用内部的intercept方法，在其中会创建CglibMethodInvocation对象，并且封装了增强器构成的拦截器链【此时只需要通知即可】。然后执行proceed方法。拦截器链递归调用。

![image-20210831141220110](../../../../../../../Programfile/Typora/upload/image-20210831141220110.png)

​                                                                                图：Advice继承体系



[（使用API接口、使用自定义类、使用注解）](https://blog.csdn.net/qq_43439968/article/details/108192187)

```

 
 proxyTargetClass属性第一个参数表示是使用JDK的动态代理还是Cglib的代理
属性exposeProxy为true，这是我们的代理对象接口会被暴漏在ThreadLocal中
  //使用AopContext的静态方法获取当前的代理对象
        ((MessageService)AopContext.currentProxy()).formartMsg(info);
 
 AopConfigUtils
       APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
```

spring通过TargetSourceCreator提前生成代理



要分析了`applyMergedBeanDefinitionPostProcessors`这段代码的作用，它的执行时机是在创建对象之后，属性注入之前。AutowiredAnnotationBeanPostProcessor找到所有的注入点，其实就是被@Autowired注解修饰的方法以及字段

populateBean

1. 处理自动注入
2. 处理属性注入（主要指处理@Autowired注解），最重要
3. 处理依赖检查





接着来看看`initializeBean`这个方法，它主要干了这么几件事

1. 执行`Aware`接口中的方法
2. 执行`生命周期回调方法`
3. 完成`AOP`代理



> #### AOP

AOL：在不同语言中实现AOP的语言。

织入：将AOP组件【Aspcet】集成到OOP组件中。过程是多样且透明。

AOP的概念

第一代AOP：静态

第二代AOP：动态

##### 在java中AOP的实现方式



###### AOP成员概念

Advice：在joinpoint的执行时间不同划分

Before Advice

After Advice[ returning throwing finally]

Around Advice 

introduction  Advice【新增特性和行为】

finally语句是在try的return语句执行之后，return返回之前执行。

Aspect：在一开始丢spring AOP中没有对应的实体。后来集成了AspectJ。通过在POJO加上注解@AspectJ。

织入器

![image-20201210162911378](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201210162911378.png)

###### spring AOP实现

一代：

Pointcut  

![image-20201211091127385](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201211091127385.png)





![image-20201211094742128](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201211094742128.png)





![image-20201211101227423](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201211101227423.png)





spring的AfterReturningAdvice不能对返回值做更改。



![image-20201211102046546](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201211102046546.png)



spring  aop 的织入器是ProxyFactory



②在使用AOP的时候，你是用xml还是注解的方式（@Aspect）？
1）如果使用xml方式，不需要任何额外的jar包。
2）如果使用@Aspect方式，你就可以在类上直接一个@Aspect就搞定，不用费事在xml里配了。但是这需要额外的jar包（ aspectjweaver.jar）。因为spring直接使用AspectJ的注解功能，注意只是使用了它 的注解功能而已。并不是核心功能 ！！！




