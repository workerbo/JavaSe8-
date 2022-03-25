

JSON.stringify()方法用于把JavaScript值（对象或者数组）序列化为JSON字符串，并返回序列化后的JSON字符串。调用格式为：
JSON.stringify(value[, replacer[, space]])

在序列化一个对象时，如果该对象拥有 toJSON()方法，那么该 toJSON() 方法就会覆盖该对象默认的序列化行为。被序列化的不是那个原始对象，而是调用 toJSON() 方法后返回的那个对象。

如果非数组对象的成员是undefined、或任意的函数、或 symbol 值，这个成员会被省略。如果数组对象的成员是undefined、或任意的函数、或 symbol 值，则这些值被转成null。如：

### java类中serialVersionUID的作用

serialVersionUID适用于java序列化机制。简单来说，JAVA序列化的机制是通过 判断类的serialVersionUID来验证的版本一致的。在进行反序列化时，JVM会把传来的字节流中的serialVersionUID于本地相应实体类的serialVersionUID进行比较。如果相同说明是一致的，可以进行反序列化，否则会出现反序列化版本一致的异常，即是InvalidCastException。

当实现java.io.Serializable接口中没有显示的定义serialVersionUID变量的时候，JAVA序列化机制会根据Class自动生成一个serialVersionUID作序列化版本比较用，这种情况下，如果Class文件(类名，方法明等)没有发生变化(增加空格，换行，增加注释等等)，就算再编译多次，serialVersionUID也不会变化的。

序列化保存的是对象的状态，因此 序列化并不保存静态变量。



Transient 关键字的作用是控制变量的序列化，在变量声明前加上该关键字，可以阻止该变量被序列化到文件中，在被反序列化后，transient 变量的值被设为初始值