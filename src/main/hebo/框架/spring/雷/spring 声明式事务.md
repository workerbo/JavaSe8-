> #### 		声明式事务

@EnableTransactionManagement *// 它是来开启基于注解的事务管理功能的* 。并且需要配置事务管理器来控制事务，这个注解导入一些类。类似在xml中  <tx:annotation-driven/>

向容器中注册事务增强器时，除了需要事务注解信息[AnnotationTransactionAttributeSource的类，用它来解析事务注解。]，还需要一个事务的拦截器，看到那个transactionInterceptor方法。

执行代理逻辑时，**如果事先没有添加指定任何TransactionManager，那么最终会从容器中按照类型来获取一个PlatformTransactionManager。**

```
事务拦截器逻辑
final TransactionAttribute txAttr = this.getTransactionAttributeSource().getTransactionAttribute(method, targetClass);  //获取当前方法的事务信息
        final PlatformTransactionManager tm = this.determineTransactionManager(txAttr);//获取事务管理器

createTransactionIfNecessary  ---> tm.getTransaction((TransactionDefinition)txAttr)  [返回TransactionStatus]

commitTransactionAfterReturning-->txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());

```

用AutoProxyRegistrar向Spring容器里面注册一个后置处理器，这个后置处理器会负责给我们包装代理对象。然后，使用ProxyTransactionManagementConfiguration（配置类）再向Spring容器里面注册一个事务增强器，此时，需要用到事务拦截器。最后，代理对象执行目标方法，在这一过程中，便会执行到当前Spring容器里面的拦截器链，而且每次在执行目标方法时，如果出现了异常，那么便会利用事务管理器进行回滚事务，如果执行过程中一切正常，那么则会利用事务管理器提交事务

## spring 声明式事务

**基本步骤**

1. 配置数据源：DataSource
2. 配置事务管理器来控制事务：PlatformTransactionManager
3. @EnableTransactionManagement开启基于注解的事务管理功能
4. 给方法上面标注@Transactional标识当前方法是一个事务方法

**声明式事务实现原理**

1. @EnableTransactionManagement利用TransactionManagementConfigurationSelector给spring容器中导入两个组件：AutoProxyRegistrar和ProxyTransactionManagementConfiguration
2. AutoProxyRegistrar给spring容器中注册一个InfrastructureAdvisorAutoProxyCreator，利用后置处理器机制在被拦截的bean创建以后包装该bean并返回一个代理对象（同**springAop**的原理）并且该代理对象里面会存有所有的增强器。最后，代理对象执行目标方法，在此过程中会利用拦截器的链式机制，依次进入每一个拦截器中进行执行。
3. ProxyTransactionManagementConfiguration：是一个spring的配置类,它为spring容器注册了一个BeanFactoryTransactionAttributeSourceAdvisor,是一个事务事务增强器。它有两个重要的字段：AnnotationTransactionAttributeSource和TransactionInterceptor。
   1. AnnotationTransactionAttributeSource：用于解析事务注解的相关信息
   2. TransactionInterceptor：事务拦截器，在事务方法执行时，都会调用TransactionInterceptor的invoke->invokeWithinTransaction方法，这里面通过配置的PlatformTransactionManager控制着事务的提交和回滚。





 1、PROPAGATION_REQUIRED：默认事务类型，如果没有，就新建一个事务；如果有，就加入当前事务。适合绝大多数情况。

  2、PROPAGATION_REQUIRES_NEW：如果没有，就新建一个事务；如果有，就将当前事务挂起。

  3、PROPAGATION_NESTED：如果没有，就新建一个事务；如果有，就在当前事务中嵌套其他事务。

  4、PROPAGATION_SUPPORTS：如果没有，就以非事务方式执行；如果有，就使用当前事务。

  5、PROPAGATION_NOT_SUPPORTED：如果没有，就以非事务方式执行；如果有，就将当前事务挂起。即无论如何不支持事务。

  6、PROPAGATION_NEVER：如果没有，就以非事务方式执行；如果有，就抛出异常。

  7、PROPAGATION_MANDATORY：如果没有，就抛出异常；如果有，就使用当前事务。

REQUIRES_NEW,当被调用时,就相当于暂停(挂起)当前事务,先开一个新的事务去执行REQUIRES_NEW的方法,如果REQUIRES_NEW中的异常得到了处理，那么他将不影响调用者的事务,同时,调用者之后出现了异常,同样也不会影响之前调用的REQUIRES_NEW方法的事务.

当调用的方法是NESTED事务,该方法抛出异常如果得到了处理(try-catch),那么该方法发生异常不会触发整个方法的回滚，而调用者出现unchecked异常,却能触发所调用的nested事务的回滚



##### spring事务

A如果没有受事务管理：  则线程内的connection 的 autoCommit为true。
B得到事务时事务传播特性依然生效，得到的还是A使用的connection，但是 不会改变autoCommit的属性。所以B当中是按照每条sql进行提交的。

检查性异常不指定就不会回滚，事务嵌套方法必须用代理对象。

线程绑定资源，通过资源获取工具类访问事务同步器获取线程本地化资源。持久化模板的执行方法严格调用工具获取和释放资源。【不要自己从数据源获取资源】。无事务方法中，工具类获取的是新资源，需要手动释放。事务中启动新线程会启动新事务。

spring事务通过TransactionManager接口提供SPI，提供实现类对不同持久化框架的事务实现做了代理。	

###### 事务是XML到注解

　使用@Transactional对类或方法进行事务增强的标注。

　在配置文件中加入<tx:annotation-driven transaction-manager="txManager"/>,对标注@Transactional的Bean进行加工处理，以织入事务管理切面。







###### 事务原理

将上面的TransactionInterceptor的BeanName传入到Advisor中，然后将AnnotationTransactionAttributeSource这个Bean注入到Advisor中。

首先注册pointcut、advice、advisor，然后将pointcut和advice注入进advisor中，在之后动态代理的时候会使用这个Advisor去寻找每个Bean是否需要动态代理（取决于是否有开启事务），因为Advisor有pointcut信息。

InfrastructureAdvisorAutoProxyCreator

```
所有bean实例化时Spring都会保证调用其postProcessAfterInstantiation方法
@Override
public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
    if (bean != null) {
        // 根据给定的bean的class和name构建出key，格式：beanClassName_beanName
        Object cacheKey = getCacheKey(bean.getClass(), beanName);
        if (!this.earlyProxyReferences.contains(cacheKey)) {
            // 如果它适合被代理，则需要封装指定bean【在postProcessBeforeInstantiation中会判定	】
            return wrapIfNecessary(bean, beanName, cacheKey);
        }
    }
    return bean;
}
```

在wrapIfNecessary函数中主要的工作如下：

（1）找出指定bean对应的增强器。

```
// 获取BeanFactory中所有对应Advisor.class的类名
        // 这里和AspectJ的方式有点不同，AspectJ是获取所有的Object.class，然后通过反射过滤有注解AspectJ的类
```

（2）根据找出的增强器创建代理。

将BeanFactoryTransactionAttributeSourceAdvisor中的getPointcut()方法返回值作为参数继续调用canApply方法，而 getPoint()方法返回的是TransactionAttributeSourcePointcut类型的实例。

首先获取对应类的所有接口并连同类本身一起遍历，遍历过程中又对类中的方法再次遍历，一旦匹配成功便认为这个类适用于当前增强器。

做匹配的时候 methodMatcher.matches(method, targetClass)会使用 TransactionAttributeSourcePointcut 类的 matches 方法。



```
@Override
public boolean matches(Method method, Class<?> targetClass) {
    if (TransactionalProxy.class.isAssignableFrom(targetClass)) {
        return false;
    }
    // 自定义标签解析时注入
    TransactionAttributeSource tas = getTransactionAttributeSource();
    return (tas == null || tas.getTransactionAttribute(method, targetClass) != null);
}
```

对于事务属性的获取规则相信大家都已经很清楚，如果方法中存在事务属性，则使用方法上的属性，否则使用方法所在的类上的属性，如果方法所在类的属性上还是没有搜寻到对应的事务属性，那么在搜寻接口中的方法，再没有的话，最后尝试搜寻接口的类上面的声明。







```
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
    // 如果处理过这个bean的话直接返回
    if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
        return bean;
    }
    // 之后如果Bean匹配不成功，会将Bean的cacheKey放入advisedBeans中
    // value为false，所以这里可以用cacheKey判断此bean是否之前已经代理不成功了
    if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
        return bean;
    }
    // 这里会将Advise、Pointcut、Advisor类型的类过滤，直接不进行代理，return
    if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
           // 这里即为不成功的情况，将false放入Map中
        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }

    // Create proxy if we have advice.
    // 这里是主要验证的地方，传入Bean的class与beanName去判断此Bean有哪些Advisor
    Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
    // 如果有相应的advisor被找到，则用advisor与此bean做一个动态代理，将这两个的信息
    // 放入代理类中进行代理
    if (specificInterceptors != DO_NOT_PROXY) {
        this.advisedBeans.put(cacheKey, Boolean.TRUE);
        // 创建代理的地方
        Object proxy = createProxy(
                bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
        this.proxyTypes.put(cacheKey, proxy.getClass());
        // 返回代理对象
        return proxy;
    }
    // 如果此Bean没有一个Advisor匹配，将返回null也就是DO_NOT_PROXY
    // 也就是会走到这一步，将其cacheKey，false存入Map中
    this.advisedBeans.put(cacheKey, Boolean.FALSE);
    // 不代理直接返回原bean
    return bean;
}
```

