AspectJ 是一个基于 Java 语言的全功能的 AOP 框架，它并不是 Spring 组成部分，是一款独立的 AOP 框架。

但由于 AspectJ 支持通过 Spring 配置 AspectJ 切面，因此它是 Spring AOP 的完美补充，通常情况下，我们都是将 AspectJ 和 Spirng 框架一起使用，简化 AOP 操作。

使用 AspectJ 需要在 Spring 项目中导入 Spring AOP 和 AspectJ 相关 Jar 包。

- spring-aop-xxx.jar
- spring-aspects-xxx.jar
- aspectjweaver-xxxx.jar


在以上 3 个 Jar 包中，spring-aop-xxx.jar 和 spring-aspects-xxx.jar 为 Spring 框架提供的 Jar 包，而 aspectjweaver-xxxx.jar 则是 AspectJ 提供的。

AspectJ 框架为 AOP 开发提供了一套 @AspectJ 注解。它允许我们直接在 Java 类中通过注解的方式对切面（Aspect）、切入点（Pointcut）和增强（Advice）进行定义，Spring 框架可以根据这些注解生成 AOP 代理。

曾经以为AspectJ是Spring **AOP**一部分，是因为Spring AOP使用了AspectJ的Annotation。使用了Aspect来定义切面,使用Pointcut来定义切入点，使用Advice来定义增强处理。虽然使用了Aspect的Annotation，**但是并没有使用它的编译器和织入器**。其实现原理是JDK 动态代理，在运行时生成代理类。为了启用 Spring 对 @AspectJ 方面配置的支持，并保证 Spring [容器](https://cloud.tencent.com/product/tke?from=10680)中的目标 Bean 被一个或多个方面自动增强，必须在 Spring 配置文件中添加如下配置。@EnableAspectJAutoProxy

一般来说，编译时增强的 AOP 框架在性能上更有优势——因为运行时动态增强的 AOP 框架需要每次运行时都进行动态增强。

AOP 代理 = 原来的业务类+增强处理。这个生成**AOP** 代理由 Spring 的 IoC 容器负责生成。也由 IoC 容器负责管理。

**CGLIB**

CGLIB（Code Generation Library）它是一个代码生成类库。它可以在运行时候动态是生成某个类的子类。代理模式为要访问的目标对象提供了一种途径，当访问对象时，它引入了一个间接的层。JDK自从1.3版本开始，就引入了动态代理，并且经常被用来动态地创建代理。JDK的动态代理用起来非常简单，唯一限制便是使用动态代理的对象必须实现一个或多个接口。而CGLIB缺不必有此限制。要想Spring AOP 通过CGLIB生成代理，只需要在Spring 的配置文件引入

```javascript
<aop:aspectj-autoproxy proxy-target-class="true"/>
```