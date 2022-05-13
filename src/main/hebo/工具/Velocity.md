###### 应用场景

Web 应用：开发者在不使用 JSP 的情况下，可以用 Velocity 让 HTML 具有动态内容的特性
源代码生成：Velocity 可以被用来生成 Java 代码、SQL 或者 PostScript
自动 Email：很多软件的用户注册、密码提醒或者报表都是使用 Velocity 来自动生成的
转换 xml

Velocity是一个基于java的模板引擎（template engine），它允许任何人仅仅简单的使用模板语言（template language）来引用由java代码定义的对象。

在 Velocity 中所有的关键字都是以#开头的，而所有的变量则是以$开头。"{}"用来明确标识Velocity变量；"!"用来强制把不存在的变量显示为空白。



关于#set的使用
　　在万不得已的时候，不要在页面视图自己声明Velocity脚本变量，也就是尽量少使用#set。有时候我们需要在页面中显示序号，而程序对象中又没有包含这个序号属性同，可以自己定义。如在一个循环体系中，如下所示：
　　#set ($i=0)
　　#foreach($info in $list)
　　序号:$i
　　#set($i=$i+1)
　　#end



15、定义宏Velocimacros ,相当于函数 支持包含功能
　　#macro( d )
　　 <tr><td></td></tr>
　　#end
　　调用
　　#d()

16、带参数的宏
　　#macro( tablerows $color $somelist )
　　#foreach( $something in $somelist )
　　 <tr><td bgcolor=$color>$something</td></tr>
　　#end
　　#end