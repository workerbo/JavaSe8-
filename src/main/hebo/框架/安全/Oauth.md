OAuth 是一个认证授权开放标准，允许用户让第三方应用访问该用户在某一网站上存储的私密的资源（如照片，视频，联系人列表），而不需要将用户名和密码提供给第三方应用。OAuth允许用户提供一个令牌，而不是用户名和密码来访问他们存放在特定服务提供者的数据。每一个令牌授权一个特定的网站在特定的时段内访问特定的资源。

OAuth 2.0定义了四种授权方式。

- 授权码模式（authorization code）【将获取授权码后请求token放在服务器上】
- 简化模式（implicit）
- 密码模式（resource owner password credentials）
- 客户端模式（client credentials）



（1） **Third-party application**：第三方应用程序，本文中又称"客户端"（client），即上一节例子中的"云冲印"。

（2）**HTTP service**：HTTP服务提供商，本文中简称"服务提供商"，即上一节例子中的Google。

（3）**Resource Owner**：资源所有者，本文中又称"用户"（user）。

（4）**User Agent**：用户代理，本文中就是指浏览器。

（5）**Authorization server**：认证服务器，即服务提供商专门用来处理认证的服务器。

（6）**Resource server**：资源服务器，即服务提供商存放用户生成的资源的服务器。它与认证服务器，可以是同一台服务器，也可以是不同的服务器。



HTTP/1.1 使用的认证方式有

　　1）BASIC 认证（基本认证）；

```
       String auth = CLIENT_ID + ":" + CLIENT_SECRET;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
		String authHeader = "Basic " + new String(encodedAuth);
```

　　2）DIGEST 认证（摘要认证）；

　　3）SSL 客户端认证；

　　4）FormBase 认证（基于表单认证）；





### Access Token 类型

Token的类型可分为两种:

1. `bearer`. 包含一个简单的Token字符串.
2. `mac`. 由消息授权码(Message Authentication Code)和Token组成.

示例:



```objectivec
// bearer
GET /resource/1 HTTP/1.1
Host: example.com
Authorization: Bearer mF_9.B5f-4.1JqM

// mac     
GET /resource/1 HTTP/1.1
Host: example.com
Authorization: MAC id="h480djs93hd8",
                   nonce="274312:dj83hs9s",
               mac="kDZvddkndxvhGRXZhvuDjEhGeE="
```

### 2.2 认证请求方式

使用Token的认证请求的方式有三种,客户端可以选择一种来实现,但是不能同时使用多种:

1. 放在请求头
2. 放在请求体
3. 放在URI

详细如下:

#### 2.2.1 放在请求头

放在Header的`Authorization`中,并使用`Bearer`开头:



```undefined
GET /resource HTTP/1.1
Host: server.example.com
Authorization: Bearer mF_9.B5f-4.1JqM
```

#### 2.2.2 放在请求体

放在body中的`access_token`参数中,并且满足以下条件:

1. HTTP请求头的`Content-Type`设置成`application/x-www-form-urlencoded`.
2. Body参数是`single-part`.
3. HTTP请求方法应该是推荐可以携带Body参数的方法,比如`POST`,不推荐`GET`.

示例:



```dart
POST /resource HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded

access_token=mF_9.B5f-4.1JqM
```

#### 2.2.3 放在URI

放在uri中的`access_token`参数中



```undefined
GET /resource?access_token=mF_9.B5f-4.1JqM
Host: server.example.com
```



OAuth规定授权流程,Token为其中一环的一个信息载体,具体的一种实现方式由JWT规定.





[四种授权模式](https://www.cnblogs.com/wudimanong/p/10821215.html)





session、cookie的问题

1.**cookie的作用域是domain本身以及domain下的所有子域名。**

2.session是有状态的。

- 服务端保存大量数据，增加服务端压力
- 服务端保存用户状态，无法进行水平扩展
- 客户端请求依赖服务端，多次请求必须访问同一台服务器

​      nginx哈希一致性，session共享，无状态登陆token【JWT加上非对称加密】





SSO是Single Sign On的缩写，OAuth是Open Authority的缩写，这两者都是使用令牌的方式来代替用户密码访问应用。 流程上来说他们非常相似，但概念上又十分不同。

 SSO大家应该比较熟悉，它将登录认证和业务系统分离，使用独立的登录中心，实现了在登录中心登录后，所有相关的业务系统都能免登录访问资源。

 OAuth2.0原理可能比较陌生，但平时用的却很多，比如访问某网站想留言又不想注册时使用了微信授权。【获取第三方资源】

以上两者，你在业务系统中都没有账号和密码，账号密码是存放在登录中心或微信服务器中的，这就是所谓的使用令牌代替账号密码访问应用。

SSO核心：1.一个全局的认证登陆系统，其他系统检测到未登录的时候带上回调URL去登陆中心，第一次登陆后将sso-token放在cookie中，放在缓存中，值为用户信息。同时根据返回的sso-token当前业务系统会请求登陆中心保存会话信息。其他系统登陆重定向到登陆中心会携带sso-token【单点认证中心是同一路径】。