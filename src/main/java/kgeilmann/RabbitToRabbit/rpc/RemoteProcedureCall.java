package kgeilmann.RabbitToRabbit.rpc;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

import javax.annotation.PostConstruct;

public class RemoteProcedureCall {


    private final RemoteProcedureCallProperties rpcProperties;
    private final ConnectionFactory original;
    private final RabbitTemplate proxyTemplate;
    private final IntegrationFlowContext flowContext;
    private final RabbitTemplate originalTemplate;

    @Autowired
    public RemoteProcedureCall(@Qualifier("original") ConnectionFactory original,
                               @Qualifier("proxyTemplate") RabbitTemplate proxyTemplate,
                               IntegrationFlowContext flowContext, RemoteProcedureCallProperties rpcProperties) {
        this.original = original;
        this.proxyTemplate = proxyTemplate;
        this.originalTemplate = new RabbitTemplate(original);
        this.flowContext = flowContext;
        this.rpcProperties = rpcProperties;
    }

    @PostConstruct
    public void init() {
        rpcProperties.getTopics().forEach(topic -> {
            var flow = rpcFlow(topic);
            flowContext.registration(flow).autoStartup(true).register();
        });
    }

    IntegrationFlow rpcFlow(String topic) {
        var exchange = ExchangeBuilder.topicExchange(topic).durable(true).build();
        var admin = new RabbitAdmin(originalTemplate);
        admin.declareExchange(exchange);

        var inExchange = ExchangeBuilder.topicExchange(topic).durable(true).build();
        var inCommands = QueueBuilder.durable().autoDelete().build();
        var proxyAdmin = new RabbitAdmin(proxyTemplate);
        proxyAdmin.declareExchange(inExchange);
        proxyAdmin.declareQueue(inCommands);
        proxyAdmin.declareBinding(BindingBuilder.bind(inCommands).to(exchange).with("#").noargs());

        return IntegrationFlows.from(Amqp.inboundGateway(proxyTemplate.getConnectionFactory(), inCommands))
                               .log()
                               .handle(Amqp.outboundGateway(originalTemplate))
                               .get();
    }
}
