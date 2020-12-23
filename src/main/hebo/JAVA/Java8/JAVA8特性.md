###### 函数式接口：只有一个方法的接口。

Function 函数型接口, 有一个输入参数，有一个输出

  

```php
Function.identity()
static  Function identity() {
    return t -> t;
}
```

Predicate 断定型接口：有一个输入参数，返回值只能是 布尔值！

Consumer 消费型接口：只有输入，没有返回值！

Supplier 供给型接口: 没有参数，只有返回值。

笔记参考：菜鸟驿站https://www.runoob.com/java/java8-new-features.html

动态类型和静态类型：类型检查时间

[Java 8 Collectors to Map](https://www.cnblogs.com/liululee/p/11038072.html)

> ###### lambda

 实质上还是匿名类对象（是对应函数式接口的实现 入参  函数体）
lambda 表达式只能引用标记了 final 的外层局部变量
在 Lambda 表达式当中不允许声明一个与局部变量同名的参数或者局部变量。
lambda 表达式的局部变量可以不用声明为 final，但是必须不可被后面的代码修改

接口默认方法：所以引进的默认方法。他们的目的是为了解决接口的修改与现有的实现不兼容的问题。
一个接口有默认方法，考虑这样的情况，一个类实现了多个接口，且这些接口有相同的默认方法，
第一个解决方案是创建自己的默认方法，来覆盖重写接口的默认方法：
第二种解决方案可以使用 接口名.super 来调用指定接口的默认方法：

> ###### 时间

线程安全、日期时间统一处理、时区问题
LocalDateTime
LocalDateTime.now()
LocalDate 
LocalTime 
https://www.runoob.com/java/java8-datetime-api.html


Lambda 表达式和方法引用（实际上也可认为是Lambda表达式）上。
函数式接口(Functional Interface)就是一个有且仅有一个抽象方法，但是可以有多个非抽象方法的接口。
 @FunctionalInterface注解只是提醒编译器去检查该接口是否仅包含一个抽象方法
 函数式接口里是可以包含Object里的public方法，这些方法对于函数式接口来说，不被当成是抽象方法（虽然它们是抽象方法）；因为任何一个函数式接口的实现，默认都继承了 Object 类，包含了来自 java.lang.Object 里对这些抽象方法的实现；

https://www.cnblogs.com/andywithu/p/7404101.html
顺序流时，数据按照源数据的顺序依次通过管道，当一个数据被filter过滤，或者经过整个管道而输出后，第二个数据才会开始重复这一过程
因此对于包含sorted、distinct()等与全局数据相关的操作，不推荐使用并行流。

> ###### Stream

Java 8 API添加了一个新的抽象称为流Stream，可以让你以一种声明的方式处理数据。
1.stream不存储数据
2.stream不改变源数据
3.stream的延迟执行特性  可以在收集、聚合、消费操作之前更改源数据
产生流
1）通过数组
 Arrays.stream（）
 //2.通过Stream.of
    Stream<Integer> stream1 = Stream.of(1,2,34,5,65);
    //注意生成的是int[]的流
    Stream<int[]> stream2 = Stream.of(arr,arr);

2）通过集合
//创建普通流
    Stream<String> stream  = strs.stream();
    //创建并行流
    Stream<String> stream1 = strs.parallelStream();

3）创建空的流
     Stream.empty();

> ######  Optional 

https://www.cnblogs.com/lijingran/p/8727149.html
 Optional 不能Serializable。因此，它不应该用作类的字段
Optional 类是一个可以为null的容器对象。如果值存在则isPresent()方法会返回true，调用get()方法会返回该对象。

Optional 是个容器：它可以保存类型T的值，或者仅仅保存null。Optional提供很多有用的方法，这样我们就不用显式进行空值检测。

Optional 类的引入很好的解决空指针异常。
对于集合应该用Lists.newArrayList() 返回一个空集合


 1、assert <boolean表达式>
如果<boolean表达式>为true，则程序继续执行。
如果为false，则程序抛出AssertionError，并终止执行。

2、assert <boolean表达式> : <错误信息表达式>
如果<boolean表达式>为true，则程序继续执行。
如果为false，则程序抛出java.lang.AssertionError，并输入<错误信息表达式>。



