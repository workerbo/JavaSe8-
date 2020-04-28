package hebo;

import java.util.*;

/**
 * @Author: Liruilong
 * @Date: 2019/7/27 10:59
 */
//编辑-进入-编译
public class Main {
    public static void main(String[] args) {
        Map<String,Integer> map =new HashMap<>(8);
        map.put("1",1);
        Set<Map.Entry<String,Integer>> set=map.entrySet();
        for(Map.Entry<String,Integer> temp:set){
            System.out.println(temp.getKey());
            System.out.println(temp.getValue());
        }
        List<String> list=new ArrayList<>();
        list.add("99");
        list.get(0).contains("888");
    }
}