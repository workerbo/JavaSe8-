@SpringBootApplication来标注一个主程序启动类，说明这是一个Spring Boot应用

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
        @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
...
}
```



```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
  String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
  
  Class<?>[] exclude() default {};
  
  String[] excludeName() default {};
}
```

个人感觉@EnableAutoConfiguration这个Annotation最为重要它的作用可以概括为：借助@Import的帮助，将所有符合自动配置条件的bean定义加载到IoC容器。

从META-INF/spring.factories位置来加载一个文件。默认扫描我们当前系统里面所有META-INF/spring.factories位置的文件spring-boot-autoconfigure-2.3.8.RELEASE.jar包里面也有META-INF/spring.factories。以EnableAutoConfiguration的包名为key，配置类包名为value，逗号隔开
