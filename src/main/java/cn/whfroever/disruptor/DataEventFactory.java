package cn.whfroever.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author wuhf
 * @Date 2018/8/14 16:20
 **/
public class DataEventFactory implements EventFactory<DataEvent> {

    @Override
    public DataEvent newInstance() {
        return new DataEvent();
    }
}
