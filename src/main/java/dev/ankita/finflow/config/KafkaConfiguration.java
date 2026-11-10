package dev.ankita.finflow.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConfiguration {
  @Bean
  NewTopic paymentEventsTopic(@Value("${finflow.kafka.payment-topic:payment.events}") String topic) {
    return TopicBuilder.name(topic).partitions(3).replicas(1).build();
  }

  @Bean
  NewTopic paymentEventsDlqTopic(@Value("${finflow.kafka.payment-topic:payment.events}") String topic) {
    return TopicBuilder.name(topic + ".DLQ").partitions(3).replicas(1).build();
  }

  @Bean
  DefaultErrorHandler paymentErrorHandler(KafkaTemplate<String, String> kafka) {
    var recoverer = new DeadLetterPublishingRecoverer(kafka,
        (ConsumerRecord<?, ?> record, Exception exception) ->
            new TopicPartition(record.topic() + ".DLQ", record.partition()));
    var backOff = new ExponentialBackOffWithMaxRetries(3);
    backOff.setInitialInterval(1_000L);
    backOff.setMultiplier(2.0);
    backOff.setMaxInterval(8_000L);
    return new DefaultErrorHandler(recoverer, backOff);
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      ConsumerFactory<Object, Object> consumerFactory,
      DefaultErrorHandler paymentErrorHandler) {
    var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
    configurer.configure(factory, consumerFactory);
    factory.setCommonErrorHandler(paymentErrorHandler);
    return factory;
  }
}
