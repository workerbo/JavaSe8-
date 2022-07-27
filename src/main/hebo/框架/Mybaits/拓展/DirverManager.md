我们知道DirverManager的类加载器是启动类加载器（BootstrapClassloader）,而数据库驱动类时各个数据库厂商提供的，显然BootstrapClassloader是无法加载数据库驱动类的。而根据类加载器的双亲委派模型，第三方实现的类库应该由系统类加载器（AppClassLoader）去加载，然而BootstrapClassloader是类加载器的最高层，无法调用底层的AppClassLoader加载器，所以这是我们需要传入一个系统类加载器。




方法内通过了**两种加载驱动类的方式，分别是：使用系统变量方式和ServiceLoader加载方式**

系统变量方式其实就是在变量jdbc.drivers中配置好驱动类，然后使用Class.forName进行加载

```
   private static void loadInitialDrivers() {
        String drivers;
//在系统属性中尝试获取驱动类的路径，但我们在代码中根本没有设置系统属性，显然获取不到
        try {
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }
        //1.通过SPI机制的工具类ServiceLoader去加载驱动类
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);【会遍历DriverManage静态代码块执行时，当前线程类加载器下找，//搜索服务的实现类(驱动实例)】
                Iterator<Driver> driversIterator = loadedDrivers.iterator();
                //如果根据配置文件获得的驱动类不存在，报错
try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next(); 【重要，在这一句forname和实例化】最终就是调用Class.forName(DriverName, false, loader)方法，也就是最开始我们注释掉的那一句代码。
                    }
                } catch(Throwable t) {
                // Do nothing
                }
                return null;
            }
        });

        println("DriverManager.initialize: jdbc.drivers = " + drivers);
//2.继续加载从系统属性中获得的驱动类
        if (drivers == null || drivers.equals("")) {
            return;
        }
        String[] driversList = drivers.split(":");
        println("number of Drivers:" + driversList.length);
        for (String aDriver : driversList) {
            try {
                println("DriverManager.Initialize: loading " + aDriver);
			//通过class.forname和驱动类路径加载驱动类
			//非常重要的一点，我们看到类加载器是获得的系统类加载器
			//AppClassLoader 可以由 ClassLoader 类提供的静态方法 getSystemClassLoader () 得到，它就是我们所说的「系统类加载器」
                Class.forName(aDriver, true,
                        ClassLoader.getSystemClassLoader());
            } catch (Exception ex) {
                println("DriverManager.Initialize: load failed: " + ex);
            }
        }
}


```

DirverManager是java的核心类，所以去加载第三方依赖的实现类时必须打破双亲委派模型。

Java 提供了很多服务提供者接口（Service Provider Interface，SPI），允许第三方为这些接口提供实现。常见的 SPI 有 JDBC、JCE、JNDI、JAXP 和 JBI 等。

这些 SPI 的接口由 Java 核心库来提供，而这些 SPI 的实现代码则是作为 Java 应用所依赖的 jar 包被包含进类路径（CLASSPATH）里。SPI接口中的代码经常需要加载具体的实现类。那么问题来了，SPI的接口是Java核心库的一部分，是由**启动类加载器(Bootstrap Classloader)来加载的；SPI的实现类是由系统类加载器(System ClassLoader)**来加载的。引导类加载器是无法找到 SPI 的实现类的，因为依照双亲委派模型，BootstrapClassloader无法委派AppClassLoader来加载类。

而线程上下文类加载器破坏了“双亲委派模型”，可以在执行线程中抛弃双亲委派加载链模式，使程序可以逆向使用类加载器。

这句Class.forName(DriverName, false, loader)代码所在的类在java.util.ServiceLoader类中，而ServiceLoader.class又加载在BootrapLoader中，因此传给 forName 的 loader 必然不能是BootrapLoader

```
public static <S> ServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
}

```

   

Thread.currentThread().getContextClassLoader();这个返回的类加载器不定！



我们在代码中经常写的class.forname(driver),不需要传入类加载器参数的原因是我们的代码本来就由系统类加载器加载，不冲突。



###### Class.forName(“com.mysql.jdbc.Driver”)

```java
public class Driver extends NonRegisteringDriver implements java.sql.Driver { 
// ~ Static fields/initializers 
// ——————————————— 
 
// 
// Register ourselves with the DriverManager 
// 
static 
{ 
  try { 
    java.sql.DriverManager.registerDriver(new Driver()); 
  } catch (SQLException E) 
  { 
    throw new RuntimeException(“Can’t register driver!”); 
  } 
} 
```



##### spi

SPI全称为(Service Provider Interface) ，是JDK内置的一种服务提供发现机制；主要被框架的开发人员使用，**比如java.sql.Driver接口**，数据库厂商实现此接口即可，当然要想让系统知道具体实现类的存在，还需要使用固定的存放规则，需要**在classpath下的META-INF/services/目录里创建一个以服务接口命名的文件，这个文件里的内容就是这个接口的具体的实现类**；

参考：https://blog.csdn.net/yangcheng33/article/details/52631940