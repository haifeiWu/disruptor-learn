package cn.whfroever.disruptor;

/**
 * @author wuhf
 * @Date 2018/8/14 17:27
 **/
public class EventMain {

    public static void main(String[] args) throws Exception {
        DisruptorManager.init(new DataEventHandler());
        for (long l = 0; true; l++) {
            DisruptorManager.putDataToQueue(l);
            Thread.sleep(1000);
        }
    }

}
