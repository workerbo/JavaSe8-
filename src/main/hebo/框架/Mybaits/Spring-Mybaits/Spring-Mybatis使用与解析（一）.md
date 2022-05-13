本篇文章便来介绍下Mybatis如何与Spring结合起来使用，并介绍下其源码是如何实现的。

## Spring-Mybatis使用

### 添加maven依赖

```
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>4.3.8.RELEASE</version>
</dependency>

<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis-spring -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>1.3.2</version>
</dependency>
```

#### 以注解的方式进行配置

相比较于单纯使用Mybaits时，内置了事务类型。

```
@Configuration
@MapperScan("com.chenhao.mapper")
public class AppConfig {

  @Bean
  public DataSource dataSource() {
     return new EmbeddedDatabaseBuilder()
            .addScript("schema.sql")
            .build();
  }
 
  @Bean
  public DataSourceTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }
 
  @Bean
  public SqlSessionFactory sqlSessionFactory() throws Exception {
    //创建SqlSessionFactoryBean对象
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    //设置数据源
    sessionFactory.setDataSource(dataSource());
    //设置Mapper.xml路径
    sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
    // 设置MyBatis分页插件
    PageInterceptor pageInterceptor = new PageInterceptor();
    Properties properties = new Properties();
    properties.setProperty("helperDialect", "mysql");
    pageInterceptor.setProperties(properties);
    sessionFactory.setPlugins(new Interceptor[]{pageInterceptor});
    return sessionFactory.getObject();
  }
}



```

















参考文档：https://www.cnblogs.com/java-chen-hao/p/11833780.html#_label0_3