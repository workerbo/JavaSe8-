##### Mybatis整合其他连接池

首先看mybatis的配置文件

<dataSource type="POOLED">代表使用了mybatis内部的连接池，UNPOOLED表示不使用连接池，不过这个连接池的性能不是很好，推荐使用阿里的druid连接池

mybatis-config.xml

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<!-- 加载外部的资源文件 -->
	<properties resource="jdbc.properties"></properties>
	<!-- 声明数据连接环境 -->
	<environments default="development">
		<environment id="development">
			<transactionManager type="JDBC" />
			<dataSource type="POOLED">
				<property name="driver" value="${driverClass}" />
				<property name="url" value="${jdbcUrl}" />
				<property name="username" value="${user}" />
				<property name="password" value="${password}" />
			</dataSource>
		</environment>
	</environments>
	<mappers>
		<!-- 映射文件<mapper resource="cn/cache/cache.xml"/> -->
		<mapper resource="sdibt/lxj/entity/Book.xml" />
	</mappers>
</configuration>
```

1.导入druid连接池需要的jar包

2.开发是一个数据源类，实现DataSourceFactory接口
		

	package sdibt.lxj.util;
	
	import java.sql.SQLException;
	import java.util.Properties;
	
	import javax.sql.DataSource;
	
	import org.apache.ibatis.datasource.DataSourceFactory;
	
	import com.alibaba.druid.pool.DruidDataSource;
	
	public class MybatisDruidDatasource implements DataSourceFactory{
		private Properties properties;
		@Override
	public DataSource getDataSource() {
		//创建druid数据源,这是druid jar包提供的一个类
		DruidDataSource ds = new DruidDataSource();
		//从配置好的properties加载配置
		ds.setUsername(this.properties.getProperty("username"));//用户名
		ds.setPassword(this.properties.getProperty("password"));//密码
		ds.setUrl(this.properties.getProperty("url"));
		ds.setDriverClassName(this.properties.getProperty("driver"));
		ds.setInitialSize(5);//初始连接数
		ds.setMaxActive(10);//最大活动连接数
		ds.setMaxWait(6000);//最大等待时间
		
		//初始化连接
		try {
			ds.init();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ds;
	}
	 
	@Override
	public void setProperties(Properties properties) {
		// xml文档会将properties注入进来
		this.properties=properties;
	}
	}


3.更改mybatis-config.xml配置文件



```
将<dataSource type="POOLED">设置成<dataSource type="sdibt.fly.util.MyDruidDataSource">就ok了。集成其他连接池流程也是如此！
```

```
<!DOCTYPE configuration
PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<!-- 加载外部的资源文件 -->
	<properties resource="jdbc.properties"></properties>
	<!-- 声明数据连接环境 -->
	<environments default="development">
		<environment id="development">
			<transactionManager type="JDBC" />
			<dataSource type="sdibt.lxj.util.MybatisDruidDatasource">
				<property name="driver" value="${driverClass}" />
				<property name="url" value="${jdbcUrl}" />
				<property name="username" value="${user}" />
				<property name="password" value="${password}" />
			</dataSource>
		</environment>
		<environment id="oracleDS">
			<transactionManager type="JDBC" />
			<dataSource type="POOLED">
				<property name="driver" value="${oracle.driverClass}" />
				<property name="url" value="${oracle.jdbcUrl}" />
				<property name="username" value="${oracle.user}" />
				<property name="password" value="${oracle.password}" />
			</dataSource>
		</environment>
	</environments>
	<mappers>
		<!-- 映射文件<mapper resource="cn/cache/cache.xml"/> -->
		<mapper resource="sdibt/lxj/entity/Book.xml" />
	</mappers>
</configuration>
```


<!DOCTYPE configuration
PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<!-- 加载外部的资源文件 -->
	<properties resource="jdbc.properties"></properties>
	<!-- 声明数据连接环境 -->
	<environments default="development">
		<environment id="development">
			<transactionManager type="JDBC" />
			<dataSource type="sdibt.lxj.util.MybatisDruidDatasource">
				<property name="driver" value="${driverClass}" />
				<property name="url" value="${jdbcUrl}" />
				<property name="username" value="${user}" />
				<property name="password" value="${password}" />
			</dataSource>
		</environment>
	</environments>
	<mappers>
		<!-- 映射文件<mapper resource="cn/cache/cache.xml"/> -->
		<mapper resource="sdibt/lxj/entity/Book.xml" />
	</mappers>
</configuration>
Mybatis使用多数据源
在配置文件中加一个oracle数据源



为避免使用jdbc里面的变量冲突，在oracle数据源加上前缀

```
driverClass=com.mysql.jdbc.Driver
jdbcUrl=jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8
user=root
password=ok

oracle.driverClass=oracle.jdbc.driver.OracleDriver
oracle.jdbcUrl=jdbc:oracle:thin:@localhost:1521/orcl
oracle.user=wyl
oracle.password=ok
```


在获取session的util中添加一个数据源





	package sdibt.fly.util;
	
	import java.io.InputStream;
	import org.apache.ibatis.session.SqlSession;
	import org.apache.ibatis.session.SqlSessionFactory;
	import org.apache.ibatis.session.SqlSessionFactoryBuilder;
	
	
	public class MybatisUtils {
		private static SqlSessionFactory sf=null;
		private static SqlSessionFactory osf=null;
		static{
			InputStream in = String.class.getResourceAsStream("/mybatis-config.xml");
			InputStream in1 = String.class.getResourceAsStream("/mybatis-config.xml");
			 sf = new SqlSessionFactoryBuilder().build(in);
			 osf = new SqlSessionFactoryBuilder().build(in1,"oracleDS");
		}/**
	 * 获取mysql的数据源
	 * @return
	 */
	public static SqlSession getSession(){
		SqlSession session = sf.openSession();
		
		return session;
	}
	/**
	 * 获取oracle的数据源
	 * @return
	 */
	public static SqlSession getOracleSession(){
		SqlSession session = osf.openSession();
		
		return session;
	}
	
	public static void closeSession(SqlSession session){
		if(session!=null){
			session.close();
		}
	}
	}

