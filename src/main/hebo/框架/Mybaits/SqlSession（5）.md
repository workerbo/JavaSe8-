```
//配置environment环境
<environments default="development">
    <environment id="development">
        /** 事务配置 type= JDBC、MANAGED 
         *  1.JDBC:这个配置直接简单使用了JDBC的提交和回滚设置。它依赖于从数据源得到的连接来管理事务范围。
             在解析配置文件的时候如果是这个类型直接创建对应的事务管理器
         *  2.MANAGED:这个配置几乎没做什么。它从来不提交或回滚一个连接。
         */
        <transactionManager type="JDBC" />
        /** 数据源类型：type = UNPOOLED、POOLED、JNDI 
         *  1.UNPOOLED：这个数据源的实现是每次被请求时简单打开和关闭连接。
         *  2.POOLED：这是JDBC连接对象的数据源连接池的实现。 
         *  3.JNDI：这个数据源的实现是为了使用如Spring或应用服务器这类的容器
         */
        <dataSource type="POOLED">
            <property name="driver" value="com.mysql.jdbc.Driver" />
            <property name="url" value="jdbc:mysql://localhost:3306/xhm" />
            <property name="username" value="root" />
            <property name="password" value="root" />
            //默认连接事务隔离级别
            <property name="defaultTransactionIsolationLevel" value=""/> 
        </dataSource>
    </environment>
</environments>
```

SqlSession是mybatis的核心接口之一，是myabtis接口层的主要组成部分，对外提供了mybatis常用的api。myabtis提供了两个SqlSesion接口的实现，常用的实现类是DefaultSqlSession。

不集成spring的时候。设置的是JdbcTransactionFactory【根据配置的】，使用的是JdbcTransaction

```
SqlSession sqlSession=sqlSessionFactory.openSession();


DefaultSqlSessionFactory
 @Override
  public SqlSession openSession() {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
  }
  
  
  
  /**
 * ExecutorType 指定Executor的类型，分为三种：SIMPLE, REUSE, BATCH，默认使用的是SIMPLE
 * TransactionIsolationLevel 指定事务隔离级别，使用null,则表示使用数据库默认的事务隔离界别
 * autoCommit 是否自动提交，传过来的参数为false，表示不自动提交
 */
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
        // 获取配置中的环境信息，包括了数据源信息、事务等
        final Environment environment = configuration.getEnvironment();
        // 创建事务工厂
        final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        // 创建事务，配置事务属性
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        // 创建Executor，即执行器
        // 它是真正用来Java和数据库交互操作的类，后面会展开说。
        final Executor executor = configuration.newExecutor(tx, execType);
        // 创建DefaultSqlSession对象返回，其实现了SqlSession接口
        return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
        closeTransaction(tx);
        throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
        ErrorContext.instance().reset();
    }
}
  
  
```



主要包含以下几个步骤：

1. 首先从configuration获取Environment对象，里面主要包含了DataSource和TransactionFactory对象

2. 创建TransactionFactory

3. 创建Transaction

4. 从configuration获取Executor

5. 构造DefaultSqlSession对象

   

JDBC类型的事务管理器:这个配置直接简单使用了JDBC的提交和回滚设置。它依赖于从数据源得到的连接来管理事务范围。
在解析配置文件的时候如果是这个类型直接创建对应的事务管理器

JdbcTransaction主要维护了一个默认autoCommit为false的Connection对象，对事物的提交，回滚，关闭等都是接见通过Connection完成的。

executor包含了Configuration和刚刚创建的Transaction，默认的执行器为SimpleExecutor，如果开启了二级缓存(默认开启)，则CachingExecutor会包装SimpleExecutor，然后依次调用拦截器的plugin方法返回一个被代理过的Executor对象。

```
public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
    try {
    // 通过MappedStatement的Id获取 MappedStatement
      MappedStatement ms = configuration.getMappedStatement(statement);
       //调用 Executor 实现类中的 query 方法
      return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```



​     wrapCollection此处对集合或者数组的单个参数做包装。

