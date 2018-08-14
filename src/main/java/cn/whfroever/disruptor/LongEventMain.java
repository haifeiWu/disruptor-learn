package cn.whfroever.disruptor;

/**
 * @author wuhf
 * @Date 2018/8/14 17:27
 **/
public class LongEventMain {

    public static void main(String[] args) throws Exception {
        DisruptorManager.init(new LongEventHandler());
        for (long l = 0; true; l++) {
            DisruptorManager.packAndPut(l);
            Thread.sleep(1000);
        }
    }

}
