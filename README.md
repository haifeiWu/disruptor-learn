
最近一直在研究队列的一些问题，今天楼主要分享一个高性能的队列 Disruptor 。
<!--more-->

## what Disruptor ?
它是英国外汇交易公司 LMAX 开发的一个高性能队列，研发的初衷是解决内存队列的延迟问题。基于 Disruptor 开发的系统单线程能支撑每秒600万订单。

目前，包括 Apache Storm、Log4j2 在内的很多知名项目都应用了Disruptor以获取高性能。在楼主公司内部使用 Disruptor 与 Netty 结合用来做 GPS 实时数据的处理，性能相当强悍。本文从实战角度来大概了解一下 Disruptor 的实现原理。

## why Disruptor ?
Disruptor通过以下设计来解决队列速度慢的问题：

- 环形数组结构
为了避免垃圾回收，采用数组而非链表。因为，数组对处理器的缓存机制更加友好。
<br/>
- 元素位置定位
数组长度2^n，通过位运算，加快定位的速度。下标采取递增的形式。不用担心index溢出的问题。index是long类型，即使100万QPS的处理速度，也需要30万年才能用完。
<br/>
- 无锁设计
每个生产者或者消费者线程，会先申请可以操作的元素在数组中的位置，申请到之后，直接在该位置写入或者读取数据。
<br/>
- 针对伪共享问题的优化
Disruptor 消除这个问题，至少对于缓存行大小是64字节或更少的处理器架构来说是这样的（有可能处理器的缓存行是128字节，那么使用64字节填充还是会存在伪共享问题），通过增加补全来确保ring buffer的序列号不会和其他东西同时存在于一个缓存行中。

## how Disruptor ?
通过上面的介绍，我们大概可以了解到 Disruptor 是一个高性能的无锁队列，那么该如何使用呢，下面楼主通过 Disruptor 实现一个简单的生产者消费者模型，介绍 Disruptor 的使用

首先，根据 Disruptor 的事件驱动的编程模型，我们需要定义一个事件来携带数据。

```java

public class DataEvent {
    private long value;

    public void set(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}

```
为了让 Disruptor 为我们预先分配这些事件，我们需要构造一个 EventFactory 来执行构造

```java

public class DataEventFactory implements EventFactory<DataEvent> {

    @Override
    public DataEvent newInstance() {
        return new DataEvent();
    }
}

```
一旦我们定义了事件，我们需要创建一个处理这些事件的消费者。 在我们的例子中，我们要做的就是从控制台中打印出值。

```java
public class DataEventHandler implements EventHandler<DataEvent> {
    @Override
    public void onEvent(DataEvent dataEvent, long l, boolean b) throws Exception {
        new DataEventConsumer(dataEvent);
    }
}
```

接下来我们需要初始化 Disruptor ，并定义一个生产者来生成消息

```java

public class DisruptorManager {

    private final static Logger LOG = LoggerFactory.getLogger(DisruptorManager.class);

    /*消费者线程池*/
    private static ExecutorService threadPool;
    private static Disruptor<DataEvent> disruptor;
    private static RingBuffer<DataEvent> ringBuffer;

//    private static IDataDecoder dataDecoder;

    private static AtomicLong dataNum = new AtomicLong();

    public static void init(EventHandler<DataEvent> eventHandler) {
//        MqManager.dataDecoder = dataDecoder;

        //初始化disruptor
        threadPool = Executors.newCachedThreadPool();
        disruptor = new Disruptor<>(new DataEventFactory(), 8 * 1024, threadPool, ProducerType.MULTI, new BlockingWaitStrategy());

        ringBuffer = disruptor.getRingBuffer();
        disruptor.handleEventsWith(eventHandler);
        disruptor.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LOG.info("放入队列中数据编号{},队列剩余空间{}", dataNum.get(), ringBuffer.remainingCapacity());
            }
        }, new Date(), 60 * 1000);
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

```

最后我们来定义一个 Main 方法来执行代码 

```java

public class EventMain {

    public static void main(String[] args) throws Exception {
        DisruptorManager.init(new DataEventHandler());
        for (long l = 0; true; l++) {
            DisruptorManager.putDataToQueue(l);
            Thread.sleep(1000);
        }
    }
}

```
上面代码具体感兴趣的小伙伴请移步 [https://github.com/haifeiWu/disruptor-learn](https://github.com/haifeiWu/disruptor-learn)

然后我们可以看到控制台打印出来的数据

![console]( http://img.whforever.cn/disruptor-console.png)

## 小结
Disruptor 通过精巧的无锁设计实现了在高并发情形下的高性能。

另外在Log4j 2中的异步模式采用了Disruptor来处理。在这里楼主遇到一个小问题，就是在使用Log4j 2通过 TCP 模式往 logstash 发日志数据的时候，由于网络问题导致链接中断，从而导致 Log4j 2 不停的往 ringbuffer 中写数据，ringbuffer数据没有消费者，导致服务器内存跑满。解决方案是设置 Log4j 2 中 Disruptor 队列有界，或者换成 UDP 模式来写日志数据（如果数据不重要的话）。  

## 参考链接
- [剖析Disruptor:为什么会这么快？(二)神奇的缓存行填充](http://ifeve.com/disruptor-cacheline-padding/)
- [高性能队列——Disruptor](https://tech.meituan.com/disruptor.html)

![关注我们](http://img.hchstudio.cn/CodePig-QRCode.jpg)
