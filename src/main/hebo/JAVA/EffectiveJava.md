> ##### javase
>

###### 基本

switch只支持一种数据类型，那就是整型，其他数据类型都是转换成整型之后在使用switch的。

当调用 intern 方法时，如果池已经包含一个等于此 String 对象的字符串（该对象由 equals(Object) 方法确定），则返回池中的字符串。否则，将此 String 对象添加到池中，并且返回此 String 对象的引用【**运行时唯一方法加入到常量池当中**】。字符串拼接有变量就不会进入到常量池当中。用new会进入堆中。

Java中Integer的缓存实现

适用于整数值区间-128 至 +127。

只适用于自动装箱。使用构造函数创建对象不适用。、



###### 创建与销毁对象

1.考虑静态工厂方法而不是构造函数

- 静态工厂方法的一个优点是，它们是有名称的，而构造函数的名称都是一样的。
- 与构造函数不同，它们不需要在每次被调用时创建一个新对象。实例控制允许一个类保证它是一个单例（第 3 项）或非实例化的（第 4 项）
- 与构造函数不同，它们返回的对象可以是返回类型的任何子类的实例对象。在 Java 8 之前，接口不能有静态方法【但是嵌套类是公共的】。按照惯例，名为 Type 的接口的静态工厂方法被放置在一个不可实例化的名为 Types 的配套类

2.只要类是自己管理内存，程序猿就应该警惕内存泄漏问题。一旦元素被释放掉，则该元素中包含的任何对象引用都应该被清空。当所要的缓存项的生命周期是由该键的外部引用而不是由值决定时，WeakHashMap 才有用处。对于更加复杂的缓存，必须直接使用 java.lang.ref。后台线程进行清理。

3.从一个对象变成不可达开始，到它的终结方法或清理方法被执行，所花费的这段时间是任意长的(也就是说我们无法预知一个对象在销毁之后和执行终结方法和清理方法之间的间隔时间)

4.在处理必须关闭的资源时，相比于 try-finally，始终优先使用 try-with-resources。生成的代码更短更清晰，它生成的异常更有用



###### 接口和类

**如果类可以在它所在的包的外部进行访问，就提供访问方法**，以保留将来改变该类的内部表示法的灵活性。想在将来改变其内部表示法是不可能的，因为公有类的客户端代码已经遍布各处。

在第一次调用的时候计算出散列码，然后把它缓存起来，以备将来被再次调用时使用。这种方法是延迟初始化.

包外继承破坏了封装性。【超类的实现有可能随着发行版本的不同而有所变化，导致子类遭到破坏。父类的具体实现很容易影响子类的正确性。】



###### 弱引用

一旦发现了**只具有弱引用**的对象，不管当前内存空间足够与否，都会回收它的内存。

例：每一个Thread有一个threadLocalMap的属性，用来存放ThreadLocal对象，ThreadLocalMap中是通过一个Entry[]的散列表存放ThreadLocal变量以及ThreadLocal的value，而作为Entry的key的ThreadLocal就是使用的弱引用。一旦其他强引用释放后一次gc后ThreadLocal就会被释放。



###### 变量

常量又分为：

- ```java
  编译期常量
  static final int A = 1024;
  编译时，所有A的引用都将被替换成字面量（即1024），类型必须是基本类型或String。
  运行时常量
  static final int len = "Rhine".length();
  运行时才能确定它的值。
  ```

###### [注解处理器](https://blog.csdn.net/hj7jay/article/details/52180023)是 javac 自带的一个工具

不是在讨论在**运行时**通过反射机制运行处理的注解，而是在讨论在**编译时**处理的注解。

###### 枚举

枚举单例保证了线程安全【实例化安全】和序列化安全【其他方式new一个对象、，枚举的序列化和反序列化是有特殊定制的】

枚举提供了编译时的类型安全。

枚举类型（enum type）是指由一组固定的常量组成合法值的类型。

枚举类型是真正的 final。因为客户端既不能创建枚举类型的实例，也不能对它进行扩展，因此很可能没有实例，而只有声明过的枚举常量。增加或者重新排列枚举类型中的常量，而无需重新编译它的客户端代码、减少一个被引用的枚举常量也会在运行时引发一个有用的异常。【区别于int枚举和string枚举（编译时常量）】。枚举类型还允许添加任意的方法和域，并实现任意的接口。

**为了将数据与枚举常量关联起来，得声明实例域，并编写一个带有数据并将数据保存在域中的构造器**。所有的域都应该为 final 的（第 17 项）。它们可以是公有的，但最好将它们做成是私有的，并提供公有的访问方法。

将不同的行为与每个枚举常量关联起来：在枚举类型中声明一个抽象的 apply 方法，并在特定于常量的类主体中，用具体方法覆盖每个常量的抽象 apply 方法。这种方法被称作特定于常量的方法实现

如果多个枚举常量同时共享相同的行为，则考虑策略枚举。

```
// The strategy enum pattern
enum PayrollDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,
    SATURDAY(PayType.WEEKEND), SUNDAY(PayType.WEEKEND);
    private final PayType payType;
    PayrollDay(PayType payType) { this.payType = payType; }
    PayrollDay() { this(PayType.WEEKDAY); } // Default
    int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
    }
    // The strategy enum type
    private enum PayType {
        WEEKDAY {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked <= MINS_PER_SHIFT ? 0 : (minsWorked - MINS_PER_SHIFT) * payRate / 2;
            }
        },
        WEEKEND {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked * payRate / 2;
            }
        };
        abstract int overtimePay(int mins, int payRate);
        private static final int MINS_PER_SHIFT = 8 * 60;
        int pay(int minsWorked, int payRate) {
        int basePay = minsWorked * payRate;
            return basePay + overtimePay(minsWorked, payRate);
        }
    }
}
```

 EnumSet 类来有效地表示从单个枚举类型中提取的多个值的多个集合

###### 泛型

使用泛型，你告诉编译器在每个集合中允许哪些类型的对 象。 编译器会自动插入强制转换【

​		这种检查基于编译而非运行，所以说是不可编译并非不可运行。编译后会擦除泛型信息】

泛型有子类型的规则， List 是原始类型 List 的子类型，但不是参数化类型 List 的子类型。

泛型类型的不变性，为了API的使用范围更大，使用通配符【泛型类型的继承、向上（下）造型一个泛型对象的引用】。

[【**在编译时不确定运行时传入方法参数的具体类型【在不同地方方法调用会传入不同的泛型类型，主要是泛型类型间的继承关系】，方法内根据读写操作选取通配符**】](https://www.cnblogs.com/minikobe/p/11547220.html)

在编译过程中，正确检验泛型结果后，会将泛型的相关信息擦出，并且在对象进入和离开方法的边界处添加类型检查和类型转换的方法。也就是说，泛型信息不会进入到运行时阶段。【泛型编译时通过，则运行时就一定正常】

PECS：含通配符泛型类型对象的引用赋值具体的泛型类型对象【[通配符操作的限制](https://www.cnblogs.com/dtx0/p/8466127.html)】

- 如果你想从一个数据类型里获取数据，使用 ? extends 类型。? extends 类型【类型范围】通配符告诉编译器我们在处理一个类型T的子类型，但我们不知道这个子类型究竟是什么。因为没法确定，为了保证类型安全，我们就不允许往里面加入任何这种类型的数据。

- 如果你想把对象写入一个数据结构里，使用 ? super 通配符。赋值后不确定究竟是什么超类。

- 如果你既想存，又想取，那就别用通配符。

  

- `List`：没有类型参数的列表。它是一个列表，其元素可以是任何类型 - 元素可以是不同类型的。

- `List`<?>：具有无界类型参数的列表。它的元素是一种特定但未知的类型;元素必须都是相同的类型。

- `List`<? extends E>：无法确定实际的子类型，只能确定是E的子类型。只能获取。

[泛型递归模式](https://www.jianshu.com/p/de49bcb6bf08)
`public interface  A<T extends A<T>>      public class B implements A<B>`对于参数类型T是递归定义的。是对泛型类型的向上转型。父类方法和子类方法用同一个形式逻辑参数。这样类`A`中方法签名里涉及到参数类型`T`的地方，在实现类里会为实现类本身，这让类型系统更加的严谨。

泛型类型【客户端调用的时候用实际逻辑参数赋值形式逻辑参数得到参数化类型】、

通配符泛型类型限制赋值的参数化类型。



如果不传入泛型类型实参的话，在泛型类中使用泛型的方法或成员变量定义的类型可以为任何的类型

###### 注解

@Repeatable 元注释来注释注释的声明

###### 集合

[1.ConcurrentModificationException](https://www.jianshu.com/p/c5b52927a61a)



在这里，迭代ArrayList的Iterator中有一个变量`expectedModCount`，该变量会初始化和`modCount`相等，但如果接下来如果集合进行修改`modCount`改变，就会造成`expectedModCount!=modCount`，此时就会抛出java.util.ConcurrentModificationException异常



fail-fast机制就是为了防止当多个线程对同一个集合进行修改结构。
之所以有modCount这个成员变量，就是为了辨别多线程修改集合时出现的错误。

2.java.util包下都是快速失败，java.util.concurrent包下都是安全失败

CopyOnWrite容器【往一个容器添加元素的时候，不直接往当前容器添加，而是先将当前容器进行Copy，复制出一个新的容器，然后新的容器里添加元素，添加完元素之后，再将原容器的引用指向新的容器】是一种读写分离的思想，读和写不同的容器。可以发现在添加的时候是需要加锁的，否则多线程写的时候会Copy出N个副本出来。即多线程中也不能同时写。[ArrayList等必须要读写都加锁、避免脏读。ConcurrentModificationException作用是提示bug，多线程时集合【共享资源】不能被修改、修改后脏读]。

自定义CopyOnWriteMap减少扩容开销、使用批量添加。因为每次添加，容器每次都会进行复制。



###### 访问级别

类：公共或者包私有【私有或者包私有，可以在不影响客户端调用的时候替换实现类】

接口：默认公共的

方法：

###### 不可变

不可变【确保对任何可变组件的互斥访问。不提供修改状态的方法 final  private】类完全处于一种状态，也就是被创建时的状态。不需要做出防御性拷贝。不可变对象本质上是线程安全的。

不可变类的主要缺点是对于每个不同的值都需要一个单独的对象。

###### 方法

Objects.requireNonNull 方法非常灵活方便，因此没有理由再手动执行空值检查。

非公有的方法通常应该使用*断言（assertion）*来检查它们的参数

**对于构造器的每个可变参数进行保护性拷贝（defensive copy）是必要的**，不使用原来的对象。防止被更改导致对象内部状态改变。访问方法，**使它返回可变内部域的保护性拷贝即可**【可变域被真正封装在对象的内部。】（对象不想被客户端改变）

**保护性拷贝是在检查参数的有效性（第 49 项）之前进行的，并且有效性检查是针对拷贝之后的对象，而不是针对原始的对象**。

**类可以在它所在的包的外部进行访问，就提供访问方法**，以保留将来改变该类的内部表示法的灵活性。

###### 接口和类

【为继承而设计】超类**必须有文档说明它可覆盖（overridable）的方法的自用性（self-use）**。对于每个公有的或受保护的方法或者构造器，它的文档必须指出方法调用哪些可覆盖的方法，以何种顺序以及每次调用的结果是如何影响后续处理的。

骨架实现类仍然能够有助于接口的实现。实现了这个接口的类可以把对于这个接口方法的调用，转发到一个内部私有类的实例上，这个内部私有类扩展了骨架实现类。这种方法被称作模拟多重继承

如果大量利用工具类导出的常量，可以通过利用静态导入机制，避免用类名来修饰常量名。直接使用常量。

接口应该只被用来定义类型，他们不应该被用来导出常量。

 **如果声明一个不需要访问外围类实例的成员类，则始终将 static 修饰符放在其声明中，使其成为静态成员类而不是非静态成员类**。如果省略此修饰符，则每个实例将对其外围类实例具有隐式的无关引用。

局部类与匿名类一样，只有在非静态上下文中定义实例时，它们才会有外围类实例，并且它们不能包含静态成员。

【嵌套类能访问外部环境的变量】

 null 对象引用被自动拆箱，就会得到一个 NullPointerException 异常

基本类型和包装类型：null、空间、相等性

**字符串也不适合代替能力（capabilities）**【缺乏唯一性】

**字符串不适合代替聚集类型** 。【静态内部类替代】

**字符串不适合代替枚举类型** 。

需要精确答案的计算任务，请不要使用 float 或者 double。使用int或者long【自己记录小数点】，使用BigDecimal【有具体的舍入模式】

谨慎的进行优化，遵循良好的编写习惯【好的代码才会有好的性能】

stream只有在终端操作出现才计算。

**forEach 操作应仅用于报告流计算的结果，而不是用于执行计算**

###### 多线程

[Thread和Runnable的实质是继承关系](https://blog.csdn.net/zhaojianting/article/details/97664370)，没有可比性。无论使用Runnable还是Thread，都会new Thread，然后执行run方法。共享资源在获取时必须加锁。

Spring 中的bean [是线程安全的吗](https://cloud.tencent.com/developer/article/1585249)

###### 动态代理

cglib是通过创建被代理类子类实现代理。通过方法拦截器拼接横切逻辑和业务逻辑，在代理对象调用方法时拦截调用。  https://www.cnblogs.com/writeLessDoMore/p/6973853.html

jdk动态代理实际上是创建一个代理Handler实例的类

###### 异常

反射调用异常会被invacationException包裹，可以去目标异常的异常堆栈去查看原因

```
InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    System.out.println(m + " failed: " + exc);
```

异常应该只用于异常的情况下；他们永远不应该用于正常的程序控制流程。

###### BigDecimal

divide()极容易出现一个错误，当除不尽时候，会产生无限循环小数，这时将会抛出异常：

double 参数的构造方法,不允许使用!!!!因为它不能精确的得到相应的值;

String 构造方法是完全可预知的: 写入 new BigDecimal("0.1") 将创建一个 BigDecimal,它正好等于预期的0.1; 因此,通常建议优先使用 String 构造方法;

###### [threadLocal](https://cloud.tencent.com/developer/article/1585249) 

每个线程都有一个map，随时随地可以存放东西。将自身threadlocal作用键。

用弱引用包装了key，实现了变量不用就回收的基本原则，与Java的传统思想一脉相承。

调用remove()方法，就会删除对应的Entry对象，可以避免内存泄漏【线程如果一直运行】，所以使用完ThreadLocal后，要调用remove()方法。

Spring框架中，如果可以使用RequestContextHolder，那么就不需要自己维护ThreadLocal，因为自己可能会忘记调用remove()方法等，造成内存泄漏。

###### SimpleDateFormat

SimpleDateFormat中的方法不是线程安全的，多线程访问会出现意想不到的结果。解决的思路就是，避免多线程使用同一个SimpleDateFormat对象。

##### java8

###### lamda

lambda只是声明，和声明变量是一样的道理

lamda表达式方法体中引用外部变量是lambda表达式实例化的时候，编译器会创建一个新的class文件，通过构造器注入的。

方法体中的外部变量必须是final，因为如果是基本类型，在声明后修改就无法感知到修改。

第 26 项告诉你不要使用原始类型【List】，第 29 项告诉你支持泛型类型，第 30 项告诉你支持泛型方法。便于上下文类型推断。

在 lambda 表达式中使用 this 关键字，获得的引用是 lambda 所在的实例的引用，在匿名类中使用 this 关键字，获得的是当前匿名类的实例的引用。

lambda 和匿名类都无法被序列化和反序列化

###### 方法引用

快速生成函数对象

如果 lambda 变得太长或太复杂：你可以将 lambda 中的代码提取到一个新方法中，并用对该方法的引用替换 lambda。

> ##### javaee
>

###### servlet生命周期

HttpSession 私有区域。 客户端浏览器第一次请求服务器，服务器端的Servlet调用了request.getSession()时创建。   ServletContext 公共区域

*@*Inherited 只是可控制 对类名上注解是否可以被继承。方法没被实现和重新就可被继承注解。

使用单个对象进行所有必需的类型参数化，但是需要编写一个静态工厂方法来重复地为每个请求的类型参数化分 配对象。 这种称为泛型单例工厂。

数组是协变和具体化的; 泛型是不变的，类型擦除的。 因此，数组 提供运行时类型的安全性，但不提供编译时类型的安全性，反之亦然。 一般来说，数组和泛型不能很好地混合工 作。 如果你发现把它们混合在一起，得到编译时错误或者警告，你的第一个冲动应该是用列表来替换数组。

- 



