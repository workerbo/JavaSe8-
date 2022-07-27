#### [JackSon](https://juejin.cn/post/6844904166809157639)

Jackson 的核心模块由三部分组成。

- jackson-core，核心包，提供基于"流模式"解析的相关 API，它包括 JsonPaser 和 JsonGenerator。 Jackson 内部实现正是通过高性能的流模式 API 的 JsonGenerator 和 JsonParser 来生成和解析 json。
- jackson-annotations，注解包，提供标准注解功能；
- jackson-databind ，数据绑定包， 提供基于"对象绑定" 解析的相关 API （ ObjectMapper ） 和"树模型" 解析的相关 API （JsonNode）；基于"对象绑定" 解析的 API 和"树模型"解析的 API 依赖基于"流模式"解析的 API。




Jackson 最常用的 API 就是基于"对象绑定" 的 ObjectMapper：它提供一些功能将转换成Java对象匹配JSON结构，反之亦然。它使用JsonParser和JsonGenerator的实例实现JSON实际的读/写。

默认情况下，Jackson通过将JSON字段的名称与Java对象中的getter和setter方法进行匹配，将JSON对象的字段映射到Java对象中的属性。 Jackson删除了getter和setter方法名称的“ get”和“ set”部分，并将其余名称的第一个字符转换为小写。



```
String string = objectMapper.writeValueAsString(user);
 insurance = objectMapper.readValue(data.get(i), Insurance.class);
```



##### 配置

通过编码配置或者注解

配置Jackson ObjectMapper忽略未知字段的示例：

```
objectMapper.configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

ObjectMapper会忽略原始字段的空值。 但是，可以将Jackson ObjectMapper配置设置为失败。

```
ObjectMapper objectMapper = new ObjectMapper();

objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

           JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Date.class, new DateSerializer());
            javaTimeModule.addDeserializer(Date.class, new DateDeserializer());
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            /**声明自定义模块**/
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(String.class, new SensitiveStringSerializer());
            simpleModule.addDeserializer(String.class, new TrimStringDeserializer());
            ObjectMapper mapper = (ObjectMapper)bean;
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.disable(new MapperFeature[]{MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS});
            mapper.registerModules(new Module[]{simpleModule, javaTimeModule});
```


​       默认情况下，Jackson会将java.util.Date对象序列化为其long型的值，该值是自1970年1月1日以来的毫秒数。但是，Jackson还支持将日期格式化为字符串。

```
 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  objectMapper.setDateFormat(dateFormat);
  
```

Jackson ObjectMapper 提供了TypeReference支持对泛型对象的反序列化；

###### TypeReference

 TypeReference 是描述 一个复杂 泛型的工具类。

#### Jackson JSON 树模型

Jackson具有内置的树模型，可用于表示JSON对象。 如果需要在使用或转化JSON之前对其进行操作。

```
  JsonNode jsonNode = objectMapper.readValue(carJson, JsonNode.class);
  或者
   JsonNode jsonNode = objectMapper.readTree(carJson);
  
  java转json对象
   JsonNode carJsonNode = objectMapper.valueToTree(car);
   json转java
   Car car = objectMapper.treeToValue(carJsonNode);
```



##### 注解

@JsonFormat