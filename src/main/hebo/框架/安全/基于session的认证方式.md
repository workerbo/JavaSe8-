用户认证通过以后，在服务端生成用户相关的数据保存在当前会话`（Session）`中，发给客户端的数据将通过`session_id`存放在`cookie`中。在后续的请求操作中，客户端将带上`session_id`，服务端就可以验证是否存在了，并可拿到其中的数据校验其合法性。当用户退出系统或`session_id`到期时，服务端则会销毁`session_id`

**UsernamePasswordAuthenticationFilter** 这个会重点分析，表单提交了username和password，被封装成token进行一系列的认证，便是主要通过这个过滤器完成的，在表单认证的方法中，这是最最关键的过滤器。【特定在登陆请求才会走这个过滤器的逻辑！】