package kgeilmann.RabbitToRabbit.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq.proxy")
public class ProxyRabbitMQConfigs extends RabbitMQConfiguration {

    @Bean(name = "proxyTemplate")
    RabbitTemplate proxyTemplate() {
        var bytes = new MessageConverter() {
            @Override
            public Message toMessage(final Object o, final MessageProperties messageProperties)
                    throws MessageConversionException {
                return new Message((byte[]) o, messageProperties);
            }

            @Override
            public Object fromMessage(final Message message) throws MessageConversionException {
                return message.getBody();
            }
        };
        var template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(bytes);
        return template;
    }

    @Bean(name = "proxy")
    ConnectionFactory connectionFactory() {
        return super.makeConnectionFactory();
    }
}
