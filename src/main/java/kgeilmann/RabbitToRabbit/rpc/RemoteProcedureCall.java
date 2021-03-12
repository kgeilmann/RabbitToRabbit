package kgeilmann.RabbitToRabbit.rpc;

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
import org.springframework.integration.amqp.dsl.AmqpInboundGatewaySMLCSpec;
import org.springframework.integration.amqp.dsl.AmqpOutboundGatewaySpec;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

import kgeilmann.RabbitToRabbit.rabbitmq.RabbitTemplateFactory;

//@Configuration
@EnableIntegration
public class RemoteProcedureCall {

    private final RemoteProcedureCallProperties rpcProperties;
    private final IntegrationFlowContext flowContext;
    private final RabbitTemplateFactory templateFactory;

    @Autowired
    public RemoteProcedureCall(RabbitTemplateFactory templateFactory, IntegrationFlowContext flowContext,
            RemoteProcedureCallProperties rpcProperties) {
        this.templateFactory = templateFactory;
        this.flowContext = flowContext;
        this.rpcProperties = rpcProperties;
    }

    @EventListener
    public void init(ContextRefreshedEvent contextEvent) {
        rpcProperties.getCommands().forEach(p -> {
            var inTemplate = templateFactory.get(p.getPublisher());
            var outTemplate = templateFactory.get(p.getConsumer());
            p.getTopics().forEach(topic -> {
                var in = getInGateway(inTemplate, topic);
                var out = getOutAdapter(outTemplate, topic);
                var flow = IntegrationFlows.from(in).handle(out).get();
                flowContext.registration(flow).autoStartup(true).register();
            });
        });
    }

    private AmqpInboundGatewaySMLCSpec getInGateway(final RabbitTemplate inTemplate, final String topic) {
        var inExchange = ExchangeBuilder.topicExchange(topic).durable(true).build();
        var inCommands = QueueBuilder.nonDurable().autoDelete().build();
        var admin = new RabbitAdmin(inTemplate);
        admin.declareExchange(inExchange);
        admin.declareQueue(inCommands);
        admin.declareBinding(BindingBuilder.bind(inCommands).to(inExchange).with("#").noargs());
        return Amqp.inboundGateway(inTemplate.getConnectionFactory(), inCommands);
    }

    private AmqpOutboundGatewaySpec getOutAdapter(RabbitTemplate outTemplate, String topic) {
        return Amqp.outboundGateway(outTemplate)
                .exchangeName(topic)
                .routingKeyFunction(m -> (String) m.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY));
    }
}
