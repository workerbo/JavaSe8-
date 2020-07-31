package hebo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: Liruilong
 * @Date: 2019/7/27 10:59
 */
//编辑-进入-编译
public class Main {
    public static void main(String[] args) {
        //Map<String,Integer> map =new HashMap<>(8);
        //map.put("1",1);
        //Set<Map.Entry<String,Integer>> set=map.entrySet();
        //for(Map.Entry<String,Integer> temp:set){
        //    System.out.println(temp.getKey());
        //    System.out.println(temp.getValue());
        //}
        //List<String> list=new ArrayList<>();
        //list.add("99");
        //
        int a=1;
        assert a!=1;
        Map<Integer, Set<String>> map = new HashMap<>();
        // Java7及以前的实现方式
        if(map.containsKey(1)){
            map.get(1).add("one");
        }else{
            Set<String> valueSet = new HashSet<String>();
            valueSet.add("one");
            map.put(1, valueSet);
        }
        // Java8的实现方式
        map.computeIfAbsent(1, (k) -> new HashSet<String>()).add("yi");
        //　将Stream规约成List
        Stream<String> stream = Stream.of("I", "love", "you", "too");
        List<String> list = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);// 方式１
        List<String> list1 = stream.collect(Collectors.toList());// 方式2
        System.out.println(list);
    }
}