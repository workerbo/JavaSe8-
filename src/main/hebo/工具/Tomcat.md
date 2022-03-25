tomcat启动通过脚本调用main方法





3个参数：acceptCount、maxConnections、maxThreads
再回顾一下Tomcat处理请求的过程：在accept队列中接收连接（当客户端向服务器发送请求时，如果客户端与OS完成三次握手建立了连接，则OS将该连接放入accept队列）；在连接中获取请求的数据，生成request；调用servlet容器处理请求；返回response。

相对应的，Connector中的几个参数功能如下：

1、acceptCount
acceptCount，表示accept队列的长度；当accept队列中连接的个数达到acceptCount时，队列满，进来的请求一律被拒绝。队列最大满值默认值是100。

2、maxConnections
maxConnections，表示Tomcat在任意时刻接收和处理的最大连接数。当Tomcat接收的连接数达到maxConnections时，Acceptor线程不会读取accept队列中的连接（Acceptor线程用于读取accept队列中的连接）；这时accept队列中的线程会一直阻塞着，直到Tomcat接收的连接数小于maxConnections。如果设置为-1，则连接数不受限制。

默认值与连接器使用的协议有关：NIO的默认值是10000，APR/native的默认值是8192，而BIO的默认值为maxThreads（如果配置了Executor，则默认值是Executor的maxThreads）。

在windows下，APR/native的maxConnections值会自动调整为设置值以下最大的1024的整数倍；如设置为2000，则最大值实际是1024。

3、maxThreads
maxThreads，表示请求处理线程的最大数量。默认值是200（Tomcat7和8都是的）。如果该Connector绑定了Executor，这个值会被忽略，因为该Connector将使用绑定的Executor，而不是内置的线程池来执行任务。

maxThreads规定的是最大的线程数目，并不是实际running的CPU数量；实际上，maxThreads的大小比CPU核心数量要大得多。这是因为，处理请求的线程真正用于计算的时间可能很少，大多数时间可能在阻塞，如等待数据库返回数据、等待硬盘读写数据等。因此，在某一时刻，只有少数的线程真正的在使用物理CPU，大多数线程都在等待；因此线程数远大于物理核心数才是合理的。

换句话说，Tomcat通过使用比CPU核心数量多得多的线程数，可以使CPU忙碌起来，大大提高CPU的利用率。



Tomcat已经实现了NIO模型，在7.x版本中需要配置，8.x版本使用NIO作为默认的模型。

omcat只能支持几百个并发的原因是什么？其实跟线程关系不大，暂且以`1秒内服务器处理的请求数`来衡量并发数，那么最大并发就是`1秒内服务器最多能处理的请求数`，很显然，平均单个请求处理时间越短，则最大并发越高。平均请求处理时间直接影响了最大并发的高度。而大部分情况下Tomcat处理请求的平均时间不会太短，有时还设计数据库操作，所以大部分情况下Tomcat的最高并发就只有几百。
然后线程模型会影响Tomcat处理请求的平均时间，要么是线程太少造成CPU等待而增加了平均处理时间，要么是线程太多而造成CPU要花费一定的周期来进行线程切换而延长了平均处理时间。所以合理的设置线程数能一定程度提高最大并发。



![image-20210812161104491](../../../../../../../Programfile/Typora/upload/image-20210812161104491.png)

​                                                                           ssm在tomcat打包后的目录结构

Tomcat——请求处理、[线程池的理解](https://blog.csdn.net/qq_18683559/article/details/102998662)



Acceptor 组件或者Worker组件

目前大多数HTTP请求使用的是长连接（HTTP/1.1默认keep-alive为true），而长连接意味着，一个TCP的socket在当前请求结束后，如果没有新的请求到来，socket不会立马释放，而是等timeout后再释放。如果使用BIO，“读取socket并交给Worker中的线程”这个过程是阻塞的，也就意味着在socket等待下一个请求或等待释放的过程中，处理这个socket的工作线程会一直被占用，无法释放；因此Tomcat可以同时处理的socket数目不能超过最大线程数，性能受到了极大限制。而使用NIO，“**读取socket并交给Worker中的线程**”这个过程是非阻塞的，当socket在等待下一个请求或等待释放时，并不会占用工作线程，因此Tomcat可以同时处理的socket数目远大于最大线程数，并发性能大大提高。

- 1) maxThreads：的设置既与应用的特点有关，也与服务器的CPU核心数量有关。通过前面介绍可以知道，maxThreads数量应该远大于CPU核心数量；而且CPU核心数越大，maxThreads应该越大；应用中CPU越不密集（IO越密集），maxThreads应该越大，以便能够充分利用CPU。当然，maxThreads的值并不总是越大越好，如果maxThreads过大，那么CPU会花费大量的时间用于**线程的切换**，整体效率会降低。
- 2) maxConnections：的设置与Tomcat的运行模式有关。如果tomcat使用的是BIO，那么maxConnections的值应该与maxThreads一致；如果tomcat使用的是NIO，那么类似于Tomcat的默认值，maxConnections值应该远大于maxThreads。
- 3) 通过前面的介绍可以知道，虽然tomcat同时可以处理的连接数目是maxConnections，但服务器中可以同时接收的连接数为maxConnections+acceptCount。acceptCount的设置，与应用在连接过高情况下希望做出什么反应有关系。如果设置过大，后面进入的请求等待时间会很长；如果设置过小，后面进入的请求立马返回connection refused。