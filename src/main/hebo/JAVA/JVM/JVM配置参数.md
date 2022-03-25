



JVM中的-Xms -Xmx -XX:newSize -XX:MaxnewSize -Xmn -XX:PermSize -XX:MaxPermSize区别介绍
一、配置参数使用条件

新生代 ( Young ) 与老年代 ( Old ) 的比例的值为 1:2 ( 该值可以通过参数 -XX:NewRatio 来指定 )

 在不断重构代码的情况下，仍然不能解决内存溢出的问题(OutOfMemoryError等)，这种情况下就需要使用JVM的配置参数对JVM运行时各个区域的内存情况进行相应的分配。

二、JVM存储数据的内容的内存分为
   ①堆区（Java堆：所有的线程共享该区域）：通过new的方式创建的对象(一个类的实例)、数组所占的空间。

        注意：1）堆区还细分为新生代（Eden空间、From Survivor空间、To Survivor空间）、老年代（Tenured Generation空间）。
    
                     2）Java垃圾回收机制只作用于堆区，对非堆区没有作用。

   ②非堆区：代码、常量、外部访问(比如流在传输数据时所占用的资源)等。

三、常见的参数种类(配置内存)
（1）配置堆区的参数：-Xms、-Xmx、-XX:newSize、-XX:MaxnewSize、-Xmn

（2）配置非堆区的参数：-XX:PermSize、-XX:MaxPermSize

四、堆区参数配置
 （1）-Xms：Java虚拟机堆区内存初始内存分配的大小，按照实际情况进行分配（一般为操作系统可用内存的1/64大小）。

（2）-Xmx：Java虚拟机堆区内存可被分配的最大上限（一般为操作系统可用内存的70-80%【给栈，方法区，直接内存和系统留空间】）。

注意：①一般-Xms、-Xmx两个参数会配置相同的值（优点：能够在Java垃圾回收机制清理完堆区后不需要重新分隔计算堆区的大小而浪费资源）。

（3）-XX:newSize:新生代初始化内存的大小(注意：该值需要小于-Xms的值)。

（4）-XX:MaxnewSize:新生代可被分配的内存的最大上限(注意：该值需要小于-Xmx的值)。

（5）-Xmn:对-XX:newSize、-XX:MaxnewSize两个参数同时进行配置（注意：JDK1.4之后才有该参数）。

- XX:SurviorRatio:新生代中eden区与survivior 区的比例

五、非堆区参数配置
（1）-XX:PermSize:非堆区初始化内存分配大小。

（2）-XX:MaxPermSize:非堆区分配的内存的最大上限。

STW：在GC时停止所有的应用线程。

安全点：在暂停时不会引起引用关系的变化。是主动中断。或者是在阻塞或者睡眠时是安全区