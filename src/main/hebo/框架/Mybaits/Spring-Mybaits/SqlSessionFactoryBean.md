#### **[SqlSessionFactoryBean](https://www.cnblogs.com/java-chen-hao/p/11833780.html)**



###### 大体思路

- mybatis-spring将mybatis的相关组件作为spring的IOC容器的bean来管理，使用了spring的FactoryBean接口来对mybatis的相关组件进行包装。spring的IOC容器在启动加载时，如果发现某个bean实现了FactoryBean接口，则会调用该bean的getObject方法，获取实际的bean对象注册到IOC容器，其中FactoryBean接口提供了getObject方法的声明，从而统一spring的IOC容器的行为。

- SqlSessionFactory作为mybatis的启动组件，在mybatis-spring中提供了SqlSessionFactoryBean来进行包装，例如首先在XML配置文件applicationContext.xml中，配置SqlSessionFactoryBean来引入SqlSessionFactory，从而可以直接在应用代码中注入使用或者作为属性，注入到mybatis的其他组件对应的bean对象。

  

###### 	注解配置【Mybaits配置文件基本可以不要了】

```
// 解析mybatisConfig.xml文件和mapper.xml，设置数据源和所使用的事务管理机制，将这些封装到Configuration对象
// 使用Configuration对象作为构造参数，创建SqlSessionFactory对象，其中SqlSessionFactory为单例bean，最后将SqlSessionFactory单例对象注册到spring容器。
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

  // mybatis配置mybatisConfig.xml的资源文件
  private Resource configLocation;

  //解析完mybatisConfig.xml后生成Configuration对象
  private Configuration configuration;

  // mapper.xml的资源文件
  private Resource[] mapperLocations;

  // 数据源
  private DataSource dataSource;

  // 事务管理，mybatis接入spring的一个重要原因也是可以直接使用spring提供的事务管理
  private TransactionFactory transactionFactory;

  private Properties configurationProperties;

  // mybatis的SqlSessionFactoryBuidler和SqlSessionFactory
  private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

  private SqlSessionFactory sqlSessionFactory;
  
  
  // 实现FactoryBean的getObject方法
  @Override
  public SqlSessionFactory getObject() throws Exception {
  
    //...

  }
  
  // 实现InitializingBean的
  @Override
  public void afterPropertiesSet() throws Exception {
  
    //...
    
  }
  // 为单例
  public boolean isSingleton() {
    return true;
  }
}
```





###### 设计与实现：

​      SqlSessionFactoryBean 的接口设计如下：实现了spring提供的**FactoryBean，InitializingBean和ApplicationListener这三个接口**

SqlSessionFactoryBean的afterPropertiesSet方法实现如下：调用buildSqlSessionFactory方法创建用于注册到spring的IOC容器的sqlSessionFactory对象。我们接着来看看**buildSqlSessionFactory**。调用mybatis的sqlSessionFactoryBuilder来创建SqlSessionFactory对象。这一点相当于前面介绍的原生的mybatis的初始化过程。

SqlSessionFactoryBean的getObject方法实现如下：由于spring在创建SqlSessionFactoryBean自身的bean对象时，已经调用了InitializingBean的afterPropertiesSet方法创建了sqlSessionFactory对象，故可以直接返回sqlSessionFactory对象给spring的IOC容器。









SqlSessionFactoryBean，通过这个FactoryBean创建SqlSessionFactory并注册进Spring容器

SqlSessionFactoryBean的afterPropertiesSet方法实现如下：调用buildSqlSessionFactory方法创建用于注册到spring的IOC容器的sqlSessionFactory对象。

解析mybatisConfig.xml文件和mapper.xml，设置数据源和所使用的事务管理机制，将这些封装到Configuration对象

为sqlSessionFactory绑定事务管理器和数据源， 这样sqlSessionFactory在创建sqlSession的时候可以通过该事务管理器获取jdbc连接，从而执行SQL。事务默认采用SpringManagedTransaction

使用Configuration对象作为构造参数，创建SqlSessionFactory对象，其中SqlSessionFactory为单例bean，最后将SqlSessionFactory单例对象注册到spring容器。

1.解析mybatis-Config.xml文件，并将相关配置信息保存到configuration

2.事务默认采用SpringManagedTransaction，这一块非常重要

3.解析mapper.xml文件，并注册到configuration对象的mapperRegistry

4.将Configuration对象实例作为参数， 调用sqlSessionFactoryBuilder创建sqlSessionFactory对象实例



###### 一句话总结

spring利用SqlSessionFactoryBean创建了SqlSessionFactory。