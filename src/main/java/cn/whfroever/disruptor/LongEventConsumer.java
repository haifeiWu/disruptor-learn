package cn.whfroever.disruptor;

public class LongEventConsumer {
    public LongEventConsumer(LongEvent event) {
        // 业务逻辑
        System.out.println("event : " + event.getValue());
    }
}
