> ###### redis源码安装

redis  解压   编译安装 默认路径 usr/local/bin  复制配置文件编辑  启动时指定。【在配置文件所在目录为redis根目录】

源码安装需要手动服务化。

cd /usr/local/redis
./bin/redis-server ./redis.conf
测试redis是否安装成功

cd /usr/local/redis/bin
./redis-cli

sudo systemctl  start /usr/local/redis/bin/redis.service
设置开机自启动