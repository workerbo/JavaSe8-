MyBatis 在解析配置文件的 <mappers> 节点的过程中，会调用 MapperRegistry 的 addMapper 方法将 Class 到 MapperProxyFactory 对象的映射关系存入到 knownMappers

解析Mapper.xml的最后阶段，获取到Mapper.xml的namespace，然后利用反射，获取到namespace的Class,并创建一个**MapperProxyFactory的实例，namespace的Class作为参数，最后将namespace的Class为key，**

###### 为 Mapper 接口创建代理对象

```
// 从 knownMappers 中获取与 type 对应的 MapperProxyFactory
13     final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
 // 创建代理对象
19         return mapperProxyFactory.newInstance(sqlSession);
```





Mybatis框架中，**MapperProxyFactory**，是mapper代理工厂，可基于接口和**MapperProxy**来构建一个mapper代理对象，实现了将接口转变成一个对象。**MapperProxy** 实现了 **InvocationHandler** 接口的invoke方法【在sqlSessionFactory生成过程中注册进了knownMappers】

我们以前是怎么创建JDK动态代理的？先创建一个接口，然后再创建一个接口的实现类，最后创建一个InvocationHandler并将实现类传入其中作为目标类，创建接口的代理类，然后调用代理类方法时会回调InvocationHandler的invoke方法，最后在invoke方法中调用目标类的方法。而Mapper不需要实现类

###### Mapper代理类如何执行SQL

回调函数**invoke**逻辑会首先检测被拦截的方法是不是定义在 Object 中的，比如 equals、hashCode 方法等。对于这类方法，直接执行即可。紧接着从缓存中获取或者创建 MapperMethod 对象，然后通过该对象中的 execute 方法执行 SQL。我们先来看看如何创建MapperMethod

MapperMethod包含SqlCommand 和MethodSignature 对象，我们来看看其创建过程

**① 创建 SqlCommand 对象**

通过拼接接口名和方法名，在configuration获取对应的MappedStatement，并设置设置 name 和 type 变量

**② 创建 MethodSignature 对象**

**MethodSignature** 包含了被拦截方法的一些信息，如目标方法的返回类型，目标方法的参数列表信息等。

里面的方法convertArgsToSqlCommandParam对参数设置参数名称和参数值

在**MapperMethod**的execute根据 SQL 类型执行相应的数据库操作。例如sqlSession.insert(command.getName(), param)，这个就是就是最早版本，根据字符串定位，传入Map参数执行SQL的过程。



每一个Mapper在启动时解析配置文件的时候产生了**MapperProxyFactory**，在通过sqlsession获取代理对象每次都是**新建**的。

###### 一句话总结