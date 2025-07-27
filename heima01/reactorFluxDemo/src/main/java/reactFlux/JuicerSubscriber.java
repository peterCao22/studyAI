package reactFlux;

import java.util.concurrent.Flow;

// 重要的订阅者，它要实现4个方法，基本操作都在这
public class JuicerSubscriber implements Flow.Subscriber<String> {


    private final int BATCH_SIZE = 5;
    private int processedCount = 0;
    private Flow.Subscription subscription; // 用来保存从果园拿到的“阀门”

    // 由发布者的subscribe方法调用， 是一个重要的subscription 沟通桥梁
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        System.out.println("【榨汁机】: 成功订阅！拿到了阀门(Subscription)，控制权在我手上了！");
        this.subscription = subscription; // 保存好这个阀门，后面要用它来请求数据
        // 立即通过阀门请求第一批苹果
        System.out.println("【榨汁机】: 先来 " + BATCH_SIZE + " 个苹果！");
        // request发起者就是订阅者自己
        this.subscription.request(BATCH_SIZE);
    }

    @Override
    public void onNext(String item) {
        System.out.println("【榨汁机】: 收到 '" + item + "'，开榨！");
        processedCount++;

        // 如果处理完了一批，就请求下一批
        if (processedCount % BATCH_SIZE == 0) {
            System.out.println("【榨汁机】: 这批处理完了，再来 " + BATCH_SIZE + " 个！");
            this.subscription.request(BATCH_SIZE);
        }
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("【榨汁机】: 出错了！" + t.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("【榨汁机】: 所有苹果都处理完了！完美！");
    }
}
