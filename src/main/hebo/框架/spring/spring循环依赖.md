### spring解决循环依赖

**以类A，B互相依赖注入为例**

1. 根据类A的名称先从singletonObjects获取Bean实例，发现获取不到，就通过doGetBean方法开始创建Bean的流程。
2. 根据A的名称找到对应的BeanDefinition，通过doCreateBean（）方法创建对象，先确定类A的构造函数，然后选择一个实例化策略去实例化类A。
3. 判断容器是否允许循环依赖，如果允许循环依赖，就创建一个ObjectFactory类并实现ObjectFactory接口的唯一的一个方法getObject（）用于返回类A。然后将该ObjectFactory添加到singletonFactories中。
4. 调用populateBean（）为类A进行属性赋值，发现需要依赖类B，此时类B尚未创建，启动创建类B的流程。
   1. 根据类B的名称先从singletonObjects获取Bean实例，发现获取不到，就开始通过doGetBean方法开始创建Bean的流程
   2. 找到类B对应的BeanDefinition，确认B的构造函数，然后实例化B。
   3. 判断容器是否允许循环依赖，创建一个ObjectFactory并实现getObject（）方法，用于返回类B，并添加到singletonFactories中。
   4. 调用populateBean（）为类B进行属性赋值，发现需要依赖类A，调用getSingleton方法获取A：A现在已存在于singletonFactories中，
   5. 调用getSingleton（）方法获取B：getSingleton将A从singletonFactories方法中移除并放入earlySingletonObjects中。
   6. 调用initializeBean初始化bean，最后将新创建出来的类B保存到singletonObjects中
5. 调用getSingleton（）方法获取A，这时A已在earlySingletonObjects中了，就直接返回A

   





### 循环依赖问题

##### 1、构造器注入循环依赖

 `根本原因`：Spring解决循环依赖依靠的是Bean的“中间态”这个概念，而这个中间态指的是`已经实例化`，但还没初始化的状态。而构造器是完成实例化的东东，所以构造器的循环依赖无法解决~~~

#### 2.`prototype` field属性注入循环依赖

  在启动不报错，但是一旦使用就会一直创建对象。

### 3.字段循环依赖

Spring通过三级缓存解决了循环依赖【】，其中一级缓存为单例池（singletonObjects）,二级缓存为早期曝光对象earlySingletonObjects，三级缓存为早期曝光对象工厂（singletonFactories）。当A、B两个类发生循环引用时，在A完成实例化后，就使用实例化后的对象去创建一个对象工厂【三级缓存】，如果A被AOP代理，那么通过这个工厂获取到的就是A代理后的对象，如果A没有被AOP代理，那么这个工厂获取到的就是A实例化的对象。

当A进行属性注入时，会去创建B，同时B又依赖了A，所以创建B的同时又会去调用getBean(a)来获取需要的依赖，此时的getBean(a)会从缓存中获取，第一步，先获取到三级缓存中的工厂；第二步，调用对象工工厂的getObject方法来获取到对应的对象，得到这个对象后将其注入到B中【删除三级缓存，进入二级缓存】。紧接着B会走完它的生命周期流程，包括初始化、后置处理器等。当B创建完后，会将B再注入到A中，此时A再完成它的整个生命周期。至此，循环依赖结束！

[三级缓存真的提高了效率了吗](https://www.cnblogs.com/daimzh/p/13256413.html#%E4%B8%89%E7%BA%A7%E7%BC%93%E5%AD%98%E7%9C%9F%E7%9A%84%E6%8F%90%E9%AB%98%E4%BA%86%E6%95%88%E7%8E%87%E4%BA%86%E5%90%97%EF%BC%9F)

设计原则是 bean 实例化、属性设置、初始化之后 再 生成aop对象，但是为了解决循环依赖但又尽量不打破这个设计原则的情况下

如果要使用二级缓存解决循环依赖，意味着所有Bean在实例化后就要完成AOP代理，这样违背了Spring设计的原则，Spring在设计之初就是通过`AnnotationAwareAspectJAutoProxyCreator`这个后置处理器来在Bean生命周期的最后一步来完成AOP代理，而不是在实例化后就立马进行AOP代理。

上面两个流程的唯一区别在于为A对象创建代理的时机不同，在使用了三级缓存的情况下为A创建代理的时机是在B中需要注入A的时候，而不使用三级缓存的话在A实例化后就需要马上为A创建代理然后放入到二级缓存中去。对于整个A、B的创建过程而言，消耗的时间是一样的【三级缓存获取的对象，如果是AOP方式，每次获取的都不一样。当然这个影响不大，如果是变为二级则执行一次缓存一个对象就可以了，主要还是要符合规范。】

**代理对象持有原生对象，属性设置和初始化都是设置在原生对象上。**

在循环依赖中如果提前实例化成代理对象，则原始对象的属性赋值，初始化还在后面。代理对象持有原始对象。