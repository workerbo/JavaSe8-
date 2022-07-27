目的是登录后实现返回 `token`

##### 首先创建一个JwtUser实现UserDetails

这个是 `Spring Security` 给我们提供的一个简单的接口，因为我们需要通过 `SecurityContextHolder` 去取得用户凭证等等信息，因为这个比较简单，所以我们实际业务要来加上我们所需要的信息。

```


```

##### 从持久层查询用户

public class JwtUserDetailsService implements UserDetailsService 



#### token拦截器

拦截器主要做了这么几件事：

1.从请求头里面获取token 2.解析token里面存放的用户信息 3.用户信息不为空，且当前请求SecurityContextHolder(默认的实现是ThreadLocal)中的用户信息为空，就设置进去。 3.1用redis标记了token是否是用户手动过期掉的，因为token本身存放了过期时间 无法修改。 3.2根据3中简要的用户信息查询全部用户信息，包括角色，菜单等。如果你足够信任token，也可以省略这里查询数据库。




##### 编写一个 [工具](https://www.codercto.com/tool.html) 类来生成令牌等

