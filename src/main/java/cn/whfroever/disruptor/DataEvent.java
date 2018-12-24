package cn.whfroever.disruptor;

/**
 * @author wuhf
 * @Date 2018/8/14 16:17
 **/
public class DataEvent {
    private long value;

    public void set(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DataEvent{" +
                "value=" + value +
                '}';
    }
}
