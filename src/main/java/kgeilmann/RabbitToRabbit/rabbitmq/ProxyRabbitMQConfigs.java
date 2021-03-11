package kgeilmann.RabbitToRabbit.rabbitmq;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq.proxy")
public class ProxyRabbitMQConfigs extends RabbitMQConfiguration {

    @Bean(name = "proxyTemplate")
    RabbitTemplate proxyTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean(name = "proxy")
    ConnectionFactory connectionFactory() {
        return super.makeConnectionFactory();
    }
}
