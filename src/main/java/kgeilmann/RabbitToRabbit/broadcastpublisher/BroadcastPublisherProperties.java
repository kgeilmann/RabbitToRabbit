package kgeilmann.RabbitToRabbit.broadcastpublisher;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "broadcastpublisher")
public class BroadcastPublisherProperties {

    private List<String> topic;

    public List<String> getTopic() {
        return topic;
    }

    public void setTopic(List<String> topic) {
        this.topic = topic;
    }
}
