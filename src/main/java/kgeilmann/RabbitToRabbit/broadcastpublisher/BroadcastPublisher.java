package kgeilmann.RabbitToRabbit.broadcastpublisher;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpInboundChannelAdapterSMLCSpec;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@EnableIntegration
public class BroadcastPublisher {

    private final ConnectionFactory original;
    private final RabbitTemplate proxyTemplate;
    private final IntegrationFlowContext flowContext;
    private final BroadcastPublisherProperties publisherProperties;

    @Autowired
    public BroadcastPublisher(@Qualifier("original") ConnectionFactory original,
            @Qualifier("proxyTemplate") RabbitTemplate proxyTemplate, IntegrationFlowContext flowContext,
            BroadcastPublisherProperties publisherProperties) {
        this.original = original;
        this.proxyTemplate = proxyTemplate;
        this.flowContext = flowContext;
        this.publisherProperties = publisherProperties;
    }

    @PostConstruct
    public void init() {
        publisherProperties.getTopic().forEach(topic -> {
            var flow = broadcastToProxyFlow(topic, "R2R-" + topic);
            flowContext.registration(flow).autoStartup(true).register();
        });
    }

    private AmqpInboundChannelAdapterSMLCSpec getOriginalAdapter(String topic, String consumerQueue) {
        var exchange = ExchangeBuilder.topicExchange(topic).durable(true).build();
        var consumption = QueueBuilder.nonDurable(consumerQueue).autoDelete().build();
        var binding = BindingBuilder.bind(consumption).to(exchange).with("#").noargs();

        var admin = new RabbitAdmin(new RabbitTemplate(original));
        admin.declareExchange(exchange);
        admin.declareQueue(consumption);
        admin.declareBinding(binding);

        return Amqp.inboundAdapter(original, consumption)
                .configureContainer(c -> c.acknowledgeMode(AcknowledgeMode.NONE));
    }

    private IntegrationFlow broadcastToProxyFlow(String topic, String consumerQueue) {
        var in = getOriginalAdapter(topic, consumerQueue);

        var topic_proxy = topic + "-proxy";
        new RabbitAdmin(proxyTemplate).declareExchange(
                ExchangeBuilder.topicExchange(topic_proxy).durable(true).build());
        var out = Amqp.outboundAdapter(proxyTemplate)
                .exchangeName(topic_proxy)
                .routingKeyFunction(m -> (String) m.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY));

        return IntegrationFlows.from(in).handle(out).get();
    }

}
