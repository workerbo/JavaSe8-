MapperScannerConfigurer将Mapper接口生成的代理对象注入到Spring

##### @MapperScan【spring-Mybaits】

```
@Import(MapperScannerRegistrar.class)
public @interface MapperScan
```

作用：指定要变成实现类的接口所在的包，然后包下面的所有接口在编译之后都会生成相应的实现类

用 `@MapperScan` 后，接口类就不需要使用 `@Mapper` 注解



MapperScannerRegistrar【ImportBeanDefinitionRegistrar 的实现类】完成事情

    1. 解析MapperScan注解的各个字段的值 ，用以初始化类路径扫描器
    2. 确定扫描类路径下哪些接口，如指定的包路径、指定的类所在包路径。上面倒数第2行代码，注册过滤器，用来指定包含哪些注解或接口的扫描（@MapperScan的annotationClass的markerInterface属性，如果设置的话）



### ClassPathMapperScanner

获取basePackages下的所有Class，并将其生成**BeanDefinition，**注入spring的**BeanDefini**tionMap中，也就是Class的描述类。然后，对生成的BeanDefinition做一些额外的处理。所有的Mapper接口扫描封装成的BeanDefinition的beanClass都设置成了MapperFactoryBean。

```
definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName())
definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
```



###### MapperFactoryBean



MapperFactoryBean  extends SqlSessionDaoSupport implements FactoryBean，那么getBean获取的对象是从其getObject()中获取，并且MapperFactoryBean是一个单例，那么其中的属性sqlSessionTemplate【封装了SqlsessionFactory】**对象也是一个单例，全局唯一**，供所有的Mapper代理类使用。通过sqlsession调用knownMappers得到**MapperProxyFactory**返回映射器代理对象。

实例化过程：

1、MapperFactoryBean通过反射调用构造器实例化出一个对象，并且通过上一节中definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName())设置的构造器参数，在构造器实例化时，传入Mapper接口的Class,并设置为MapperFactoryBean的mapperInterface属性。

2、进行属性赋值，通过上一节中definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);设置的属性值，在populateBean属性赋值过程中通过反射调用setSqlSessionFactory方法，并创建SqlSessionTemplate对象设置到sqlSession属性中。

```
 //还记得上一节中我们往BeanDefinition中设置的sqlSessionFactory这个属性吗？
    //在实例化MapperFactoryBean后，进行属性赋值时，就会通过反射调用setSqlSessionFactory
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (!this.externalSqlSession) {
            //创建一个SqlSessionTemplate并赋值给sqlSession
            this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
        }
    }
```

3、由于MapperFactoryBean实现了FactoryBean，最终注册进Spring容器的对象是从getObject()方法中取，接着获取SqlSessionTemplate这个SqlSession调用getMapper(this.mapperInterface);生成Mapper接口的代理对象，将Mapper接口的代理对象注册进Spring容器

```
//继承SqlSessionDaoSupport、实现FactoryBean，那么最终注入Spring容器的对象要从getObject()中取
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {
    private Class<T> mapperInterface;
    private boolean addToConfig = true;

    public MapperFactoryBean() {
    }

    //构造器，我们上一节中在BeanDefinition中已经设置了构造器输入参数
    //所以在通过反射调用构造器实例化时，会获取在BeanDefinition设置的构造器输入参数
    //也就是对应得每个Mapper接口Class
    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    protected void checkDaoConfig() {
        super.checkDaoConfig();
        Assert.notNull(this.mapperInterface, "Property 'mapperInterface' is required");
        Configuration configuration = this.getSqlSession().getConfiguration();
        if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
            try {
                configuration.addMapper(this.mapperInterface);
            } catch (Exception var6) {
                this.logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", var6);
                throw new IllegalArgumentException(var6);
            } finally {
                ErrorContext.instance().reset();
            }
        }

    }
    //最终注入Spring容器的就是这里的返回对象
    public T getObject() throws Exception {
        //获取父类setSqlSessionFactory方法中创建的SqlSessionTemplate
        //通过SqlSessionTemplate获取mapperInterface的代理类
        //我们例子中就是通过SqlSessionTemplate获取com.chenhao.mapper.UserMapper的代理类
        //获取到Mapper接口的代理类后，就把这个Mapper的代理类对象注入Spring容器
        return this.getSqlSession().getMapper(this.mapperInterface);
    }

    public Class<T> getObjectType() {
        return this.mapperInterface;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return this.mapperInterface;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public boolean isAddToConfig() {
        return this.addToConfig;
    }
}


public abstract class SqlSessionDaoSupport extends DaoSupport {
    private SqlSession sqlSession;
    private boolean externalSqlSession;

    public SqlSessionDaoSupport() {
    }
    //还记得上一节中我们往BeanDefinition中设置的sqlSessionFactory这个属性吗？
    //在实例化MapperFactoryBean后，进行属性赋值时，就会通过反射调用setSqlSessionFactory
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (!this.externalSqlSession) {
            //创建一个SqlSessionTemplate并赋值给sqlSession
            this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSession = sqlSessionTemplate;
        this.externalSqlSession = true;
    }

    public SqlSession getSqlSession() {
        return this.sqlSession;
    }

    protected void checkDaoConfig() {
        Assert.notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
    }
}
```

###### 一句话总结

通过MapperScannerConfigurer将Mapper接口生成代理注入到Spring。

在Mybaits本身的代理基础之上，Mybaits是通过动态代理，而spring是通过修改BeanDefinition。

这一步是吧手动通过sqlsession去获取Mapper的过程给代理了。

spring通过@MapperScan注解导入了一个ImportBeanDefinitionRegister的实现类，实现类通过ClassPathMapperScanner这个类扫描获取Mapper，并修改BeanDefinition，使其变成了MapperFactoryBean，并设置了sqlSessionFactory属性和构造参数className的值。这样调用MapperFactoryBean的getObject方法返回Mapper的代理对象。











