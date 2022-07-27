```
public class Login {
    /**
     *    第一步，加载驱动，创建数据库的连接
     *    第二步，编写sql
     *    第三步，需要对sql进行预编译
     *    第四步，向sql里面设置参数
     *    第五步，执行sql
     *    第六步，释放资源 
     * @throws Exception 
     */
     
    public static final String URL = "jdbc:mysql://localhost:3306/chenhao";
    public static final String USER = "liulx";
    public static final String PASSWORD = "123456";
    public static void main(String[] args) throws Exception {
        login("lucy","123");
    }
    
    public static void login(String username , String password) throws Exception{
        Connection conn = null; 
        PreparedStatement psmt = null;
        ResultSet rs = null;
        try {
            //加载驱动程序
            我们去掉第一行Class.forName后，我们依旧可以获得相应数据库的连接
            Class.forName("com.mysql.jdbc.Driver");
            //获得数据库连接
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            //编写sql
            String sql = "select * from user where name =? and password = ?";//问号相当于一个占位符
            //对sql进行预编译
            psmt = conn.prepareStatement(sql);
            //设置参数
            psmt.setString(1, username);
            psmt.setString(2, password);
            //执行sql ,返回一个结果集
            rs = psmt.executeQuery();
            //输出结果
            while(rs.next()){
                System.out.println(rs.getString("user_name")+" 年龄："+rs.getInt("age"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            //释放资源
            conn.close();
            psmt.close();
            rs.close();
        }
    }
}
```

