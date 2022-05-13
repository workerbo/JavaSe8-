```
public static void main(String[] args) throws IOException {
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
         EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
         List<Employee> all = employeeMapper.getAll();
         for (Employee item : all)
            System.out.println(item);
    } finally {
        sqlSession.close();
    }
}
```

