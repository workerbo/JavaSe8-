

Mybatis集成Spring后，默认使用的transactionFactory是SpringManagedTransactionFactory。mybatis的执行事务的事务管理器就切换成了SpringManagedTransaction

###### SqlSessionFactory

```
private SqlSession openSessionFromConnection(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    
     Transaction tx = null;
    try {
      final Environment environment = configuration.getEnvironment();
      //从environment中取出TransactionFactory
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
         //创建Transaction
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
      final Executor executor = configuration.newExecutor(tx, execType);//创建包含事务操作的执行器
      return new DefaultSqlSession(configuration, executor, autoCommit); //构建包含执行器的SqlSession
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
}

private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
    if (environment == null || environment.getTransactionFactory() == null) {
      return new ManagedTransactionFactory();
    }
    //这里返回SpringManagedTransactionFactory
    return environment.getTransactionFactory();
}
```

###### SpringManagedTransactionFactory

```
 

@Override
public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
    //创建SpringManagedTransaction
    return new SpringManagedTransaction(dataSource);
}
```

###### SpringManagedTransaction

```
public class SpringManagedTransaction implements Transaction {
    private static final Log LOGGER = LogFactory.getLog(SpringManagedTransaction.class);
    private final DataSource dataSource;
    private Connection connection;
    private boolean isConnectionTransactional;
    private boolean autoCommit;

    public SpringManagedTransaction(DataSource dataSource) {
        Assert.notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null) {
            this.openConnection();
        }

        return this.connection;
    }

    private void openConnection() throws SQLException {
        //通过DataSourceUtils获取connection，这里和JdbcTransaction直接从数据源获取不一样，
        this.connection = DataSourceUtils.getConnection(this.dataSource);
        this.autoCommit = this.connection.getAutoCommit();
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JDBC Connection [" + this.connection + "] will" + (this.isConnectionTransactional ? " " : " not ") + "be managed by Spring");
        }

    }

    public void commit() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Committing JDBC Connection [" + this.connection + "]");
            }
            //通过connection提交，这里和JdbcTransaction一样
            this.connection.commit();
        }

    }

    public void rollback() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Rolling back JDBC Connection [" + this.connection + "]");
            }
            //通过connection回滚，这里和JdbcTransaction一样
            this.connection.rollback();
        }

    }

    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(this.connection, this.dataSource);
    }

    public Integer getTimeout() throws SQLException {
        ConnectionHolder holder = (ConnectionHolder)TransactionSynchronizationManager.getResource(this.dataSource);
        return holder != null && holder.hasTimeout() ? holder.getTimeToLiveInSeconds() : null;
    }
}
```



因为Spring事务在没调用Mapper方法之前就需要开一个Connection，并设置事务不自动提交，那么transactionManager中自然要配置dataSource。实际上获取的Connection都是通过dataSource来获取的。

Mybatis和Spring整合后SpringManagedTransaction和Spring的Transaction的关系：

- 如果开启Spring事务，则先有Spring的Transaction，然后mybatis创建sqlSession时，会创建SpringManagedTransaction并加入sqlSession中，SpringManagedTransaction中的connection会从Spring的Transaction创建的ThreadLocal中获取当前线程的Connection.
- 如果没有开启Spring事务或者第一个方法没有事务后面的方法有事务，则SpringManagedTransaction**创建Connection并放入ThreadLocal中**

```
DataSourceUtils【spring-jdbc包中的类】
public static Connection doGetConnection(DataSource dataSource) throws SQLException {
   Assert.notNull(dataSource, "No DataSource specified");

   ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
   if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
      conHolder.requested();
      if (!conHolder.hasConnection()) {
         logger.debug("Fetching resumed JDBC Connection from DataSource");
         conHolder.setConnection(fetchConnection(dataSource));
      }
      return conHolder.getConnection();
   }
   // Else we either got no holder or an empty thread-bound holder here.

   logger.debug("Fetching JDBC Connection from DataSource");
   Connection con = fetchConnection(dataSource);

   if (TransactionSynchronizationManager.isSynchronizationActive()) {
      logger.debug("Registering transaction synchronization for JDBC Connection");
      // Use same Connection for further JDBC actions within the transaction.
      // Thread-bound object will get removed by synchronization at transaction completion.
      ConnectionHolder holderToUse = conHolder;
      if (holderToUse == null) {
         holderToUse = new ConnectionHolder(con);
      }
      else {
         holderToUse.setConnection(con);
      }
      holderToUse.requested();
      //这儿不同的线程会有不同的connection对象。
      TransactionSynchronizationManager.registerSynchronization(
            new ConnectionSynchronization(holderToUse, dataSource));
      holderToUse.setSynchronizedWithTransaction(true);
      if (holderToUse != conHolder) {
         TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
      }
   }

   return con;
}
```

```--
Mapper【TransactionSynchronizationManager是否当前线程有缓存的sqlsession】---》SqlSessionFactory--》openSession---》SpringManagedTransactionFactory 中openSessionFromDataSource---》获取事务并设置到执行器。
```

​          如果当前业务没有使用@Transation,那么每次执行了Mapper接口的方法直接commit。也就是说通过org.springframework.jdbc.datasource.DataSourceUtils#fetchConnection获取到的Connection将会commit，相当于Connection是自动提交的，也就是说如果不使用@Transation，Mybatis将没有事务可言。

```
private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations =
      new NamedThreadLocal<>("Transaction synchronizations");
```

###### 一句话总结

   mybaits在通过mapper代理对象执行方法的时候，获取sqlsession里会创建自己的Mybaits事务对象，并且通过事务获取的conn对象，如果spring没有设置事务，则自己去数据源里获取conn。

spring整合springJDBC，springJDBC是第一方框架，所以不需要太多整合。