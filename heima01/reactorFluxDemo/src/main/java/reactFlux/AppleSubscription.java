package reactFlux;

import java.util.List;
import java.util.concurrent.Flow;


public class AppleSubscription implements Flow.Subscription {


    // 负责与发布者之间的关系，控制权在订阅者手上，所以这里要传入订阅者对象，后面通过它的方法进行操作

    private final Flow.Subscriber subscriber;
    private List<String> apples;

    private volatile boolean cancelled = false;
    private int currentIndex = 0; // 记录送到了第几个苹果

    public AppleSubscription(Flow.Subscriber<? super String> subscriber, List<String> apples)  {
        this.apples = apples;
        this.subscriber = subscriber;
    }


    @Override
    public void request(long n) {
        if (cancelled) {
            return;
        }
        System.out.println("【阀门】: 收到请求，数量为 " + n + "。开始传输...");
        // 循环发送n个苹果，或者直到苹果送完
        long requestedCount = 0;
        while (requestedCount < n) {
            if(currentIndex >= apples.size()) {
                // 所有苹果都送完了
                subscriber.onComplete();
                return;
            }
            // 每次发送一个苹果，调用订阅者的 onNext
            subscriber.onNext(apples.get(currentIndex));
            currentIndex++;
            requestedCount++;
        }
    }

    @Override
    public void cancel() {
        System.out.println("【阀门】: 收到取消信号，停止一切传输！");
        cancelled = true;
    }
}
