package kgeilmann.RabbitToRabbit.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public interface RabbitTemplateFactory {

    RabbitTemplate get(String brokerName);
}
