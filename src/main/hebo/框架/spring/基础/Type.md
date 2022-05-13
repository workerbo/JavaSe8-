## 概述

在class中是保存了一些泛型信息的。通配符用于标识任意实际类型【中间状态，最终也要实际类型。搭配上界，无论传入的是何种类型的集合，我们都可以使用其父类的方法统一处理。】，泛型变量用于声明一个变量

Java 编程语言从Java 5以后就引入**Type**体系，应该是为了加入泛型而引入的。其存在于`java.lang.reflect`包下面，所以平时较少使用，但是理解Java类型体系是深入理解Java泛型的基础，也是一个**高级**Java开发者的必备素质。

## Type 系统总览

Java Type 体系，始于`Type`接口，其是Java编程语言中所有类型的父接口，是对Java编程语言类型的一个抽象，源码如下所示：

```text
public interface Type {
    //返回这个类型的描述，包括此类型的参数描述。
    default String getTypeName() {
        return toString();
    }
}
```

其有4个子接口`GenericArrayType`，`ParameterizedType`，`TypeVariable`，`WildcardType`和一个实现类`Class`, Java 的整个泛型体系就由他们支撑，下面我们分别看一下他们到底是个什么东东:

我们需要一个类和一个注解来作为讲解代码的基础。

示例类TypeTest 是一个泛型类，声明了两个泛型参数T 和 V，一个构造函数和一个泛型方法以及若干属性。如果你看不懂下面这个示例类，说明你对Java 泛型的了解还停留在初级阶段，你更需要这篇文章[秒懂Java泛型](https://link.zhihu.com/?target=https%3A//blog.csdn.net/ShuSheng0007/article/details/80720406)，然后才是这篇，如若强行阅读，效果可能不佳。

```text
/**
 * Created by Shusheng007 on 2019/4/25.
 * 泛型类，参数为T 和 V
 */
public class TypeTest<T, V extends @Custom Number & Serializable> {
    private Number number;
    public T t;
    public V v;
    public List<T> list = new ArrayList<>();
    public Map<String, T> map = new HashMap<>();

    public T[] tArray;
    public List<T>[] ltArray;

    public TypeTest testClass;
    public TypeTest<T, Integer> testClass2;

    public Map<? super String, ? extends Number> mapWithWildcard;

    //泛型构造函数,泛型参数为X
    public <X extends Number> TypeTest(X x, T t) {
        number = x;
        this.t = t;
    }

    //泛型方法，泛型参数为Y
    public <Y extends T> void method(Y y) {
        t = y;
    }
}
```

自定义注解 Custom：

```text
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ANNOTATION_TYPE, CONSTRUCTOR, FIELD,
        METHOD, PACKAGE, PARAMETER, TYPE, TYPE_PARAMETER, TYPE_USE})
public @interface Custom {
}
```

下面的`code` 都是以上面的类为基础撰写的。

个人觉得我们应该先从`TypeVariable`作为切入点来进入Java Type的世界，因为它是其他类型的基础。

## TypeVariable：

类型变量，例如`List`中的`T,` `Map`中的`K`和`V`，我们的测试类`class TypeTest`中的`T`和`V`。

此接口源码如下：

```text
interface TypeVariable<D extends GenericDeclaration> extends Type, AnnotatedElement {
    //返回此类型参数的上界列表，如果没有上界则放回Object. 例如  V extends @Custom Number & Serializable 这个类型参数，有两个上界，Number 和 Serializable 
    Type[] getBounds();
    //类型参数声明时的载体，例如 `class TypeTest<T, V extends @Custom Number & Serializable>` ，那么V 的载体就是TypeTest
    D getGenericDeclaration();
    String getName();
    //Java 1.8加入 AnnotatedType: 如果这个这个泛型参数类型的上界用注解标记了，我们可以通过它拿到相应的注解
    AnnotatedType[] getAnnotatedBounds();
}
```

从typeVariable的定义看到其也有一个泛型参数，要求需要是GenericDeclaration 的子类，

```text
//所有可以申明泛型参数的entities都必须实现这个接口
 public interface GenericDeclaration extends AnnotatedElement {     
    public TypeVariable<?>[] getTypeParameters();
}
```

我们从源码中看到，只有三个类实现了这个接口,分别是：

```text
java.lang.reflect.Method,
java.lang.reflect.Constructor，
java.lang.Class
```

所以我们只能在**类型**（例如Class，Interface）、**方法**和**构造函数**这三个地方声明泛型参数，其他地方只能使用。不明白这块的，请自行参考 [秒懂Java泛型](https://link.zhihu.com/?target=https%3A//blog.csdn.net/ShuSheng0007/article/details/80720406)

翠花提醒到，二狗别哔哔了，千言万语不如一段源码，该上code了：

```text
//****************************TypeVariable************************
Field v = TypeTest.class.getField("v");//用反射的方式获取属性 public V v; 
TypeVariable typeVariable = (TypeVariable) v.getGenericType();//获取属性类型
System.out.println("TypeVariable1:" + typeVariable);
System.out.println("TypeVariable2:" + Arrays.asList(typeVariable.getBounds()));//获取类型变量上界
System.out.println("TypeVariable3:" + typeVariable.getGenericDeclaration());//获取类型变量声明载体
//1.8 AnnotatedType: 如果这个这个泛型参数类型的上界用注解标记了，我们可以通过它拿到相应的注解
AnnotatedType[] annotatedTypes = typeVariable.getAnnotatedBounds();        
System.out.println("TypeVariable4:" + Arrays.asList(annotatedTypes) + " : " +
                                                                    Arrays.asList(annotatedTypes[0].getAnnotations()));
System.out.println("TypeVariable5:" + typeVariable.getName());
```

输出结果为：

```text
TypeVariable1:V
TypeVariable2:[class java.lang.Number, interface java.io.Serializable]
TypeVariable3:class typeInfo.TypeTest
TypeVariable4:[sun.reflect.annotation.AnnotatedTypeFactory$AnnotatedTypeBaseImpl@511d50c0, sun.reflect.annotation.AnnotatedTypeFactory$AnnotatedTypeBaseImpl@60e53b93] : [@typeInfo.Custom()]
TypeVariable5:V
```

值得注意的是，我们通过`annotatedTypes[0].getAnnotations()[0]`获取到了类型参数`V`的上界`Number` 上的注解`Custom()`,这里需要重点理解一下。

## ParameterizedType:

参数化类型，即带参数的类型，也可以说带`<>`的类型。例如`List`, `User` 等。

其源码如下：

```text
interface ParameterizedType extends Type {
     //获取参数类型<>里面的那些值,例如Map<K,V> 那么就得到 [K,V]的一个数组
     Type[] getActualTypeArguments(); 
     //获取参数类型<>前面的值，例如例如Map<K,V> 那么就得到 Map
     Type getRawType();
     //获取其父类的类型，例如Map 有一个内部类Entry,  那么在Map.Entry<K,V> 上调用这个方法就可以获得 Map
     Type getOwnerType();
}
```

这个接口的3个方法已经在上面描述的很清楚了，接下来我实际操作一下。翠花，上**code**：

```text
//*********************************ParameterizedType**********************************************       
Field list = TypeTest.class.getField("list");
Type genericType1 = list.getGenericType();
System.out.println("参数类型1:" + genericType1.getTypeName()); //参数类型1:java.util.List<T>

Field map = TypeTest.class.getField("map");
Type genericType2 = map.getGenericType();
System.out.println("参数类型2:" + genericType2.getTypeName());//参数类型2:java.util.Map<java.lang.String, T>

if (genericType2 instanceof ParameterizedType) {
     ParameterizedType pType = (ParameterizedType) genericType2;
     Type[] types = pType.getActualTypeArguments();
     System.out.println("参数类型列表:" + Arrays.asList(types));//参数类型列表:[class java.lang.String, T]
     System.out.println("参数原始类型:" + pType.getRawType());//参数原始类型:interface java.util.Map
     System.out.println("参数父类类型:" + pType.getOwnerType());//参数父类类型:null,因为Map没有外部类，所以为null
}
```

输出：

```text
参数类型1:java.util.List<T>
参数类型2:java.util.Map<java.lang.String, T>
参数类型列表:[class java.lang.String, T]
参数原始类型:interface java.util.Map
参数父类类型:null
```

上面的代码，先使用反射获取`TypeTest`类的`List list` 和`Map map`属性的类型，然后调用getGenericType()获取他们的声明类型，他们是ParameterizedType类型，然后调用里面的方法。

## GenericArrayType

泛型数组类型，用来作为数组的泛型声明类型。例如`List[] ltArray`， `T[] tArray`两个数组，其中`List[], 和`T[]`就是`GenericArrayType`类型。

此接口的源码如下：

```text
public interface GenericArrayType extends Type {
    //获取泛型类型数组的声明类型，即获取数组方括号 [] 前面的部分
    Type getGenericComponentType();
}
```

`GenericArrayType` 接口只有一个方法getGenericComponentType()，其可以用来获取数组方括号 **[]** 前面的部分，例如`T[]`，在其上调用getGenericComponentType 就可以获得`T`. 值得注意的是多维数组得到的是最后一个[] 前面的部分，例如`T[][]`, 得到的是`T[]`.

翠花，上code：

```text
//**********************GenericArrayType*********************
 Field tArray = TypeTest.class.getField("tArray");
 System.out.println("数组参数类型1:" + tArray.getGenericType());
 Field ltArray = TypeTest.class.getField("ltArray");
 System.out.println("数组参数类型2:" + ltArray.getGenericType());//数组参数类型2:java.util.List<T>[]
 if (tArray.getGenericType() instanceof GenericArrayType) {
     GenericArrayType arrayType = (GenericArrayType) tArray.getGenericType();
     System.out.println("数组参数类型3:" + arrayType.getGenericComponentType());//数组参数类型3:T
 }
```

输出：

```text
数组参数类型1:T[]
数组参数类型2:java.util.List<T>[]
数组参数类型3:T
```

## WildcardType:

通配符类型，即带有`?`的泛型参数, 例如 `List`中的`？`，`List`里的`? extends Number` 和`List`的`? super Integer` 。

此接口源码如下：

```text
public interface WildcardType extends Type {
   // 获取上界
    Type[] getUpperBounds();
    //获取下界
    Type[] getLowerBounds();
}
```

翠花,上**code**：

```text
//***************************WildcardType*********************************
 Field mapWithWildcard = TypeTest.class.getField("mapWithWildcard");
 Type wild = mapWithWildcard.getGenericType();//先获取属性的泛型类型 Map<? super String, ? extends Number>
 if (wild instanceof ParameterizedType) {
     ParameterizedType pType = (ParameterizedType) wild;
     Type[] actualTypes = pType.getActualTypeArguments();//获取<>里面的参数变量 ? super String, ? extends Number
     System.out.println("WildcardType1:" + Arrays.asList(actualTypes));
     WildcardType first = (WildcardType) actualTypes[0];//? super java.lang.String
     WildcardType second = (WildcardType) actualTypes[1];//? extends java.lang.Number
     System.out.println("WildcardType2: lower:" + Arrays.asList(first.getLowerBounds()) + "  upper:" + Arrays.asList(first.getUpperBounds()));//WildcardType2: lower:[class java.lang.String]  upper:[class java.lang.Object]
     System.out.println("WildcardType3: lower:" + Arrays.asList(second.getLowerBounds()) + "  upper:" + Arrays.asList(second.getUpperBounds()));//WildcardType3: lower:[]  upper:[class java.lang.Number]
 }
```

输出：

```text
WildcardType1:[? super java.lang.String, ? extends java.lang.Number]
WildcardType2: lower:[class java.lang.String]  upper:[class java.lang.Object]
WildcardType3: lower:[]  upper:[class java.lang.Number]
```

## Class:

其是Type的一个实现类，是反射的基础，每一个类在虚拟机中都对应一个Calss 对象,我们可以用在运行时从这个Class对象中获取到类型所有信息。

```text
//**********************************Class*********************************
 Field tClass = TypeTest.class.getField("testClass");
 System.out.println("Class1:" + tClass.getGenericType());//获取泛型类型，由于我们这个属性声明时候没有使用泛型，所以会获得原始类型
 Field tClass2 = TypeTest.class.getField("testClass2");
 System.out.println("Class2:" + tClass2.getGenericType());//获取泛型类型
```

输出：

```text
Class1:class typeInfo.TypeTest
Class2:typeInfo.TypeTest<T, java.lang.Integer>
```

可以看到 属性 public TypeTest testClass; 通过getGenericType()获取到的类型就是其原始类型`TypeTest`。而属性 public TypeTest testClass2;获取到的则是`ParameterizedType`类型`TypeTest`。