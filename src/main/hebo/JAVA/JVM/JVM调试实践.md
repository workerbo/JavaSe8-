###### 运维

执行`jstack`命令，将得到进程的堆栈信息。我一般使用`jstack -l pid`来得到长列表，显示其详细信息。
有时线程挂起的时候，需要执行`jstack -F pid`来获取。往往一次 dump的信息，还不足以确认问题。建议产生三次 dump信息，如果每次 dump都指向同一个问题，我们才确定问题的典型性。

jstack统计线程数 jstack -l 28367 | grep 'java.lang.Thread.State' | wc -l

转换线程ID printf "%x\n" 17880  

##### JVM处理和监控工具

工具【知识】 数据【ThreadDump、HeapDump、GC日志、运行日志、异常堆栈】  问题【】

1. jps

![image-20201111134322908](https://gitee.com/workerbo/gallery/raw/master/2020/image-20201111134322908.png)

2. jstat  是用于监视虚拟机各种运行状态信息的命令行工具
3. jinfo -flags vmid
4. [jmap](https://www.jianshu.com/p/3275bb8c8819)  
5. 查看各内存区域  jmap -heap pid
6. 了解系统运行时的对象分布  jmap -histo pid | head 30

