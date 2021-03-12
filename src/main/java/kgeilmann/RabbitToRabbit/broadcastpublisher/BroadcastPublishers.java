package kgeilmann.RabbitToRabbit.broadcastpublisher;

import java.util.UUID;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpInboundChannelAdapterSMLCSpec;
import org.springframework.integration.amqp.dsl.AmqpOutboundChannelAdapterSpec;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.stereotype.Component;

import kgeilmann.RabbitToRabbit.rabbitmq.RabbitTemplateFactory;

@Component
@EnableIntegration
public class BroadcastPublishers {

    private final RabbitTemplateFactory templateFactory;
    private final BroadcastPublisherProperties broadcastPublisherProperties;
    private final IntegrationFlowContext flowContext;

    @Autowired
    public BroadcastPublishers(RabbitTemplateFactory templateFactory,
            BroadcastPublisherProperties broadcastPublisherProperties, final IntegrationFlowContext flowContext) {
        this.templateFactory = templateFactory;
        this.broadcastPublisherProperties = broadcastPublisherProperties;
        this.flowContext = flowContext;
    }

    @EventListener
    public void init(ContextRefreshedEvent contextEvent) {
        broadcastPublisherProperties.getPublishers().forEach(p -> {
            var inTemplate = templateFactory.get(p.getPublisher());
            var outTemplate = templateFactory.get(p.getConsumer());
            p.getTopics().forEach(topic -> {
                var in = getInAdapter(inTemplate, topic);
                var out = getOutAdapter(outTemplate, topic);
                var flow = IntegrationFlows.from(in).handle(out).get();
                flowContext.registration(flow).autoStartup(true).register();
            });
        });
    }

    private AmqpInboundChannelAdapterSMLCSpec getInAdapter(RabbitTemplate template, String topic) {
        var exchange = ExchangeBuilder.topicExchange(topic).durable(true).build();
        var consumption = QueueBuilder.nonDurable("R2R-" + UUID.randomUUID()).autoDelete().build();
        var binding = BindingBuilder.bind(consumption).to(exchange).with("#").noargs();

        var admin = new RabbitAdmin(template);
        admin.declareQueue(consumption);
        admin.declareBinding(binding);

        return Amqp.inboundAdapter(template.getConnectionFactory(), consumption)
                .configureContainer(c -> c.acknowledgeMode(AcknowledgeMode.NONE));
    }

    private AmqpOutboundChannelAdapterSpec getOutAdapter(RabbitTemplate template, String topic) {
        var admin = new RabbitAdmin(template);
        admin.declareExchange(ExchangeBuilder.topicExchange(topic).durable(true).build());

        return Amqp.outboundAdapter(template)
                .exchangeName(topic)
                .routingKeyFunction(m -> (String) m.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY));
    }

}
