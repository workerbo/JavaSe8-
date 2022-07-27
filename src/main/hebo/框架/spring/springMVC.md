[springMVC参考](https://www.jianshu.com/p/5de7475a646b)

```  
mvc:resources  处理静态资源
mvc:view-controller  视图控制器
```

早期的Spring MVC不能很好地处理静态资源，所以在web.xml中配置DispatcherServlet的请求映射，往往使用 *.do 、 *.xhtml等方式。【避免处理以html结尾的静态页面和资源】（不太聪明，不符合Restful风格）

将DispatcherServlet的请求映射配置为"/"的前提，将静态资源的请求转由Web容器处理或者自己处理。

1.在springMVC-servlet.xml中配置<mvc:default-servlet-handler />后，会在Spring MVC上下文中定义一个org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler，它会像一个检查员，对进入DispatcherServlet的URL进行筛查，如果发现是静态资源的请求，就将该请求转由Web应用服务器默认的Servlet处理

2.<mvc:resources />允许静态资源放在任何地方，如**WEB-INF**目录下、类路径下等，你甚至可以将JavaScript等静态文件打到JAR包中。通过location属性指定静态资源的位置，由于location属性是Resources类型，因此可以使用诸如"classpath:"等的资源前缀指定资源位置。传统Web容器的静态资源只能放在Web容器的根路径下，<mvc:resources />完全打破了这个限制。

servlet的url-pattern[匹配规则](https://www.cnblogs.com/canger/p/6084846.html)

1 精确匹配

2 路径匹配【以“/”字符开头，并以“/*”结尾的字符串用于路径匹配】

3 扩展名匹配、以“*.”开头的字符串被用于扩展名匹配

4 缺省匹配



如果参数时放在请求体中，application/json【非表单形式】传入后台的话，那么后台要用@RequestBody才能接收到。

在后端的同一个接收方法里，@RequestBody与@RequestParam【URL上的kv对，可以省略注解】可以同时使用

@RestController替代@Controller和@ResponseBody



