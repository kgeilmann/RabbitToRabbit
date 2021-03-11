package kgeilmann.RabbitToRabbit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class AppConfig {

    private String instanceId;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

}
