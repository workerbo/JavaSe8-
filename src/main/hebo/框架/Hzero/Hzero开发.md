#### 微服务组件



###### 快速参考

权限：https://open.hand-china.com/community/detail/625872021372407808#1.2%20%E8%A7%92%E8%89%B2%E6%9D%83%E9%99%90%E5%88%86%E9%85%8D



缓存：

通过继承SmartInitializingSingleton的类实现缓存在启动时加载。

##### hzero-admin

平台治理服务，作为基础服务之一，把路由、限流、熔断等功能易用化，集中在管理服务来管控，提供自动化的路由刷新、权限刷新、swagger信息刷新服务，提供界面化的服务、配置、路由、限流等功能。【原来网关的一部分功能？】

这里HZERO为用户做了三件事：

- hzero-admin自动监听服务注册

     容器启动的生命周期回调接口中，采用异步线程的方式循环。admin的客户端提供了自动注册，会调用注册端口【所以其他服务需要依赖admin的客户端】。

- hzero-admin服务通过远程调用通知hzero-iam服务完成权限刷新工作

- hzero-iam从新服务拉取权限接口信息，并保存到数据库和缓存



hadm.services-to-init为key的hash当中存储了待初始化的服务。通过ThreadLocal存储当前要操作的db。

两个异步线程通过锁机制和循环分别初始化和检查下线服务。

初始化：用构造器模式构造对象，用InitChainFactoryBean封装初始化器链，然后链式模式被调用。权限、路由、文档三个初始化器。

在容器的生命周期接口Lifecycle中被调用对其他服务初始化。





服务和路由信息处理：

ExtraDataProcessor后置处理器处理对@ChoerodonExtraData注解进行处理。存储了服务和路由信息。

问题是后面的初始化器ExtraDataInitialization的执行时间和注解信息被处理的时间不确定？



在不修改前端路由的情况下修改到后端的路由



##### hzero-iam

身份权限管理-多租户版本，是基于单租户版本的基础上，增加了多租户管理功能，大多功能与单租户版本相同，主要涵盖平台权限体系的用户、角色、菜单、权限、客户端配置等维护管理功能。





##### *hzero-oauth*

`hzero-oauth` 服务是基于 `Spring Security`、`Spring OAuth2`、`JWT` 实现的统一认证服务中心，登录基于 spring security 的标准登录流程，客户端授权支持 `oauth2.0` 的四种授权模式：`授权码模式`、`简化模式`、`密码模式`、`客户端模式`，授权流程跟标准的 oauth2 流程一致。web 端采用`简化模式(implicit)`登录系统，移动端可使用`密码模式(password)`登录系统 。并支持基于 `Spring Social` 的三方账号登录方式(如微信)。

##### hzero-platform

基础管理服务，主要涵盖系统基础设置，如：租户管理、配置管理等；开发管理，如：值集、个性化、编码规则、配置维护、数据源管理等

##### hzero-gateway

hzero-gateway 是基于开源 spring-cloud-gateway 实现的网关服务，所有外部的API访问都会通过网关服务路由到其它服务中。

hzero-gateway 集成了网关鉴权组件来对API统一鉴权，还增加了限流、动态路由、运维等功能。

hzero-gateway-helper 是网关鉴权插件，所有外部API调用都会经过网关进行路由转发，网关首先会调用鉴权组件对请求进行权限校验。

在通过一系列的权限校验后，会将用户信息转换成 Jwt_Token 返回给网关，网关会在请求头中携带 Jwt_Token 去访问目标服务

目标服务中会有一个过滤器来解析 Jwt_Token 转换成用户信息（CustomUserDetails），这样就可以在服务中获取到当前登录用户了。

##### hzero-config





启动类上的 `@EnableChoerodonResourceServer` 注解，该注解开启了 Jwt_Token 的校验

如果调用接口抛出 PERMISSION_MISSMATCH 异常，可在 [开发管理>系统工具] 下刷新服务权限

## API调用链路

① API调用

- 所有API调用都是进入网关，通过网关路由到正确的服务上。对外暴露端口时，只需暴露网关服务的端口即可。

② API鉴权

- 在网关服务内，所有API请求都会经过 `GateWayHelperFilter` 过滤器，在过滤器中，会调用 `hzero-gateway-helper` 鉴权组件对API及用户鉴权。
- 根过滤器 `HelperChain` 包含了一系列过滤器来对请求进行鉴权，将按图中的顺序进行校验。认证通过或不通过都会返回相应的状态码，红色部分表示认证失败，绿色部分表示认证通过，具体状态码的含义可参考最后 `鉴权常见状态码`。
- 一般服务出现 403、500 等问题时，可先查看返回错误码，或检查 hzero-gateway-helper 的日志，看是哪一步校验不通过，对症下药。

③ 获取登录用户

- `GetUserDetailsFilter` 默认会从 Redis 根据 `access_token` 来获取用户认证信息，获取不到再从 hzero-oauth 服务获取。如果不想从 redis 读取，可以通过配置关闭。
- `GetUserDetailsFilter` 会带着 `access_token` 访问 hzero-oauth 认证服务的 `/api/user` 接口获取用户信息。

④ 返回用户 Principal

- 用户登录后，oauth 服务将 access_token 和用户认证实体（Authentication）关系存储在 redis session 中，在调用 /api/user 时，将通过 access_token 获取 Authentication，并在接口中返回用户信息 `Principal`。
- 得到 Principal 后，转换成 `CustomUserDetails`。之后，在`AddJwtFilter`里将 UserDetails 转成 JwtToken。

⑤ 返回状态码及JwtToken

- 如果认证失败，将返回状态码，并在网关直接返回前端，不会再调用业务服务。
- 如果API要求用户登录，将返回用户的 JwtToken 信息

⑥ 路由到业务服务

- 鉴权通过后，网关将带着 JwtToken，根据路由信息，转发到对应的业务服务上。
- 在业务服务内部，首先 `JwtTokenFilter` 会将 JwtToken 转换成 UserDetails，并设置到 `SecurityContextHolder` 上下文中，相当于用户在此服务中已登录。之后的程序中就可以通过 `DetailsHelper.getUserDetails()` 得到当前登录用户信息。
- 若要开启此过滤器，需在启动类上配置 `@EnableChoerodonResourceServer` 注解开启此功能。

⑦ 返回数据

- 之后返回数据，经过网关，再返回到前端。如果服务响应比较慢，就需要调整网关的超时时间。

## 服务注册监听

服务启动时，服务会注册到注册中心以及 hzero-admin 服务，hzero-admin 服务监听到服务注册后，会拉取服务文档信息，并做如下事情：

- 解析服务路由信息，更新服务（`hadm_service`）及路由信息（`hadm_service_route`），并通知网关服务拉取最新的路由信息。
- 通知 hzero-iam 服务更新服务API权限（`iam_permission`），并刷新缓存中的服务权限
- 通知 hzero-swagger 服务更新 Swagger 文档信息（`hadm_swagger`）。

如果服务启动时，未及时更新权限或Swagger文档信息，由于有心跳机制，可等注册中心实例下线后再重启服务。





# 开发组件

### Mybaits增强

和Mybaits-plus的区别。





# 辅助开发核心包

服务路由的配置替代了网关？





#### 后端链路过程详解

#### feign组件

