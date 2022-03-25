编写插件时，除了需要让插件类实现 Interceptor 接口，还需要通过注解标注该插件的拦截点。所谓拦截点指的是插件所能拦截的方法，MyBatis 所允许拦截的方法如下：

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

Executor 实例会在创建 SqlSession 的过程中被创建，Executor 实例创建完毕后，MyBatis 会通过 JDK 动态代理为实例生成代理类。这样，插件逻辑即可在 Executor 相关方法被调用前执行。

```
 // 遍历拦截器集合
        for (Interceptor interceptor : interceptors) {
            // 调用拦截器的 plugin 方法植入相应的插件逻辑
            target = interceptor.plugin(target);
        }
```

Plugin 实现了 InvocationHandler 接口，因此它的 invoke 方法会拦截所有的方法调用。invoke 方法会对所拦截的方法进行检测，以决定是否执行插件逻辑。









































##### PageHelper

PageHelper类中有几个重载方法startPage，最后进入到上图中的方法，通过源码可以看到，这个方法主要是把pageNum和pageSize封装到对象中。同时，更重要的一点，这里通过SqlUtil类中的setLocalPage(page)方法将page对象放到了ThreadLocal中，为了隔离不同线程之间的page对象，使线程之间互不影响。后面的查询操作也会通过SqlUtil类获取当前线程下的page对象，利用当前线程下配置的page对象来进行sql的分页查询。
     **查询之前会调用PageHelper的intercept方法，从源码很明显可以看到，因为PageHelper实现了MyBatis的拦截器接口Interceptor，并实现了intercept方法**

**这里会调用SqlUtil的processPage方法，processPage方法再调用本类中的_processPage方法，_processPage最后再调用本类的doProcessPage方法，doProcessPage返回最终结果。**

每个线程在执行一次PageHelper.startPage(xx, xx)之后只能执行一次分页查询，因为每次分页查询后会把当前线程中的page对象清除掉



 log4j.rootLogger = [ level ] , appenderName1, appenderName2, …

##### Mybaits动态SQL

###### `foreach` 用法

当参数类型为 `Collection` 的时候，默认会转换为 `Map` 类型，并添加一个 key 为 `collection` 的值（MyBatis 3.3.0版本增加），如果参数类型是 `List` 集合，那么就继续添加一个 key 为 `list` 的值

当参数类型为 `Array` 的时候，也会转换成 `Map` 类型，默认的 key 为 `array`。

动态更新

foreach 实现动态UPDATE
这一节主要讲当参数类型是 Map 时，foreach 如何实现动态 UPDATE 。

当参数是 Map 类型的时候，foreach 标签的 index 属性值对应的不是索引值，而是 Map 中的 key，利用这个 key 可以实现动态 UPDATE。
    `bind` 用法

`bind` 标签可以使用 OGNL 表达式创建一个变量并将其绑定到上下文中。

MyBatis如何利用RowBounds实现通用分页，简单点说道，就是先把数据全部查询到ResultSet，然后从ResultSet中取出offset和limit之间的数据，这就实现了分页查询。

参数配直＠ Param 注解后， MyBatis 就会自动将参数封装成 Map 类型，＠ Param 注解值作为 Map 中的 key ，因此在 SQL 部分就可以通过配置的注解值来使用参数。不关心这个参数叫什么名字就会直接把这个唯一 的参数值拿来使用。

支出多数据库,if 标签配合默认的上下文中的 _databaseid 参数这种写法去实现。bind方法创建了上下文变量，name 为绑定到上下文的变量名， value为OGNL达式【算术关系运算  方法调用】。

因此为了保证数据类型的正确，需要手动指定日期类型， date time datetime应的 JDBC 类型分别为 DATE TIME TIMESTAMP

##### XML和注解配置

Mabitys  面向SQL和映射器

只有XML-》XML+接口-》注解【特别是@provider】+接口

当只使用 XML 而不使用接口的时候 namespace 的值可以设置为任意不重复的名称

SqlSession以通过命名空间间接调用.使用接口调用方式就会方便很多，



![image-20210607185603735](../../../../../../../Programfile/Typora/upload/image-20210607185603735.png)



##### 高级查询

resultMap 可以继承  

如果使用 result Type 来设置返回结果的类型，需要在 SQL 中为所有列名和属性名不 一致的列设置别名，通过设置别名使最终的查询结果列和 result Type 指定对象的属性名保持 致，进而实现自动映射。【或者 MyBatis 还提供 个全局属性mapUnderscoreToCamelCase ，通过配置这个属性为 true 可以自动将以下画线方式命名的数据库列映射到 Java 对象的驼峰式命名属性中。】

###### 一对一关联

assiaciation 标签的嵌套查询 【延迟加载：如果查询出来并没有使用，如果查询的不是一 条数据，而是N条数据，那就会出现 N+l 问题，主SQL 查询一次，查询出N条结果，这 N条结果要各自执行一次查询，只有当调用 getRole （）方法获取 role 的时候， MyBatis 会执行嵌套查询去获取数据。fetch Type ：数据加载方式，可选值为 lazy eager ，分别为延迟加载和积极加这个配置会覆盖全局的 lazyLoadingEnabled 配置。有 个参数为 aggressivelazyloading 。这个参数的含义是，当该参数设置为 true 时，对任意延迟属性的调用会使带有延迟 载属性的对象完整加载】、嵌套结果映射【点，标签、现有map】



提供了参数 lazyLoadTriggerMethods 帮助解决这个问题，这个参数的含义是，当调用配置中的方法时，加载全部的延迟加载数据。默认值为 eq ls clone ,hashCode,toSt ring

在和 Spring 集成时，要确保只能在 Service 层调用延迟加载的属性 当结果从 Service 层返回至 Controller 层时， 如果

获取延迟加载的属性值，会因为 SqlSessio口已经关闭而抛出异常

###### 一对多关联

id 的 作用就是在嵌套的映射配置 判断数据是否相同，

类型处理器：以枚举类型举例

Mybaits

现在有了 SQL 封装的 MappedStatement 对象和执行 SQL Executor 对象

当使用 MyBatis 时， 项目启动后就已经准备好了所有方法对应的 MappedStatemeηt 对象。

SqlSession sqlSession = new DefaultSqlSes s 工 on(conexecutor, false) ; 

首先将 MappedStatement 添加到 Configuration 中，在 Configurat io 口中会以 Map的形式记录，其中 Map key MappedStatement id ，这样就可 以很方便地通过 id  去Configuration 中获取 MappedStatement 了。在使用完整 id 保存的同时，还会尝试使用“．”分割最后的字符串（通常是方法名）作为 key 井保存 份。如果 key 已经存在，就会标记该 key 有歧义，这种情况下若通过短的 key 调用就会因为有歧义而抛出异常。

然后再将 Configuration和 Executor 封装 DefaultSqlSession 中，有了这两项就能方便地通过 MappedStateme id 来调用相应的方法了，代码如下

Country country = sqlSession.selectOne （” sel ectCountry ”， L);

MyBatis 使用 JDK 动态代理解决了 DAO 接口实现类的问题。并且通过接口调用方法。





##### Mybatis与缓存

###### 一级缓存

My Batis 一级缓存【默认开启且不可改变】存在于 SqlSession 的生命周期中，在同 SqlSession 中查询时， MyBatis 会把执行的方法和参数通过算法生成缓存的键值，将键值和查询结果存入一个 Map对象中。如果同一个 SqlSession 中执行的方法和参数完全一致，那么通过算法会生成相同的键值，当 Map 缓存对象中己经存在该键值时，则会返回缓存中的对象。[此时只更改不更新再次查询会查到缓存中的被更新对象，然后会被送入二级缓存中]

该修改在原来方法的基础上增加了 flushCache= true ，这个属性配置为 true 后，在查询数据前清空当前的一级缓存。

任何的 INSERTUPDATE DELET 操作都会清空一级缓存，所以查询 user3 的时候由于缓存不存在，就会再次执行数据库查询获取数据。

######  二级缓存

MyBatis 二级缓存是和命名空间绑定的。在保证二级缓存的全局配置开启的情况下，添加＜cache/＞元素即可

映射语句文件中的所有 SELECT 语句将会被缓存

• 映射语句文件中的所有 SERT UPDAT ETE 语句会刷新缓存

由于配置的是可读写的缓存，而 MyBatis MyBatis 缉存配置 使用 SerializedCache Corg . apache bat . cache . decorators . SerializedCache) 序列化缓存来实现可读写缓存类，井通过序列化和反序列 来保证通过缓存获取数据时，得到的是个新的实例。因此，如果配置为只读缓存，就会使用 Map 来存储缓存值 这种情况下 ，从缓存中获取的对象就是同个实例

调用 close 方法关闭 SqlSession 时， SqlSession 才会保存查询数据到 级缓存中在这之后二级缓存才有了缓存数据

MyBatis 默认提供的缓存实现是基于 Map 实现的内存缓存，还可以选择些类 EhCache 的缓存框架【java进程内】或 Redis 缓存数据库【和应用本身的状态无关】等工具来保存 My atis 级缓存数据

MyBatis 级缓存是和命名空间绑定的 ，所以通常情况下每 Mapper 映射文件都拥有自己的二级缓存，不同 Mapper 级缓存互不影 。在常见的数据库操作中， 多表联合查询【二级缓存适用于大多数为查询且没有太多关联，少部分关联查询使用参照缓存】

在无法保证数据不出现脏读的情况下，建议在业务层使用可控制的缓存代替二级缓存