**reduce方法有三个override的方法：**

> Optional reduce(BinaryOperator accumulator);

> T reduce(T identity, BinaryOperator accumulator);

> <U> U reduce(U identity,BiFunction<U, ? super T, U> accumulator,BinaryOperator<U> combiner);

​        1. 第一个参数：返回实例u，传递你要返回的U类型对象的初始化实例u

​        2. 第二个参数：累加器accumulator，可以使用lambda表达式，声明你在u上累加你的数据来源t的逻辑，例如(u,t)->u.sum(t),此时lambda表达式的行参列表是返回实例u和遍历的集合元素t，函数体是在u上累加t

​        3. 第三个参数：参数组合器combiner，接受lambda表达式。

 *//第三个参数---参数的数据类型必须为返回数据类型，改参数主要用于合并多个线程的result值*



eg：

```
  Optional<Double> reduce = Arrays.asList(25.0, 59.0, 60.0).stream().reduce(Double::max);
        System.out.println(reduce.get());
        //第二种求最大值：
        Optional<Double> max = Arrays.asList(25.0, 59.0, 60.0).stream().max(Double::compareTo);
        System.out.println(reduce.get());
```

```
  @Override
    public final Optional<P_OUT> max(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.maxBy(comparator));
    }
    
```

```
@FunctionalInterface
public interface BiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u);

    /**
     在BiFunction对象之后执行andThen会返回一个新的BiFunction，嵌套了一层Function的逻辑
     */
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}

```

```
@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T,T,T> {
   
    public static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

  
    public static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }
}

List<? super A> 的意思是List集合 list,它可以持有 A 及其父类的实例。

```

