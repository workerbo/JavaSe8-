TransactionSynchronizationManager是事务同步管理器，监听事务的操作，来实现在事务前后可以添加一些指定操作.



```text
public abstract class TransactionSynchronizationManager {

     //线程上下文中保存着【线程池对象：ConnectionHolder】的Map对象。同一线程可以通过该属性获取到同一个Connection对象。
    private static final ThreadLocal<Map<Object, Object>> resources = new NamedThreadLocal<>("Transactional resources");
    //事务同步器，是Spring交由程序员进行扩展的代码，每个线程可以注册N个事务同步器。
    private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations = new NamedThreadLocal<>("Transaction synchronizations");
    // 事务的名称  
    private static final ThreadLocal<String> currentTransactionName = new NamedThreadLocal<>("Current transaction name");
    // 事务是否是只读  
    private static final ThreadLocal<Boolean> currentTransactionReadOnly = new NamedThreadLocal<>("Current transaction read-only status");
    // 事务的隔离级别
    private static final ThreadLocal<Integer> currentTransactionIsolationLevel = new NamedThreadLocal<>("Current transaction isolation level");
    // 事务是否开启   actual：真实的
    private static final ThreadLocal<Boolean> actualTransactionActive = new NamedThreadLocal<>("Actual transaction active");
}
```

```
ublic void registerSynchronization(Consumer<String> afterCommit, String uuid) {
        // 这里主要是判断是否开启了事务，如果没有开启事务是会报错的
        // 所以这里判断是否开启了事务，如果没有开启事务则直接执行方法
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (!actualTransactionActive) {
            LOGGER.info("uuid: {}, 当前线程: {}, 没有激活事务, 直接执行 afterCommit.", uuid, Thread.currentThread().getName());
            afterCommit.accept(uuid);
            return;
        }
       // 如果开始了事务则在这里注册一个同步事务，将监听当前线程事务的动作
        LOGGER.info("uuid: {}, 当前线程: {}, 激活事务, 注册事务提交后的回调 afterCommit.", uuid, Thread.currentThread().getName());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
            // 调用父类的事务提交方法
                super.afterCommit();
                LOGGER.info("uuid: {}, 当前线程: {}, 开始执行事务提交后的回调 afterCommit.", uuid, Thread.currentThread().getName());
                // 事务提交之后，则执行我们的目标方法
                afterCommit.accept(uuid);
            }
        });
    }
```



封装一个方法来调用，实现异步线程去事务提交后方法

```
@Component
public class AfterCommitExecutorImpl extends TransactionSynchronizationAdapter implements AfterCommitExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AfterCommitExecutorImpl.class);
   // 保存要运行的任务线程
    private static final ThreadLocal<List<Runnable>> RUNNABLES = new ThreadLocal<List<Runnable>>();
   // 设置线程池
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);
 
    @Override
    public void execute(Runnable runnable) {
        LOGGER.info("Submitting new runnable {} to run after commit", runnable);
       // 如果没有开启事务，则执行运行
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            LOGGER.info("Transaction synchronization is NOT ACTIVE. Executing right now runnable {}", runnable);
            threadPool.execute(runnable);
            return;
        }
        // 开启了事务，则判断是否初始化，没有初始化则初始化，并注册
        List<Runnable> threadRunnables = RUNNABLES.get();
        if (threadRunnables == null) {
            threadRunnables = new ArrayList<Runnable>();
            RUNNABLES.set(threadRunnables);
            TransactionSynchronizationManager.registerSynchronization(this);
        }
        threadRunnables.add(runnable);
    }
 
 // 监听到事务提交之后执行方法
    @Override
    public void afterCommit() {
        List<Runnable> threadRunnables = RUNNABLES.get();
        LOGGER.info("Transaction successfully committed, executing {} runnables", threadRunnables.size());
        // 循环遍历执行任务
        for (int i = 0; i < threadRunnables.size(); i++) {
            Runnable runnable = threadRunnables.get(i);
            LOGGER.info("Executing runnable {}", runnable);
            try {
                threadPool.execute(runnable);
            } catch (RuntimeException e) {
                LOGGER.error("Failed to execute runnable " + runnable, e);
            }
        }
    }
    // 判断
    @Override
    public void afterCompletion(int status) {
        LOGGER.info("Transaction completed with status {}", status == STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK");
        RUNNABLES.remove();
    }
 
}
```



## **TransactionSynchronization源码探讨**

这个类是程序员对事务同步的扩展点：用于事务同步回调的接口:

```text
// 正常提交状态
    int STATUS_COMMITTED = 0;
    // 回滚状态
    int STATUS_ROLLED_BACK = 1;
    // 不明状态
    int STATUS_UNKNOWN = 2;

   // 事务挂起
    default void suspend() {
    }
    // 事务恢复
    default void resume() {
    }
   // 将基础会话刷新到数据存储区(如果适用)，比如Hibernate/JPA的Session
    default void flush() {
    }
   // 在事务提交前触发，此处若发生异常，会导致回滚
    default void beforeCommit(boolean readOnly) {
    }
   // 在beforeCommit之后，commit/rollback之前执行。即使异常，也不会回滚。
    default void beforeCompletion() {
    }
    // 事务提交后执行。
    default void afterCommit() {
    }
    // 事务提交/回滚执行
    default void afterCompletion(int status) {
    }
```

###### 触发

```
private void processCommit(DefaultTransactionStatus status) throws TransactionException {
    try {
        //提交事务
        doCommit(status);
        ...
        try {
           //回调所有事务同步器的afterCommit方法。
           triggerAfterCommit(status);
        }
        finally {
           //回调所有事务同步器的afterCompletion方法。
           triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
        }

    }
    finally {
        //清除TransactionSynchronizationManager的ThreadLocal绑定的数据。
        //解除Thread绑定的resources资源。
        //将Commit设置为自动提交。
        //清理ConnectionHolder资源。
        cleanupAfterCompletion(status);
    }
}

```





应用：

1. sql的数据库连接跟当前线程绑定，也是用到这事务管理器.
2. 在SpringCache的自定义CacheManager中。装饰Cache对象使其支持事务操作。即只有在事务提交成功之后，才会进行缓存.这个也是运用了事务管理器.