package kgeilmann.RabbitToRabbit.rabbitmq;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq.original")
public class OriginalRabbitMQConfigs extends RabbitMQConfiguration {

    @Bean(name="original")
    @Primary
    ConnectionFactory connectionFactory() {
        return super.makeConnectionFactory();
    }

}
