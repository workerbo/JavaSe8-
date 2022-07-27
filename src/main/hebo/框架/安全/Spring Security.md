**Spring Security 的web基础是Filters。**

这句话展示了Spring Security的设计思想：**即通过一层层的Filters来对web请求做处理。**

放到真实的Spring Security中，用文字表述的话可以这样说:

**一个web请求会经过一条过滤器链，在经过过滤器链的过程中会完成认证与授权，如果中间发现这条请求未认证或者未授权，会根据被保护API的权限去抛出异常，然后由异常处理器去处理这些异常。**

构成Spring Security进行认证的流程：
\1. 先是一个请求带着身份信息进来
\2. 经过AuthenticationManager的认证，
\3. 再通过SecurityContextHolder获取SecurityContext，
\4. 最后将认证后的信息放入到SecurityContext。

###### 基础概念`

SecurityContextHolder`默认使用`ThreadLocal` 策略来存储认证信息。看到`ThreadLocal` 也就意味着，这是一种与线程绑定的策略。Spring Security在用户登录时自动绑定认证信息到当前线程，在用户退出时，自动清除当前线程的认证信息。

UserDetails便是Spring对身份信息封装的一个接口。

Authentication是spring security包中的接口，直接继承自Principal类，而Principal是位于`java.security`包中的。可以见得，Authentication在spring security中是最高级别的身份/认证的抽象。

- getAuthorities()，权限信息列表，默认是GrantedAuthority接口的一些实现类，通常是代表权限信息的一系列字符串。

- getCredentials()，密码信息，用户输入的密码字符串，在认证过后通常会被移除，用于保障安全。

- getDetails()，细节信息，web应用中的实现接口通常为 WebAuthenticationDetails，它记录了访问者的ip地址和sessionId的值。

- getPrincipal()，敲黑板！！！最重要的身份信息，大部分情况下返回的是UserDetails接口的实现类，也是框架中的常用接口之一。

   


###### Spring Security是如何完成身份认证的？

1 用户名和密码被过滤器【springSecurityFilterChain】获取到，封装成`Authentication`,通常情况下是`UsernamePasswordAuthenticationToken`这个实现类。

2 `AuthenticationManager` 身份管理器负责验证这个`Authentication`

3 认证成功后，`AuthenticationManager`身份管理器返回一个被填充满了信息的（包括上面提到的权限信息，身份信息，细节信息，但密码通常会被移除）`Authentication`实例。

4 `SecurityContextHolder`安全上下文容器将第3步填充了信息的`Authentication`，通过SecurityContextHolder.getContext().setAuthentication(…)方法，设置到其中。

###### 重要过滤器

- **SecurityContextPersistenceFilter** 两个主要职责：请求来临时，创建`SecurityContext`安全上下文信息，请求结束时清空`SecurityContextHolder`。
- CsrfFilter 在spring4这个版本中被默认开启的一个过滤器，用于防止csrf攻击，了解前后端分离的人一定不会对这个攻击方式感到陌生，前后端使用json交互需要注意的一个问题。
- **UsernamePasswordAuthenticationFilter** 这个会重点分析，表单提交了username和password，被封装成token进行一系列的认证，便是主要通过这个过滤器完成的，在表单认证的方法中，这是最最关键的过滤器。【特定在登陆请求才会走这个过滤器的逻辑！】
- **AnonymousAuthenticationFilter** 匿名身份过滤器，这个过滤器个人认为很重要，需要将它与UsernamePasswordAuthenticationFilter 放在一起比较理解，spring security为了兼容未登录的访问，也走了一套认证流程，只不过是一个匿名的身份。
- **ExceptionTranslationFilter** 直译成异常翻译过滤器，还是比较形象的，这个过滤器本身不处理异常，而是将认证过程中出现的异常交给内部维护的一些类去处理
- **FilterSecurityInterceptor** 这个过滤器决定了访问特定路径应该具备的权限，访问的用户的角色，权限是什么【鉴权】



###### 认证器

AuthenticationManager（接口）是认证相关的核心接口，也是发起认证的出发点，

AuthenticationManager接口的常用实现类`ProviderManager` 内部会维护一个`List`列表，存放多种认证方式，实际上这是委托者模式的应用（Delegate），在默认策略下，只需要通过一个AuthenticationProvider的认证，即可被认为是登录成功。





AuthenticationProvider最最最常用的一个实现便是DaoAuthenticationProvider：它获取用户提交的用户名和密码，比对其正确性，如果正确，返回一个数据库中的用户信息

UserDetailsService只负责从特定的地方（通常是数据库）加载用户信息，仅此而已



###### 权限管理的投票器与表决机制

在 Spring Security 中，投票器是由 AccessDecisionVoter 接口来规范的

vote 则是具体的投票方法。在不同的实现类中实现。三个参数，authentication 表示当前登录主体；object 是一个 ilterInvocation，里边封装了当前请求；attributes 表示当前所访问的接口所需要的角色集合【只需要有其中一个权限就好】。

三个决策器的区别如下：

1. AffirmativeBased：有一个投票器同意了，就通过。
   
2.  ConsensusBased：多数投票器同意就通过，平局的话，则看 allowIfEqualGrantedDeniedDecisions 参数的取值。
    
3. UnanimousBased 所有投票器都同意，请求才通过。






