当然，真正开始之前还需要经过一些判断，比如是否已经处理过或者是否是需要跳过的bean，而真正创建代理的代码是从getAdvicesAndAdvisorsForBean开始的。
创建代理主要包含了两个步骤：
（1）获取增强方法或者增强器；
（2）根据获取的增强进行代理。 



对于指定bean的增强方法的获取一定是包含两个步骤的，获取所有的增强以及寻找所有增强中使用于bean的增强并应用，那么**findCandidateAdvisors**与**findAdvisorsThatCanApply**便是做了这两件事情。

## 获取增强器

```
@Override
protected List<Advisor> findCandidateAdvisors() {
    // Add all the Spring advisors found according to superclass rules.
    // 当使用注解方式配置AOP的时候并不是丢弃了对XML配置的支持，
    // 在这里调用父类方法加载配置文件中的AOP声明
    List<Advisor> advisors = super.findCandidateAdvisors();
    // Build Advisors for all AspectJ aspects in the bean factory.
    if (this.aspectJAdvisorsBuilder != null) {
        advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    }
    return advisors;
}

```







（1）获取所有beanName，这一步骤中所有在beanFactory中注册的Bean都会被提取出来。
		（2）遍历所有beanName，并找出声明AspectJ注解的类，进行进一步的处理。
		（3）对标记为AspectJ注解的类进行增强器的提取。
		（4）将提取结果加入缓存。 

```
public List<Advisor> buildAspectJAdvisors() {
    List<String> aspectNames = this.aspectBeanNames;

    if (aspectNames == null) {
        synchronized (this) {
            aspectNames = this.aspectBeanNames;
            if (aspectNames == null) {
                List<Advisor> advisors = new ArrayList<>();
                aspectNames = new ArrayList<>();
                // 获取所有的beanName
                String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                        this.beanFactory, Object.class, true, false);
                // 循环所有的beanName找出对应的增强方法
                for (String beanName : beanNames) {
                    // 不合法的bean则略过，由子类定义规则，默认返回true
                    if (!isEligibleBean(beanName)) {
                        continue;
                    }
                    // We must be careful not to instantiate beans eagerly as in this case they
                    // would be cached by the Spring container but would not have been weaved.
                    // 获取对应的bean的Class类型
                    Class<?> beanType = this.beanFactory.getType(beanName);
                    if (beanType == null) {
                        continue;
                    }
                    // 如果存在Aspect注解
                    if (this.advisorFactory.isAspect(beanType)) {
                        aspectNames.add(beanName);
                        AspectMetadata amd = new AspectMetadata(beanType, beanName);
                        if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                            MetadataAwareAspectInstanceFactory factory =
                                    new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                            // 解析标记Aspect注解中的增强方法
                            List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                            if (this.beanFactory.isSingleton(beanName)) {
                                //将增强器存入缓存中，下次可以直接取
                                this.advisorsCache.put(beanName, classAdvisors);
                            }
                            else {
                                this.aspectFactoryCache.put(beanName, factory);
                            }
                            advisors.addAll(classAdvisors);
                        }
                        else {
                            // Per target or per this.
                            if (this.beanFactory.isSingleton(beanName)) {
                                throw new IllegalArgumentException("Bean with name '" + beanName +
                                        "' is a singleton, but aspect instantiation model is not singleton");
                            }
                            MetadataAwareAspectInstanceFactory factory =
                                    new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                            this.aspectFactoryCache.put(beanName, factory);
                            advisors.addAll(this.advisorFactory.getAdvisors(factory));
                        }
                    }
                }
                this.aspectBeanNames = aspectNames;
                return advisors;
            }
        }
    }

    if (aspectNames.isEmpty()) {
        return Collections.emptyList();
    }
    // 记录在缓存中
    List<Advisor> advisors = new ArrayList<>();
    for (String aspectName : aspectNames) {
        List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
        if (cachedAdvisors != null) {
            advisors.addAll(cachedAdvisors);
        }
        else {
            MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
            advisors.addAll(this.advisorFactory.getAdvisors(factory));
        }
    }
    return advisors;
}
```

## 寻找匹配的增强器

前面的函数中已经完成了所有增强器的解析，但是对于所有增强器来讲，并不一定都适用于当前的bean，还要挑取出适合的增强器，也就是满足我们配置的通配符的增强器。具体实现在findAdvisorsThatCanApply中。