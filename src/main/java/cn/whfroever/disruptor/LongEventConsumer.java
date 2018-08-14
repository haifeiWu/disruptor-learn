package cn.whfroever.disruptor;

/**
 * 消费者业务逻辑
 *
 * @author wuhaifei
 * @date 2018-08-15
 */
public class LongEventConsumer {
    public LongEventConsumer(LongEvent event) {
        // 业务逻辑
        System.out.println("event : " + event.getValue());
    }
}
