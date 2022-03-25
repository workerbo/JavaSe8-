###### 

**BeanFactoryAware**：Spring框架提供了一个BeanFactoryAware接口，容器在实例化实现了该接口的bean定义的过程 中，会自动将容器本身注入该bean。这样，该bean就持有了它所处的BeanFactory的引用。

###### IOC容器概念和实现

ApplicationContext所管理 的对象，在该类型容器启动之后，默认全部初始化并绑定完成。所以，相对于BeanFactory来 说，ApplicationContext要求更多的系统资源，同时，在启动时就完成所有初始化【BeanFactory不同之处】。

BeanFactory可以完成作为IoC Service Provider的所有职责，包括业务对象的注册和对象间依赖关系的 绑定。

BeanFactory接口只定义如何访问容 器内管理的Bean的方法，各个BeanFactory的具体实现类负责具体Bean的注册以及管理工作。 BeanDefinitionRegistry接口定义抽象了Bean的注册逻辑。

需要根据不同的外部配置文件格式，给出相应的BeanDefinitionReader实现类，由BeanDefinitionReader的相应实 现类负责将相应的配置文件Configuration Metadata内容读取并映射到BeanDefinition

这印证了之前在 比较构造方法注入和setter方法注入方式不同时提到的差异，即构造方法注入无法通过参数名 称来标识注入的确切位置，而setter方法注入则可以通过属性名称来明确标识注入

##### IOC容器

容器启动阶段和Bean实例化阶段。 Spring的IoC容器在实现的时候，充分运用了这两个实现阶段的不同特点，在每个阶段都加入了相应的容器扩展点，以便我们可以根据具体场景的需要加入自定义的扩展逻辑。

###### 容器启动阶段

除了代码方式比较直接，在大部分情况下，容器需要依赖某些工具类（BeanDefinitionReader）对加载的Configuration MetaData 进行解析和分析，并将分析后的信息编组为相应的BeanDefinition，最后把这些保存了bean定义必要信息的BeanDefinition，注册到相应的BeanDefinitionRegistry。

Spring提供了一种叫做BeanFactoryPostProcessor的容器扩展机制。该机制允许我们在容器实 例化相应对象之前，对注册到容器的BeanDefinition所保存的信息做相应的修改。第一阶段最后一道工序。

其中，org.springframework.beans. factory.config.PropertyPlaceholderConfigurer和org.springframework.beans.factory. config.Property OverrideConfigurer是两个比较常用的BeanFactoryPostProcessor。

Spring内部通过JavaBean的PropertyEditor【BeanFactoryPostProcessor】来帮助进行String类型到其他类型的转换工作。我们通过CustomEditorConfigurer将刚实现的DatePropertyEditor注册到容器，以告知容器按照DatePropertyEditor的形式进行String到java.util. Date BeanFactory 类型的转换工作。【给容器注入后面所需要的信息】



###### 实例化阶段

org.springframework.beans.factory.support.AbstractBeanFactory类的代码中查看到getBean()方法的完整实现逻辑，可以在其子类org.springframework.beans. factory.support.AbstractAutowireCapableBeanFactory的代码中一窥createBean()方法的全貌。

容器在内部实现的时候，采用“策略模式（Strategy Pattern）”来决定采用何种方式初始化bean实例。

以BeanWrapper对构造完成的对象实例 进行包裹，返回相应的BeanWrapper实例。

BeanWrapper定义继承了org.springframework.beans.PropertyAccessor接口，可以以统一的 方式对对象属性进行访问；BeanWrapper定义同时又直接或者间接继承了PropertyEditorRegistry 和TypeConverter接口。

CglibSubclassingInstantiationStrategy继承了SimpleInstantiationStrategy的以反射方式实例化对象的功能，并且通过CGLIB 的动态字节码生成功能，该策略实现类可以动态生成某个类的子类，进而满足了方法注入所需的对象实例化需求。【bean的实例化和DI】

比较常见的使用BeanPostProcessor的场景，是处理标记接口实现类，或者为当前对象提供代理实现。ApplicationContext对应的那些Aware接口实际上就是通过BeanPostProcessor的方式进行处理的。【AWARE和BeanPostProcessor】

只有该对象实例不再被使用的时候， 才会执行相关的自定义销毁逻辑，此时通常也就是Spring容器关闭的时候。但Spring容器在关闭之前， 不会聪明到自动调用这些回调方法。所以，需要我们告知容器，在哪个时间点来执行对象的自定义销 毁方法。

但AbstractApplicationContext为我们 提供了registerShutdownHook()方法，该方法底层使用标准的Runtime类的addShutdownHook()方 式来调用相应bean对象的销毁逻辑



###### ApplicationContext

![image-20201210094741280](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201210094741280.png)

ApplicationContext除了拥有 BeanFactory支持的所有功能之外，还进一步扩展了基本容器的功能，包括BeanFactoryPostProcessor、BeanPostProcessor以及其他特殊类型bean的自动识别、容器启动后bean实例的自动初始化、 国际化的信息支持、容器内事件发布【继承了对应的接口】等。[spring提供了三个默认实现]

Spring提出了一套基于org.springframework.core.io.Resource和 org.springframework.core.io.ResourceLoader接口的资源抽象和加载策略。【相比与javase的这一套】

通过结合ResourceBundle和Locale，我们就能够实现应用程序的国际化信息支持。Spring在Java SE的国际化支持的基础上，进一步抽象了国际化信息的访问接口，也就是 org.springframework.context.MessageSource，

在独立 运行的应用程序（Standalone Application）中，就如我们上面这些应用场景所展示的那样，直接使用 MessageSource的相应实现类就行了。不过在Web应用程序中，通常会公开ApplicationContext给 视图（View）层，这样，通过标签（tag）就可以直接访问国际化信息了。

javase事件

给出自定义事件类型（define your own event object）

实现针对自定义事件类的事件监听器接口（define custom event listener）

组合事件类和监听器，发布事件。

一旦容器内发布ApplicationEvent及其子类型的事件， 注册到容器的ApplicationListener就会对这些事件进行处理。

在Bean中注入ApplicationContext，然后委托给ApplicationContext发布事件。



###### 面向注解的配置信息

依赖注入方式【构造器、方法、接口】  配置载体【xml+注解、javaconfig+注解】   简化操作【组件扫描（bean定义）、自动类型配置（bean依赖关系）】

**bean定义**：classpath-scanning功能可以从某一顶层 包（base package）开始扫描。当扫描到某个类标注了相应的注解之后，就会提取该类的相关信息，构 建对应的BeanDefinition，然后把构建完的BeanDefinition注册到容器。

对于第三方提供的类库，肯定没法给其中的相关类标注@Component之类的注解。【xml或者javaconfig】

**bean依赖关系**：为了给容器中定义的每个bean定义对应的实例注入依赖，可以遍历它们，然后通过反射，检查每 个bean定义对应的类上各种可能位置上的@Autowired。如果存在的话，就可以从当前容器管理的对象 中获取符合条件的对象，设置给@Autowired所标注的属性域、构造方法或者方法定义。[Spring 的IoC容器使用的BeanPostProcessor自定义实现，使用< context:annotation-config/>激活注解的相关功能]

