package reactFlux;

public class ManualReactiveStreamExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- 手工打造的响应式果汁店开张了 ---");
        // 1. 创建我们的果园 (Publisher)
        OrchardPublisher orchard = new OrchardPublisher();

        // 2. 创建我们的榨汁机 (Subscriber)
        JuicerSubscriber juicer = new JuicerSubscriber();

        // 3. 榨汁机订阅果园 (核心交互的开始)
        orchard.subscribe(juicer);

        // 保证主线程不退出，让我们能看到完整的异步流程
        Thread.sleep(5000);
        System.out.println("--- 果汁店打烊了 ---");
    }
}
