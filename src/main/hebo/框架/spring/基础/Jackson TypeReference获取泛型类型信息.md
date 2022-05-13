# Java泛型 | Jackson TypeReference获取泛型类型信息



## 前言

Jackson是一个比较流行的Json序列化和反序列化框架。本文以Jackson为例介绍TypeReference实现涉及泛型的反序列化，及TypeReference的实现原理。对于获取泛型类型信息的场景，TypeReference是一个可以参考的通用解决方案。

## 实例

Jackson ObjectMapper的readValue可以将Json字符串反序列化为Java对象。如下例中将`[{"id":null,"name":" ","age":500,"gender":false,"email":"email","employed":true,"salary":10}]`反序列化为`List`类型。

Json字符串：

```json
[{
    "id": null,
    "name": " ",
    "age": 500,
    "gender": false,
    "email": "email",
    "employed": true,
    "salary": 10
}]
```

UserResource实体类：

```java
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResource {
    private UUID id;
    private String name;
    private int age;
    private boolean gender;
    private String email;
    private boolean employed;
    private BigDecimal salary;
}
```

## 实现

### 理想的实现方式

理想的实现方式是告诉`ObjectMapper`的`readValue`方法，我要的是`List`，帮我反序列化成这个类型。

```java
List<UserResource> list = new ObjectMapper().readValue(userResourcesStr, List<UserResource>.class);
```

现实是编译器告诉你这不行，`Cannot select from parameterized type`. 也很好理解，Java编译器认为List是Class，而List<UserResource>则不是。

### 换一种方式实现

既然不能用`List.class`, 那如果我告诉`ObjectMapper`的`readValue`方法，我要的是`List`类型，但返回值类型是`List`, 会发生什么呢？

```java
List<UserResource> list = new ObjectMapper().readValue(userResourcesStr, List.class);
```

这时候倒没有编译错误, 但是会有警告：`Unchecked assignment: 'java.util.List' to 'java.util.List'`, 显然ObjectMapper并不能反序列化为UserResource类型，而是LinkedHashMap类型。如下图所示：

![img](https://pic4.zhimg.com/v2-f03564ab7d123b4c68522822f6f676ab_b.jpg)

### TypeReference的实现方式

ObjectMapper提供了`readValue(String content, TypeReference valueTypeRef)`接口，第二个参数为new一个`TypeReference`的子类实例：`new TypeReference>(){}`。泛型抽象类TypeReference用于通过子类获取完整的泛型类型信息。

```java
public <T> T readValue(String content, TypeReference valueTypeRef)
List<UserResource> list = new ObjectMapper().readValue(userResourcesStr, new TypeReference<List<UserResource>>(){});
```

![img](https://pic4.zhimg.com/v2-74e626ef3f3495787546c1152813375b_b.jpg)

## TypeReference 实现原理

上例中`new TypeReference>(){}`子类的实例，`TypeReference`源码部分比较简单，主要逻辑是，通过`getClass().getGenericSuperclass()`获取父类中的参数化类型（ParameterizedType）：

TypeReference主要源码：

```java
protected TypeReference()
    {
        Type superClass = getClass().getGenericSuperclass();
        _type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
```

getGenericSuperclass返回一个Type类型的对象，代表实体（class, interface, primitive type or void）的直接父类，如果父类是参数化类型，则返回的Type对象可准确反映源代码中使用的实际type参数。

Class的genericInfo：

![img](https://pic3.zhimg.com/v2-25efecb92601598953234ce989a5f40a_b.jpg)

## 总结

- Jackson ObjectMapper 提供了TypeReference支持对泛型对象的反序列化；
- 对于获取泛型类型信息的场景，TypeReference是一个可以参考的通用解决方案。