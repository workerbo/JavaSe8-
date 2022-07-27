###### 删除约束

declare @name varchar(50)
select  @name =b.name from sysobjects b join syscolumns a on b.id = a.cdefault
where a.id = object_id('hliv_acr_receipt_ln')
and a.name ='amount'

exec('alter table hliv_acr_receipt_ln drop constraint ' + @name)

###### go的意义

GO 语句后面跟数字代表提交的次数，上图中的第一个GO依然起到了示例1中提到的分割.sql文件的作用。



###### 排序和分页

```
1 DECLARE @PageIndex INT = 1
2 DECLARE @PageSize INT = 10
3 
4 SELECT * FROM [T_Student] 
5 ORDER BY [iCreatedOn] DESC 
6 OFFSET ((@PageIndex-1)*@PageSize) ROWS FETCH NEXT @PageSize ROWS ONLY
```



Top是先执行后再去排序【在同一级】