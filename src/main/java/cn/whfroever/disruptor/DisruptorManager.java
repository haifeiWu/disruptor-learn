package cn.whfroever.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wuhf
 * @Date 2018/8/14 19:39
 **/
public class DisruptorManager {

    private final static Logger LOG = LoggerFactory.getLogger(DisruptorManager.class);

    /**
     * 消费者线程池
     */
    private static ExecutorService threadPool;
    private static Disruptor<DataEvent> disruptor;
    private static RingBuffer<DataEvent> ringBuffer;

//    private static IDataDecoder dataDecoder;

    private static AtomicLong dataNum = new AtomicLong();

    public static void init(List<EventHandler<DataEvent>> eventHandlerList) {
//        MqManager.dataDecoder = dataDecoder;

        //初始化disruptor
        threadPool = Executors.newCachedThreadPool();
//        disruptor = new Disruptor<>(new DataEventFactory(), 8 * 1024, threadPool, ProducerType.MULTI, new BlockingWaitStrategy());
        disruptor = new Disruptor<>(new DataEventFactory(), 8 * 1024, threadPool);

        ringBuffer = disruptor.getRingBuffer();
        for (EventHandler<DataEvent> eventHandler : eventHandlerList) {
            disruptor.handleEventsWith(eventHandler);
        }
        disruptor.start();
    }

    /**
     *
     * @param message
     */
    public static void putDataToQueue(long message) {
        if (dataNum.get() == Long.MAX_VALUE) {
            dataNum.set(0L);
        }

        // 往队列中加事件
//        DataPackage dataPackage = new DataPackage(message, dataDecoder);
        long next = ringBuffer.next();
        try {
            ringBuffer.get(next).set(message);
            dataNum.incrementAndGet();
        } catch (Exception e) {
            LOG.error("向RingBuffer存入数据[{}]出现异常=>{}", message, e.getStackTrace());
        } finally {
            ringBuffer.publish(next);
        }
    }

    public static void close() {
        threadPool.shutdown();
        disruptor.shutdown();
    }
}
