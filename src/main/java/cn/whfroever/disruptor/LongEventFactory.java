package cn.whfroever.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author wuhf
 * @Date 2018/8/14 16:20
 **/
public class LongEventFactory implements EventFactory<LongEvent> {

    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
