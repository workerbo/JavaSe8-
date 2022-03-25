DriverManage在决定使用哪个驱动的时候并不是由开发者指定的，而是通过遍历所有已注册的驱动来尝试获取连接，成功就返回，失败就next，所以代码中并没有显示的指定驱动

```
Class.forName("com.mysql.jdbc.Driver");
```

在类加载时会自动执行，所以就把自己注册到DriverManage的registerDriver中了，这样整个流程就全部通了



每一个 SPI 接口都需要在实现类项目的静态资源目录中声明一个 services 文件，文件名为实现规范接口的类名全路径，在文件中，则写上一行具体实现类的全路径

