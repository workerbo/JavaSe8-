堆分为新生代和老年代，默认情况下新生代占堆的1/3，老年代占堆的2/3。

年轻代具体分为: Eden，From Space，To Space，比例一般为8:1:1

1.新建对象，放入Eden

2.Eden放不下，垃圾回收并放入Survior【使用的时候只使用Eden + 一块Survivor。用Eden区用满时会进行一次minor gc，将存活下面的对象复制到另外一块Survivor上。如果另一块Survivor放不下(对应虚拟机参数为 XX:TargetSurvivorRatio，默认50，即50%)，对象直接进入老年代】

3.From Space与To Space相互交换身份互相复制，存活时间较长则放入老年代

4.老年代放不下，可能执行Major GC清理老年代内存，也可能Full GC 老年代年轻代一起清理



[Minor GC](https://blog.csdn.net/zc19921215/article/details/83029952):

![img](https://gitee.com/workerbo/gallery/raw/master/2020/882980-20200102223928936-343120699.png)

  对于复制算法来说，当年轻代Eden区域满的时候会触发一次Minor GC，将Eden和From Survivor的对象复制到另外一块To Survivor上。

 注意：如果某个对象存活的时间超过一定Minor gc次数会直接进入老年代，不在分配到To Survivor上(默认15次，对应虚拟机参数 -XX:+MaxTenuringThreshold)。

**JVM****的空间分配担保机制可能会触发Full GC

YongGC空间担保

![img](http://5b0988e595225.cdn.sohucs.com/images/20190927/39a0f6f043bc46c482167524a24ded63.png)

参考[1](https://www.sohu.com/a/343836125_172964)

[垃圾收集器基础和具体实现](https://www.cnblogs.com/xiaoxi/p/6486852.html)

jps:查看本地正在运行的java进程和进程ID（pid）

jinfo pid，查看指定pid的所有JVM信息

jinfo -flags pid 查询虚拟机运行参数信息。

jmap -heap pid：输出堆内存设置和使用情况

 jstat -gcutil pid:可以显示gc的信息，查看gc的次数，及时间。其中最后五项，分别是young gc的次数，young gc的时间，full gc的次数，full gc的时间，gc的总时间。【ygc不超过一个小时一次，每次时间】

 jstat -gc pid:【以s和KB为单位】

 获取系统执行时间

```bash
ps -eo pid,tty,user,comm,lstart,etime | grep 9
```



top -Hp  pid  进程下各个线程分析

![img](https://images2015.cnblogs.com/blog/249993/201703/249993-20170306195851516-1068507269.png)

执行`jstack`命令，将得到进程的堆栈信息。我一般使用`jstack -l pid`来得到长列表，显示其详细信息。
有时线程挂起的时候，需要执行`jstack -F pid`来获取。往往一次 dump的信息，还不足以确认问题。建议产生三次 dump信息，如果每次 dump都指向同一个问题，我们才确定问题的典型性。

jstack统计线程数 jstack -l 28367 | grep 'java.lang.Thread.State' | wc -l

转换线程ID printf "%x\n" 17880  

2. jstat  是用于监视虚拟机各种运行状态信息的命令行工具
3. jinfo -flags vmid
4. [jmap](https://www.jianshu.com/p/3275bb8c8819)  
5. 查看各内存区域  jmap -heap pid
6. 了解系统运行时的对象分布  jmap -histo pid | head 30



为了方便分析可以使用gc分析工具：[gceasy](https://gceasy.io/)

jstat -gc -t 1 1000 30



Allocation Failure表示向young generation(eden)给新对象申请空间，但是young generation(eden)剩余的合适空间不够所需的大小导致的minor gc。

## 新生代垃圾回收机制

新对象尝试栈上分配，不行再尝试TLAB分配，不行则考虑是否直接绕过eden区在年老代分配空间(`-XX:PretenureSizeThreshold设置大对象直接进入年老代的阈值，当对象大小超过这个值时，将直接在年老代分配。`)，不行则最后考虑在eden申请空间

向eden申请空间创建新对象，eden没有合适的空间，因此触发minor gc

minor gc将eden区及from survivor区域的存活对象进行处理

- 如果这些对象年龄达到阈值，则直接晋升到年老代
- 若要拷贝的对象太大，那么不会拷贝到to survivor，而是直接进入年老代
- 若to survivor区域空间不够/或者复制过程中出现不够，则发生survivor溢出，直接进入年老代【会重新计算threshold】
- 其他的，若to survivor区域空间够，则存活对象拷贝到to survivor区域

此时eden区及from survivor区域的剩余对象为垃圾对象，直接抹掉回收，释放的空间成为新的可分配的空间

minor gc之后，若eden空间足够，则新对象在eden分配空间；若eden空间仍然不够，则新对象直接在年老代分配空间



-XX:+PrintTenuringDistribution有几个要点，要明确一下：

- 这个打印的哪个区域的对象分布(`survivor`)
- 是在gc之前打印，还是在gc之后打印(`gc之后打印`)
- 对象的年龄就是他经历的MinorGC次数，对象首次分配时，年龄为0，第一次经历MinorGC之后，若还没有被回收，则年龄+1



```css
jstat -gcutil -h10 7 10000 10000
```

添加JVM参数
到Tomcat的bin目录下，打开文件catalina.sh