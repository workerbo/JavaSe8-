

###### 日期

```
String dateStr = "2021-04-21 01:02:03";
Date date = DateUtil.parse(dateStr);
 
//结果 2021/04/21
String format = DateUtil.format(date, "yyyy/MM/dd");
 
//常用格式的格式化，结果：2021-04-21
String formatDate = DateUtil.formatDate(date);
 
//结果：2021-04-01 01:02:03
String formatDateTime = DateUtil.formatDateTime(date);
 
//结果：01:02:03
String formatTime = DateUtil.formatTime(date);
```

###### 字符串

```
String a="db dg";
// 判断是否为空
//  hasBlank 和 hasEmpty的区别
// hasBlank 判断是否为null或者空字符串和不可见的字符也所做是空
boolean b = StrUtil.hasBlank(a);
// hasEmpty 只会判断 是否为null或者是空字符串
boolean b1 = StrUtil.hasEmpty(a);
// 去掉指定的前缀
String c=a+"dddd";
String s = StrUtil.removePrefix(c, a);
// 去掉指定的后缀
String d="test.properties";
String properties = StrUtil.removeSuffix(d, ".properties");
// 格式化字符串
// %s 代表一个字符串的占位
String format = String.format("你是%s", "小明");
// hutool中 格式化字符串
String template = "{}爱{}，就像老鼠爱大米";
String format1 = StrUtil.format(template, "我", "你");

// 字符串反转
String e="abcd";
String reverse = StrUtil.reverse(e);

```

##### 语言特性

Java中有`assert`关键字，但是存在许多问题：

1. assert关键字需要在运行时候显式开启才能生效，否则你的断言就没有任何意义。
2. 用assert代替if是陷阱之二。assert的判断和if语句差不多，但两者的作用有着本质的区别：assert关键字本意上是为测试调试程序时使用的，但如果不小心用assert来控制了程序的业务流程，那在测试调试结束后去掉assert关键字就意味着修改了程序的正常的逻辑。
3. assert断言失败将面临程序的退出。

因此，并不建议使用此关键字。相应的，在Hutool中封装了更加友好的Assert类，用于断言判定。

Assert类更像是Junit中的Assert类，也很像Guava中的Preconditions，主要作用是在方法或者任何地方对参数的有效性做校验。当不满足断言条件时，会抛出IllegalArgumentException或IllegalStateException异常。

#### 参考

- [hutool的wiki](https://hutool.mydoc.io/#text_319431)