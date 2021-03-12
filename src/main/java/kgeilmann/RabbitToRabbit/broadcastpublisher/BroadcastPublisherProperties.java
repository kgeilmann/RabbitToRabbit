package kgeilmann.RabbitToRabbit.broadcastpublisher;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "broadcastpublisher")
public class BroadcastPublisherProperties {

    private List<Publisher> publishers;

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(final List<Publisher> publishers) {
        this.publishers = publishers;
    }

     static class Publisher {
        private String publisher;
        private String consumer;
        private List<String> topics;

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(final String publisher) {
            this.publisher = publisher;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(final List<String> topics) {
            this.topics = topics;
        }

        public String getConsumer() {
            return consumer;
        }

        public void setConsumer(final String consumer) {
            this.consumer = consumer;
        }
    }
}
