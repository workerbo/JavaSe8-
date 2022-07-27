![DispatcherServlet继承关系](https://img-blog.csdnimg.cn/7ab18882257e49ce8a1f22d0b181bc56.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5pio5aSp5LuK5aSp5piO5aSp5aW95aSa5aSp,size_20,color_FFFFFF,t_70,g_se,x_16)

1：Servlet的接口和实现类.
2：SpringMVC具有Spring的一些环境变量和Spring容器，类似的XXXAware接口就是对该类提供Spring感知,简单来说就是如果想使用Spring的XXXX就要实现XXXAware，spring会把需要的东西传送过来。
HttpServletBean：进行初始化工作
FrameworkServlet：初始化WebApplicationContext，并提供service方法预处理请求
DispatcherServlet：具体分发处理

FrameworkServlet 类重写了service()、doGet()、doPost() 等方法，方法里面都有一个预处理方法 processRequest(request, response)；
查看 processRequest(request, response) 的实现：获取上一个请求的参数 ➡ 重新建立新的参数 ➡ 设置到XXContextHolder ➡ 父类的service()处理请求 ➡ 恢复request ➡ 发布事件



processRequest()方法只是做了一些线程安全的隔离，真正的请求处理，发生在doService()方法中。点开FrameworkServlet类中的doService()方法。



真正发生请求转发的方法doDispatch()中，它的参数是HttpServletRequest和HttpServletResponse对象。这给我们传递的意思也很明确，**从request中能获取到一切请求的数据，从response中，我们又可以往服务器端输出任何响应，Http请求的处理，就应该围绕这两个对象来设计**。

```
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpServletRequest processedRequest = request;
    HandlerExecutionChain mappedHandler = null;
    boolean multipartRequestParsed = false;

    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

    try {
        ModelAndView mv = null;
        Exception dispatchException = null;

        try {
            // 处理文件上传
            processedRequest = checkMultipart(request);
            multipartRequestParsed = (processedRequest != request);

            // 决定当前请求的Handler【只是获取支持当前处理器的适配器】
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null || mappedHandler.getHandler() == null) {
                noHandlerFound(processedRequest, response);
                return;
            }

            // 决定当前请求的HandlerAdapter
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

            // 处理last-modified请求头
            String method = request.getMethod();
            boolean isGet = "GET".equals(method);
            if (isGet || "HEAD".equals(method)) {
                long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                if (logger.isDebugEnabled()) {
                    logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                }
                if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                    return;
                }
            }

            // 拦截器的前置处理
            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return;
            }

            // Handler实际执行请求
            mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

            if (asyncManager.isConcurrentHandlingStarted()) {
                return;
            }

            // 设置默认视图名
            applyDefaultViewName(processedRequest, mv);
            // 拦截器后置处理
            mappedHandler.applyPostHandle(processedRequest, response, mv);
        }
        catch (Exception ex) {
            dispatchException = ex;
        }
        catch (Throwable err) {
            // As of 4.3, we're processing Errors thrown from handler methods as well,
            // making them available for @ExceptionHandler methods and other scenarios.
            dispatchException = new NestedServletException("Handler dispatch failed", err);
        }

        // 选择视图并渲染视图
        processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
    }
    catch (Exception ex) {
        triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
    }
    catch (Throwable err) {
        triggerAfterCompletion(processedRequest, response, mappedHandler,
                new NestedServletException("Handler processing failed", err));
    }
    finally {
        if (asyncManager.isConcurrentHandlingStarted()) {
            // Instead of postHandle and afterCompletion
            if (mappedHandler != null) {
                mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
            }
        }
        else {
            // Clean up any resources used by a multipart request.
            if (multipartRequestParsed) {
                cleanupMultipart(processedRequest);
            }
        }
    }
}
```

