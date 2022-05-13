

### springboot中使用Mybaits



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









##### @Mapper注解【Mybaits原生】

作用：在接口类上添加了@Mapper，在编译之后会生成相应的接口实现类【暂时应该是拦截器插件去处理？】



@Mapper，最终 Mybatis 会有一个拦截器，会自动的把 @Mapper 注解的接口生成动态代理类。这点可以在 MapperRegistry 类中的源代码中查看。





###### 只配置Mapper.xml路径

如果Mapper.xml与Mapper.class不在同一个包下或者不同名，就必须使用配置mapperLocations指定mapper.xml的位置。

此时spring是通过识别mapper.xml中的 <mapper namespace="com.fan.mapper.UserDao"> namespace的值来确定对应的Mapper.class的。

如果Mapper.xml与Mapper.class在同一个包下且同名，spring扫描Mapper.class的同时会自动扫描同名的Mapper.xml并装配到Mapper.class。就不用配置mapperLocations

##### 通过MapperScannerConfigurer将Mapper接口生成代理注入到Spring【XML】

是BeanDefinitionRegistryPostProcessor的实现类。

spring将数据访问层的异常做了转化成DataAccessException





```
//所以如果我们业务代码使用了@Transaction注解，在Spring中就已经通过dataSource创建了一个Connection并放入ThreadLocal中
        //那么当Mapper代理对象调用方法时，通过SqlSession的SpringManagedTransaction获取连接时，就直接获取到了当前线程中Spring事务创建的Connection并返回
        
        
```