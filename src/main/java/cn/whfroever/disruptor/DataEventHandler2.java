package cn.whfroever.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * @author wuhf
 * @Date 2018/8/14 16:21
 **/
public class DataEventHandler2 implements EventHandler<DataEvent> {
    @Override
    public void onEvent(DataEvent dataEvent, long l, boolean b) throws Exception {
        System.out.println("Event2: " + dataEvent.toString());
//        new DataEventConsumer(dataEvent);
    }
}
