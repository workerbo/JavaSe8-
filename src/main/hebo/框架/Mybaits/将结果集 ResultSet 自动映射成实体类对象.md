```
public List<Object> handleResultSets(Statement stmt) throws SQLException {
    
    final List<Object> multipleResults = new ArrayList<Object>();

    int resultSetCount = 0;
    //获取第一个ResultSet,通常只会有一个
    ResultSetWrapper rsw = getFirstResultSet(stmt);
    //从配置中读取对应的ResultMap，通常也只会有一个，设置多个是通过逗号来分隔，我们平时有这样设置吗？
    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount);

    while (rsw != null && resultMapCount > resultSetCount) {
        ResultMap resultMap = resultMaps.get(resultSetCount);
        // 处理结果集
        handleResultSet(rsw, resultMap, multipleResults, null);
        rsw = getNextResultSet(stmt);
        cleanUpAfterHandlingResultSet();
        resultSetCount++;
    }

    // 以下逻辑均与多结果集有关，就不分析了，代码省略
    String[] resultSets = mappedStatement.getResultSets();
    if (resultSets != null) {...}

    return collapseSingleResultList(multipleResults);
}
```

1. 创建实体类对象
2. 自动映射结果集中有的column，但resultMap中并没有配置
3. 根据 <resultMap> 节点中配置的映射关系进行映射