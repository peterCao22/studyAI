package other;

import java.util.HashMap;
import java.util.Map;

public class CommonTest {

    public static void main(String[] args) {
        test1();
    }

    private static void test1() {

        Map<String, Integer> mapB = Map.of("X", 3, "Y", 2, "Z", 1);
        mapB.forEach((k, v) -> System.out.println(k + "=" + v));

        Map<String, Integer> mapA = new HashMap<>();
        mapA.put("X", 1);

        mapB.forEach((k, v) -> {
            // merge第三个参数是BiFunction<T,U,R>
            // sum方法接收两个参数:oldValue,newValue。 oldValue=mapA.get(k),newValue=v，计算后返回新的value。
            mapA.merge(k, v, Integer::sum);
        });
        System.out.println();
        mapA.forEach((k, v) -> System.out.println(k + "=" + v));
    }
}
