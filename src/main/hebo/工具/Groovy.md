导读：在看spring源码的时候，发现使用的构建工具是gradle，然后gradle是由Groovy编写的。同时也久听Groovy的大名。于是在了解了Gradle的同时学习起了Groovy。大多推荐《Groovy in action》，在豆瓣上的书评也会指引我如何读、学习，在这本书发现了好多python相似的语法，并且的确简洁明了、令人振奋和惊讶。同时和java同源，在解释Groovy设计想法的时候会带出java的设计的不足之处。

Groovy本质上是一个拥有动态特性的字节码语言[**有相同的Class对象**]。groovy 有自己的编译器，通过 java 命令运行其预编译的字节码需要在 classpath 额外加上 groovy 自己特有的 **GDK** jar 包和类库，在执行之前是被完整编译、构建的，不是直接执行源码。

注意书籍版本、书籍配套源码、以及官网参考！

变量【容器】的类型是否可以改变！

所有的groovy代码都运行在JVM中并且使用的是java对象模型，不管你写的是groovy类，或者是groovy脚本，它们都作为java类在JVM中运行。

- 基于源码直接运行，譬如 groovy Test.groovy。**通过groovy的类加载器在运行时直接加载*.groovy文件并且生成Class对象**
- 基于预编译模式运行，譬如先 groovyc Test.groovy 然后 java Test。

Groovy在运行时可以像读取*.class文件一样读取*.groovy文件

类能够在运行时生成，唯一的区别是输入的字符串也可以像*.groovy一样生成类。

通过MetaClass，groovy能修改groovy调用者调用的任何方法

在groovy中出现的字面符号（数字、字符串等等）没有任何问题，他们都是对象，他们仅仅传递给java时才进行装箱和拆箱，操作符是方法调用的快速途径

能互相调用【集成】、语法接近【更顺利的学习曲线】。并且拥有众多的语法特性

使用GString对String进行功能增强，简化了的for循环处理，可选的类型声明，可选的语句结束符（在java中为分号）



groovyc产生一个父类为groovy.lang.Script的java类，这个类包括一个main方法，因此java能直接运行它，编译出来的class的名称同脚本的名称是一致的，在这里为Fibonacci。



groovy中，这两者都是允许的，能够在数字上使用数字操作符，并且也可以在数字实例上调用方法。def x = 1

groovy在java上有三个主要类型的特性：语言特性，groovy类库和附加到已经存在的java标准类上的功能（GDK）

![image-20210114222720442](https://gitee.com/workerbo/gallery/raw/master/2020/image-20210114222720442.png)





Gstring通过双引号和$符号进行判断。

通过${}符号表示的完全表达式，在花括号中可以是任意的groovy表达式，花括号表示一个闭包

Groovy通过/编写模式，不用对\转义。

一些方法工作在集合的一个副本上并且完成的时候返回这个副本，而另外一些方法直接工作在调用的这个集合对象上（这个对象我们叫做接受者对象）

在groovy中类型是可选的，因此闭包的参数是可选的，如果闭包的参数进行了显式的类型声明，那么类型的检查发生在运行时而不是在编译的时候。

不像java，Boolean测试的类型没有被严格的限制为boolean。=出现在表达式中带来意外错误

groovy的立足点是它的一组动态特性

缺省的方法访问范围为public。

groovy提供了额外的（?.）操作符来进行安全的引用，当操作符之前是一个null引用的时候，当前表达式的评估被终止，并且null被返回。