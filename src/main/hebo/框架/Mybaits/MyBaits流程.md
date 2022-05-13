MyBatis的底层操作封装了JDBC的API，MyBatis的核心对象（SqlSession，Executor）与JDBC的核心对象（Connection，Statement）相互对应。

![mybatis.png](https://gitee.com/workerbo/gallery/raw/master/2020/326517643.png)



在spring中通过扫描后修改BeanDefinition，并设置了sqlSessionFactory属性【设置时创建了SqlSessionTemplate对象【实现了SqlSession】，通过代理将手动获取SqlSession的过程给代理了。并且实现在一个线程的一个事务当中多个Mapper共用一个DefaultSqlsession实例。这里同一线程创建了SqlSession后放入ThreadLocal中，同一线程中其他Mapper接口调用方法时，将会直接从TransactionSynchronizationManager的ThreadLocal中获取。获取的数据库连接也放在了TransactionSynchronizationManager的ThreadLocal中】和构造参数className的值。代理了通过Mapper接口的class对象获取Mapper代理类对象的过程。

同一个SqlSession中只有一个SimpleExecutor，SimpleExecutor中有一个Transaction，Transaction有一个connection。



Mybaits本身在解析配置文件的过程中将Mapper接口动态代理，代理了将Mapper接口方法名转为唯一标识符这个过程。



![img](https://gitee.com/workerbo/gallery/raw/master/2020/3925609-a2fd58c139efa9ac.png)



SqlSession对象，该对象中包含了执行SQL语句的所有方法

将SQL的配置信息加载成为一个个MappedStatement对象（包括了传入参数映射配置、执行的SQL语句、结果映射配置),存储在内存中

- **Executor** 主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，它会将数据库相关操作委托给 StatementHandler完成。
- **StatementHandler** 首先通过 **ParameterHandler** 完成 SQL 语句的实参绑定，然后通过 `java.sql.Statement` 对象执行 SQL 语句并得到结果集，最后通过 **ResultSetHandler** 完成结果集的映射，得到结果对象并返回