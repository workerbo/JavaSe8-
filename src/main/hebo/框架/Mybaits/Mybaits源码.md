![分层图](https://gitee.com/workerbo/gallery/raw/master/2020/07.png)

[田小波Mybaits](https://www.tianxiaobo.com/2018/07/16/MyBatis-%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E7%B3%BB%E5%88%97%E6%96%87%E7%AB%A0%E5%AF%BC%E8%AF%BB/)





###### 日志模块



###### 事务模块

JdbcTransaction：单独使用Mybatis时，默认的事务管理实现类，就和它的名字一样，它就是我们常说的JDBC事务的极简封装，和编程使用mysql-connector-java-5.1.38-bin.jar事务驱动没啥差别。其极简封装，仅是让connection支持连接池而已。

ManagedTransaction：含义为托管事务，空壳事务管理器，皮包公司。仅是提醒用户，在其它环境中应用时，把事务托管给其它框架，比如托管给Spring，让Spring去管理事务

**autoCommit=false，但是没有手动commit，在sqlSession.close()时，Mybatis会将事务进行rollback()操作，然后才执行conn.close()关闭连接，当然数据最终也就没能持久化到数据库中了。**

对于JDBC来说，autoCommit=false时，是自动开启事务的



###### 执行器

预编译语句的优势在于归纳为：**一次编译、多次运行，省去了解析优化等过程；此外预编译语句能防止sql注入。**



MyBatis 通过 Binding 模块，将用户自定义的 Mapper 接口与映射配置文件关联起来，系统可以通过调用自定义 Mapper 接口中的方法执行相应的 SQL 语句完成数据库操作

###### SQL执行过程：

1. 为 mapper 接口生成实现类
2. 根据配置信息生成 SQL，并将运行时参数设置到 SQL 中
3. 一二级缓存的实现
4. 插件机制
5. 数据库连接的获取与管理
6. 查询结果的处理，以及延迟加载等

## 什么是 MyBatis

MyBatis 是一种半自动化的 Java 持久层框架（persistence framework），其通过注解或 XML 的方式将对象和 SQL 关联起来。

#### JDBC 访问数据库的过程演示

加载数据库驱动，创建数据库连接对象，创建 SQL 执行对象，执行 SQL 和处理结果集、关闭连接

默认自动提交，设置为手动之后，需要手动提交和回滚

#### MyBatis VS JDBC

使用 MyBatis 无需处理受检异常，比如 SQLException。另外，把 SQL 写在配置文件中，进行集中管理，利于维护。同时将 SQL 从代码中剥离，在提高代码的可读性的同时，也避免拼接 SQL 可能会导致的错误。除了上面所说这些，MyBatis 会将查询结果转为相应的对象，无需用户自行处理 ResultSet。

### 使用 Spring JDBC 访问数据库

Spring JDBC 还是比较容易使用的。不过它也是存在一定缺陷的，比如 SQL 仍是写在代码中。又比如，对于较为复杂的结果（数据库返回的记录包含多列数据），需要用户自行处理 ResultSet 等。不过与 JDBC 相比，使用 Spring JDBC 无需手动加载数据库驱动，获取数据库连接，以及创建 Statement 对象等操作。总的来说，易用性上得到了不少的提升。

### 使用 Hibernate 访问数据库

需要配置文件和映射文件

可以通过`OID`的方式进行，也就是`testORM`方法中对应的代码。这种方式不需要写 SQL，完全由 Hibernate 去生成。

第二种方式是通过`HQL`进行查询，查询过程对应测试类中的`testHQL`方法。这种方式需要写一点 HQL，并为其设置相应的参数

第三种方式是通过 JPA Criteria 进行查询，JPA Criteria 具有类型安全、面向对象和语义化的特点。使用 JPA Criteria，我们可以用写 Java 代码的方式进行数据库操作，无需手写 SQL。

#### MyBatis VS Hibernate

Hibernate 是把实体类（POJO）和表进行了关联，是一种完整的 ORM (O/R mapping) 框架。而 MyBatis 则是将数据访问接口（Dao）与 SQL 进行了关联，本质上算是一种 SQL 映射。

#### 单独使用Mybaits常用配置

```
<!-- mybatis-congif.xml -->
<configuration>
    <properties resource="jdbc.properties"/>

    <typeAliases>
        <typeAlias alias="Article" type="xyz.coolblog.model.ArticleDO"/>
        <typeAlias alias="Author" type="xyz.coolblog.model.AuthorDO"/>
    </typeAliases>

    <typeHandlers>
        <typeHandler handler="xyz.coolblog.mybatis.ArticleTypeHandler" javaType="xyz.coolblog.constant.ArticleTypeEnum"/>
    </typeHandlers>

    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${jdbc.driver}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <mapper resource="mapper/AuthorMapper.xml"/>
        <mapper resource="mapper/ArticleMapper.xml"/>
    </mappers>
</configuration>
```



| 标签名称     | 用途                                                         |
| ------------ | ------------------------------------------------------------ |
| properties   | 用于配置全局属性，这样在配置文件中，可以通过占位符 ${} 进行属性值配置 |
| typeAliases  | 用于定义别名。如上所示，这里把`xyz.coolblog.model.ArticleDO`的别名定义为`Article`，这样在 SQL 映射文件中，就可以直接使用别名，而不用每次都输入长长的全限定类名了 |
| typeHandlers | 用于定义全局的类型处理器，如果这里配置了，SQL 映射文件中就不需要再次进行配置。前面为了讲解需要，我在 SQL 映射文件中也配置了 ArticleTypeHandler，其实是多余的 |
| environments | 用于配置事务，以及数据源                                     |
| mappers      | 用于配置 SQL 映射文件的位置信息                              |
|              | 全局配置参数                                                 |







spring测试

@RunWith（SpringJUnit4ClassRunner.class）

@ContextConfiguration("")





Java 中如无意外，应该所有的数据库操作都是基于 JDBC 进行的，而 MyBatis 也是对 JDBC 做了简单的封装而已。

连接池和DB【避免浪费和不可控】

拥有了一个存放着有限数量连接实例的连接池，1.当我们需要执行一个 SQL 语句时，就到这个池里面去拿一个 connection；2.如果没有可用的 connection 就看最大连接数是否满了，2.1.满了就等待其他线程释放 connection 再获取，直到成功或超时，2.2.没满就创建一个 connection；3.用完再把 connection 放回去。

##### Mybaits[事务](https://www.cnblogs.com/dongguangming/p/12846052.html)

它有两种实现方式：

1、使用JDBC的事务管理机制：利用java.sql.Connection对象完成对事务的提交（commit()）、回滚（rollback()）、关闭（close()）等。JdbcTransaction

2、使用MANAGED的事务管理机制：这种机制MyBatis自身不会去实现事务管理，而是让程序的容器如（tomcat，JBOSS，Weblogic，spring）来实现对事务的管理

##### spring事务、Mybaits事务关系

每个 sqlSession 对象都对应一个 connection

在 Spring 中，如果存在事务，每个线程都会缓存当前创建的 sqlSession 对象，不同线程之间的 sqlSession 对象是完全隔离的。而 MyBatis 每创建一个 sqlSession 对象前，都会先判断当前是否存在事务（也就是 Spring 中声明的事务），1.没有事务时就是正常创建即可，并且这样创建的 sqlSession 对象都会在执行完 SQL 语句后自动 commit 和 close 的；2.有事务时 MyBatis 创建 sqlSession 对象前先查看当前线程是否存在缓存的 sqlSession 对象，2.1.不存在 sqlSession 对象，创建并缓存到当前线程，2.2.存在 sqlSession 对象，不再创建，直接使用缓存中的 sqlSession 对象；并且这时的 sqlSession 对象执行完之后是不会进行 commit 与 close 的，这些操作交给了 Spring 事务来完成

如果一个事务中存在多个 SQL 语句需要执行，那么在这个事务中，自始至终都是只有一个 sqlSession 对象的，并且只对应一个 connection，全部语句都执行完后再 commit 与 close，如果出现异常（或手动回滚）就回滚事务。

Spring事务管理的一个优点就是为不同的事务API提供一致的编程模型





#### 源码解析

1.[ 根据配置文件创建SqlSessionFactory](https://www.cnblogs.com/java-chen-hao/p/11743430.html)

SqlSessionFactory是通过SqlSessionFactoryBuilder的build方法创建的，build方法内部是通过一个XMLConfigBuilder对象解析mybatis-config.xml文件生成一个Configuration对象。

2，[Mapper映射的解析过程](https://www.cnblogs.com/java-chen-hao/p/11743442.html)

解析Mapper.xml的最后阶段，获取到Mapper.xml的namespace，然后利用反射，获取到namespace的Class,并创建一个**MapperProxyFactory的实例，namespace的Class作为参数，最后将namespace的Class为key，\**MapperProxyFactory的实例为value存入\******knownMappers。**

***\*要求xml配置中的namespace要和和对应的Mapper接口的全限定名了\****

```
public class MapperProxyFactory<T> {
    //存放Mapper接口Class
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return this.mapperInterface;
    }

    public Map<Method, MapperMethod> getMethodCache() {
        return this.methodCache;
    }

    protected T newInstance(MapperProxy<T> mapperProxy) {
        //生成mapperInterface的代理类
        return Proxy.newProxyInstance(this.mapperInterface.getClassLoader(), new Class[]{this.mapperInterface}, mapperProxy);
    }

    public T newInstance(SqlSession sqlSession) {
         /*
         * 创建 MapperProxy 对象，MapperProxy 实现了 InvocationHandler 接口，代理逻辑封装在此类中
         * 将sqlSession传入MapperProxy对象中，第二个参数是Mapper的接口，并不是其实现类
         */
        MapperProxy<T> mapperProxy = new MapperProxy(sqlSession, this.mapperInterface, this.methodCache);
        return this.newInstance(mapperProxy);
    }
}
```

MapperProxy这个InvocationHandler 创建的时候，传入的参数并不是Mapper接口的实现类，我们以前是怎么创建JDK动态代理的？先创建一个接口，然后再创建一个接口的实现类，最后创建一个InvocationHandler并将实现类传入其中作为目标类，创建接口的代理类，然后调用代理类方法时会回调InvocationHandler的invoke方法，最后在invoke方法中调用目标类的方法，但是我们这里调用Mapper接口代理类的方法时，需要调用其实现类的方法吗？不需要，我们需要调用对应的配置文件的SQL

invoke方法里通过创建和缓存MapperMethod，【包含SqlCommand和MethodSignature】，然后执行execute方法通过sqlSession去操作。这就是通过映射器去执行和sqlSession直接执行的区别。

mybatis执行sql有两种方式：

1.命名空间加上 SQL id 组合而成的。

2.通过DAO接口调用。





3.[SqlSession的创建过程](https://www.cnblogs.com/java-chen-hao/p/11743506.html)

1. 首先从configuration获取Environment对象，里面主要包含了DataSource和TransactionFactory对象
2. 创建TransactionFactory
3. 创建Transaction
4. 从configuration获取Executor
5. 构造DefaultSqlSession对象

DefaultSqlSession传参configuration和刚生成的executor，默认不自动提交。

SqlSession的所有查询接口最后都归结位Exector的方法调用。

executor含有事务，事务包含数据源。

JdbcTransaction主要维护了一个默认autoCommit为false的Connection对象，对事物的提交，回滚，关闭等都是接见通过Connection完成的。【连接是从连接池DataSource得来的】



在一级缓存中，同一个SqlSession下，查询语句相同的SQL会被缓存，如果执行增删改操作之后，该缓存就会被删除

二级缓存构建在一级缓存之上，在收到查询请求时，MyBatis 首先会查询二级缓存。若二级缓存未命中，再去查询一级缓存。与一级缓存不同，二级缓存和具体的命名空间绑定，一个Mapper中有一个Cache

**二级缓存的生效必须在session提交或关闭之后才会生效**







