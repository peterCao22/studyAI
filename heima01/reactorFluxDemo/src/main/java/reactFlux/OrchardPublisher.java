package reactFlux;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.IntStream;

public class OrchardPublisher implements Flow.Publisher<String> {

    // 果园里有20个苹果
    private final List<String> apples = Collections.unmodifiableList(   // 接收一个List
            IntStream.rangeClosed(1, 20)
                    .mapToObj(i-> "苹果# " + i)
                    .toList()
    );

    // 发布者订阅方法，接收一个订阅者
    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {
        System.out.println("【果园】: 检测到有新的订阅者 " + subscriber.getClass().getSimpleName() + "，正在建立连接...");
        // 为这个新的订阅者创建一个专属的 "订阅关系/阀门"
        // AppleSubscription 就是订阅者和发布者的沟通的桥梁/传输的管道， 由订阅者和要传输的物体集合构造
        AppleSubscription subscription = new AppleSubscription(subscriber, apples);
        // 【重要契约】: 发布者必须调用订阅者的 onSubscribe 方法，将“阀门”交给它。
        subscriber.onSubscribe(subscription);
    }
}
