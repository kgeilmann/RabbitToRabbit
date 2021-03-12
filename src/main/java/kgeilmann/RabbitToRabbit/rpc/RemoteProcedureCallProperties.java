package kgeilmann.RabbitToRabbit.rpc;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rpc")
public class RemoteProcedureCallProperties {

    private List<Command> commands;

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(final List<Command> commands) {
        this.commands = commands;
    }

    static class Command {

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
