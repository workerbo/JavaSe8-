SqlSessionTemplate实现了SqlSession接口，那么Mapper代理类中执行所有的数据库操作，都是通过SqlSessionTemplate来执行，然后内部由对象sqlSessionProxy执行查询【静态代理】，sqlSessionProxy又是一个动态代理对象。

```
public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {
        Assert.notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
        Assert.notNull(executorType, "Property 'executorType' is required");
        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
        this.sqlSessionProxy = (SqlSession)Proxy.newProxyInstance(SqlSessionFactory.class.getClassLoader(), new Class[]{SqlSession.class}, new SqlSessionTemplate.SqlSessionInterceptor());
    }
```



Mapper代理类调用方法执行逻辑：

1、生成Mapper代理类时，设置SqlSessionBeanFactory时创建SqlSessionTemplate对象赋值给成员变量sqlSession，调用Mapper代理类的方法时，最后会通过SqlSession类执行，也就是调用SqlSessionTemplate中的方法。[spring-Mybaits代理了通过Sqlsession获取Mapper对象的逻辑。]

2、SqlSessionTemplate中操作数据库的方法中又交给了**sqlSessionProxy**这个代理类去执行【静态代理，**sqlSessionProxy**是通过动态代理生成的】，那么每次执行的方法都会回调其SqlSessionInterceptor这个InvocationHandler的invoke方法。

3、在invoke方法中，为每个线程创建一个新的SqlSession，并通过反射调用SqlSession的method。这里sqlSession是一个线程局部变量，不同线程相互不影响，实现了SqlSessionTemplate的线程安全性

4、如果当前操作并没有在Spring事务中，那么每次执行一个方法，都会提交，相当于数据库的事务自动提交，Mysql的一级缓存也将不可用	【 sqlSession提交，会清空一级缓存】

SqlSessionTemplate的SqlSessionInterceptor

```
private class SqlSessionInterceptor implements InvocationHandler {
    //很奇怪，这里并没有真实目标对象？
    private SqlSessionInterceptor() {
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取一个sqlSession来执行proxy的method对应的SQL,
        // 每次调用都获取创建一个sqlSession线程局部变量，故不同线程相互不影响，在这里实现了SqlSessionTemplate的线程安全性
        SqlSession sqlSession = SqlSessionUtils.getSqlSession(SqlSessionTemplate.this.sqlSessionFactory, SqlSessionTemplate.this.executorType, SqlSessionTemplate.this.exceptionTranslator);

        Object unwrapped;
        try {
            //直接通过新创建的SqlSession反射调用method
            //这也就解释了为什么不需要目标类属性了，这里每次都会创建一个
            Object result = method.invoke(sqlSession, args);
            // 如果当前操作没有在一个Spring事务中，则手动commit一下
            // 如果当前业务没有使用@Transation,那么每次执行了Mapper接口的方法直接commit。也就是说通过org.springframework.jdbc.datasource.DataSourceUtils#fetchConnection获取到的Connection将会commit，相当于Connection是自动提交的，也就是说如果不使用@Transation，Mybatis将没有事务可言。
            // 还记得我们前面讲的Mybatis的一级缓存吗，这里一级缓存不能起作用了，因为每执行一个Mapper的方法，sqlSession都提交了。
            // sqlSession提交，会清空一级缓存
            if (!SqlSessionUtils.isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
                sqlSession.commit(true);
            }

            unwrapped = result;
        } catch (Throwable var11) {
            unwrapped = ExceptionUtil.unwrapThrowable(var11);
            if (SqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                SqlSessionUtils.closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
                sqlSession = null;
                Throwable translated = SqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException)unwrapped);
                if (translated != null) {
                    unwrapped = translated;
                }
            }

            throw (Throwable)unwrapped;
        } finally {
            if (sqlSession != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
            }

        }
        return unwrapped;
    }
}
```



###### invoke中是如何创建**SqlSession的**

```
public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {
    Assert.notNull(sessionFactory, "No SqlSessionFactory specified");
    Assert.notNull(executorType, "No ExecutorType specified");
    //通过TransactionSynchronizationManager内部的ThreadLocal中获取
    SqlSessionHolder holder = (SqlSessionHolder)TransactionSynchronizationManager.getResource(sessionFactory);
    SqlSession session = sessionHolder(executorType, holder);
    if(session != null) {
        return session;
    } else {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating a new SqlSession");
        }
        //这里我们知道实际上是创建了一个DefaultSqlSession
        session = sessionFactory.openSession(executorType);
        //将创建的SqlSession对象放入TransactionSynchronizationManager内部的ThreadLocal中
        registerSessionHolder(sessionFactory, executorType, exceptionTranslator, session);
        return session;
    }
}
```

TransactionSynchronizationManager这个类，其内部维护了一个**ThreadLocal的**Map

```
private static final ThreadLocal<Map<Object, Object>> resources =
      new NamedThreadLocal<>("Transactional resources");
      只有在spring事务开启的情况下才会存储sqlsession。
```

这里同一线程创建了SqlSession后放入ThreadLocal中，同一线程中其他Mapper接口调用方法时，将会直接从ThreadLocal中获取。

总结：SqlSessionTemplate【实现了SqlSession】通过代理将手动获取SqlSession的过程给代理了。并且实现在一个线程的一个事务当中多个Mapper共用一个DefaultSqlsession实例。