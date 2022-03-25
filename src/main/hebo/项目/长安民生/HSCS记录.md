> ###### 基础信息

###### git&svn

gitlab 结算  https://rdc.hand-china.com/gitlab/cml/scs开发是基于dev-master分支

资金 https://rdc.hand-china.com/gitlab/cml/ceb开发是基于bug_fix分支

svn    zhuangwm/000000 svn服务器路径/opt/java/svn/repo

svn://192.168.89.194

启动：svnserve -d -r /opt/java/svn/repo

svn技术参考目录：二期技术  

###### vpn

1. 登录 https://vpn.cmal.cn下载新版VPN客户端（EasyConnect）。安装完成后填写服务器地址：https://vpn.cmal.cn，连接服务器后再填写账号密码。
2. 账号： wenwen.xiong@hand-china.com
   密码：ww=1118
   VPN账号有效期至 2021-11-21 。
3. 登录 https://vpn.cmal.cn下载VPN客户端（EasyConnect）。
   账号：bo.he03@hand-china.com
   密码：feg32
   有效期至 2021/8/14 。

###### 堡垒机

路径：https://172.16.23.21/ops/Login

堡垒机账号密码：pangz   密码：Cmal1q2w3e  初次使用下载运行环境检测和配置工具路径【Navicat和PLSQL的路径】。

heb  1234QWER



> ###### hscs基本信息

###### 账号密码

​    结算测试 https://192.168.89.194:8443/scs/ 账号/密码 admin Abcd12345678
​           结算正式 https://172.16.25.30:7979/ 账号 admin Cmal12345678

###### uat

uat远程连接 192.168.89.194  root/cwtest=123

uat  tomcat路径  /opt/prg/apache-tomcat-8.5.16

uat 测试数据库192.168.89.100  cwjs  cwtest=123

Jenkins  http://192.168.89.194:8080/jenkins/login?from=%2Fjenkins%2Fjob%2Fscs_dev%2F382%2Fconsole   admin  admin

###### prod

正式环境：负载均衡  172.16.25.30  172.16.25.32   172.16.25.31

账号：admin  密码：Cmal12345678

###### 数据库

用了两个数据源。具体定义参见/webapp/MEA-INF/context.xml 各自数据源的范围参见applicationContext-dataSource.xml



------

###### redis

结算使用了主从配置和哨兵

主：/usr/local/redis-master-6379

```
redis.sentinel=\
  172.16.25.32:26379
redis.useSentinel=true
redis.ip=172.16.25.32
```



> ###### 资金系统

资金正式 https://172.16.25.20:8443/login admin/Cmal12345678
      资金测试 http://192.168.89.122:8080/login admin/cmal12345678

资金测试服务器环境 192.168.89.122  apps/hand_dev

tomcat路径：/u01/tomcat/tomcat8

###### 数据库信息

资金系统使用Oracle数据库为主要的数据库【采用了多数据源，一部分数据在MySQL数据库】。同时采用DBLINK的形式直接对接了EBS的表。



- 资金测试环境数据库

  DBLink：HAP_TO_EBS   apps/cmalapps2019

```
<Resource auth="Container" driverClassName="oracle.jdbc.driver.OracleDriver" name="jdbc/hap_dev" type="javax.sql.DataSource" url="jdbc:oracle:thin:@192.168.89.110:1521/zjtest" username="hap_dev" password="handhand"/>

<Resource auth="Container" driverClassName="com.mysql.jdbc.Driver"
          url="jdbc:mysql://192.168.89.100:3306/cwjs?useUnicode=true&amp;characterEncoding=UTF-8&amp;allowMultiQueries=true&amp;zeroDateTimeBehavior=convertToNull"
          name="jdbc/scs_dev" type="javax.sql.DataSource" username="cwjs" password=""
          maxActive="8" maxIdle="4"/>

```

- 资金正式环境数据库【mysql数据库和结算在一个mysql服务器上】


```
 <Resource auth="Container" driverClassName="oracle.jdbc.driver.OracleDriver" name="jdbc/hap_prod" type="javax.sql.DataSource" url="jdbc:oracle:thin:@192.168.21.118:1529/ZJGLPROD" username="cwzj" password="cwzj_2019"/>

<Resource auth="Container" driverClassName="com.mysql.jdbc.Driver"
          url="jdbc:mysql://192.168.21.108:3306/cwjs_prod?useUnicode=true&amp;characterEncoding=UTF-8&amp;allowMultiQueries=true&amp;zeroDateTimeBehavior=convertToNull"
          name="jdbc/scs_prod" type="javax.sql.DataSource" username="cwjs" password="cwjs2019_prod"
          maxActive="8" maxIdle="4"/>

```

  redis正式环境参照结算做了主从。

