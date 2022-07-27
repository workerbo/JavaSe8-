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







