#### FastJson

FastJson对于json格式字符串的解析主要用到了下面三个类：
1.JSON：fastJson的解析器，用于JSON格式字符串与JSON对象及javaBean之间的转换
2.JSONObject：fastJson提供的json对象【继承了JSON，实现了Map接口】
3.JSONArray：fastJson提供json数组对象

我们在使用fastjson的时候，一般来讲，只需要使用最简单的API，例如

```

 Student stu = new Student("公众号编程大道", "m", 2);

        //Java对象转化为JSON对象
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(stu);
          //JSON对象转换成Java对象
         Student student = JSONObject.toJavaObject(jsonObject, Student.class);
         
         //JSON字符串转java对象
        Student stu = JSON.parseObject("{...}", Studentclass); //反序列化
        //java对象转JSON字符串
      String  str=  JSON.toJSONString(stu)
   
        //JSON字符串转换成JSON对象

        JSONObject jsonObject1 = JSONObject.parseObject(stuString);
         //JSON对象转换为JSON字符串

        String jsonString = jsonObject.toJSONString();
        
        
        创建Json对象
         JSONObject object = new JSONObject();

            object.put("name",stu.getName());

            object.put("age",stu.getAge());

            System.out.println(object.get("name")  + ":" + object.get("age"));

```



###### 注解

@JSONField 的作用对象:

- Field
- Setter 和 Getter 方法

@JSONField(name="DATE OF BIRTH", format="dd/MM/yyyy", ordinal = 3)

###### SerializerFeature

但是如果我们希望我们的JSON对象在[序列化](https://so.csdn.net/so/search?q=序列化&spm=1001.2101.3001.7020)和反序列化有一些特殊的要求，例如使用单引号而不是双引号，我们可以通过配置来达到。

注意：FastJson 在进行操作时，是根据 getter 和 setter 的方法进行的，并不是依据 Field 进行。

注意：若属性是私有的，必须有 set 方法。否则无法反序列化。

######  7、JSONArray数组

```html
JSONArray中的数据转换为String类型需要在外边加"";不然会报出类型强转异常！
public String testArray(){
        String s = "[{\"id\":\"17051801\",\"name\":\"lucy\"},{\"id\":\"17051802\",\"name\":\"peter\"}]";

        JSONArray array = JSONObject.parseArray(s);

        for (int i = 0; i < array.size(); i++) {

            //JSONArray中的数据转换为String类型需要在外边加"";不然会报出类型强转异常！
            String str = array.get(i)+"";
            JSONObject object = JSON.parseObject(str);
            System.out.println(object.get("name"))；

        }
        return "index";
    }
```

###### 使用 ContextValueFilter 配置 JSON 转换

在某些场景下，对Value做过滤，需要获得所属JavaBean的信息，包括类型、字段、方法等。在fastjson-1.2.9中，提供了ContextValueFilter，类似于之前版本提供的ValueFilter，只是多了BeanContext参数可用。

```
@Test
public void givenContextFilter_whenJavaObject_thanJsonCorrect() {
    ContextValueFilter valueFilter = new ContextValueFilter () {
        public Object process(
          BeanContext context, Object object, String name, Object value) {
            if (name.equals("DATE OF BIRTH")) {
                return "NOT TO DISCLOSE";
            }
            if (value.equals("John")) {
                return ((String) value).toUpperCase();
            } else {
                return null;
            }
        }
    };
    String jsonOutput = JSON.toJSONString(listOfPersons, valueFilter);
}
```

以上实例中我们隐藏了 DATE OF BIRTH 字段，并过滤名字不包含 John 的字段：

```
[
    {
        "FULL NAME":"JOHN DOE",
        "DATE OF BIRTH":"NOT TO DISCLOSE"
    }
]
```

###### 使用 NameFilter 和 SerializeConfig

NameFilter: 序列化时修改 Key。

SerializeConfig：内部是个map容器主要功能是配置并记录每种Java类型对应的序列化类。

```
@Test
public void givenSerializeConfig_whenJavaObject_thanJsonCorrect() {
    NameFilter formatName = new NameFilter() {
        public String process(Object object, String name, Object value) {
            return name.toLowerCase().replace(" ", "_");
        }
    };
     
    SerializeConfig.getGlobalInstance().addFilter(Person.class,  formatName);
    String jsonOutput =
      JSON.toJSONStringWithDateFormat(listOfPersons, "yyyy-MM-dd");
}
```

实例中我们声明了 formatName 过滤器使用 NameFilter 匿名类来处理字段名称。 新创建的过滤器与 Person 类相关联，然后添加到全局实例，它是 SerializeConfig 类中的静态属性。

现在我们可以轻松地将对象转换为JSON格式。

注意我们使用的是 toJSONStringWithDateFormat() 而不是 toJSONString() ，它可以更快速的格式化日期。

