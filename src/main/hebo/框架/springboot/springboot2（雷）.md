https://www.yuque.com/atguigu/springboot



```
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {}
```

Spring Boot在启动的时候从类路径下的META-INF/spring.factories中获取EnableAutoConfiguration指定的值，将 这些值作为自动配置类导入到容器中，自动配置类就生效，帮我们进行自动配置工作；



@AutoConfigurationPackage

Spring 依据 ComponentScan 指定的 package 进行组件扫描；而 AutoConfigurationPackage 更多的是提供了一个工具或接口，我们可以通过它获取自动配置的package列表。@AutoConfigurationPackage 把启动类所在的包又设置一次，是为了给各种自动配置的第三方库扫描用的。比如带@Mapper注解的类，Spring自身其实是不认识的，但自动配置的Mybatis需要扫描用到。



```。
AutoConfigurationPackages.get(AutoConfigurationPackages.BEAN)获取列表List<String>

```



**@PropertySource&@ImportResource&@Bean** 

@**PropertySource**：加载指定的配置文件； 

@**ImportResource**：导入Spring的配置文件，让配置文件里面的内容生效； 



@**ConfifigurationProperties**支持JSR303校验



###### xxxxAutoConfifigurartion：自动配置类； 

**1****）、****SpringBoot****启动会加载大量的自动配置类** 

**2****）、我们看我们需要的功能有没有****SpringBoot****默认写好的自动配置类；** 

**3****）、我们再来看这个自动配置类中到底配置了哪些组件；（只要我们要用的组件有，我们就不需要再来配置了）** 

**4****）、给容器中自动配置类添加组件的时候，会从****properties****类中获取某些属性。我们就可以在配置文件中指定这** 些属性的值；** 

**@Conditional****派生注解**



**自动配置类必须在一定的条件下才能生效**，我们可以通过启用** **debug=true****属性；来让控制台打印自动配置报告**

SpringBoot推荐给容器中添加组件的方式；推荐使用全注解的方式 1、配置类@Configuration------>Spring配置文件 2、使用@Bean给容器中添加组件







