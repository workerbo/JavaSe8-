

### spring使用Mybaits

需要一个中间框架mybatis-spring。这个框架一方面负责加载和解析 MyBatis 相关配置。另一方面，该框架还会通过 Spring 提供的拓展点，把各种 Dao 接口及其对应的对象放入 bean 工厂中。

   1.扫描和解析Mybaits的配置。

2. 扫描和注册 Dao 接口

> #### XML方式
>

```
<!-- application-mybatis.xml -->
<beans>
    <context:property-placeholder location="jdbc.properties"/>

    <!-- 配置数据源 -->
    <bean id="dataSource" class="org.apache.ibatis.datasource.pooled.PooledDataSource">
        <property name="driver" value="${jdbc.driver}" />
        <property name="url" value="${jdbc.url}" />
        <property name="username" value="${jdbc.username}" />
        <property name="password" value="${jdbc.password}" />
    </bean>

    <!-- 配置 SqlSessionFactory -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!-- 配置 mybatis-config.xml 路径 -->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
        <!-- 给 SqlSessionFactory 配置数据源，这里引用上面的数据源配置 -->
        <property name="dataSource" ref="dataSource"/>
        <!-- 配置 SQL 映射文件 -->
        <property name="mapperLocations" value="mapper/*.xml"/>
    </bean>

    <!-- 配置 MapperScannerConfigurer -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!-- 配置 Dao 接口所在的包 -->
        <property name="basePackage" value="xyz.coolblog.dao"/>
    </bean>
</beans>


```



其他的类型别名，全局类型转化器，setting配置可以放在 mybatis-config.xml 中

```
<!-- mybatis-config.xml -->
<configuration>
    <settings>
        <setting name="cacheEnabled" value="true"/>
    </settings>
    
    <typeAliases>
        <typeAlias alias="Article" type="xyz.coolblog.model.ArticleDO"/>
        <typeAlias alias="Author" type="xyz.coolblog.model.AuthorDO"/>
    </typeAliases>

    <typeHandlers>
        <typeHandler handler="xyz.coolblog.mybatis.ArticleTypeHandler" javaType="xyz.coolblog.constant.ArticleTypeEnum"/>
    </typeHandlers>
</configuration>


```

以sqlSessionFactory为核心，configLocation指定配置文件的路径。mapperLocations指定映射文件的扫描路径。

通过接口名称+接口方法与映射命名空间+映射ID可以一一匹配。mapperScannerConfigurer的basePackage指定路径下的所有接口类转化为spring管理的bean。

> #### 注解的方式
>

- sqlSessionFactory主要是在@Configuration注解的配置类中使用@Bean注解的名为sqlSessionFactory的方法来配置；
- Mapper接口主要是通过在@Configuration注解的配置类中结合@MapperScan注解来指定需要扫描获取mapper接口的包。

##### @MapperScan【spring-Mybaits】

作用：指定要变成实现类的接口所在的包，然后包下面的所有接口在编译之后都会生成相应的实现类

用 `@MapperScan` 后，接口类就不需要使用 `@Mapper` 注解

在加载MybatisConfig配置类的bean定义时候，通过@MapperScan注解找到了ImportBeanDefinitionRegistrar 的实现类MapperScannerRegistrar，便会回调这个MapperScannerRegistrar的registerBeanDefinitions方法。 

MapperScannerRegistrar完成事情

  1. 解析MapperScan注解的各个字段的值 ，用以初始化类路径扫描器
  2. 确定扫描类路径下哪些接口，如指定的包路径、指定的类所在包路径。上面倒数第2行代码，注册过滤器，用来指定包含哪些注解或接口的扫描（@MapperScan的annotationClass的markerInterface属性，如果设置的话）

Mybatis框架中，**MapperProxyFactory**，是mapper代理工厂，可基于接口和**MapperProxy**来构建一个mapper代理对象，实现了将接口转变成一个对象。**MapperProxy** 实现了 **InvocationHandler** 接口的invoke方法【在sqlSessionFactory生成过程中注册进了knownMappers】

MapperFactoryBean

MapperFactoryBean  extends SqlSessionDaoSupport implements FactoryBean，那么getBean获取的对象是从其getObject()中获取，并且MapperFactoryBean是一个单例，那么其中的属性sqlSessionTemplate【封装了SqlsessionFactory】**对象也是一个单例，全局唯一**，供所有的Mapper代理类使用。通过sqlsession调用knownMappers得到**MapperProxyFactory**返回映射器代理对象。

SqlSessionTemplate实现了SqlSession接口，那么Mapper代理类中执行所有的数据库操作，都是通过SqlSessionTemplate来执行，然后内部由对象sqlSessionProxy执行查询【静态代理】，sqlSessionProxy又是一个动态代理对象。

TransactionSynchronizationManager这个类，其内部维护了一个**ThreadLocal的**Map

这里同一线程创建了SqlSession后放入ThreadLocal中，同一线程中其他Mapper接口调用方法时，将会直接从ThreadLocal中获取。



##### @Mapper注解【Mybaits原生】

作用：在接口类上添加了@Mapper，在编译之后会生成相应的接口实现类【暂时应该是拦截器插件去处理？】



@Mapper，最终 Mybatis 会有一个拦截器，会自动的把 @Mapper 注解的接口生成动态代理类。这点可以在 MapperRegistry 类中的源代码中查看。

#### **[SqlSessionFactoryBean](https://www.cnblogs.com/java-chen-hao/p/11833780.html)**

SqlSessionFactoryBean，通过这个FactoryBean创建SqlSessionFactory并注册进Spring容器

SqlSessionFactoryBean的afterPropertiesSet方法实现如下：调用buildSqlSessionFactory方法创建用于注册到spring的IOC容器的sqlSessionFactory对象。

解析mybatisConfig.xml文件和mapper.xml，设置数据源和所使用的事务管理机制，将这些封装到Configuration对象

为sqlSessionFactory绑定事务管理器和数据源， 这样sqlSessionFactory在创建sqlSession的时候可以通过该事务管理器获取jdbc连接，从而执行SQL。事务默认采用SpringManagedTransaction

​        使用Configuration对象作为构造参数，创建SqlSessionFactory对象，其中SqlSessionFactory为单例bean，最后将SqlSessionFactory单例对象注册到spring容器。

1.解析mybatis-Config.xml文件，并将相关配置信息保存到configuration

2.事务默认采用SpringManagedTransaction，这一块非常重要

3.解析mapper.xml文件，并注册到configuration对象的mapperRegistry

4.将Configuration对象实例作为参数， 调用sqlSessionFactoryBuilder创建sqlSessionFactory对象实例







##### 通过MapperScannerConfigurer将Mapper接口生成代理注入到Spring【XML】

是BeanDefinitionRegistryPostProcessor的实现类。





spring将数据访问层的异常做了转化成DataAccessException





```
//所以如果我们业务代码使用了@Transaction注解，在Spring中就已经通过dataSource创建了一个Connection并放入ThreadLocal中
        //那么当Mapper代理对象调用方法时，通过SqlSession的SpringManagedTransaction获取连接时，就直接获取到了当前线程中Spring事务创建的Connection并返回
```