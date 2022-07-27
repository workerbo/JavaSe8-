```
public interface UserService{
	public void a();
	public void a();
}

public class UserServiceImpl implements UserService{
	@Transactional(propagation = Propagation.REQUIRED)
	public void a(){
		this.b();
	}
	@Transactional(propagation = Propagation.REQUIRED_NEW)
	public void b(){
		System.out.println("b has been called");
	}
}

```

Q1：b中的事务会不会生效？
A1：不会，a的事务会生效，b中不会有事务，因为a中调用b属于内部调用，没有通过代理，所以不会有事务产生。
Q2：如果想要b中有事务存在，要如何做？
A2：<aop:aspectj-autoproxy expose-proxy=“true”> ，设置expose-proxy属性为true，将代理暴露出来，使用AopContext.currentProxy()获取当前代理，将this.b()改为((UserService)AopContext.currentProxy()).b()
