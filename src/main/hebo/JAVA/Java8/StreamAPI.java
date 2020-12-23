package hebo.Java8;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * description
 *https://www.jianshu.com/p/02c593aed81d
 * @author workerbo 2020/05/25 21:17
 */
public class StreamAPI  {
    //缩减操作reduce的三个约束条件，无状态，不干预【不改变数据源】，有关联性【无先后顺序，并行流需要】
        public static void main(String[] args) {
            learnStream();
            //reduce3th();
        }
    //应用reduce()时，apply()的第一个参数t,包含的是一个结果，u包含的是下一个元素。
    // 在第一次调用时，将取决于使用reduce()的版本，t可能是单位值，或者前一个元素

        private static void learnStream() {
            List<Integer> lists = new ArrayList<>();
            lists.add(1);
            lists.add(2);
            lists.add(3);
            lists.add(4);
            lists.add(5);
            lists.add(6);

            Optional<Integer> sum = lists.stream().reduce((a, b) -> a + b);
            if (sum.isPresent()) {
                System.out.println("list的总和为:" + sum.get());//21
            }
            //<====> lists.stream().reduce((a, b) -> a + b).ifPresent(System.out::println);

            Integer sum2 = lists.stream().reduce(0, (a, b) -> a + b);//21
            System.out.println("list的总和为:" + sum2);
            //第一次时a是第一个元素，b是第二个元素。然后a存放积
            Optional<Integer> product = lists.stream().reduce((a, b) -> a * b);
            if (product.isPresent()) {
                System.out.println("list的积为:" + product.get());//720
            }

            Integer product2 = lists.stream().reduce(2, (a, b) -> a * b);
            System.out.println("list的积为:" + product2);//720

            Integer product3 = lists.stream().reduce(1,(a, b) -> {
                System.out.println(b);//b是集合中的每一个元素

                if (b % 2 == 0) {
                    return a * b;
                } else {
                    return a;//这里你可以为所欲为!
                }
            });
            System.out.println("list的偶数的积为:" + product3);//48
        }


    private static void reduce3th() {
        List<Integer> lists = new ArrayList<>();
        lists.add(1);
        lists.add(2);
        lists.add(3);

        Integer product = lists.parallelStream().reduce(1, (a, b) -> a *  (b * 2),
              (a, b) -> a * b);
        System.out.println("product:" + product);//48
    }
    //映射流
    private static void learnMap2th() {
        List<HeroPlayerGold> lists = new ArrayList<>();
        lists.add(new HeroPlayerGold("盖伦", "RNG-Letme", 100));
        lists.add(new HeroPlayerGold("诸葛亮", "RNG-Xiaohu", 300));
        lists.add(new HeroPlayerGold("露娜", "RNG-MLXG", 300));
        lists.add(new HeroPlayerGold("狄仁杰", "RNG-UZI", 500));
        lists.add(new HeroPlayerGold("牛头", "RNG-Ming", 500));

        //计算两个C位的经济和
        lists.stream()
              .filter(player-> "RNG-Xiaohu".equals(player.getPlayer()) || "RNG-UZI".equals(player.getPlayer()))
              .map(player->new Gold(player.getGold()))
              .mapToInt(Gold::getGold)
              .reduce((a,b)->a+b)
              .ifPresent(System.out::println);//800
    }
    }
    //玩家使用的英雄以及当前获得的金币数
 class HeroPlayerGold {
    /** 使用的英雄名字 */
       private String hero;
        /** 玩家的ID */
        private String player;
        /** 获得的金币数 */
        private int gold;

        public String getHero() {
            return hero;
        }

        public void setHero(String hero) {
            this.hero = hero;
        }

        public String getPlayer() {
            return player;
        }

        public void setPlayer(String player) {
            this.player = player;
        }

        public int getGold() {
            return gold;
        }

        public void setGold(int gold) {
            this.gold = gold;
        }



    public HeroPlayerGold(String hero, String player, int gold) {
        this.hero = hero;
        this.player = player;
        this.gold = gold;
    }
    //省略get/set/toString
}

//玩家获得的金币数
 class Gold {
    /**
     * 获得的金币数
     */
    private int gold;

    public Gold(int gold) {
        this.gold = gold;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
    //省略get/set/toString
}
