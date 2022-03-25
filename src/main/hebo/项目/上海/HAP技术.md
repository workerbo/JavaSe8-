#### 安全

```

标准登录的权限投票器
PermissionVoter:通过当前用户拥有角色的资源和请求的资源比较确定
<!-- 认证管理器,确定用户,角色及相应的权限 -->
<beans:bean id="accessDecisionManager" class="org.springframework.security.access.vote.UnanimousBased">
    <!-- 投票器 -->
    <beans:constructor-arg>
        <beans:list>
            <beans:bean class="com.hand.hap.security.CustomWebExpressionVoter"/>
            <beans:bean class="org.springframework.security.access.vote.RoleVoter"/>
            <beans:bean class="org.springframework.security.access.vote.AuthenticatedVoter"/>
            <beans:bean class="com.hand.hap.security.PermissionVoter"/>
        </beans:list>
    </beans:constructor-arg>
</beans:bean>


oauth
/api/*  对外提供的url【需要认证】
/api/public/  对外提供的URL【不需要认证】
获取到token之后URL加access_token访问

客制化session 策略，集成spring session存储到redis当中
```

#### 缓存

1. 使用spring  session存储session

2. 简易操作值  redisTemplate.opsForValue()   例如缓存其他系统的token令牌。

3. 缓存在启动时通过监听器加载：例如加载常用的基础数据。
4. 主从模式，持久化
5. 

#### SQL

MySQL的安装和目录结构

主从配置：分库

#### Mybatis

通用Mapper

Mybaits-Plus

分页插件

#### Acitivti

监听器

#### java基础

集合



#### spring

事务

统一异常处理和返回

监听器

#### 定时任务

Quartz实现定时任务组件

#### 消息队列



#### 日志



#### KendoUI





