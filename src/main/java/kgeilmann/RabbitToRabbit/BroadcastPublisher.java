package kgeilmann.RabbitToRabbit;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpInboundChannelAdapterSMLCSpec;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.UUID;

@Configuration
@EnableRabbit
@EnableIntegration
public class BroadcastPublisher {

    private String topic = "TripPublicationEvents";
    private String consumptionQueue = "R2R-" + UUID.randomUUID();

    private ConnectionFactory connectionFactory;

    @Autowired
    public BroadcastPublisher(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    @Bean
    Exchange broadcastExchange() {
        return ExchangeBuilder.topicExchange(topic).durable(true).build();
    }

    @Bean
    Queue consumptionQueue() {
        return QueueBuilder.nonDurable(consumptionQueue).build();
    }

    @Bean
    Binding broadcastExchangeToConsumptionQueue() {
        return BindingBuilder.bind(consumptionQueue()).to(broadcastExchange()).with("#")
                .noargs();
    }

    @Bean
    IntegrationFlow consumerToProducer() {
        return IntegrationFlows.from(consumer()).log().get();
    }

    @Bean
    AmqpInboundChannelAdapterSMLCSpec consumer() {
        return Amqp.inboundAdapter(connectionFactory, consumptionQueue()).configureContainer(c -> c.acknowledgeMode(AcknowledgeMode.NONE));
    }
}
