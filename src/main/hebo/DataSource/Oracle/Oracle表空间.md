```
/*分为四步 */
/*第1步：创建临时表空间  */
create temporary tablespace user_temp 
tempfile 'C:\app\ORACLE\oradata\orcl\user_temp.dbf'
size 50m 
autoextend on 
next 50m maxsize 20480m 
extent management local; 
  
/*第2步：创建数据表空间  */
create tablespace user_data 
logging 
datafile 'C:\app\ORACLE\oradata\orcl\user_data.dbf'
size 50m 
autoextend on 
next 50m maxsize 20480m 
extent management local; 
  
/*第3步：创建用户并指定表空间  */
create user username identified by password 
default tablespace user_data 
temporary tablespace user_tempquota unlimited on user_dataprofile default ;
  
/*第4步：给用户授予权限  */
grant connect,resource to username;
```

