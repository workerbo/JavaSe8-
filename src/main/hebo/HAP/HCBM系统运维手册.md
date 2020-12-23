### HCBM系统运维手册

一  前置准备

程序包: hcm.war

二  停止应用

在tomcat目录下执行：  bin/shutdown.sh  .(备注：有时可能会报错，是由于当前tomcat已经停止，不需要处理)

三  删除旧程序与备份

在tomcat目录下的webapp目录中执行(删除操作，谨慎执行)： 

\1. rm -rf hcm 

2（备份） mv hcm.war hcm.war.bck

四  上传新程序

将hcm.war上传至tomcat目录下的webapp目录。

五  启动应用（启动需要1-3分钟左右）

在tomcat目录中执行： bin/startup.sh  .

六  查看应用

在浏览器输入应用地址+:端口号/hcm，应用启动成功后应该能访问到登录页面。

七  重启应用（为了让tomcat的虚拟路径生效）

在tomcat目录下执行：bin/shutdown.sh,  等待3秒后，执行bin/startup.sh.

八 再次查看应用（启动需要1-3分钟左右）

在浏览器直接输入应用地址+：端口号，应用启动成功后应用能够访问到登录页面。（备注：生产环境8081端口没开，访问不到是正常现象）

九  查看请求分发

在浏览器直接输入应用地址，查看是否能否访问到登录页面。

十 删除备份

在tomcat的webapp下执行: rm -f hcm.war.bck.

十一tomcat目录下执行： tail -lf logs/catalina.out

十二：系统重启后应该检查应用的启动情况，顺序如下。

mysql  

检查启动 service  mysqld status 

启动 service  mysqld start



redis 

 onlyoffice

  fastdfs  nginx tomcat

ps -ef |grep mysql |grep -v grep  

docker  ps 

