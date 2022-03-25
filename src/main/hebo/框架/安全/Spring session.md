```
<filter>
    <filter-name>spring-session</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    <async-supported>true</async-supported>
    <init-param>
        <param-name>targetBeanName</param-name>
        <param-value>springSession</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>spring-session</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

```
<!-- 这个是Session策略过滤器，即将容器原有的Session持久化机制，代替为Spring的 Redis持久化Session机制。 -->
<!-- 注意，这个名字与 web.xml里的targetBean的下value是要一致的。 -->
<bean name="springSession" class="org.springframework.session.web.http.SessionRepositoryFilter">
    <constructor-arg ref="redisOperationsSessionRepository"/>
    <property name="httpSessionStrategy" ref="cookieHttpSessionStrategy"/>
</bean>

SessionRepository
   <bean name="redisOperationsSessionRepository"
          class="org.springframework.session.data.redis.RedisOperationsSessionRepository">
        <constructor-arg ref="v2redisConnectionFactory"/>
        <property name="defaultMaxInactiveInterval" value="${session.expire.time:3600}"/>
    </bean>
```