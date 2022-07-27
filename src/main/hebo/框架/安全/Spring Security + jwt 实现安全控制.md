### 用户登录的逻辑和jwt

用户到底是怎么登录的？ 这个问题对于初级工程师来说会很迷惑，曾经也经历过。所以简单说明下。在一般的web软件开发中，开发者不需要关注会话这件事情，因为tomcat容器自动帮我们管理的会话session，他的流程是这样的，用户访问服务，服务端生成session会话，并且把sessionId回写到浏览期的cookie中，浏览器后面的每次请求就会携带上这个sessionId。服务端就能标识这个用户了，至于登陆鉴权的逻辑都是基于你能唯一标识当前的用户来做的。通用的做法是，用户成功登陆后，服务端会把用户信息存放在sessionId标识的session中。随着用户体量增多，在分布式的环境下一般的做法是session共享，或者采用redis接替tomcat管理session会话的方案。 为什么要用jwt？ 全程是json web token，关于jwt是什么，可以参考阮一峰的文章：[JSON Web Token 入门教程](https://link.juejin.cn?target=http%3A%2F%2Fwww.ruanyifeng.com%2Fblog%2F2018%2F07%2Fjson_web_token-tutorial.html)。使用了jwt后，我们完全把登陆信息存放在客户端，每次认证都是由客户端带着鉴权参数过来。具体的逻辑是服务端生成token，包含token有效期，存放的鉴权信息等，下发给客户端。客户端自放在本地。服务端就可以提供无状态的服务了，非常方便扩展。

接下来回顾一下整个会话管理流程：

- 客户端使用用户名和密码认证

- 服务端校验用户名和密码，下发access_token(2小时有效)和refresh_token(7天有效)

- 客户端带着access_token访问需要认证的资源，access_token有效，返回资源。

- access_token过期，返回和客户端约定的响应码，客户端带着refresh_token刷新access_token.

- refresh_token 有效，正常返回，refresh_token过期走重新登陆流程。

- 客户端使用新的 access_token 访问需要认证的接口

  