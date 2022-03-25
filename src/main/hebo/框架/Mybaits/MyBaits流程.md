MyBatis的底层操作封装了JDBC的API，MyBatis的核心对象（SqlSession，Executor）与JDBC的核心对象（Connection，Statement）相互对应。

![mybatis.png](https://gitee.com/workerbo/gallery/raw/master/2020/326517643.png)

![img](https://gitee.com/workerbo/gallery/raw/master/2020/3925609-a2fd58c139efa9ac.png)



SqlSession对象，该对象中包含了执行SQL语句的所有方法

将SQL的配置信息加载成为一个个MappedStatement对象（包括了传入参数映射配置、执行的SQL语句、结果映射配置),存储在内存中

- **Executor** 主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，它会将数据库相关操作委托给 StatementHandler完成。
- **StatementHandler** 首先通过 **ParameterHandler** 完成 SQL 语句的实参绑定，然后通过 `java.sql.Statement` 对象执行 SQL 语句并得到结果集，最后通过 **ResultSetHandler** 完成结果集的映射，得到结果对象并返回