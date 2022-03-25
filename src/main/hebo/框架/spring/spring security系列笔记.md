AuthenticationManager接口的常用实现类ProviderManager 内部会维护一个List<AuthenticationProvider>列表，存放多种认证方式

在Spring Security中。提交的用户名和密码，被封装成了UsernamePasswordAuthenticationToken，而根据用户名加载用户的任务则是交给了UserDetailsService

Authentication的getCredentials()与UserDetails中的getPassword()需要被区分对待，前者是用户提交的密码凭证，后者是用户正确的密码，认证器其实就是对这两者的比对、

![image-20210812140213736](../../../../../../../../Programfile/Typora/upload/image-20210812140213736.png)