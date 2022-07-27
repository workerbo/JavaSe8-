```


//得到存储进去的request
private static final ThreadLocal<RequestAttributes> requestAttributesHolder =
new NamedThreadLocal<RequestAttributes>("Request attributes");
//可被子线程继承的request
private static final ThreadLocal<RequestAttributes> inheritableRequestAttributesHolder =
new NamedInheritableThreadLocal<RequestAttributes>("Request context");

  public static RequestAttributes getRequestAttributes() {
        RequestAttributes attributes = requestAttributesHolder.get();
        if (attributes == null) {
            attributes = inheritableRequestAttributesHolder.get();
        }
        return attributes;
    }
```

