package kgeilmann.RabbitToRabbit.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

class RabbitMQConfiguration {

    private String address;
    private String username;
    private String password;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private ConnectionFactory makeConnectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(address);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    RabbitTemplate createTemplate() {
        var nopConverter = new MessageConverter() {
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
        var template = new RabbitTemplate(makeConnectionFactory());
        template.setMessageConverter(nopConverter);
        return template;
    }
}

