##### Apache HttpClient

socket read没有超时设置，会导致HttpClient阻塞，因为Http Client默认的SO_TIMEOUT是0，即一直等待。

[Apache](https://so.csdn.net/so/search?q=Apache&spm=1001.2101.3001.7020) HttpClient需要设置TimeOut。两个timeout,一个是获取连接的timeout:CONNECTION_TIMEOUT，还有一个是socket的timeout，表示获取请求返回内容的超时时间SO_TIMEOUT。

setHttpRequestRetryHandler是设置请求的超时重试次数，超时了进行重试3次，true为重试开关。