# linux深入

## vim

因为很多命令都可以和这些移动光标的命令连动

<start position><command><end position>

看一下vim是怎么重复自己的：

1. `.` → (小数点) 可以重复上一次的命令
2. N<command> → 重复某个命令N次

**Undo/Redo**

> - `u` → undo
> - `ctrl+r` → redo

`:bn` *和* `:bp` *→ 你可以同时打开很多文件，使用这两个命令来切换下一个或上一个文件。（陈皓注：我喜欢使用:n到下一个文件）*

`:x`*，* `ZZ` *或* `:wq` *→ 保存并退出 (*`:x` *表示仅在需要时保存，ZZ不需要输入冒号并回车)*

`cw` *→ 替换从光标所在位置后到一个单词结尾的字符*[change word]

`a` *→ 在光标后插入*[i是之后]

`:help ` *→ 显示相关命令的帮助。你也可以就输入* `:help` *而不跟命令。*

`:e ` *→ 打开一个文件*

1. `w` → 到下一个单词的开头。
2. `e` → 到下一个单词的结尾。【大写的话以blank字符间隔】

- `%` : 匹配括号移动，包括 `(`, `{`, `[`. （陈皓注：你需要把光标先移到括号上）
- `*` 和 `#`:  匹配光标当前所在的单词，移动光标到下一个（或上一个）匹配单词（*是下一个，#是上一个）

### bash

ctrl+R 搜索历史命令

```
 awk '{printf "%-8s %-8s %-8s %-18s %-22s %-15s\n",$1,$2,$3,$4,$5,$6}' netstat.txt
```

格式化字符串其中-表示左对齐，8表示字符宽度。

sed全名叫stream editor，流编辑器，用程序的方式来编辑文本

使用 -i 参数直接修改文件内容：

Bash 没有数据类型的概念，所有的变量值都是字符串。

#### script

单引号''剥夺了所有字符的特殊含义，单引号''内就变成了单纯的字符，。

双引号""则对于双引号""内的参数替换($)和命令替换(``)是个例外。

反引号``与$()命令替换的作用

${ }中放的是变量，这种写法可以用于变量名与其他字符连用的情况。

`source`命令最大的特点是在当前 Shell 执行脚本，不像直接执行脚本时，会新建一个子 Shell

`if`关键字后面，跟的是一个命令。这个命令可以是`test`命令，也可以是其他命令。

判断字符串时，变量要放在双引号之中。

`[[ expression ]]`这种判断形式，支持正则表达式。

`test`命令内部使用的圆括号，必须使用引号或者转义。

只要是算术表达式，都能用于`((...))`语法。

圆括号之中使用变量，不必加上美元符号`$`。

函数总是在当前 Shell 执行，即如果函数与别名同名，那么别名优先执行。

`shopt`命令用来调整 Shell 的参数

非登录 Session 是用户进入系统以后，手动新建的 Session，这时不会进行环境初始化

Here 文档三个小于号  作用是将字符串通过标准输入，传递给命令。



#### 常见应用

kill -s 9 1827

pgrep firefox  得到PID

pkill - 9 进程名

service命令其实是去/etc/init.d目录下，去执行相关程序

systemd是Linux系统最新的初始化系统，systemd对应的进程管理命令是systemctl



**缓存（cached）**：缓存读过的数据

**缓冲（buffers）**：把分散的写操作集中，通过sync命令手动清空缓冲。

cache是高速缓存，用于CPU和内存之间的缓冲；
       buffer是I/O缓存，用于内存和硬盘的缓冲；

Swap用途：Swap意思是交换分区，通常我们说的虚拟内存，是从硬盘中划分出的一个分区。当物理内存不够用的时候，内核就会释放缓存区（buffers/cache）里一些长时间不用的程序，然后将这些程序临时放到Swap中

#### 磁盘分区

【磁盘最多四个分区([MBR大小决定](https://blog.csdn.net/qq_44714603/article/details/88659996))，分区是一个在/dev目录下的文件，最后挂载在任意目录下。】

fdisk -l  命令查看分区情况：

[分区-格式化-挂载-开机自动挂载/etc/fstab](https://blog.csdn.net/xuplus/article/details/51668878)

使用mount -a 命令来检验编辑的内容是否有错

fdisk命令只支持msdos，分区的时候只支持小容量硬盘（<=2T）

**查看分区**parted -l



与传统的磁盘与分区相比，LVM为计算机提供了更高层次的磁盘存储。磁盘空间的动态管理。

卷组（Volume Group,VG）：是由一个或多个物理卷所组成的存储池，在卷组上能创建一个或多个逻辑卷。如果把PV比作地球的一个板块，VG则是一个地球，因为地球是由多个板块组成的，那么在地球上划分一个区域并标记为亚洲，则亚洲就相当于一个LV。

#### selinux

当一个主体Subject（如一个程序）尝试访问一个目标Object（如一个文件），SELinux 安全服务器SELinux Security Server（在内核中）从策略数据库Policy Database中运行一个检查。基于当前的模式mode，如果 SELinux 安全服务器授予权限，该主体就能够访问该目标。如果 SELinux 安全服务器拒绝了权限，就会在 /var/log/messages 中记录一条拒绝信息。

临时关闭：

getenforce

setenforce 0

永久关闭：

vim /etc/sysconfig/selinux

重启服务reboot

### mysql安装

1.新建mysql用户组合用户

2.解压

3.配置

cp mysql.server /etc/init.d/mysqld

/etc/init.d/mysqld start

##### 搭建java应用

#查看本机安装的jdk
rpm -qa | grep jdk
解压
tar -zxvf jdk-8u181-linux-x64.tar.gz

查看java的版本
java -version
查看java家目录
echo $JAVA_HOME

date +%F_%T
开机启动执行：vim /etc/rc.local
系统开机启动时会去加载/etc/init.d/下面的脚本

======关闭防火墙
在之前的版本中关闭防火墙等服务的命令是
service iptables stop
/etc/init.d/iptables stop
在RHEL7中,其实没有这个服务
仅仅关闭防火墙并不行。应该再开启，再关闭
systemctl stop firewalld.service （重启恢复）
systemctl disable firewalld.service （永久关闭）

firewall-cmd --zone=public --add-port=8081/tcp --permanent
#重新载入 
firewall-cmd --reload
#查看
firewall-cmd --zone=public --query-port=8081/tcp
#删除，以备设错端口移除
firewall-cmd --zone=public --remove-port=8081/tcp --permanent
======测试端口
输入Telnet测试端口命令： “Telnet IP 端口 或者 Telnet 域名 端口” 并且回车
netstat -tunpl |grep   8080
ps -ef|grep 8081


用法: ssh -v -p port username@ip

说明：

-v 调试模式(会打印日志).

-p 指定端口

username:远程主机的登录用户


ip:远程主机

curl ip:port


======磁盘、分区、文件系统、目录树
临时挂载：mount -o loop /tmp/rhel-server-7.2-x86_64-dvd.iso  /mnt

物理分区（逻辑分区）---格式化----物理卷----卷组---扩展逻辑卷组--扩展文件系统空间
fdisk  -l 
cfdisk

df  -h
du  文件
free
mkfs -t ext4 /dev/sda3  格式化

=======权限
chown -R oracle:oinstall /tmp/Oracle   改变属主。
cat /etc/passwd 可以查看所有用户的列表
用户、密码
groupadd dba
useradd -g oinstall -G dba,asmdba oracle
passwd oracle

sudo 表示 “superuser do”。 它允许已验证的用户以其他用户的身份来运行命令。
https://www.cnblogs.com/sparkdev/p/6189196.html
====vi编辑
命令模式下输入“:set nu” 

====文件
删除目录下的所有。

rm -rf /home/happy/baidu/*
清除缓存
真正剩余的内存是free+buff/cache
sync
echo 1 > /proc/sys/vm/drop_caches

=====免费yum源一般有两种：本地yum源和第三方yum源
本地源

第三方源
https://blog.csdn.net/watt1208/article/details/81868801
安装epel源  wget http://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm

rpm -ivh epel-release-latest-7.noarch.rpm

只下载不安装
yum install --downloadonly --downloaddir=/usr/package
=========维护
man手册中文化
whatis
whereis
which
type
file


同步时间
https://www.hangge.com/blog/cache/detail_2499.html


journalctl工具查看日志
在/var/log下是系统运行的各种日志文件。排查错误的时候用


linux下systemctl enable （service名）时出现file exists的解决办法
https://blog.csdn.net/m0_37876745/article/details/78188626


源码位置

rpm包位置

应用程序位置

大程序位置  


自动保存  混淆



nohup COMMAND & 
把 nohup与& 结合在一起，这样就可以不挂断的永久在后台执行


sof是系统管理/安全的尤伯工具。将这个工具称之为lsof真实名副其实，因为它是指“列出打开文件（lists openfiles）”

如果你把文本放在双引号中， shell 使用的特殊字符，除了 $，\ (反斜杠），和 `（倒引号）之外， 则失去它们的特殊含义，被当作普通字符来看待。
需要禁止所有的展开，我们使用单引号