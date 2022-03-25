

**1.double-计算时容易出现不精确的问题**

![img](https://pic2.zhimg.com/v2-df13a03720f9ff21ac033a67c5e56615_b.png)

**double的小数部分容易出现使用二进制无法准确表示**

如十进制的0.1，0.2，0.3，0.4 都不能准确表示成二进制；

可参考：[https://blog.csdn.net/Lixuanshengchao/article/details/82049191](https://link.zhihu.com/?target=https%3A//blog.csdn.net/Lixuanshengchao/article/details/82049191)



**2.dobule的=比较要注意**

![img](https://gitee.com/workerbo/gallery/raw/master/2020/v2-89fd0d23add36980693cf0433cb479b6_b.png)



**3.BigDecimal-除法除不尽会出现异常：ArithmeticException**

![img](https://pic2.zhimg.com/v2-a1b602e4ce3ea3c16e1983606ccb1e0d_b.jpg)



**4.new BigDecimal(double)-也许不是你想要的**

一般情况下都不使用new BigDecimal(double) 应该使用BigDecimal.valueOf(double)

```text
  BigDecimal d1 = BigDecimal.valueOf(12.3)//结果是12.3 你预期的
 BigDecimal d2 = new BigDecimal(12.3) //结果是12.300000000000000710542735760100185871124267578125
```

我想12.300000000000000710542735760100185871124267578125肯定不是你想要的结果，因此 new BigDecimal(double)可能会产生不是你预期的结果，原理可以自行看一下底层源代码，还是比较容易的；

**另：BigDecimal.valueOf(xxx ) 是静态工厂类，永远优先于构造函数（摘自<>，此书也是非常推荐的一本经典书）**



**5.BigDecimal-是不可变对象**

如原来d1=1.11 ，又加了一个数2.11，这种操作要注意结果要指向新对象；

![img](https://pic1.zhimg.com/v2-9d670fba7e479c6ff55640dcc5e72330_b.jpg)

任何针对BigDecimal对象的修改都会产生一个新对象；

BigDecimal newValue = BigDecimal.*valueOf*(1.2222).add(BigDecimal.*valueOf*(2.33333));

BigDecimal newValue = BigDecimal.*valueOf*(1.2222).setScale(2);

总之每次修改都要重新指向新对象，才能保证计算结果是对的。



**6.BigDecimal比较大小操作不方便，毕竟是对象操作**

比较大小和相等都使用compareTo,如果需要返回大数或小数可使用max，min。**且注意不能使用equals**





涉及到精准计算如金额，一定要使用BigDecimal或转成long或int计算

若不需要精准的，如一些统计值：（本身就没有精确值）

用户平均价格，店铺评分，用户经纬度等本身就没有精准值一说的推荐使用double或float



在数据库中如果确实不确定使用什么double或Decimal哪种类型合适，那最好使用Decimal，毕竟稳定，安全高于一切；