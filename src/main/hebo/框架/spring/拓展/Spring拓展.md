## Spring 扩展(钩子)

1. BeanFactoryPostProcessor：beanFactory后置处理器，的拦截时机：所有Bean的定义信息正在加载到容器，但还没有被实例化。可以对beanFactory进行一些操作。

   		Spring容器初始化两大步
      				1、加载保存和读取所有bean配置
      				2、按照之前的配置创建bean

2. BeanPostProcessor：bean后置处理器，拦截时机：bean创建对象初始化前后进行拦截工作。可以对每一个Bean进行一些操作。

3. BeanDefinitionRegistryPostProcessor：是BeanFactoryPostProcessor的子接口，拦截时机：所有Bean的定义信息已经加载到容器，但还没有被实例化，可以对每一个Bean的BeanDefinition进行一些操作。【动态修改beadDefinition】

4. ApplicationListener,自定义ApplicationListener实现类并加入到容器中,可以监听spring容器中发布的事件。spring在创建容器的时候（finishRefresh（）方法）会发布ContextRefreshedEvent事件，关闭的时候（doClose()）会发布ContextClosedEvent事件。也可以通过spring容器的publishEvent发布自己的事件。

   1. 事件发布流程：publishEvent方法
      1. 获取事件的多播器，getApplicationEventMulticaster()。
      2. 调用multicastEvent(applicationEvent, eventType)派发事件。获取到所有的ApplicationListener,即getApplicationListeners()，然后同步或者异步的方式执行监听器的onApplicationEvent。
   2. 事件的多播器的初始化中（initApplicationEventMulticaster（）），如果容器中没有配置applicationEventMulticaster，就使用SimpleApplicationEventMulticaster。然后获取所有的监听器，并把它们注册到SimpleApplicationEventMulticaster中。

5. ###### SmartInitializingSingleton

     @EventListener(class={})：在普通的业务逻辑的方法上监听事件特定的事件。原理：EventListenerMethodProcessor是一个SmartInitializingSingleton，当所有的单例bean都初始化完以后， 容器会回调该接口的方法afterSingletonsInstantiated(),该方法里会遍历容器中所有的bean，并判断每一个bean里是否带有@EventListener注解的Method，然后创建ApplicationListenerMethodAdapter存储并包装该Method，最后将ApplicationListenerMethodAdapter添加到spring容器中。

6. ###### MergedBeanDefinitionPostProcessor、DestructionAwareBeanPostProcessor

     ##### CommonAnnotationBeanPostProcessor

     - 实现了MergedBeanDefinitionPostProcessor.postProcessMergedBeanDefinition()方法缓存初始化后和销毁前执行的方法。
     - 实现了BeanPostProcessor.postProcessBeforeInitialization()用来执行@PostConstruct标注的方法。
     - 实现了DestructionAwareBeanPostProcessor.postProcessBeforeDestruction()用来执行@PreDestroy标注的方法。

     实例化一个bean后此时还未进行依赖注入，beandefinition会被MergedBeanDefinitionPostProcessor的postProcessMergedBeanDefinition()方法执行一遍用来获取一些元数据来增加额外的功能，例如InitDestroyAnnotationBeanPostProcessor【即CommonAnnotationBeanPostProcessor】就是将bean定义了被@PostConstruct和@PreDestroy注解的方法缓存到一个Map中。

     ​           要分析了``applyMergedBeanDefinitionPostProcessors``这段代码的作用，它的执行时机是在创建对象之后，属性注入之前。AutowiredAnnotationBeanPostProcessor找到所有的注入点，其实就是被@Autowired注解修饰的方法以及字段

7. ###### SmartInstantiationAwareBeanPostProcessor、InstantiationAwareBeanPostProcessor

     

     ```
     public` `interface` `InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
       ``//这个方法用来在对象实例化前直接返回一个对象（如代理对象）来代替通过内置的实例化流程创建对象；
       ``@Nullable
       ``default` `Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
         ``return` `null``;
       ``}
       ``//在对象实例化完毕执行populateBean之前 如果返回false则spring不再对对应的bean实例进行自动依赖注入。
       ``default` `boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
         ``return` `true``;
       ``}
       ``//这里是在spring处理完默认的成员属性，应用到指定的bean之前进行回调，可以用来检查和修改属性，最终返回的PropertyValues会应用到bean中
       ``//@Autowired、@Resource等就是根据这个回调来实现最终注入依赖的属性的。
       ``@Nullable
       ``default` `PropertyValues postProcessPropertyValues(
           ``PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
         ``return` `pvs;
       ``}
     }
     ```

     populateBean为bean实例赋值。执行InstantiationAwareBeanPostProcessor的postProcessAfterInstantiation和postProcessPropertyValues方法。@Autowire由AutowiredAnnotationBeanPostProcessor完成
     AutowiredAnnotationBeanPostProcessor执行过程：

     1. postProcessAfterInstantiation方法执行，直接return null。
     2. postProcessPropertyValues方法执行，主要逻辑在此处理。待补充。。。。。

     ​       AnnotationAwareAspectJAutoProxyCreator实现了SmartInstantiationAwareBeanPostProcessor

     

     

8. ###### DisposableBean、initializeBean

9. ###### LifeCycel





#### 		ApplicationListener

第一步，写一个监听器来监听某个事件。把监听器加入到容器中，这样Spring才能知道有这样一个监听器。

第二步，当然了，监听的这个事件必须是ApplicationEvent及其子类。

第三步，只要容器中有相关事件发布，通过多播器获取到相应事件类型的监听器进行回调【applicationContext.publishEvent()】

**不管是容器发布的事件，还是咱们自己发布的事件，都会走以上这个事件发布流程，即先拿到事件多播器，然后再拿到所有的监听器，接着再挨个回调它的方法**。

**在整个事件派发的过程中，我们可以自定义事件多播器**。没有就新new 一个。

```
initApplicationEventMulticaster()  
registerListeners    // Check for listener beans and register them.

```

使用@EventListener注解，我们就可以让任意方法都能监听事件，而不是让它去实现ApplicationListener这个接口。

**Spring会使用EventListenerMethodProcessor这个处理器来解析方法上的@EventListener注解**。

EventListenerMethodProcessor实现了一个接口，叫SmartInitializingSingleton,preInstantiateSingletons**获取所有创建好的单实例bean，然后判断每一个bean对象是否是SmartInitializingSingleton这个接口类型的，如果是，那么便调用它里面的afterSingletonsInstantiated方法**

遍历容器中的bean找到有@EventListener注解的方法拿到Method对象和对应bean的beanName,将两个参数封装到ApplicationListener的实现类ApplicationListenerMethodAdapter里,并注册到容器中,事件发布时通过Method和beanName反射执行@EventListener注解的方法