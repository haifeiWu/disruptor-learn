package cn.whfroever.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * @author wuhf
 * @Date 2018/8/14 16:21
 **/
public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        System.out.println("Event: " + longEvent.getValue());
    }
}
