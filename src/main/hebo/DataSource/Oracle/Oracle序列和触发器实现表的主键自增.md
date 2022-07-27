首先创建Oracle序列，序列的语法格式为：

```
CREATE SEQUENCE 序列名

[INCREMENT BY n]  

[START WITH n]  

[{MAXVALUE/MINVALUE n|NOMAXVALUE}]  

[{CYCLE|NOCYCLE}]  

[{CACHE n|NOCACHE}];
```

- **INCREMENT BY** 用于定义序列的步长，如果省略，则默认为1，如果出现负值，则代表序列的值是按照此步长递减的。
- **START WITH** 定义Oracle序列的初始值(即产生的第一个值)，默认为1。
- **MAXVALUE** 定义序列生成器能产生的最大值。选项NOMAXVALUE是默认选项，代表没有最大值定义，这时对于递增序列，系统能够产生的最大值是10的27次方;对于递减序列，最大值是-1。
- **MINVALUE**定义序列生成器能产生的最小值。选项NOMAXVALUE是默认选项，代表没有最小值定义，
- **CYCLE和NOCYCLE** 表示当序列生成器的值达到限制值后是否循环。CYCLE代表循环，NOCYCLE代表不循环。如果循环，则当递增序列达到最大值时，循环到最小值;对于递减序列达到最小值时，循环到最大值。如果不循环，达到限制值后，继续产生新值就会发生错误。
- **CACHE**(缓冲)定义存放序列的内存块的大小，默认为20。NOCACHE表示不对序列进行内存缓冲。对序列进行内存缓冲，可以改善序列的性能。

删除序列的语法是

```
DROP SEQUENCE 序列名;
```

假设有表TEST，首先建立递增Oracle序列SEQ_TEST:

```
create sequence SEQ_TEST  
increment by 1  
start with 1  
minvalue 1 nomaxvalue  
nocylce  
```

然后建立触发器，当有数据插入表TEST时，使用序列为其去的递增的主键值

```
create trigger TRG_TEST before insert on TEST  
for each row  
begin  
select SEQ_TEST.nextval into :new.TEST_ID from dual;  
end; 
```


 

 