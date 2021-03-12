package kgeilmann.RabbitToRabbit.rabbitmq;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "broker")
public class RabbitMQProperties {

    final AnnotationConfigApplicationContext context;

    private Map<String, RabbitMQConfiguration> rabbitmq;

    @Autowired
    public RabbitMQProperties(final AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    public Map<String, RabbitMQConfiguration> getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(final Map<String, RabbitMQConfiguration> rabbitmq) {
        this.rabbitmq = rabbitmq;
    }

    @PostConstruct
    void init() {
        rabbitmq.keySet().forEach(name -> {
            context.registerBean(name, RabbitTemplate.class, rabbitmq.get(name)::createTemplate);
        });
    }

    @Bean
    public FactoryBean templateFactory() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(RabbitTemplateFactory.class);
        return (FactoryBean) factoryBean;
    }
}
