package com.rst.cgi.common.kafka;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Description: logback日志监听器
 * @Author:  fengpan
 * @Date:  2018/8/16 下午4:12
 */

@Data
public class KafkaAppender extends AppenderBase<ILoggingEvent> {

    //写入kafka的线程，
    ExecutorService executorService = Executors.newFixedThreadPool(1000);
    private Formatter formatter = new JsonFormatter();
    private String topic;
    private String brokers;
    //配置kafka
    private Producer producer;
//    private ConcurrentHashMap map =new ConcurrentHashMap();
//    private String timeout;
//    private boolean syncSend=true;
//public static void main(String[] args) throws Exception {
//    Properties props = new Properties();
//    props.put("bootstrap.servers", "47.98.55.223:9092");
////            props.put("timeout.ms", "3000");
////            props.put("request.timeout.ms","3000");
////            props.put("metadata.fetch.timeout.ms",  "3000");
////            props.put("network.request.timeout.ms",  "3000");
//    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
////            props.put(ProducerConfig.ACKS_CONFIG, "-1");
////            props.put(ProducerConfig.BATCH_SIZE_CONFIG,1);
//
////            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,0);
////            props.put(ProducerConfig.SEND_BUFFER_CONFIG, "false");
//
//    producer = new KafkaProducer(props);
//    go();
//}
//    public static void go() throws Exception{
//        Future send = producer.send(new ProducerRecord("cgitest", "手动消息AOAAAAA"));
//        System.out.println(send.get());
//
//    }

    @Override
    public void start() {
        super.start();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("producer.type","async");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer(props);
//            props.put("timeout.ms", "3000");
//            props.put("request.timeout.ms","3000");
//            props.put("metadata.fetch.timeout.ms",  "3000");
//            props.put("network.request.timeout.ms",  "3000");
//            props.put(ProducerConfig.ACKS_CONFIG, "-1");
//            props.put(ProducerConfig.BATCH_SIZE_CONFIG,1);
//            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,0);
//            props.put(ProducerConfig.SEND_BUFFER_CONFIG, "false");
//        System.out.println("producer-----创建完成");
//        System.out.println(producer);
//         map.put(1,producer);
//        log.info("Starting KafkaAppender...");
//        Properties props = new Properties();
//        try {
//
//            props.put("bootstrap.servers", "47.98.55.223:9092");
////            props.put("timeout.ms", "3000");
////            props.put("request.timeout.ms","3000");
////            props.put("metadata.fetch.timeout.ms",  "3000");
////            props.put("network.request.timeout.ms",  "3000");
//            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
////            props.put(ProducerConfig.ACKS_CONFIG, "-1");
////            props.put(ProducerConfig.BATCH_SIZE_CONFIG,1);
//
////            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,0);
////            props.put(ProducerConfig.SEND_BUFFER_CONFIG, "false");
//
//            producer = new KafkaProducer(props);
//        } catch (Exception e) {
//            log.error("初始化KafkaAppender失败{}={}", e+"", e.getMessage());
//        }
//        log.info("kafkaProducerProperties = {}", kafkaProducerProperties);
//        log.info("Kafka Producer Properties = {}", props);
    }

    //cgi服务关闭，调用此方法关闭与kafka的socket链接
    @Override
    public void stop() {
        super.stop();
        producer.close();
    }

    //日志追加写入kafka
    @Override
    protected void append(ILoggingEvent event) {
        String string = this.formatter.format(event);
        executorService.execute(() ->{
            producer.send(new ProducerRecord(topic,string))   ;
        });
    }

}