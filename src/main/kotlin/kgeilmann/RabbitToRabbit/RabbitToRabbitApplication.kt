package kgeilmann.RabbitToRabbit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [])
@EnableConfigurationProperties
class RabbitToRabbitApplication

fun main(args: Array<String>) {
    runApplication<RabbitToRabbitApplication>(*args)
}
