

##### 第一种是配置文件的修改：

如果是default就是所有的[微服务](https://so.csdn.net/so/search?q=微服务&spm=1001.2101.3001.7020)都会生效，如果是单独的服务名称，就是该服务生效



##### 第二种是在代码里配置：

首先先写个类，声明一个bean：

如果是全局配置，可以再Application上加上注解，全局都配置

如果不是全局而是具体某个服务配置，可以再定义[feign](https://so.csdn.net/so/search?q=feign&spm=1001.2101.3001.7020)的客户端加上注解，使用局部配置



参考：https://blog.csdn.net/Abenazhan/article/details/125708063