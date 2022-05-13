默认的执行器为SimpleExecutor，如果开启了二级缓存(默认开启)，则CachingExecutor会包装SimpleExecutor，那么我们该看CachingExecutor的**query**方法了

```
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    // 获取 BoundSql
    BoundSql boundSql = ms.getBoundSql(parameterObject);
   // 创建 CacheKey
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    // 调用重载方法
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```

拼接SQL语句呢，并将运行时参数设置到SQL语句中，这个完整的 SQL 以及其他的一些信息最终会存储在 BoundSql 对象中

###### BoundSql 

| 变量名               | 类型       | 用途                                                         |
| -------------------- | ---------- | ------------------------------------------------------------ |
| sql                  | String     | 一个完整的 SQL 语句，可能会包含问号 ? 占位符                 |
| parameterMappings    | List       | 参数映射列表，SQL 中的每个 #{xxx} 占位符都会被解析成相应的 ParameterMapping 对象 |
| parameterObject      | Object     | 运行时参数，即用户传入的参数，比如 Article 对象，或是其他的参数 |
| additionalParameters | Map        | 附加参数集合，用于存储一些额外的信息，比如 datebaseId 等     |
| metaParameters       | MetaObject | additionalParameters 的元信息对象                            |

MappedStatement 的 getBoundSql 方法

```
public BoundSql getBoundSql(Object parameterObject) {

    // 调用 sqlSource 的 getBoundSql 获取 BoundSql，把method运行时参数传进去
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);return boundSql;
}
```

SQL 配置中包含 `${}`（不是 #{}）占位符，或者包含 <if>、<where> 等标签时，会被认为是动态 SQL，此时使用 DynamicSqlSource 存储 SQL 片段。否则，使用 RawSqlSource 存储 SQL 配置信息



```
public BoundSql getBoundSql(Object parameterObject) {
    // 创建 DynamicContext
    DynamicContext context = new DynamicContext(configuration, parameterObject);

    // 解析 SQL 片段，并将解析结果存储到 DynamicContext 中，这里会将${}替换成method对应的运行时参数，也会解析<if><where>等SqlNode
    rootSqlNode.apply(context);
    
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    /*
     * 构建 StaticSqlSource，在此过程中将 sql 语句中的占位符 #{} 替换为问号 ?，
     * 并为每个占位符构建相应的 ParameterMapping
     */
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    
 // 调用 StaticSqlSource 的 getBoundSql 获取 BoundSql
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);

    // 将 DynamicContext 的 ContextMap 中的内容拷贝到 BoundSql 中
    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
        boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;
}
```



BoundSql中包含了sql，#{}解析成的parameterMappings，还有运行时参数parameterObject。



先从二级缓存中查找【需要Cache**Executor**】，若未命中二级缓存，再从一级缓存中查找，若未命中一级缓存，再从数据库查询数据



**SimpleExecutor**

```
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
        Configuration configuration = ms.getConfiguration();
        // 创建 StatementHandler
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        // 创建 Statement、通过事务获取连接，通过连接生成对象【本地预编译对象】。并且设置参数
        stmt = prepareStatement(handler, ms.getStatementLog());
        // 执行查询操作
        return handler.<E>query(stmt, resultHandler);
    } finally {
        // 关闭 Statement
        closeStatement(stmt);
    }
}

private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    // 获取数据库连接
    Connection connection = getConnection(statementLog);
    // 创建 Statement，
    stmt = handler.prepare(connection, transaction.getTimeout());
  // 为 Statement 设置参数
    handler.parameterize(stmt);
    return stmt;
}

protected Connection getConnection(Log statementLog) throws SQLException {
    //通过transaction来获取Connection
    Connection connection = this.transaction.getConnection();
    return statementLog.isDebugEnabled() ? ConnectionLogger.newInstance(connection, statementLog, this.queryStack) : connection;
}
```

JdbcTransaction中有一个**Connection属性和dataSource属性，使用**connection来进行提交、回滚、关闭等操作，也就是说JdbcTransaction其实只是在jdbc的connection上面封装了一下，实际使用的其实还是jdbc的事务。如果当前事务没有关闭，也就是没有释放connection，那么在同一个Transaction中使用的是同一个connection。所以说我们的sqlSession是线程不安全的。

**同一个SqlSession中只有一个SimpleExecutor，SimpleExecutor中有一个Transaction，Transaction有一个connection。**

**PreparedStatementHandler**【有成员变量ParameterHandler 、】

StatementHandler有什么作用呢？通过这个对象获取Statement对象【里面 也是通过connection获取Statement，将sql语句传进去】，然后填充运行时参数，最后调用query完成查询。

```
stmt = handler.prepare(connection, transaction.getTimeout());

public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
    Statement statement = null;
    try {
        // 创建 Statement
        statement = instantiateStatement(connection);
        // 设置超时和 FetchSize
        setStatementTimeout(statement, transactionTimeout);
        setFetchSize(statement);
        return statement;
    } catch (SQLException e) {
        closeStatement(statement);
        throw e;
    } catch (Exception e) {
        closeStatement(statement);
        throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
    }
}

protected Statement instantiateStatement(Connection connection) throws SQLException {
    //获取sql字符串，比如"select * from user where id= ?"
    String sql = boundSql.getSql();
    // 根据条件调用不同的 prepareStatement 方法创建 PreparedStatement
    if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
        String[] keyColumnNames = mappedStatement.getKeyColumns();
        if (keyColumnNames == null) {
            //通过connection获取Statement，将sql语句传进去
            return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
            return connection.prepareStatement(sql, keyColumnNames);
        }
    } else if (mappedStatement.getResultSetType() != null) {
        return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
        return connection.prepareStatement(sql);
    }
}
```

##### 设置运行时参数到 SQL 中

```
public void parameterize(Statement statement) throws SQLException {
    // 通过参数处理器 ParameterHandler 设置运行时参数到 PreparedStatement 中
    parameterHandler.setParameters((PreparedStatement) statement);
}
```

```
public void parameterize(Statement statement) throws SQLException {
    // 通过参数处理器 ParameterHandler 设置运行时参数到 PreparedStatement 中
    parameterHandler.setParameters((PreparedStatement) statement);
}

public class DefaultParameterHandler implements ParameterHandler {
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private final BoundSql boundSql;
    private final Configuration configuration;

    public void setParameters(PreparedStatement ps) {
        /*
         * 从 BoundSql 中获取 ParameterMapping 列表，每个 ParameterMapping 与原始 SQL 中的 #{xxx} 占位符一一对应
         */
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    // 获取属性名
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        // 为用户传入的参数 parameterObject 创建元信息对象
                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        // 从用户传入的参数中获取 propertyName 对应的值
                        value = metaObject.getValue(propertyName);
                    }

                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        jdbcType = configuration.getJdbcTypeForNull();
                    }
                    try {
                        // 由类型处理器 typeHandler 向 ParameterHandler 设置参数
                        typeHandler.setParameter(ps, i + 1, value, jdbcType);
                    } catch (TypeException e) {
                        throw new TypeException(...);
                    } catch (SQLException e) {
                        throw new TypeException(...);
                    }
                }
            }
        }
    }
}
```

typeHandler.setParameter(ps, i + 1, value, jdbcType);，这句代码最终会向我们例子中一样执行，如下

```
public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter);
}
```

###### 执行查询

```
return handler.<E>query(stmt, resultHandler);
如下

public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    PreparedStatement ps = (PreparedStatement)statement;
    //直接执行ServerPreparedStatement的execute方法
    ps.execute();
    return this.resultSetHandler.handleResultSets(ps);
}
```